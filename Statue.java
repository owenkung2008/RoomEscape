import greenfoot.*;

/**
 * A Statue is a solid blocker that 
 * also shows a larger decorative image on top.
 *
 * The Statue itself uses an invisible, tile-sized image for collision.
 * 
 * A separate Decoration actor ("visual") is added to the world 
 * to display the actual statue sprite (often bigger than a tile)
 * aligned so the bottom of the sprite sits on the tile.
 */
public class Statue extends Blocker
{
    //The visible statue sprite actor 
    //so that the sprite can display
    private Decoration visual;

    /**
     * Creates a statue with a collision hitbox of size (w, h).
     * The statue's own image is made transparent so it blocks movement
     * without visibly covering the decoration sprite.
     *
     * @param w hitbox width (usually tile width)
     * @param h hitbox height (usually tile height)
     */
    public Statue(int w, int h)
    {
        super(w, h);

        //create a transparent image for the hitbox.
        //the actor still collides because the Blocker logic uses this size,
        //but the player won't see a sprite drawn for the hitbox.
        setImage(new GreenfootImage(w, h));

    }

    /**
     * after the hitbox ix created
     * spawns the visible statue decoration 
     * and positions it so its bottom lines up with this tile.
     *
     * @param world the world this actor was added to
     */
    protected void addedToWorld(World world)
    {
        //create the visual decoration
        visual = new Decoration(GameConfig.STATUE_IMG, GameConfig.STATUE_W, GameConfig.STATUE_H);

        //add the decoration at the same tile position as the hitbox.
        world.addObject(visual, getX(), getY());

        // Bottom-align the larger decoration sprite to the tile-sized hitbox:
        int dy = (visual.getImage().getHeight() / 2) - (getImage().getHeight() / 2);
        visual.setLocation(getX(), getY() - dy);
    }

    /**
     * when the hitbox is removed
     * removes the visible decoration too
     * so it doesn't get left behind.
     *
     * @param world the world this actor was removed from
     */
    protected void removedFromWorld(World world)
    {
        ///only remove if the decoration exists and is still in a world.
        if (visual != null && visual.getWorld() != null) 
        {
            world.removeObject(visual);
        }
    }
}