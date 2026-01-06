import greenfoot.*;

/**
 * Warrior is a player-style character that can:
 * - walk using WalkingActor's movement/facing/walk animation
 * - play a directional attack animation
 * - trigger a "hit" once at a chosen frame
 *
 * How to use:
 * Subclass constructor, call:
 *      loadDirectionalFrames();
 *      loadAttackFrames();
 *  Subclass implement wantsToAttack()
 *  Subclassmplement onAttackHit() (spawn weapon hitbox)
 */
public abstract class Player extends WalkingActor implements HasHealth
{
    //attack frames (directional)
    protected GreenfootImage[] atkUp;
    protected GreenfootImage[] atkDown;
    protected GreenfootImage[] atkLeft;
    protected GreenfootImage[] atkRight;

    //attack state and animation timing
    protected boolean attacking = false;
    protected int atkAnimDelay = 2;
    protected int atkAnimTick = 0;
    protected int atkFrameIndex = 0;

    //cooldown (frames)
    protected int attackCooldownMax = 12;
    protected int attackCooldown = 0;

    //which frame triggers the hit (0-based)
    protected int hitFrame = 2;
    protected boolean hitDone = false;
    
   
    //health and damges
    //Maximum health the player can have
    protected int maxHealth = GameConfig.DEFAULT_MAX_HP;
    protected int health = maxHealth;
    
    
    //While > 0, 
    //the player cannot take damage again (prevents losing HP every frame).
    protected int hurtCooldown = 0;
     //How many frames after being hurt, before another damage can count 
    protected int hurtCooldownFrames = GameConfig.DEFAULT_INVINCIBILITY_FRAMES;

 
    /**
     * Loads directional attack frames from:
     *   images/<folder>/attack/up1.png ...
     *   images/<folder>/attack/down1.png ...
     *   images/<folder>/attack/right1.png ...
     *
     * LEFT is auto-created by mirroring the right frames.
     *
     * @param folder     the base folder in images
     * @param frameCount number of attack frames for each direction
     */
    protected void loadAttackFrames(String folder, int frameCount)
    {
        atkUp    = loadFramesRequired(folder + "/up", frameCount);
        atkDown  = loadFramesRequired(folder + "/down", frameCount);
        atkRight = loadFramesRequired(folder + "/right", frameCount);
    
        //left mirror from right
        atkLeft  = mirrorImage(atkRight);
    }

    /**
     * Subclass can override
     * 
     * Parant class provides default movement
     *
     * @return int[]{dx, dy}
     */
    protected int[] computeMove()
    {
        if (attacking) 
        {
            return new int[] {0, 0};   
        }

        int dx = 0;
        int dy = 0;

        boolean leftKey  = Greenfoot.isKeyDown("left")  || Greenfoot.isKeyDown("a");
        boolean rightKey = Greenfoot.isKeyDown("right") || Greenfoot.isKeyDown("d");
        boolean upKey    = Greenfoot.isKeyDown("up")    || Greenfoot.isKeyDown("w");
        boolean downKey  = Greenfoot.isKeyDown("down")  || Greenfoot.isKeyDown("s");

        if (leftKey)
        {
            dx -= speed;   
        }
        if (rightKey)
        {
            dx += speed;
        }
        if (upKey)
        {
            dy -= speed;   
        }
        if (downKey) 
        {
            dy += speed;   
        }

        return new int[] {dx, dy};
    }
    

    /**
     * Subclass decides when to start an attack.
     *
     * @return true if attack should begin this frame
     */
    protected abstract boolean wantsToAttack();

    /**
     * Called exactly once during the attack animation at hitFrame.
     * Subclass defines the attack logic
     */
    protected abstract void onAttackHit();

    public void act()
    {
        //freeze while paused 
        if (GameWorld.isPaused()) return;
        
        //reduce invincibility timer each frame
        if (hurtCooldown > 0) hurtCooldown--;
    
        //cooldown countdown
        if (attackCooldown > 0)
        {
            attackCooldown--;
        }

        //if attacking, only play attack animation
        if (attacking)
        {
            doAttackAnim();
            return;
        }

        //movement comes from subclass
        int[] mv = computeMove();
        int dx = mv[0];
        int dy = mv[1];

        boolean moving = (dx != 0 || dy != 0);

        //update facing direction (same logic as before)
        int newDir = dir;
        if (dx < 0) newDir = LEFT;
        else if (dx > 0) newDir = RIGHT;
        else if (dy < 0) newDir = UP;
        else if (dy > 0) newDir = DOWN;

        if (newDir != dir)
        {
            dir = newDir;
            resetAnim();
        }

        //move and animate
        if (moving)
        {
            avoidWallMoving(dx, dy);
            animate(framesFor(dir));
        }
        else
        {
            frameIndex = 0;
            setImage(framesFor(dir)[0]);
        }

        //start attack?
        if (attackCooldown == 0 && wantsToAttack())
        {
            startAttack();
        }
    }

    /**
     * Start the attack animation from frame 0.
     */
    protected void startAttack()
    {
        attacking = true;
        atkAnimTick = 0;
        atkFrameIndex = 0;
        hitDone = false;

        GreenfootImage[] frames = attackFramesFor(dir);
        if (frames != null && frames.length > 0)
        {
            setImage(frames[0]);
        }
    }

    /**
     * Plays attack animation as a one-shot, triggers onAttackHit() once.
     */
    protected void doAttackAnim()
    {
        GreenfootImage[] frames = attackFramesFor(dir);
        if (frames == null || frames.length == 0)
        {
            //fail-safe: if attack frames missing, end attack immediately
            attacking = false;
            attackCooldown = attackCooldownMax;
            setImage(framesFor(dir)[0]);
            return;
        }

        //trigger hit exactly once at hitFrame
        int safeHitFrame = Math.min(hitFrame, frames.length - 1);
        if (!hitDone && atkFrameIndex == safeHitFrame)
        {
            hitDone = true;
            onAttackHit();
        }

        //animate one-shot
        atkAnimTick++;
        if (atkAnimTick < atkAnimDelay)
        {
            setImage(frames[atkFrameIndex]);
            return;
        }

        atkAnimTick = 0;
        atkFrameIndex++;

        //end attack when finished
        if (atkFrameIndex >= frames.length)
        {
            attacking = false;
            attackCooldown = attackCooldownMax;
            resetAnim();
            setImage(framesFor(dir)[0]);
            return;
        }

        setImage(frames[atkFrameIndex]);
    }

    /**
     * Select correct attack frames based on direction.
     */
    protected GreenfootImage[] attackFramesFor(int direction)
    {
        if (direction == UP) return atkUp;
        if (direction == DOWN) return atkDown;
        if (direction == LEFT) return atkLeft;
        return atkRight;
    }
    /**
     * @return the player's current health
     */
    public int getHealth()
    {
        return health;
    }
    
    /**
     * @return the player's maximum health.
     */
    public int getMaxHealth()
    {
        return maxHealth;
    }
    
    /**
     * Heals the player by a given amount
     * without exceeding maxHealth.
     *
     * @param amount how much to heal (ignored if <= 0)
     */
    public void heal(int amount)
    {
        if (amount <= 0) return;
    
        health += amount;
        if (health > maxHealth) 
        {
            health = maxHealth;   
        }
    }
    /**
     * setHealth of the player when resume the game
     */
    public void setHealth(int amount)
    {
        if (amount <= 0) return;
    
        health=amount;
        if (health > maxHealth) 
        {
            health = maxHealth;   
        }
    }
    /**
     * Damages the player by a given amount.
     * Uses invincibility frames (hurtCooldown) to prevent rapid repeat damage.
     * If health reaches 0, the player dies and the game stops.
     *
     * @param amount how much damage to take (ignored if <= 0)
     */
    public void takeDamage(int amount)
    {
        if (amount <= 0) return;
    
        //if invincibility is active, ignore this damage
        if (hurtCooldown > 0) return;
    
        //apply damage
        health -= amount;
        if (health < 0) health = 0;
    
        //start invincibility frames
        hurtCooldown = hurtCooldownFrames;
    
        //if dead, end the game
        if (health <= 0)
        {
           Greenfoot.setWorld(new DefeatWorld());
        }
    }
}