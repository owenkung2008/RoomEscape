import greenfoot.*;

/**
 * DefeatWorld (Game Over Screen)
 * Shows game-over message and lets the player restart or return to settings/menu.
 */
public class DefeatWorld extends World
{
    public DefeatWorld()
    {
        super(GameConfig.WORLD_W, GameConfig.WORLD_H, 1);

        //simple dark background
        GreenfootImage bg = new GreenfootImage(getWidth(), getHeight());
        bg.setColor(new Color(0, 0, 0, 220));
        bg.fill();
        setBackground(bg);

        //show ext
        showText("GAME OVER", getWidth()/2, getHeight()/2 - 40);
        showText("Press R to Restart", getWidth()/2, getHeight()/2 + 10);
        showText("Press S to go to Setting", getWidth()/2, getHeight()/2 + 40);
    }

    public void act()
    {
        // restart: create a fresh GameWorld
        if (Greenfoot.isKeyDown("r"))
        {
            Greenfoot.setWorld(new GameWorld());
        }

        //menu: go back to your settings page
        if (Greenfoot.isKeyDown("s"))
        {
            //return to setting
            Greenfoot.setWorld(new SettingWorld());
        }
    }
}