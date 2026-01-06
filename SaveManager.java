import java.io.*;
import java.util.Scanner;

/**
 * SaveManager reads/writes simple save data to a text file.
 *
 * Saved data 
 * - roomR, roomC
 * - visited grid
 * - cleared grid
 * - lastRoomR,lastRoomC
 *
 * File format example:
 * roomR=1
 * roomC=2
 * visited=1101/0111/0101/1111
 * cleared=1000/0100/0000/0000
 * playerHealt=5
 * roomsCleared=2
 */
public class SaveManager
{

    /**
     * @return true if a save file exists
     */
    public static boolean hasSave()
    {
        //simple check
        //no exception to catch
        //because has not tried to read files.
        File file = new File(GameConfig.SAVE_FILE);
        
        //check to see if file exists
        //and file size >0
        if(file.exists() && file.length()>0)
        {
            return true;
        }
        return false;
    }

    /**
     * Deletes the save file (used for "New Game").
     */
    public static void deleteSave()
    {
        File f = new File(GameConfig.SAVE_FILE);
        if (f.exists())
        {
            f.delete();
        }
    }

    /**
     * Saves progress to file using SaveData.
     *
     * @param data all info to save
     */
    public static void save(SaveData data)
    {
        if (data == null) return;
    
        try
        {
            PrintWriter out = new PrintWriter(new FileWriter(GameConfig.SAVE_FILE));
    
            out.println("roomR=" + data.roomR);
            out.println("roomC=" + data.roomC);
            out.println("lastRoomR=" + data.lastRoomR);
            out.println("lastRoomC=" + data.lastRoomC);
    
            out.println("visited=" + (data.visited == null ? "" : data.visited));
            out.println("cleared=" + (data.cleared == null ? "" : data.cleared));
            out.println("playerHealth=" + data.playerHealth);
            out.println("roomsCleared=" + data.roomsCleared);
    
            out.close();
        }
        catch (IOException e)
        {
            System.out.println("Save failed: " + e.getMessage());
        }
    }
    
    /**
     * Loads progress from file and applies visited/cleared onto the map.
     *
     * @param map map to apply visited/cleared into
     * @return SaveData if loaded, or null if missing/invalid
     */
    public static SaveData load(GameMap map)
    {
        if (map == null) return null;
        if (!hasSave()) return null;
    
        SaveData data = new SaveData();
    
        //defaults
        data.roomR = 0;
        data.roomC = 0;
        data.lastRoomR = 0;
        data.lastRoomC = 0;
    
        Scanner sc = null;
    
        try
        {
            sc = new Scanner(new File(GameConfig.SAVE_FILE));
    
            while (sc.hasNextLine())
            {
                String line = sc.nextLine().trim();
    
                if (line.startsWith("roomR="))
                {
                    data.roomR = parseIntSafe(line.substring("roomR=".length()), 0);
                }
                else if (line.startsWith("roomC="))
                {
                    data.roomC = parseIntSafe(line.substring("roomC=".length()), 0);
                }
                else if (line.startsWith("lastRoomR="))
                {
                    data.lastRoomR = parseIntSafe(line.substring("lastRoomR=".length()), data.roomR);
                }
                else if (line.startsWith("lastRoomC="))
                {
                    data.lastRoomC = parseIntSafe(line.substring("lastRoomC=".length()), data.roomC);
                }
                else if (line.startsWith("visited="))
                {
                    data.visited = line.substring("visited=".length());
                }
                else if (line.startsWith("cleared="))
                {
                    data.cleared = line.substring("cleared=".length());
                }
                else if (line.startsWith("playerHealth="))
                {
                    data.playerHealth=parseIntSafe(line.substring("playerHealth=".length()), 0);
                }
                else if (line.startsWith("roomsCleared="))
                {
                    data.roomsCleared=parseIntSafe(line.substring("roomsCleared=".length()), 0);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Load failed: " + e.getMessage());
            return null;
        }
        finally
        {
            if (sc != null) sc.close();
        }
    
        //Apply visited/cleared to map
        if (data.visited != null && data.visited.length() > 0)
        {
            applyVisited(map, data.visited);
        }
        if (data.cleared != null && data.cleared.length() > 0)
        {
            applyCleared(map, data.cleared);
        }
    
        return data;
    }
    /**
     * Encodes visited state into a string like 1101/0111/...
     */
    protected static String encodeVisited(GameMap map)
    {
        String s = "";

        for (int r = 0; r < map.getRows(); r++)
        {
            if (r > 0) s += "/";

            for (int c = 0; c < map.getCols(); c++)
            {
                s += (map.wasVisited(r, c) ? "1" : "0");
            }
        }

        return s;
    }

    /**
     * Encodes cleared state into a string like 1000/0100/...
     */
    protected static String encodeCleared(GameMap map)
    {
        String s = "";

        for (int r = 0; r < map.getRows(); r++)
        {
            if (r > 0) s += "/";

            for (int c = 0; c < map.getCols(); c++)
            {
                s += (map.isCleared(r, c) ? "1" : "0");
            }
        }

        return s;
    }

    /**
     * Applies visited data onto the map.
     * Calls map.setVisited(r,c) for each '1'.
     */
    private static void applyVisited(GameMap map, String data)
    {
        String[] rows = data.split("/");

        for (int r = 0; r < rows.length && r < map.getRows(); r++)
        {
            String row = rows[r];

            for (int c = 0; c < row.length() && c < map.getCols(); c++)
            {
                char ch = row.charAt(c);
                if (ch == '1')
                {
                    map.setVisited(r, c);
                }
            }
        }
    }

    /**
     * Applies cleared data onto the map.
     * Calls map.markCleared(r,c) for each '1'.
     */
    private static void applyCleared(GameMap map, String data)
    {
        String[] rows = data.split("/");

        for (int r = 0; r < rows.length && r < map.getRows(); r++)
        {
            String row = rows[r];

            for (int c = 0; c < row.length() && c < map.getCols(); c++)
            {
                char ch = row.charAt(c);
                if (ch == '1')
                {
                    map.markCleared(r, c);
                    //cleared implies visited
                    map.setVisited(r, c);
                }
            }
        }
    }

    /**
     * Parses an integer safely, returning a default value if invalid.
     */
    private static int parseIntSafe(String s, int defaultValue)
    {
        try
        {
            return Integer.parseInt(s.trim());
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
        catch (NullPointerException e)
        {
            return 0;
        }
    }
}