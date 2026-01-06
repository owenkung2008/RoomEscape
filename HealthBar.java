import greenfoot.*;

/**
 * HealthBar renders a health bar for any HasHealth unit.
 * Can follow an actor or display at a fix place on screen
 */
public class HealthBar extends Actor
{
    private HasHealth unit;      // read HP from this
    private Actor follow;        // optional: follow this actor's position
    private boolean followTarget;

    private int barW;
    private int barH;
    private int yOffset;

    /**
     * @param unit         object that provides HP via HasHealth
     * @param follow       actor to follow (usually same as unit if unit is an Actor)
     * @param width        bar width in pixels
     * @param height       bar height in pixels
     * @param followTarget true to follow follow actor each frame
     * @param yOffset      y offset from follow actor
     */
    public HealthBar(HasHealth unit, Actor follow, int width, int height, boolean followTarget, int yOffset)
    {
        this.unit = unit;
        this.follow = follow;
        this.barW = width;
        this.barH = height;
        this.followTarget = followTarget;
        this.yOffset = yOffset;

        setImage(new GreenfootImage(barW, barH));
        updateImage();
    }

    public void act()
    {
        if (getWorld() == null) return;

        //if following something and it disappears, remove this bar too
        if (followTarget)
        {
            if (follow == null || follow.getWorld() == null)
            {
                getWorld().removeObject(this);
                return;
            }
            setLocation(follow.getX(), follow.getY() + yOffset);
        }

        updateImage();
    }

    //redraw based on unit HP ratio
    private void updateImage()
    {
        GreenfootImage img = getImage();
        img.clear();

        //background and border
        img.setColor(Color.DARK_GRAY);
        img.fillRect(0, 0, barW, barH);

        img.setColor(Color.WHITE);
        img.drawRect(0, 0, barW - 1, barH - 1);

        if (unit == null)
        {
            return;
        }

        int hp = unit.getHealth();
        int max = unit.getMaxHealth();
        if (max <= 0) 
        {
            return;
        }

        int innerX = 3, innerY = 3;
        int innerW = barW - 6;
        int innerH = barH - 6;

        double ratio = (double) hp / (double) max;
        if (ratio < 0) 
        {
            ratio=0;
        }
        if (ratio > 1) 
        {
            ratio=1;
        }

        int fillW = (int) Math.round(innerW * ratio);

        //choose color by percent
        Color fill;
        if (ratio > 0.6)
        {
            fill = Color.GREEN;   
        }
        else if (ratio > 0.3) 
        {
            fill = Color.YELLOW;   
        }
        else
        {
            fill = Color.RED;   
        }

        //bar background  and fill
        img.setColor(Color.BLACK);
        img.fillRect(innerX, innerY, innerW, innerH);

        img.setColor(fill);
        img.fillRect(innerX, innerY, fillW, innerH);
    }
}