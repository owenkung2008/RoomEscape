import greenfoot.*;
import java.util.*;

/**
 * GameWorld is a room-to-room adventure world driven by a 2D grid of rooms.
 *
 * Door rules:
 * - If the current room is cleared (enemies = 0): all doors are usable.
 * - If not cleared: ONLY the "back door" (the room came from) is usable.
 * - Locked door gaps are physically blocked using invisible Wall objects.
 */
public class GameWorld extends World 
{

    //game pausle related variables
    private static boolean paused = false; //pause state
    private boolean lastEsc = false; //ESC just-pressed detection
    private PauseOverlay pauseUI; //pause overlay actor

    //RoomMap stores all data related to a room
    private GameMap map;

    //Systems that handle specific responsibilities
    private RoomRenderer renderer;
    private DoorSystem doorSystem;
    private SpawnerSystem spawner;
    
    //(1,1), starting room
    private int roomR = 1;
    private int roomC = 1;

    //The room immediately BEFORE the current room 
    //(for backtracking)
    private int lastRoomR;
    private int lastRoomC;

    //these objects does not get removed when new room loads
    private Player player;
    private MiniMap minimap;
    private HealthBar playerBar;
    
    private int roomsClearedCount = 0;

    
    public GameWorld()
    {
        this(GameConfig.WARRIOR_AXE,false); //default warrior selection
    }
    /**
     * Constructs the world
     * initializes room data
     * spawns Player + MiniMap,
     * loads the starting room.
     */
    public GameWorld(int warriorType,boolean resume)
    {
        super(GameConfig.WORLD_W, GameConfig.WORLD_H, 1);
        
        map = new GameMap();

        renderer = new RoomRenderer(this, map);
        doorSystem = new DoorSystem(this, map);
        spawner = new SpawnerSystem(this, map);

        //Add minimap to the side panel (IMPORTANT: use the field minimap, not a local variable)
        minimap = new MiniMap();
        addObject(minimap, GameConfig.MINIMAP_X, GameConfig.MINIMAP_Y);
    
        //warrior to use
        if (warriorType == GameConfig.WARRIOR_BULLET)
        {
            player = new BulletWarrior();
        }
        else if (warriorType == GameConfig.WARRIOR_AXE)
        {
            player = new AxeWarrior();
        }
        else
        {
            player = new SwordWarrior();
        }
        addObject(player, GameConfig.roomCenterX(), GameConfig.roomCenterY());
        //HealthBar(HasHealth unit, Actor follow, int width, int height, boolean followTarget, int yOffset)
        int barW = GameConfig.SIDE_PANEL_W - 30;
        int barH = 18;
        int panelCenterX = GameConfig.PLAY_W + GameConfig.SIDE_PANEL_W / 2;
        int barY = GameConfig.WORLD_H - 25;
        playerBar = new HealthBar(player, null, barW, barH, false, 0);    
        addObject(playerBar, panelCenterX, barY);
        
        //Pick starting room:
        //- Resume: load roomR/roomC + visited/cleared into map
        //- New Game: start at first room
        int[] start = map.findFirstRoom();
    
        roomR = start[0];
        roomC = start[1];
    
        //whatever new data saved
        //must also update this section
        if (resume)
        {
            SaveData data = SaveManager.load(map);
        
            if (data != null && map.hasRoom(data.roomR, data.roomC))
            {
                roomR = data.roomR;
                roomC = data.roomC;
        
                if (map.hasRoom(data.lastRoomR, data.lastRoomC))
                {
                    lastRoomR = data.lastRoomR;
                    lastRoomC = data.lastRoomC;
                }
                else
                {
                    lastRoomR = roomR;
                    lastRoomC = roomC;
                }
            }
            
            roomsClearedCount=data.roomsCleared;
            player.setHealth(data.playerHealth);
        }

        //start in center only at the beginning
        loadRoom(roomR, roomC, 0, 0); 
        
        setPaintOrder(
            PauseOverlay.class,
            Decoration.class,   // the statue image actor
            Enemy.class
        );
    }
    /**
     * Toggles pause on/off and shows/hides the pause overlay.
     */
    private void togglePause()
    {
        paused = !paused;
    
        if (paused)
        {
            //add overlay in the center of the room
            pauseUI = new PauseOverlay();
            addObject(pauseUI, GameConfig.roomCenterX(), GameConfig.roomCenterY());
        }
        else
        {
            //remove overlay
            if (pauseUI != null && pauseUI.getWorld() != null)
            {
                removeObject(pauseUI);
            }
            pauseUI = null;
        }
    }
    /**
     * Exit to setup screen.
     *
     * Later add saving here:
     *   saveGameToFile();
     * then go back to setup.
     */
    private void exitToSetup()
    {
        SaveData data = new SaveData();
    
        data.roomR = roomR;
        data.roomC = roomC;
        data.lastRoomR = lastRoomR;
        data.lastRoomC = lastRoomC;
    
        data.visited = SaveManager.encodeVisited(map); 
        data.cleared = SaveManager.encodeCleared(map);
    
        data.playerHealth=player.getHealth();
        data.roomsCleared=roomsClearedCount;
        
        SaveManager.save(data);
    
        paused = false;
        Greenfoot.setWorld(new SettingWorld());
    }
    
    /**
     * - Marks a room cleared when enemies reach 0
     * - Updates HUD, the side panel
     * - Updates door lock states and physical door blockers
     */
     public void act() 
     {

        //ESC "just pressed" toggle
        boolean esc = Greenfoot.isKeyDown("escape");
        boolean escJustPressed = esc && !lastEsc;
        lastEsc = esc;
    
        if (escJustPressed)
        {
              togglePause();
        }
    
        //If paused:
        //- do not update doors/win checks/etc
        //- but still allow pressing Q to exit
        if (paused)
        {
            if (Greenfoot.isKeyDown("q"))
            {
                exitToSetup();
            }
            return;
        }
    
    
         //mark room cleared once
        if (countEnemies() == 0 && !map.isCleared(roomR, roomC)) 
        {
            if (map.markCleared(roomR, roomC)) 
            {
                roomsClearedCount++;
            }
        }

        //HUD text in side panel
        int hudX = GameConfig.ROOM_X + GameConfig.ROOM_W + GameConfig.SIDE_PANEL_W / 2;
        showText("Room: (" + roomR + "," + roomC + ")", hudX, 30);
        showText("Enemies: " + countEnemies(), hudX, 50);
        showText("Rooms cleared: " + roomsClearedCount + " / " + GameConfig.WIN_ROOMS, hudX, 70);
        showText("Heath Remaind: " + player.getHealth(), hudX, 650);

        //Win check
        //show in the window of the room
        if (roomsClearedCount >= GameConfig.WIN_ROOMS) 
        {
            showText("YOU WIN!", 
            GameConfig.ROOM_X + GameConfig.ROOM_W / 2, 
            GameConfig.ROOM_Y + GameConfig.ROOM_H / 2);
            Greenfoot.stop();
        }


        boolean unlockedNow = isRoomUnlocked();

        //unlock all doors if cleared, otherwise only the "back door"
        doorSystem.updateDoorStates(roomR, roomC, lastRoomR, lastRoomC, unlockedNow);

        //block door gaps while locked, but not the back door gap
        doorSystem.syncDoorBlockers(roomR, roomC, lastRoomR, lastRoomC, unlockedNow);
    }   

    
    /**
     * Loads a room.
     * - Removes old objects except Player and MiniMap
     * - Draws the room background and the side panel
     * - Builds interior walls and border walls
     * - Places doors on the border
     * - Spawns enemies (only if the room has not been cleared before)
     *
     * @param r room row
     * @param c room col
     */
    private void loadRoom(int r, int c, int enterDr, int enterDc)
    {
        //Remove everything except player + minimap
        List<Actor> all = new ArrayList<Actor>(getObjects(Actor.class));
        for (Actor a : all) 
        {
            if (a != player && a != minimap && a!=playerBar) removeObject(a);
        }

        //Door blockers list is owned by DoorSystem now
        doorSystem.onRoomLoaded();

        //set visited of 
        //the current room to true
        map.setVisited(r, c);

        //Background + walls + doors
        renderer.buildRoom(r, c);

        //add object using map e.g. statue 
        //moved to renderer to buildobject from tiles
        //spawner.spawnObjectsFromTiles(r, c);

        //Place the player where he enters the room with
        //unless it's the first room, the player will appear in the middle
        placeAtEntrance(enterDr, enterDc);

        //Spawn enemies only if room not cleared
        spawner.spawnEnemiesIfNeeded(r, c, player);

        //Force blocker rebuild + door state update immediately
        boolean unlockedNow = isRoomUnlocked();
        doorSystem.updateDoorStates(roomR, roomC, lastRoomR, lastRoomC, unlockedNow);
        doorSystem.syncDoorBlockers(roomR, roomC, lastRoomR, lastRoomC, unlockedNow);
    }

    /**
     * Places the player just inside the doorway they entered from.
     *
     * enterDr/enterDc = the direction used to move INTO this room.
     * Example: enterDr = -moved UP to get here,
     * should appear near the BOTTOM doorway in the new room.
     *
     * Special case: (0,0) means "starting room" -> spawn in center.
     */
    private void placeAtEntrance(int enterDr, int enterDc)
    {
        if (enterDr == 0 && enterDc == 0) 
        {
            player.setLocation(GameConfig.roomCenterX(), GameConfig.roomCenterY());
            return;
        }
    
        int halfW = player.getImage().getWidth() / 2;
        int halfH = player.getImage().getHeight() / 2;
    
        //push player far enough
        //so dthey are NOT touching the Door actor
        //or the graphics will blink
        int offsetX = GameConfig.BORDER_THICK + halfW + GameConfig.PLAYER_DOOR_OFFSET_X;  
        int offsetY = GameConfig.BORDER_THICK + halfH + GameConfig.PLAYER_DOOR_OFFSET_Y;  
    
        //Find the "back door" in THIS room (the door that leads back)
        Door backDoor = findDoor(-enterDr, -enterDc);
    
        //Fallback if something goes wrong (door missing)
        if (backDoor == null)
        {
            int midX = GameConfig.roomCenterX();
            int midY = GameConfig.roomCenterY();
            player.setLocation(midX, midY);
            return;
        }
    
        int x = backDoor.getX();
        int y = backDoor.getY();
    
        //Move player inward from that door's border position
        if (backDoor.getDr() == -1 && backDoor.getDc() == 0)
        {   // top door
            y += offsetY;
        }
        else if (backDoor.getDr() == 1 && backDoor.getDc() == 0)
        {   // bottom door
            y -= offsetY;
        }
        else if (backDoor.getDr() == 0 && backDoor.getDc() == -1)
        {   // left door
            x += offsetX;
        }
        else if (backDoor.getDr() == 0 && backDoor.getDc() == 1)
        {   // right door
            x -= offsetX;
        }
    
        player.setLocation(x, y);
    }

    /**
     * Attempts to move to an adjacent room.
     *
     * Rules:
     * - If current room cleared: can move to any neighbor room.
     * - If not cleared: can ONLY move back to the last room.
     *
     * @param dr change in room row
     * @param dc change in room col
     */
    public void tryMove(int dr, int dc) 
    {
        //calculate the target room (neighbor) in the grid
        //dr = change in row (up/down), 
        //dc = change in col (left/right)
        int nr = roomR + dr;   //neighbor row
        int nc = roomC + dc;   //neighbor column

        //If there is no room in that direction, do nothing
        if (!map.hasRoom(nr, nc)) 
        {
            return;   
        }

        //check if the player is trying to go back to the previous room
        boolean goingBack = (nr == lastRoomR && nc == lastRoomC);

        //If the room is still locked (enemies remain)
        //NOT going back, block the move.
        if (!isRoomUnlocked() && !goingBack) 
        {
            return;   
        }

        //Save the current room coordinates before moving
        int oldR = roomR;
        int oldC = roomC;

        //Move to the new room
        roomR = nr;
        roomC = nc;

        //update "last room" so we can allow going back next time
        lastRoomR = oldR;
        lastRoomC = oldC;

        //Load the new room
        loadRoom(roomR, roomC, dr, dc);
    }
    /**
     * @return number of Enemy actors currently in the world
     */
    private int countEnemies() 
    {
        return getObjects(Enemy.class).size();
    }
    /**
     * @return true if the current room has no enemies
     */
    public boolean isRoomUnlocked() 
    {
        return countEnemies() == 0;
    }
    /**
     * Finds the Door in the current room that matches (dr, dc).
     */
    private Door findDoor(int dr, int dc)
    {
        for (Door d : getObjects(Door.class))
        {
            if (d.getDr() == dr && d.getDc() == dc) return d;
        }
        return null;
    }
    /**
     * Allows any cctor to check if the game is paused.
     */
    public static boolean isPaused()
    {
        return paused;
    }
    
    /**
     * @return the number of room rows in the world grid
     */
    public int getRows()
    {
        return map.getRows();
    }
    
    /**
     * @return the number of room columns in the world grid
     */
    public int getCols()
    {
        return map.getCols();
    }
    
    /**
     * @return the current room row the player is in
     */
    public int getRoomR()
    {
        return roomR;
    }
    
    /**
     * @return the current room column the player is in
     */
    public int getRoomC()
    {
        return roomC;
    }
    
    /**
     * Checks whether a room exists at the given grid location.
     *
     * @param r room row
     * @param c room column
     * @return true if that room exists
     */
    public boolean roomExists(int r, int c)
    {
        return map.hasRoom(r, c);
    }
    
    /**
     * Checks whether the given room has been visited before.
     *
     * @param r room row
     * @param c room column
     * @return true if visited
     */
    public boolean wasVisited(int r, int c)
    {
        return map.wasVisited(r, c);
    }
    
    /**
     * Checks whether the given room has been cleared (enemies defeated).
     *
     * @param r room row
     * @param c room column
     * @return true if cleared
     */
    public boolean isCleared(int r, int c)
    {
        return map.isCleared(r, c);
    }
    /**
     * Allows actors (like Boss) to access the current room's RoomData.
     */
    public RoomData getCurrentRoomData()
    {
        return map.getRoomData(roomR, roomC);
    }
    public int getRoomsClearedCount()
    {
        return roomsClearedCount;
    }
}