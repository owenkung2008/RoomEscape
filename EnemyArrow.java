import greenfoot.*;

/**
 * EnemyArrow is a Bullet that hits the Player instead of Enemy.
 * It reuses Bullet movement, wall collision, bounds removal, and lifetime.
 */
public class EnemyArrow extends Bullet
{
    private int DAMAGE_LEVEL = 5;

    /**
     * Create an arrow moving in a 4-direction unit vector.
     * dirX/dirY should be -1, 0, or 1 (same as Bullet).
     */
    public EnemyArrow(int dirX, int dirY)
    {
        super(dirX, dirY);

        speed=2;

        // arrow sprite points LEFT by default
        if (dirX == 1)
        {
            //RIGHT
            setImage(new GreenfootImage("enemy/skeleton/arrow.png"));
            getImage().mirrorHorizontally(); 
            getImage().scale(45, 20);
        }
        else if (dirX == -1)
        {
            //LEFT
            setImage(new GreenfootImage("enemy/skeleton/arrow.png"));
            getImage().scale(45, 20);
        }
        else if (dirY == -1)
        {
            //UP
            setImage(new GreenfootImage("enemy/skeleton/arrow_up.png"));
            getImage().scale(20, 45);  
        }
        else if (dirY == 1)
        {
            //DOWN
            setImage(new GreenfootImage("enemy/skeleton/arrow_down.png"));
            getImage().scale(20, 45);  
        }  
  
    }

    /**
     * Hook override: enemy arrow hits Player, not Enemy.
     */
    protected boolean handleHit()
    {
        Player p = (Player) getOneIntersectingObject(Player.class);
        if (p != null)
        {
            p.takeDamage(DAMAGE_LEVEL);

            if (getWorld() != null)
            {
                getWorld().removeObject(this);
            }
            return true;
        }
        return false;
    }
    
    
}