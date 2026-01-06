/**
 * SaveData stores all information needed to restore a saved game.
 *
 * Keep this class simple:
 * - public fields, mainly string and int
 */
public class SaveData
{
    public int roomR;
    public int roomC;

    public int lastRoomR;
    public int lastRoomC;
    
    public int roomsCleared;

    //These are encoded strings so we don't store big arrays here
    public String visited;
    public String cleared;
    
    public int playerHealth;

    public SaveData()
    {
    }
}