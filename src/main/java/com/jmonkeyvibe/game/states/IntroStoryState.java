package com.jmonkeyvibe.game.states;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jmonkeyvibe.game.input.GamepadManager;

/**
 * Intro story screen shown when the game starts.
 * Displays backstory, context, and player goals before transitioning to exploration.
 */
public class IntroStoryState extends BaseAppState implements ActionListener {

    private SimpleApplication app;
    private Node guiNode;
    private Node storyNode;

    private Geometry backgroundGeom;
    private BitmapText titleText;
    private BitmapText storyText;
    private BitmapText continueText;

    private GamepadManager gamepadManager;
    private float blinkTimer = 0f;
    private boolean continueVisible = true;

    // Story content
    private static final String GAME_TITLE = "The Realm of Shadows";
    private static final String STORY_TEXT =
        "For centuries, the Kingdom of Eldoria lived in peace, its people " +
        "prospering under the guidance of wise rulers and ancient magic.\n\n" +
        "But darkness has awakened. From the depths of forgotten dungeons, " +
        "monsters now emerge, threatening to consume all that remains of the light.\n\n" +
        "You are a wanderer who has arrived at this troubled land. The villagers " +
        "speak in hushed tones of the growing danger. Perhaps you can help them.\n\n" +
        "Explore the world, speak with its inhabitants, and venture into the " +
        "cursed dungeons to face the evil within. The fate of Eldoria rests " +
        "in your hands.";

    private static final String CONTINUE_PROMPT = "Press any key, click, or press A to begin your adventure...";

    // UI Constants
    private static final int PADDING = 60;
    private static final float TITLE_SIZE_MULT = 2.5f;
    private static final float STORY_SIZE_MULT = 1.3f;
    private static final float PROMPT_SIZE_MULT = 1.0f;
    private static final float BLINK_INTERVAL = 0.6f;

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        this.guiNode = this.app.getGuiNode();
        this.storyNode = new Node("IntroStory");

        createUI();

        System.out.println("Intro story state initialized");
    }

    @Override
    protected void cleanup(Application app) {
        // Nothing to clean up
    }

    @Override
    protected void onEnable() {
        guiNode.attachChild(storyNode);
        setupInput();
    }

    @Override
    protected void onDisable() {
        guiNode.detachChild(storyNode);
        clearInput();
    }

    @Override
    public void update(float tpf) {
        // Blink the continue prompt
        blinkTimer += tpf;
        if (blinkTimer >= BLINK_INTERVAL) {
            blinkTimer = 0f;
            continueVisible = !continueVisible;
            continueText.setCullHint(continueVisible ?
                com.jme3.scene.Spatial.CullHint.Never :
                com.jme3.scene.Spatial.CullHint.Always);
        }

        // Check gamepad input
        handleGamepadInput();
    }

    /**
     * Set the gamepad manager for controller support
     */
    public void setGamepadManager(GamepadManager gamepadManager) {
        this.gamepadManager = gamepadManager;
    }

    private void createUI() {
        int screenWidth = app.getCamera().getWidth();
        int screenHeight = app.getCamera().getHeight();

        BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        float baseSize = font.getCharSet().getRenderedSize();

        // Dark background overlay
        Quad bgQuad = new Quad(screenWidth, screenHeight);
        backgroundGeom = new Geometry("IntroBackground", bgQuad);
        Material bgMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        bgMat.setColor("Color", new ColorRGBA(0.05f, 0.05f, 0.1f, 1.0f));
        backgroundGeom.setMaterial(bgMat);
        backgroundGeom.setLocalTranslation(0, 0, -1);
        storyNode.attachChild(backgroundGeom);

        // Title text
        titleText = new BitmapText(font);
        titleText.setSize(baseSize * TITLE_SIZE_MULT);
        titleText.setColor(new ColorRGBA(0.9f, 0.8f, 0.3f, 1.0f)); // Golden color
        titleText.setText(GAME_TITLE);
        float titleWidth = titleText.getLineWidth();
        titleText.setLocalTranslation(
            (screenWidth - titleWidth) / 2,
            screenHeight - PADDING,
            0
        );
        storyNode.attachChild(titleText);

        // Story text - centered with word wrap
        storyText = new BitmapText(font);
        storyText.setSize(baseSize * STORY_SIZE_MULT);
        storyText.setColor(ColorRGBA.White);

        int storyWidth = screenWidth - (PADDING * 4);
        int storyHeight = screenHeight - (PADDING * 4);
        storyText.setBox(new com.jme3.font.Rectangle(0, 0, storyWidth, storyHeight));
        storyText.setLineWrapMode(com.jme3.font.LineWrapMode.Word);
        storyText.setAlignment(com.jme3.font.BitmapFont.Align.Center);
        storyText.setText(STORY_TEXT);

        // Position story text below title
        float titleHeight = baseSize * TITLE_SIZE_MULT;
        storyText.setLocalTranslation(
            PADDING * 2,
            screenHeight - PADDING - titleHeight - PADDING,
            0
        );
        storyNode.attachChild(storyText);

        // Continue prompt at bottom
        continueText = new BitmapText(font);
        continueText.setSize(baseSize * PROMPT_SIZE_MULT);
        continueText.setColor(new ColorRGBA(0.7f, 0.7f, 0.7f, 1.0f));
        continueText.setText(CONTINUE_PROMPT);
        float promptWidth = continueText.getLineWidth();
        continueText.setLocalTranslation(
            (screenWidth - promptWidth) / 2,
            PADDING + baseSize,
            0
        );
        storyNode.attachChild(continueText);
    }

    private void setupInput() {
        // Multiple keys to continue
        app.getInputManager().addMapping("IntroContinue_Space", new KeyTrigger(KeyInput.KEY_SPACE));
        app.getInputManager().addMapping("IntroContinue_Enter", new KeyTrigger(KeyInput.KEY_RETURN));
        app.getInputManager().addMapping("IntroContinue_Escape", new KeyTrigger(KeyInput.KEY_ESCAPE));
        app.getInputManager().addMapping("IntroContinue_E", new KeyTrigger(KeyInput.KEY_E));
        app.getInputManager().addMapping("IntroContinue_Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        app.getInputManager().addMapping("IntroContinue_RightClick", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

        app.getInputManager().addListener(this,
            "IntroContinue_Space", "IntroContinue_Enter", "IntroContinue_Escape",
            "IntroContinue_E", "IntroContinue_Click", "IntroContinue_RightClick");
    }

    private void clearInput() {
        app.getInputManager().deleteMapping("IntroContinue_Space");
        app.getInputManager().deleteMapping("IntroContinue_Enter");
        app.getInputManager().deleteMapping("IntroContinue_Escape");
        app.getInputManager().deleteMapping("IntroContinue_E");
        app.getInputManager().deleteMapping("IntroContinue_Click");
        app.getInputManager().deleteMapping("IntroContinue_RightClick");
        app.getInputManager().removeListener(this);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (isPressed && name.startsWith("IntroContinue_")) {
            proceedToGame();
        }
    }

    private void handleGamepadInput() {
        if (gamepadManager == null) {
            return;
        }

        // A button, Start button, or any button press to continue
        if (gamepadManager.isAButtonJustPressed() ||
            gamepadManager.isStartButtonJustPressed() ||
            gamepadManager.isBButtonJustPressed() ||
            gamepadManager.isXButtonJustPressed() ||
            gamepadManager.isYButtonJustPressed()) {
            proceedToGame();
        }
    }

    private void proceedToGame() {
        System.out.println("Starting game - transitioning to exploration...");

        // Get reference to Main to access the exploration state
        com.jmonkeyvibe.game.Main mainApp = (com.jmonkeyvibe.game.Main) app;

        // Detach this state and attach exploration state
        app.getStateManager().detach(this);
        mainApp.startExploration();
    }
}
