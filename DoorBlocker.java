import greenfoot.*;

/**
 * DoorBlocker is a Wall collider that is always invisible.
 *
 */
public class DoorBlocker extends Blocker
{
    /**
     * Creates an invisible blocker rectangle with width and height in pixels.
     *
     * @param w width in pixels
     * @param h height in pixels
     */
    public DoorBlocker(int w, int h)
    {
        super(w, h);
        
        VISIBLE=false; 
        GreenfootImage img = new GreenfootImage(w, h);       
        if(VISIBLE)
        {
            img.setColor(new Color(100, 0, 0, 200));
        }
        else
        {
            img.setColor(new Color(0, 0, 0, 0));
        }
        img.fill();
        setImage(img);
    }
}