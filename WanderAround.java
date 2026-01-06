import greenfoot.*;

/**
 * WanderAround generates simple random wandering movement:
 * - Pick a random direction (up/down/left/right)
 * - Keep moving that direction for a random time
 * - Then pick again

 */
public class WanderAround
{
    private int dirX = 0;
    private int dirY = 1;

    private int timer = 0;

    private int minHoldFrames;
    private int extraHoldFrames;

    /**
     * @param minHoldFrames  minimum frames to keep a direction
     * @param extraHoldFrames additional random frames (0..extraHoldFrames-1)
     */
    public WanderAround(int minHoldFrames, int extraHoldFrames)
    {
        this.minHoldFrames = minHoldFrames;
        this.extraHoldFrames = extraHoldFrames;
    }

    /** Default tuning (similar to your WanderEnemy feel). */
    public WanderAround()
    {
        this(30, 60);
    }

    /**
     * @param speed movement speed
     * @return int[]{dx, dy} movement for this frame
     */
    public int[] nextMove(int speed)
    {
        if (timer <= 0)
        {
            pickNewDirection();
            timer = minHoldFrames + Greenfoot.getRandomNumber(extraHoldFrames);
        }

        timer--;
        return new int[]{ dirX * speed, dirY * speed };
    }

    /**
     * Chooses a new random direction and a random duration to walk.
     */
    private void pickNewDirection() 
    {
        int pick = Greenfoot.getRandomNumber(6); //0-5

        //0..3 = 4 directions, 4..5 = pause
        if (pick == 0) 
        { 
            dirX =  1; dirY =  0; 
        }
        else if (pick == 1) 
        { 
            dirX = -1; dirY =  0; 
        }
        else if (pick == 2) 
        { 
            dirX =  0; dirY =  1;
        }
        else if (pick == 3) 
        { 
            dirX =  0; dirY = -1; 
        }
        else 
        { 
            dirX = 0; dirY = 0; 
        }

        //timer = 20 + Greenfoot.getRandomNumber(60); //20-79
    }
}