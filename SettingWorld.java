import greenfoot.*;

/**
 * SetupWorld lets the user pick a warrior using images,
 * then start the game.
 */
public class SettingWorld extends World
{
    //default selected type
    private int selectedType = GameConfig.WARRIOR_AXE;

    //warrior selecttion
    private WarriorSelectIcon axeIcon;
    private WarriorSelectIcon bulletIcon;
    private WarriorSelectIcon swordIcon;
    
    //start buttons
    private StartButton newGameBtn;
    private StartButton resumeBtn;

    public SettingWorld()
    {
        super(GameConfig.WORLD_W, GameConfig.WORLD_H, 1);

        //title text
        showText("SETUP", GameConfig.WORLD_W / 2, 80);
        showText("Click a warrior image to select", GameConfig.WORLD_W / 2, 120);

        //show selections
        axeIcon = new WarriorSelectIcon("axe_warrior.png", GameConfig.WARRIOR_AXE);
        bulletIcon = new WarriorSelectIcon("bullet_warrior.png", GameConfig.WARRIOR_BULLET);
        swordIcon = new WarriorSelectIcon("sword_warrior.png", GameConfig.WARRIOR_SWORD);

        addObject(axeIcon, GameConfig.WORLD_W / 2 - 250, 400);
        addObject(bulletIcon, GameConfig.WORLD_W / 2 , 400);
        addObject(swordIcon, GameConfig.WORLD_W / 2 + 250, 400);

        //new game and resume buttons (images)
        newGameBtn = new StartButton("start.png", StartButton.MODE_NEW_GAME);
        resumeBtn  = new StartButton("resume.png", StartButton.MODE_RESUME);

        addObject(newGameBtn, GameConfig.WORLD_W / 2 - 120, 650);
        addObject(resumeBtn,  GameConfig.WORLD_W / 2 + 120, 650);

        //Disable resume if no save exists
        resumeBtn.setEnabled(SaveManager.hasSave());

        //default highlight
        updateHighlights();
    }

    /**
     * Called by WarriorSelectIcon when clicked.
     */
    public void chooseWarrior(int type)
    {
        selectedType = type;
        updateHighlights();
    }
    /**
     * Starts a brand new game.
     */
    public void startNewGame()
    {
        SaveManager.deleteSave();
        Greenfoot.setWorld(new GameWorld(selectedType, false));
    }

    /**
     * Resumes a saved game
     */
    public void resumeGame()
    {
        if (!SaveManager.hasSave())
        {
            showText("No save found.", GameConfig.WORLD_W / 2, 470);
            return;
        }

        Greenfoot.setWorld(new GameWorld(selectedType, true));
    }

    /**
     * Updates button visuals so the selected one looks highlighted.
     */
    private void updateHighlights()
    {
        axeIcon.setSelected(selectedType == GameConfig.WARRIOR_AXE);
        bulletIcon.setSelected(selectedType == GameConfig.WARRIOR_BULLET);
        swordIcon.setSelected(selectedType == GameConfig.WARRIOR_SWORD);

        String selectedStr="";
        if(selectedType==GameConfig.WARRIOR_AXE)
        {
            selectedStr="Axe Warrior";
        }
        if(selectedType==GameConfig.WARRIOR_BULLET)
        {
            selectedStr="Bullet Warrior";
        }
        if(selectedType==GameConfig.WARRIOR_SWORD)
        {
            selectedStr="Sword Warrior";
        }
        showText(
            "Selected: " + 
            selectedStr,
            GameConfig.WORLD_W / 2,
            175
        );
    }
}