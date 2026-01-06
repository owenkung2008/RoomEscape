import greenfoot.*;

/**
 * SpawnerSystem handles random spawning:
 * - enemies in a room
 *
 */
public class SpawnerSystem 
{

    private final GameWorld world;
    private final GameMap map;

    public SpawnerSystem(GameWorld world, GameMap map) 
    {
        this.world = world;
        this.map = map;
    }
    /**
     * Spawns enemies only if the room is not cleared.
     */
    public void spawnEnemiesIfNeeded(int r, int c, Player player)
    {
        //Spawn enemies only if room not cleared
        if (map.isCleared(r, c))
        {
            return;   
        }

        int enemiesToSpawn = 1 + Greenfoot.getRandomNumber(GameConfig.ENEMIES_TO_SPAWN); //1 to GameConfigENEMIES_TO_SPAWN
        for (int i = 0; i < enemiesToSpawn; i++) 
        {
            //randomly assigned x, y for eaach player
            //p is x, y co-ordiantes to spawn the enemy
            int[] p = randomFloorSpawn(r, c);

            //adds different typs of enemies randomly
            if (Greenfoot.getRandomNumber(2) == 0)
            {
                world.addObject(new ChaserEnemy(player),  p[0], p[1]);
            } 
            else if (Greenfoot.getRandomNumber(2) == 1)
            {
                world.addObject(new WanderEnemy(player),  p[0], p[1]);
            }
            else
            {
                 world.addObject(new SkeletonEnemy(player),  p[0], p[1]);
            }
        }
        if(world.getRoomsClearedCount()==GameConfig.SUMMONER_BOSS_DOOR_CLEARED)
        {
            //start from the top
            //descend to centre
            //create visual effect
            world.addObject(new SummonerBoss(player),  GameConfig.roomCenterX(),0);  
        }
         
    }
    /**
     * Spawns enemies only on FLOOR tiles so they never appear inside wall colliders.
     *
     * @param r room row
     * @param c room col
     * @return int[]{x,y} coordinates in pixel
     */
    private  int[] randomFloorSpawn(int r, int c)
    {
        //Get the RoomData for this room
        //to get the tile map
        RoomData rd = map.getRoomData(r, c);

       
        return  randomFloomSpawnInRoom(rd);
    }
    /*
     * Given roomdata and returns x, y where floor tile it.
     * makes it static
     * it can also acs as a helper method for boss to spawn mini enemies
     */
    public static int[] randomFloomSpawnInRoom(RoomData rd)
    {
        if (rd == null)
        {
             //if something is wrong, don't crash
             //give the x, y co-ordinates at room centre
            return new int[]{GameConfig.roomCenterX(), GameConfig.roomCenterY()};
        }

        //Try up to 250 random tiles to find a FLOOR tile
        //Avoid the outer border by choosing ROWS-2 and COLS-2)
        for (int tries = 0; tries < 250; tries++) 
        {
            //Pick a random interior tile row/col
            int tr = 1 + Greenfoot.getRandomNumber(GameConfig.MAP_ROWS - 2);
            int tc = 1 + Greenfoot.getRandomNumber(GameConfig.MAP_COLS - 2);

            //Only spawn on walkable floor tiles
            if (rd.tiles[tr][tc] == GameConfig.FLOOR) {

                //Convert tile position to the center pixel position in the room
                int x = GameConfig.tileCenterX(tc);
                int y = GameConfig.tileCenterY(tr);

                //return the spawn position as [x, y]
                return new int[]{x, y};
            }
        }

        //couldn't find a floor tile after many tries,
        //Spawn at the room center so the game doesn't break.
        return new int[]{GameConfig.roomCenterX(), GameConfig.roomCenterY()};
    }

}