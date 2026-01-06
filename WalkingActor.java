import greenfoot.*;

/**
 * WalkingActor is a shared superclass for Player and Enemy.
 *
 * In this class:
 * - Direction constants (UP/DOWN/LEFT/RIGHT)
 * - Directional frame arrays (up/down/left/right)
 * - Sprite loading and scaling
 * - Animation ticking
 * - Avoid Walking through walls
 *
 */
public abstract class WalkingActor extends SuperSmoothMover 
{

    //direction constants
    protected static final int DOWN  = 0;
    protected static final int UP    = 1;
    protected static final int LEFT  = 2;
    protected static final int RIGHT = 3;

    //sprite size
    protected int spriteW = 45;
    protected int spriteH = 55;

    //animation timing
    //bigger = slower animation
    protected int animDelay = 6;
    protected int animTick = 0;
    protected int frameIndex = 0;

    //initial facing direction
    protected int dir = DOWN;

    //sprite frames for each direction
    protected GreenfootImage[] up;
    protected GreenfootImage[] down;
    protected GreenfootImage[] left;
    protected GreenfootImage[] right;
    
    //player movement speed (pixels per act)
    //subclass should overrid this speed
    protected int speed = 0;
    
    public void act() 
    {
        //Freeze while paused 
        if (GameWorld.isPaused()) return;
        
        int dx = 0;
        int dy = 0;

        //arrows or WASD
        boolean leftKey;
        if (Greenfoot.isKeyDown("left") || Greenfoot.isKeyDown("a"))
        {
            leftKey = true;
        }
        else
        {
            leftKey = false;
        }
        
        boolean rightKey;
        if (Greenfoot.isKeyDown("right") || Greenfoot.isKeyDown("d"))
        {
            rightKey = true;
        }
        else
        {
            rightKey = false;
        }
        
        boolean upKey;
        if (Greenfoot.isKeyDown("up") || Greenfoot.isKeyDown("w"))
        {
            upKey = true;
        }
        else
        {
            upKey = false;
        }
        
        boolean downKey;
        if (Greenfoot.isKeyDown("down") || Greenfoot.isKeyDown("s"))
        {
            downKey = true;
        }
        else
        {
            downKey = false;
        }

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

        boolean moving = false;
        if(dx != 0 || dy != 0)
        {
            moving=true;
        }
        
        //update direction 
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
            //reset animation variables
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
    }
    /**
     * Resets the animation back to the first frame 
     * and restarts the animation timer
     */
    protected void resetAnim() 
    {
        frameIndex = 0;
        animTick = 0;
    }

    /**
     * Loads 2-direction frames from a folder like:
     * images/player/up1.png up2.png ... etc
     * 
     * file must be in png
     *
     */
    protected void loadDirectionalFrames(String folder, int frameCount) 
    {
        //e.g. player/up1.png
        up      = loadFramesRequired(folder + "/up", frameCount);
        down    = loadFramesRequired(folder + "/down", frameCount);
        right   = loadFramesRequired(folder + "/right", frameCount);
        //left    = loadFramesRequired(folder + "/left", frameCount);
        left    =mirrorImage(right);
        
        //default start image
        setImage(down[0]);
    }

    /**
     * Loads a fixed set of animation frames that are expected to exist.
     *
     * - load the image
     * - scale it if needed
     * - if the file is missing, return a placeholder image
     *
     * @param prefix the beginning of the filename (e.g. plaer/up )
     * @param count  how many frames to load
     * @return an array of frames in order (index 0 holds frame 1)
     */
    protected GreenfootImage[] loadFramesRequired(String prefix, int count)
    {
        GreenfootImage[] frames = new GreenfootImage[count];
    
        for (int i = 1; i <= count; i++)
        {
            int index = i - 1;
    
            //build filename e.g. player/up1.png
            String fileName = prefix + i + ".png";
    
            //load image by the file name
            //if missing a place holder images will be returned
            //also resize the iimage
            frames[index] = safeLoadAndScale(fileName);
        }
    
        //return the loaded frames.
        return frames;
    }
    /**
     * Loads an image from the given file path and scale it
     *
     * If the image cannot be loaded
     * this method returns a placeholder image instead of returning null 
     * to prevent crashing the game.
     *
     * The placeholder:
     * - has size (spriteW x spriteH)
     * - is filled with magenta (easy to notice)
     * - has the text "MISSING" drawn on it
     *
     * @param path the image file path/name to load
     * @return the loaded/scaled image if it exists; otherwise a placeholder image
     */
    protected GreenfootImage safeLoadAndScale(String path)
    {
        GreenfootImage img=null;
        try 
        {
            img = new GreenfootImage(path);
            img.scale(spriteW, spriteH);
        } 
        catch (IllegalArgumentException e) 
        {
            System.out.println("Failed toload the file:"+e.getMessage());
            System.out.println("PlacerHolder Images will be used instead");
        }
    
        //image found
        if (img != null) 
        {
            return img;   
        }
    
        //Instead of return null
        //Return a placeholder image
        return placeHolderImage();
    } 
    /**
     * Creates and returns a placeholder image used when a sprite file is missing.
     *
     * The placeholder is:
     * - the same size as a normal sprite (spriteW by spriteH)
     * - filled with a bright magenta color so it stands out
     * - labeled with the text "MISSING" in black
 
     * @return a placeholder GreenfootImage
     */
    protected GreenfootImage placeHolderImage()
    {
        GreenfootImage ph = new GreenfootImage(spriteW, spriteH);
    
        //yse a bright color (magenta) to stand out.
        ph.setColor(Color.MAGENTA);
        ph.fill();
    
        //write "MISSING" on top
        ph.setColor(Color.BLACK);
        ph.drawString("MISSING", 2, 16);

        return ph;
    }
    /**
     * Returns the animation frame array that matches the given direction.
     *
     * @param direction the direction the actor is facing (UP, DOWN, LEFT, RIGHT)
     * @return the frame array for that direction (up, down, left, or right)
     */
    protected GreenfootImage[] framesFor(int direction)
    {
        //check each possible direction and return its matching frames array.
        if (direction == UP)
        {
            return up;   
        }
        if (direction == DOWN)
        {
            return down;
        }
        if (direction == LEFT) 
        {
            return left;   
        }
    
        return right;
    }
    /**
     * Updatethe animation for the actor's current facing direction.
     */
    protected void animate() 
    {
        GreenfootImage[] frames = framesFor(dir);
        animate(frames);
    }

    /**
     * Updates the animation using the given frames array.
     *
     * @param frames the animation frames to aniamate
     */
    protected void animate(GreenfootImage[] frames) 
    {
        animTick++;
    
        if (animTick < animDelay) 
        {
            setImage(frames[frameIndex]);
            return;
        }
    
        animTick = 0;
        frameIndex++;
        if (frameIndex >= frames.length) frameIndex = 0;
    
        setImage(frames[frameIndex]);
    }

    /**
     * Moves while preventing going through Blocker objects.
     * Move X first then Y
     * Always move in straight line
     * 
     */
    protected void avoidWallMoving(int dx, int dy) 
    {
        int oldX = getX();
        int oldY = getY();

        if (dx != 0) {
            setLocation(getX() + dx, getY());
            if (isTouching(Blocker.class)) 
            {
                setLocation(oldX,getY());
            }
        }

        if (dy != 0) 
        {
            setLocation(getX(), getY() + dy);
            if (isTouching(Blocker.class)) 
            {
                setLocation(getX(),oldY);
            }
        }
    }
    /**
     * Creates mirrored images
     * Call this original is loaded and scaled
     */
    protected GreenfootImage[] mirrorImage(GreenfootImage[] original)
    {
        if (original == null) return null;
    
        GreenfootImage[] mirrored = new GreenfootImage[original.length];
    
        for (int i = 0; i < original.length; i++)
        {
            GreenfootImage copy = new GreenfootImage(original[i]);
            copy.mirrorHorizontally();
            mirrored[i] = copy;
        }
        return mirrored;
    }
}