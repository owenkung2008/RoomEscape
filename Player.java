import greenfoot.*;

/**
 * Warrior
 * - walk using WalkingActor's movement/facing/walk animation
 * - play a directional attack animation
 * - trigger a "hit" once at a chosen frame
 *
 * Subclass constructor, call:
 *      loadDirectionalFrames();
 *      loadAttackFrames();
 *  Subclass implement wantsToAttack()
 *  Subclass implement onAttackHit()
 */
public abstract class Player extends CombatActor implements HasHealth
{
    //health and damges
    //Maximum health the player can have. */
    protected int maxHealth = GameConfig.DEFAULT_MAX_HP;
    protected int health = maxHealth;
    
    
    //While > 0, 
    //the player cannot take damage again
    protected int hurtCooldown = 0;
     //how many frames before another damage can count 
    protected int hurtCooldownFrames = GameConfig.DEFAULT_INVINCIBILITY_FRAMES;

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
     * Subclass defines the attack logic (melee hitbox, projectile, etc.)
     */
    protected abstract void onAttackHit();

    public void act()
    {
        //Freeze while paused 
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

        //update facing direction
        int newDir = dir;
        if (dx < 0) 
        {
            newDir = LEFT;   
        }
        else if (dx > 0)
        {
            newDir = RIGHT;   
        }
        else if (dy < 0)
        {
            newDir = UP;   
        }
        else if (dy > 0) 
        {
            newDir = DOWN;   
        }

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