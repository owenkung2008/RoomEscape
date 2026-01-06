import greenfoot.*;

/**
 * StartButton
 *
 * Modes:
 * - MODE_NEW_GAME: deletes save and starts from beginning
 * - MODE_RESUME: loads save and continues
 */
public class StartButton extends Actor
{
    public static final int MODE_NEW_GAME = 0;
    public static final int MODE_RESUME   = 1;

    private int mode;
    private boolean enabled = true;

    /**
     * @param imgPath image path
     * @param mode MODE_NEW_GAME or MODE_RESUME
     */
    public StartButton(String imgPath, int mode)
    {
        setImage(new GreenfootImage(imgPath));
        getImage().scale(100,50);
        this.mode = mode;
    }

    /**
     * allows SetupWorld to disable the Resume button if there is no save.
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;

        //transparent when disabled
        if (getImage() != null)
        {
            getImage().setTransparency(enabled ? 255 : 120);
        }
    }

    public void act()
    {
        if (!enabled) return;

        if (Greenfoot.mouseClicked(this))
        {
            World w = getWorld();
            if (w instanceof SettingWorld)
            {
                SettingWorld sw = (SettingWorld) w;

                if (mode == MODE_NEW_GAME)
                {
                    sw.startNewGame();
                }
                else if (mode == MODE_RESUME)
                {
                    sw.resumeGame();
                }
            }
        }
    }
}