import greenfoot.*;

/**
 * WarriorSelectIcon is a clickable image used on the setup screen.
 *
 * - Shows a warrior picture (png)
 * - When clicked, it tells SetupWorld which warrior type was chosen
 * - If selected, it draws a highlight border
 */
public class WarriorSelectIcon extends Actor
{
    private String imgPath;
    private int warriorType;
    private boolean selected = false;

    //store the original image
    private GreenfootImage baseImg;

    /**
     * @param imgPath path inside images folder
     * @param warriorType the selection code (GameConfig.WARRIOR_AXE)
     */
    public WarriorSelectIcon(String imgPath, int warriorType)
    {
        this.imgPath = imgPath;
        this.warriorType = warriorType;

        baseImg = new GreenfootImage(imgPath);
        baseImg.scale(245,260);
 setImage(new GreenfootImage(baseImg)); //show it
    }

    public void act()
    {
        if (Greenfoot.mouseClicked(this))
        {
            World w = getWorld();
            if (w instanceof SettingWorld)
            {
                SettingWorld sw = (SettingWorld) w;
                sw.chooseWarrior(warriorType);
            }
        }
    }

    /**
     * Set whether this icon is currently selected.
     * If selected, draw a border to show it.
     */
    public void setSelected(boolean sel)
    {
        selected = sel;
        redraw();
    }

    /**
     * Rebuilds the displayed image with or without highlight.
     */
    private void redraw()
    {
        GreenfootImage img = new GreenfootImage(baseImg);

        if (selected)
        {
            //draw a bright border around the icon
            img.setColor(Color.YELLOW);
            img.drawRect(0, 0, img.getWidth() - 1, img.getHeight() - 1);
            img.drawRect(1, 1, img.getWidth() - 3, img.getHeight() - 3);
        }

        setImage(img);
    }
}