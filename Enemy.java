import greenfoot.*;

/**
 * Enemy is the abastrct base class for all enemies.
 *
 * Subclasses need to implement computeMove().
 *
 * Required images in images/enemy/:
 * up1.png up2.png
 * down1.png down2.png
 * left1.png left2.png
 * right1.png right2.png
 */
public abstract class Enemy extends CombatActor implements HasHealth
{

    //The player this enemy can interact with 
    //(chase and/or knockback)
    protected Player target;

    //Damage cooldown so enemy doesn't hit every single frame
    protected int hitCooldown = 0;

    //Knockback strength in pixels
    protected int knockBack = 20;

    //Knockback cooldown frames
    protected int hitCooldownFrames = 30;
    
    //damage to the player when this enemy touches them
    protected int contactDamage = 1;
    
    
    //enemy health
    protected int maxHealth = 5;
    protected int health = maxHealth;
    
    //health bar Health bar that follows this enemy
    private HealthBar hpBar;
    protected int HP_BAR_W = 40;
    protected int HP_BAR_H = 8;
   //Positive offset = bar appears UNDER the enemy
    protected int HP_BAR_Y_OFFSET = 35;

    public Enemy(Player target) 
    {
        this.target = target;
        animDelay=8;
        
        //use default sprite size
        //spriteW,spriteH
        spriteW=60;
        spriteH=65;
        
        //default
        loadDirectionalFrames("enemy", 2);
        dir = DOWN;
        //initial facing down
        setImage(framesFor(dir)[0]);
    }

    //subclass must implement this method
    protected abstract int[] computeMove();
    /**
     * Creates a health bar that follows underneath the enemy.
     */
    protected void addedToWorld(World world)
    {
        nudgeOffBlockers();  //preventing sticking to blocker forever
        
        if (hpBar == null)
        {
            //HealthBar(HasHealth unit, Actor follow, int width, int height, boolean followTarget, int yOffset)
            hpBar = new HealthBar(this, this, HP_BAR_W, HP_BAR_H, true, HP_BAR_Y_OFFSET);
    
            //add bar at the correct starting position
            world.addObject(hpBar, getX(), getY() + HP_BAR_Y_OFFSET);
        }
    }
    /**
     * When enemy is removed
     * Ensures its health bar disappears too.
     */
    protected void removedFromWorld(World world)
    {
        if (hpBar != null && hpBar.getWorld() != null)
        {
            world.removeObject(hpBar);
        }
    }
    public void act() 
    {
         //freeze  while paused
        if (GameWorld.isPaused()) return;
    
        if (getWorld() == null)
        {
            return;   
        }
        

        if (hitCooldown > 0)
        {
            hitCooldown--;
        }

        regularMovement();
        
        handlePlayerContact();
    }

    protected void regularMovement()
    {
        
        int[] mv = computeMove(); //subclass decides movement
        int dx = mv[0];
        int dy = mv[1];

        boolean moving = false;
        if (dx != 0 || dy != 0)
        {
            moving=true;
        }

        if (moving) 
        {
            //from WalkingActor parent class
            updateDirectionFromMove(dx, dy);
            avoidWallMoving(dx, dy);
            animate(framesFor(dir));
        } 
        else 
        {
            //idle frame
            frameIndex = 0;
            setImage(framesFor(dir)[0]);
        }
    }
    /**
     * Updates the actor's facing direction based on movement (dx, dy).
     * If the facing direction changes, the animation is reset to frame 0.
     *
     * @param dx change in x (negative = left, positive = right)
     * @param dy change in y (negative = up, positive = down)
     */
    protected void updateDirectionFromMove(int dx, int dy)
    {
        int oldDir = dir;
    
        //decide whether horizontal or vertical movement is "stronger".
        //if |dx| >= |dy|, face LEFT/RIGHT; otherwise face UP/DOWN.
        if (Math.abs(dx) >= Math.abs(dy)) 
        {
            //horizontal direction
            if (dx < 0)
            {
                dir = LEFT;   
            }
            else if (dx > 0)
            {
                dir = RIGHT;   
            }
        }
        else 
        {
            //vertical direction
            if (dy < 0) 
            {
                dir = UP;   
            }
            else if (dy > 0)
            {
                dir = DOWN;   
            }
        }
    
        //If the direction changed
        //restart the animation so it looks clean
        if (dir != oldDir)
        {
            resetAnim();   
        }
    }

    //Damages/knocks the player on contact
    protected void handlePlayerContact() 
    {
        if (target == null) 
        {
            return;
        }

        if (isTouching(Player.class) && hitCooldown == 0) 
        {
            hitCooldown = hitCooldownFrames;
            //deal damage to the player (player has its own invincibility too)
            if (target != null)
            {
                target.takeDamage(contactDamage);     
            }
        }
    }
    //@return enemy current HP
    public int getHealth() 
    { 
        return health; 
    }
    
    //@return enemy max HP
    public int getMaxHealth() 
    { 
        return maxHealth;
    }
    
    /**
     * Damage this enemy. If HP reaches 0, remove it.
     *
     * @param amount damage amount
     */
    public void takeDamage(int amount)
    {
        if (amount <= 0) return;
    
        health -= amount;
        if (health < 0) health = 0;
    
        if (health <= 0 && getWorld() != null)
        {
            getWorld().removeObject(this);
        }
    }
    /**
     * If spawn touching a Blocker (wall/statue)
     * moveto the nearest non-colliding spot it does't get "stuck" forever.
     */
    private void nudgeOffBlockers()
    {
        if (getWorld() == null) return;

        // If not touching anything solid, we're fine.
        if (getOneIntersectingObject(Blocker.class) == null) return;

        int startX = getX();
        int startY = getY();

        // Search outward in a small spiral/grid
        int step = 6;          // pixels per attempt
        int maxRadius = 12;    // how far we search (12*6 = 72px)

        for (int r = 1; r <= maxRadius; r++)
        {
            for (int dx = -r; dx <= r; dx++)
            {
                for (int dy = -r; dy <= r; dy++)
                {
                    // Only check the perimeter of this "ring" (faster)
                    if (Math.abs(dx) != r && Math.abs(dy) != r) continue;

                    setLocation(startX + dx * step, startY + dy * step);

                    if (getOneIntersectingObject(Blocker.class) == null)
                    {
                        return;
                    }  
                }
            }
        }

        //failed to find a spot
        //put it back
        setLocation(startX, startY);
    }
}