import greenfoot.*;

/**
 *  Sword Warrior:
 * - Walks using WalkingActor movement (WASD / arrow keys)
 * - Attacks with SPACE using directional attack sprites
 * - Melee hit is an invisible hitbox (AxeSlash) spawned in front
 */
public class SwordWarrior extends Player
{
    //space "just pressed" detection
    private boolean lastSpace = false;
    private boolean spaceJustPressed = false;

    public SwordWarrior()
    {
        //movement
        speed = 4;

        //walk animation timing
        animDelay = 6;

        //sprite size
        spriteW = 70;
        spriteH = 75;

        //walk frames
        loadDirectionalFrames("player/sword_warrior/walking", 4);
        left = mirrorImage(right);

        //attack frames
        loadAttackFrames("player/sword_warrior/attack", 10);

        //attack tuning
        atkAnimDelay = 3;         //smaller = faster swing
        attackCooldownMax = 12;   //bigger = slower swing rate
        hitFrame = 2;             //damage happens on this frame

        //default facing
        dir = DOWN;
        setImage(framesFor(dir)[0]);
    }

    public void act()
    {
        //space just pressed detection
        //so holding space does not trigger multiple attack
        boolean space = Greenfoot.isKeyDown("space");
        spaceJustPressed = space && !lastSpace;
        lastSpace = space;

        super.act();
    }
    /**
     * Start attack when SPACE is pressed.
     */
    protected boolean wantsToAttack()
    {
        return spaceJustPressed;
    }

    /**
     * Spawn a short-lived hitbox in front of the player on the hit frame.
     * This matches an axe swing in front like your sprite.
     */
    protected void onAttackHit()
    {
        if (getWorld() == null) return;

        //hitbox size (tune these)
        int boxW = 55;
        int boxH = 45;

        //how far in front of the player the hitbox is placed
        int reach = 35;

        int ox = 0;
        int oy = 0;

        if (dir == UP)
        {
            oy = -reach;
        }
        else if (dir == DOWN)
        {
            oy = reach;
        }
        else if (dir == LEFT)
        {
            ox = -reach;
        }
        else //RIGHT
        {
            ox = reach;
        }

        //spawn a short-lived hitbox (lasts a few frames to match swing)
        AxeSlash slash = new AxeSlash(boxW, boxH, 3);
        getWorld().addObject(slash, getX() + ox, getY() + oy);
    }
}