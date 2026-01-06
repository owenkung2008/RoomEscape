import greenfoot.*;

/**
 * MiniMap draws a small map of the room-grid on the side panel.
 *
 * Color key:
 *   Dark gray: no room
 *   Light Gray: room exists but not visited
 *   White: visited but not cleared
 *   Green: cleared
 *   Cyan outline: current room
 */
public class MiniMap extends Actor 
{

    private GameWorld w;

    //Mini cell size (pixels)
    private static int CELL = GameConfig.MINIMAP_CELL;

    //Padding between cells (pixels)
    private static int PAD =  GameConfig.MINIMAP_PAD;

    /**
     * Called automatically when MiniMap is added to the world.
     * The minimap is created first and then added to the world
     * 
     *
     * @param world the world this actor was added to
     */
    public void addedToWorld(World world) 
    {
        w = (GameWorld) world;
        updateImage();
    }

    //redraw every frame
    public void act() 
    {
        updateImage();
    }

    /**
     * Rebuilds the minimap image based on visited/cleared states.
     */
    private void updateImage() 
    {
        if (w == null) return;

        //grid size
        int rows = w.getRows();
        int cols = w.getCols();
        
        //image size:
        //cach cell is CELL pixels wide/high, 
        //with PAD pixels between cells and around the edges.
        int width  = cols * (CELL + PAD) + PAD;
        int height = rows * (CELL + PAD) + PAD;
        

        //create a fresh image every time 
        GreenfootImage img = new GreenfootImage(width, height);
        //draw the background panel 
        //black with transparnecy = 120
        img.setColor(new Color(0, 0, 0, 120));
        img.fill();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                //convert grid position (r,c)
                //into (x,y) on the minimap image
                int x = PAD + c * (CELL + PAD);
                int y = PAD + r * (CELL + PAD);

                if (!w.roomExists(r, c)) 
                {
                    img.setColor(new Color(40, 40, 40));
                    img.fillRect(x, y, CELL, CELL);
                    continue;
                }

                if (!w.wasVisited(r, c)) 
                {
                    //gray
                    //room exists but player has never entered it yet
                    img.setColor(new Color(120, 120, 120));
                } else if (w.isCleared(r, c)) 
                {
                    //bright green
                    //room was visited and cleared
                    img.setColor(new Color(70, 200, 70));
                } else 
                {
                    //light gray
                    //room was visited but not cleared yet
                    img.setColor(new Color(220, 220, 220));
                }
                img.fillRect(x, y, CELL, CELL);

                //highlight the player's current room with a cyan outline
                if (r == w.getRoomR() && c == w.getRoomC()) {
                    img.setColor(Color.CYAN);
                    img.drawRect(x, y, CELL- 1, CELL - 1);
                    img.drawRect(x + 1, y + 1, CELL - 3, CELL - 3);
                }
            }
        }

        setImage(img);
    }
}