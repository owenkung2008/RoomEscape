import greenfoot.*;

/**
 * PauseOverlay displays a pause message on top of the game.
 *
 */
public class PauseOverlay extends Actor
{
    public PauseOverlay()
    {
        redraw();
    }

    /**
     * Builds the pause overlay image.
     */
    private void redraw()
    {
        int w = 360;
        int h = 160;

        GreenfootImage img = new GreenfootImage(w, h);

        //semi-transparent black background
        img.setColor(new Color(0, 0, 0, 170));
        img.fillRect(0, 0, w, h);

        //white border
        img.setColor(Color.WHITE);
        img.drawRect(0, 0, w - 1, h - 1);

        //text
        img.drawString("PAUSED", 140, 40);
        img.drawString("ESC = Resume", 110, 80);
        img.drawString("Q = Exit to Setup", 95, 110);

        setImage(img);
    }
}