import greenfoot.*;

/**
 * ExteriorWall is a wall collider that uses a different PNG,
 * but works the same as Wall for collisions.
 *
 * Checks Wall.class collisions
 */
public class ExteriorWall extends Blocker
{
    private static final String EXTERIOR_WALL_SPRITE = "exterior_wall.png"; 

    /**
     * Creates an exterior wall collider with width and height in pixels.
     *
     * @param w width in pixels
     * @param h height in pixels
     */
    public ExteriorWall(int w, int h) 
    {
            super(w,h);
            
            GreenfootImage img = new GreenfootImage(w, h);
            VISIBLE=false;
            if (VISIBLE) 
            {
                try 
                {
                    GreenfootImage tex = new GreenfootImage(EXTERIOR_WALL_SPRITE);
                    tex.scale(w, h);
                    setImage(tex);
                    return;
                } 
                catch (IllegalArgumentException e) 
                {
                    //sprite missing,, draw out a rectangle
                    img.setColor(new Color(0, 200, 255, 120));
                    img.fill();
                    img.setColor(Color.BLUE);
                    img.drawRect(0, 0, w - 1, h - 1);
                    setImage(img);
                    return;
                }
            } 
            else 
            {
                //transparent
                //but still has size for collisions
                img.setColor(new Color(0, 0, 0, 0)); 
                img.fill();  //img size=w,h
                setImage(img);
            }
    }
}