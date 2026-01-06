import greenfoot.*;

/**
 * A collectible coin that updates RoomData when collected.
 */
public class Coin extends Actor 
{

    private RoomData roomData;
    private int r,c,tr,tc;

    private static final String COIN_SPRITE = "coin.png";
    /**
     * @param RoomDate rd
     * @param roomR room row in the world grid
     * @param roomC room col in the world grid
     * @param tr tile row inside the room
     * @param tc tile col inside the room
     */
    public Coin(RoomData rd,int r, int c, int tr, int tc) 
    {
        this.roomData=rd;
        this.r=r;
        this.c=c;
        this.tr=tr;
        this.tc=tc;
        setImage(loadCoinImage());
    }

    public void act() 
    {
        if (isTouching(Player.class)) 
        {
            collect();
        }
    }

    /**
     * Collect the coin:
     * - Remove this coin actor
     * - Update the RoomData
     */
    private void collect() 
    {
        //Update RoomData directly so it won't respawn next time
        if (roomData != null) 
        {
            if (roomData.tiles[tr][tc] == GameConfig.COIN) 
            {
                   roomData.setTile(tr, tc, GameConfig.FLOOR);  //permanently remove coin from that room’s layout
            }
        }

        World world = getWorld();
        if (world != null) 
        {
            world.removeObject(this);   
        }
    }
    private GreenfootImage loadCoinImage() 
    {
        try 
        {
            GreenfootImage img = new GreenfootImage(COIN_SPRITE);
            img.scale(30, 30);
            return img;
        } 
        catch (IllegalArgumentException e) 
        {
            //use drawing instead
            GreenfootImage img = new GreenfootImage(30, 30);
            img.setColor(Color.YELLOW);
            img.fillOval(2, 2, 26, 26);
            img.setColor(Color.ORANGE);
            img.drawOval(2, 2, 26, 26);
            return img;
        }
    }
}