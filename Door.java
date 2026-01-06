import greenfoot.*;

/**
 * Door moves the player to a neighboring room when unlocked and touched.
 *
 * The movement direction is stored as (dr, dc).
 * UP = (-1, 0)
 * DOWN = (1, 0)
 * LEFT = (0, -1)
 * RIGHT = (0, 1)
 */
public class Door extends Actor
{
    private String name;

    //destination dr
    //destination dc
    private int dr;
    private int dc;

    private boolean unlocked = false;

    //door images
    private static final String IMG_LOCKED   = "door_locked.png";
    private static final String IMG_UNLOCKED = "door_unlocked.png";

    /**
     * Creates a Door with a label and a direction.
     *
     * @param name label text
     * @param dr row change
     * @param dc col change
     */
    public Door(String name, int dr, int dc)
    {
        this.name = name;
        this.dr = dr;
        this.dc = dc;
        updateImage();
    }

    /**
     * @return the row that door opens to
     */
    public int getDr()
    {
        return dr;
    }

    /**
     * @return the col that the door opens to
     */
    public int getDc()
    {
        return dc;
    }

    /**
     * Locks/unlocks the door and redraws the image.
     *
     * @param on true to unlock, false to lock
     */
    public void setUnlocked(boolean on)
    {
        if (unlocked != on) {
            unlocked = on;
            updateImage();
        }
    }

    /**
     * Draws the door image.
     *
     * UPDATE:
     * - Uses door_locked.png / door_unlocked.png
     * - UP/DOWN doors use a horizontal image scaled to (DOOR_GAP_W x BORDER_THICK)
     * - LEFT/RIGHT doors use a vertical image scaled to (BORDER_THICK x DOOR_GAP_H)
     * - If image is missing, falls back to a colored rectangle (your old style)
     */
    private void updateImage()
    {
        // Decide final size based on orientation
        int w;
        int h;

        if (dc != 0) 
        {
            //vertical strip on border
            w = GameConfig.BORDER_THICK;
            h = GameConfig.DOOR_GAP_H;
        } 
        else 
        {
            //horizontal strip on border
            w = GameConfig.DOOR_GAP_W;
            h = GameConfig.BORDER_THICK;
        }

        //try to load image
        String path = null;
        if(unlocked)
        {
            path= IMG_UNLOCKED;
        }
        else
        {
            path= IMG_LOCKED;
        }

        try 
        {
            GreenfootImage img = new GreenfootImage(path);
            img.scale(w, h);
            setImage(img);
            return;
        } 
        catch (IllegalArgumentException e) 
        {
            //if image missing
            GreenfootImage img = new GreenfootImage(w, h);
            img.setColor(unlocked ? Color.GREEN : Color.YELLOW);
            img.fill();
            img.setColor(Color.BLACK);
            img.drawRect(0, 0, w - 1, h - 1);
    
            setImage(img);
        }

    }
    /**
     * When unlocked and the player touches the door
     * ask GameWorld to move rooms.
     */
    public void act()
    {
        if (!unlocked) return;

        if (isTouching(Player.class))
        {
            GameWorld w = (GameWorld) getWorld();
            w.tryMove(dr, dc);
        }
    }
}