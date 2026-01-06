import greenfoot.*;

/**
 * CombatActor adds reusable directional one-shot attack animation logic.
 *
 * Used by:
 * - Player (warriors)
 * - AttackingEnemy (enemies that attack)
 *
 * It does NOT decide *when* to attack. Subclasses decide when to call startAttack().
 */
public abstract class CombatActor extends WalkingActor
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

    /**
     * Loads directional attack frames from:
     *   images/<folder>/up1.png ...
     *   images/<folder>/down1.png ...
     *   images/<folder>/right1.png ...
     *
     * LEFT is auto-created by mirroring the right frames.
     *
     * @param folder     the folder prefix in images
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
     * Start the attack animation from frame 0.
     * Subclasses usually call this after deciding an attack should begin.
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
     * Call this while (attacking == true).
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
     * Called exactly once during the attack animation at hitFrame.
     * Default is no-op; Player / AttackingEnemy can make it abstract.
     */
    protected void onAttackHit()
    {
        // default: do nothing
    }
}