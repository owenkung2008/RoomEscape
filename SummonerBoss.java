import greenfoot.*;

/**
 * A stationary boss that periodically performs a "summon" animation.
 *
 * Behaviour:
 *  The boss does not move (computeMove() always returns {0,0}).
 *  After a random interval, it starts a summon animation.
 *  When the summon animation finishes, it spawns a small wave of enemies
 *    in the current room using SpawnerSystem.randomFloomSpawnInRoom(RoomData).
 *  It then returns to an idle state and waits for the next interval.
 *
 */
public class SummonerBoss extends Enemy
{
   
    //summon timer setting
    private int summonTimer;
    //inimum frames to wait before summoning again
    private int minInterval = 180; 
    //maximum frames to wait before summoning again.
    private int maxInterval = 300; 

    //animations
    private GreenfootImage[] summonFrames;
    private int summonAnimDelay = 20;

    //summoning state
    private boolean summoning = false;

    //How many minions to spawn 
    //after each summon animation finishes
    private int minionsPerSummon = 1;

    /**
     * Creates a SummonerBoss that targets the given Player (required by Enemy).
     *
     * @param target the player this boss (and its minions) will target/track
     */
    public SummonerBoss(Player target)
    {
        super(target);

        spriteW = 160;
        spriteH = 125;

        //boos health
        maxHealth = 100;
        health = maxHealth;

        //contact damage when touching the player
        contactDamage = 2;

        //health bar
        HP_BAR_W = spriteW-20;
        HP_BAR_H = 10;
        HP_BAR_Y_OFFSET = 90;

        //loadFramesRequired("pathPrefix", count) loads:
        summonFrames = loadFramesRequired("enemy/boss/summon/summon", 8);

        //iIdle image: first summon frame.
        setImage(summonFrames[0]);

        //start the boss in the waiting state.
        scheduleNextSummon();
    }

    /**
     * Prevent movement..
     *
     * @return {0,0} so the boss never moves
     */
    protected int[] computeMove()
    {
        return new int[]{0, 0};
    }

    /**
     * Main update loop:
     * - Handles pause
     * - Applies contact damage logic (reused from Enemy)
     * - Either counts down to summon OR plays the summon animation
     * - Spawns minions only at the end of the animation
     */
    public void act()
    {

        if (GameWorld.isPaused()) return;

        if (getWorld() == null) return;

        //enemy uses hitCooldown 
        //so the player isn't damaged every single frame.
        //summoner enemy does not hit
        if (hitCooldown > 0) hitCooldown--;

        //move to centre of the room
        if(getY()<=GameConfig.roomCenterY())
        {
            setLocation(getX(),getY()+5);
        }
        
        //summoner is big
        //when player touches
        //it does not takeDamage of player
        handlePlayerContact();

        if (summoning)
        {
            playSummonAnimation();
        }
        else
        {
            summonTimer--;
            if (summonTimer <= 0)
            {
                startSummon();
            }
        }
    }
    protected void handlePlayerContact() 
    {
        //do nothing
        //otherwise, axe or sword player has now way to kill the boos
        return;
    }

    /**
     * Schedules the next summon by setting summonTimer to a random value
     * between minInterval and maxInterval (inclusive).
     */
    private void scheduleNextSummon()
    {
        summonTimer = minInterval + Greenfoot.getRandomNumber(maxInterval - minInterval + 1);
    }

    /**
     * Enters the summoning state and resets animation counters.
     * The actual spawn happens only when the animation finishes.
     */
    private void startSummon()
    {
        summoning = true;

        //start with the first frame
        frameIndex = 0;
        animTick = 0;

        setImage(summonFrames[0]);
    }

    /**
     * Advances the summon animation.
     *
     * When the last frame finishes, this method:
     * - switches back to idle
     * - spawns minions in the current room
     * - schedules the next summon timer
     */
    private void playSummonAnimation()
    {
        //animTick counts 
        //how many acts have passed since last frame change.
        animTick++;

        //only change frame when reach the delay threshold.
        if (animTick < summonAnimDelay) return;

        animTick = 0;
        frameIndex++;

        //if reached the end of the animation
        //do the spawn and reset to idle.
        if (frameIndex >= summonFrames.length)
        {
            summoning = false;
            setImage(summonFrames[0]); // idle

            //spawn happens
            //at the end of the animation.
            spawnMinions();

            //sart waiting for the next summon.
            scheduleNextSummon();
            return;
        }

        //oherwise show the next frame.
        setImage(summonFrames[frameIndex]);
    }

    /**
     * Spawns minion enemies inside the CURRENT room.
     *
     * Uses:
     * - GameWorld.getCurrentRoomData() to get the RoomData for this room
     * - SpawnerSystem.randomFloomSpawnInRoom(rd) to find a random FLOOR spawn point
     *
     * Minion selection here is randomized
     */
    private void spawnMinionsBySpawnner()
    {
        GameWorld gw = (GameWorld) getWorld();
        RoomData rd = gw.getCurrentRoomData();

        for (int i = 0; i < minionsPerSummon; i++)
        {
            //use SpawnerSystem's randomFloorSpawninRoom to spawn
            int[] p = SpawnerSystem.randomFloomSpawnInRoom(rd);

            //randomly choose a minion type.
            int roll = Greenfoot.getRandomNumber(3);
            if (roll == 0)
            {
                getWorld().addObject(new ChaserEnemy(target), p[0], p[1]);
            }
            else if (roll == 1)
            {
                getWorld().addObject(new WanderEnemy(target), p[0], p[1]);
            }
            else
            {
                getWorld().addObject(new SkeletonEnemy(target), p[0], p[1]);
            }
        }
    }
    /**
     * Spawns minions near the boss (around its position).
     *
     * Strategy:
     * - Try random points in a square around the boss (radius).
     * - Reject points that are out of bounds or inside blockers.
     * - If we fail to find a valid spot after many tries, fall back to
     *   SpawnerSystem.randomFloomSpawnInRoom(currentRoomData).
     */
    private void spawnMinions()
    {
        GameWorld gw = (GameWorld) getWorld();
        RoomData rd = gw.getCurrentRoomData();
    
        int radius = 140; //where to spawn the minions
    
        //how many random tries per minion
        int triesPerMinion = 30;
    
        for (int i = 0; i < minionsPerSummon; i++)
        {
            int[] p = findSpawnPointNearBoss(radius, triesPerMinion);
    
            //if no good point found near the boss, use room-floor helper
            if (p == null)
            {
                p = SpawnerSystem.randomFloomSpawnInRoom(rd);
            }
    
            // Spawn a random minion type
            int roll = Greenfoot.getRandomNumber(3);
            if (roll == 0)
            {
                getWorld().addObject(new ChaserEnemy(target), p[0], p[1]);
            }
            else if (roll == 1)
            {
                getWorld().addObject(new WanderEnemy(target), p[0], p[1]);
            }
            else
            {
                getWorld().addObject(new SkeletonEnemy(target), p[0], p[1]);
            }
        }
    }
    
    /**
     * Attempts to find a valid spawn location near the boss.
     *
     * Valid locations:
     * - inside world bounds
     * - not inside a Blocker
     *
     * @param radius maximum distance (pixels) from the boss center
     * @param maxTries how many random attempts before giving up
     * @return {x,y} pixel location, or null if none found
     */
    private int[] findSpawnPointNearBoss(int radius, int maxTries)
    {
        World w = getWorld();
        if (w == null) return null;
    
        for (int t = 0; t < maxTries; t++)
        {
            int x = getX() + Greenfoot.getRandomNumber(radius * 2 + 1) - radius;
            int y = getY() + Greenfoot.getRandomNumber(radius * 2 + 1) - radius;
    
            // Keep inside world bounds 
            //(use a little padding so enemies don't spawn half off-screen)
            if (x < 20 || y < 20 || x > w.getWidth() - 20 || y > w.getHeight() - 20)
            {
                continue;
            }
    
            // skip if location is inside a blocker.
            if (!w.getObjectsAt(x, y, Blocker.class).isEmpty())
            {
                continue;
            }
    
            return new int[]{x, y};
        }
    
        return null;
    }
}