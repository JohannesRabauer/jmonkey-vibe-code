package com.jmonkeyvibe.game.ui;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jmonkeyvibe.game.entities.Player;
import com.jmonkeyvibe.game.input.GamepadManager;

/**
 * Level Up UI screen displayed when the player gains a level.
 * Shows stat choices and allows selection via keyboard or controller.
 */
public class LevelUpUI implements ActionListener {

    private SimpleApplication app;
    private Node guiNode;
    private Node levelUpNode;

    private BitmapText titleText;
    private BitmapText levelText;
    private BitmapText instructionsText;

    // Stat option texts
    private BitmapText strengthText;
    private BitmapText agilityText;
    private BitmapText vitalityText;
    private BitmapText dexterityText;

    // Stat option backgrounds
    private Geometry strengthBg;
    private Geometry agilityBg;
    private Geometry vitalityBg;
    private Geometry dexterityBg;

    private Geometry darkOverlay;

    private boolean isVisible = false;
    private int selectedOption = 0; // 0=Strength, 1=Agility, 2=Vitality, 3=Dexterity
    private Player player;

    private LevelUpListener listener;
    private GamepadManager gamepadManager;

    // UI layout constants
    private static final int OPTION_WIDTH = 400;
    private static final int OPTION_HEIGHT = 50;
    private static final int OPTION_SPACING = 15;

    public LevelUpUI(SimpleApplication app) {
        this.app = app;
        this.guiNode = app.getGuiNode();
        this.levelUpNode = new Node("LevelUpUI");

        createUI();
    }

    private void createUI() {
        BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");

        int screenWidth = app.getCamera().getWidth();
        int screenHeight = app.getCamera().getHeight();

        // Dark overlay covering the entire screen
        Quad overlayQuad = new Quad(screenWidth, screenHeight);
        darkOverlay = new Geometry("DarkOverlay", overlayQuad);
        Material overlayMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        overlayMat.setColor("Color", new ColorRGBA(0f, 0f, 0.1f, 0.85f));
        overlayMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        darkOverlay.setMaterial(overlayMat);
        darkOverlay.setLocalTranslation(0, 0, -1);

        // "LEVEL UP!" title - large and prominent
        titleText = new BitmapText(font);
        float titleSize = font.getCharSet().getRenderedSize() * 3.0f;
        titleText.setSize(titleSize);
        titleText.setColor(new ColorRGBA(1.0f, 0.85f, 0.0f, 1.0f)); // Gold color
        titleText.setText("LEVEL UP!");
        float titleWidth = titleText.getLineWidth();
        titleText.setLocalTranslation(
            (screenWidth - titleWidth) / 2,
            screenHeight * 0.85f,
            1
        );

        // Level text showing new level
        levelText = new BitmapText(font);
        float levelTextSize = font.getCharSet().getRenderedSize() * 1.5f;
        levelText.setSize(levelTextSize);
        levelText.setColor(ColorRGBA.White);
        levelText.setText("You are now Level 2");
        float levelWidth = levelText.getLineWidth();
        levelText.setLocalTranslation(
            (screenWidth - levelWidth) / 2,
            screenHeight * 0.75f,
            1
        );

        // Calculate center positions for stat options
        int centerX = screenWidth / 2;
        int optionsStartY = (int)(screenHeight * 0.60f);
        float optionTextSize = font.getCharSet().getRenderedSize() * 1.1f;

        // Create stat option backgrounds and texts
        // Strength option
        strengthBg = createOptionBackground(centerX, optionsStartY, 0);
        strengthText = createOptionText(font, centerX, optionsStartY, 0, optionTextSize, "1. Strength");

        // Agility option
        agilityBg = createOptionBackground(centerX, optionsStartY, 1);
        agilityText = createOptionText(font, centerX, optionsStartY, 1, optionTextSize, "2. Agility");

        // Vitality option
        vitalityBg = createOptionBackground(centerX, optionsStartY, 2);
        vitalityText = createOptionText(font, centerX, optionsStartY, 2, optionTextSize, "3. Vitality");

        // Dexterity option
        dexterityBg = createOptionBackground(centerX, optionsStartY, 3);
        dexterityText = createOptionText(font, centerX, optionsStartY, 3, optionTextSize, "4. Dexterity");

        // Instructions text
        instructionsText = new BitmapText(font);
        float instructionSize = font.getCharSet().getRenderedSize() * 0.9f;
        instructionsText.setSize(instructionSize);
        instructionsText.setColor(ColorRGBA.LightGray);
        instructionsText.setText("Press 1-4, W/S + Enter, or use D-pad + A to select");
        float instructionsWidth = instructionsText.getLineWidth();
        instructionsText.setLocalTranslation(
            (screenWidth - instructionsWidth) / 2,
            screenHeight * 0.10f,
            1
        );

        // Add all elements to the level up node
        levelUpNode.attachChild(darkOverlay);
        levelUpNode.attachChild(titleText);
        levelUpNode.attachChild(levelText);
        levelUpNode.attachChild(strengthBg);
        levelUpNode.attachChild(strengthText);
        levelUpNode.attachChild(agilityBg);
        levelUpNode.attachChild(agilityText);
        levelUpNode.attachChild(vitalityBg);
        levelUpNode.attachChild(vitalityText);
        levelUpNode.attachChild(dexterityBg);
        levelUpNode.attachChild(dexterityText);
        levelUpNode.attachChild(instructionsText);
    }

    private Geometry createOptionBackground(int centerX, int startY, int index) {
        Quad quad = new Quad(OPTION_WIDTH, OPTION_HEIGHT);
        Geometry bg = new Geometry("OptionBg" + index, quad);
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0.2f, 0.2f, 0.3f, 0.8f));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        bg.setMaterial(mat);
        bg.setLocalTranslation(
            centerX - OPTION_WIDTH / 2,
            startY - (index + 1) * (OPTION_HEIGHT + OPTION_SPACING),
            0
        );
        return bg;
    }

    private BitmapText createOptionText(BitmapFont font, int centerX, int startY, int index, float textSize, String text) {
        BitmapText optionText = new BitmapText(font);
        optionText.setSize(textSize);
        optionText.setColor(ColorRGBA.White);
        optionText.setText(text);
        float textWidth = optionText.getLineWidth();
        optionText.setLocalTranslation(
            centerX - OPTION_WIDTH / 2 + 20, // Left-aligned with padding
            startY - index * (OPTION_HEIGHT + OPTION_SPACING) - (OPTION_HEIGHT - textSize) / 2,
            1
        );
        return optionText;
    }

    /**
     * Show the level up screen for the given player
     */
    public void show(Player player) {
        this.player = player;
        this.selectedOption = 0; // Default to Strength

        // Update level text
        int newLevel = player.getLevel() + 1; // Show the level they will become
        levelText.setText("You are now Level " + newLevel);
        int screenWidth = app.getCamera().getWidth();
        float levelWidth = levelText.getLineWidth();
        levelText.setLocalTranslation(
            (screenWidth - levelWidth) / 2,
            levelText.getLocalTranslation().y,
            1
        );

        // Update stat texts with current values and what they will become
        updateStatTexts();
        updateSelectionHighlight();

        if (!isVisible) {
            guiNode.attachChild(levelUpNode);
            setupInput();
            isVisible = true;
        }
    }

    /**
     * Update the stat option texts to show current and new values
     */
    private void updateStatTexts() {
        if (player == null) return;

        // Format: "1. Strength (DMG: X -> Y)"
        float currentDmg = player.getBaseDamage();
        float newDmg = 10f * (1.0f + player.getStrength() * 0.15f); // After +1 strength
        strengthText.setText(String.format("1. Strength     DMG: %.0f%% -> %.0f%%",
            player.getDamageMultiplier() * 100,
            (1.0f + player.getStrength() * 0.15f) * 100));

        // Agility affects move speed
        float currentSpeed = player.getMoveSpeed();
        float newSpeed = 7.0f + player.getAgility() * 0.5f; // After +1 agility
        agilityText.setText(String.format("2. Agility      Speed: %.1f -> %.1f",
            currentSpeed, newSpeed));

        // Vitality affects max health
        float currentMaxHp = player.getMaxHealth();
        float newMaxHp = 100f + player.getVitality() * 20f; // After +1 vitality
        vitalityText.setText(String.format("3. Vitality     HP: %.0f -> %.0f (Full Heal!)",
            currentMaxHp, newMaxHp));

        // Dexterity affects fire rate (show as attacks per second)
        float currentCooldown = player.getFireCooldown();
        float tempDex = player.getDexterity();
        float newReduction = 1.0f - tempDex * 0.10f;
        float newCooldown = 0.15f * Math.max(0.30f, newReduction);
        dexterityText.setText(String.format("4. Dexterity    Fire Rate: %.1f/s -> %.1f/s",
            1.0f / currentCooldown, 1.0f / newCooldown));
    }

    /**
     * Hide the level up screen
     */
    public void hide() {
        if (isVisible) {
            guiNode.detachChild(levelUpNode);
            clearInput();
            isVisible = false;
        }
    }

    /**
     * Check if the level up screen is visible
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Set the listener for level up events
     */
    public void setListener(LevelUpListener listener) {
        this.listener = listener;
    }

    /**
     * Set the gamepad manager for controller support
     */
    public void setGamepadManager(GamepadManager gamepadManager) {
        this.gamepadManager = gamepadManager;
    }

    /**
     * Update method called each frame to handle gamepad input
     */
    public void update() {
        if (!isVisible || gamepadManager == null) {
            return;
        }

        // Handle D-pad navigation
        if (gamepadManager.isDpadUpJustPressed()) {
            moveSelection(-1);
        }
        if (gamepadManager.isDpadDownJustPressed()) {
            moveSelection(1);
        }

        // Handle A button for selection
        if (gamepadManager.isAButtonJustPressed()) {
            confirmSelection();
        }
    }

    private void setupInput() {
        InputManager inputManager = app.getInputManager();

        // Number keys for direct selection
        inputManager.addMapping("LevelUp_Select1", new KeyTrigger(KeyInput.KEY_1));
        inputManager.addMapping("LevelUp_Select2", new KeyTrigger(KeyInput.KEY_2));
        inputManager.addMapping("LevelUp_Select3", new KeyTrigger(KeyInput.KEY_3));
        inputManager.addMapping("LevelUp_Select4", new KeyTrigger(KeyInput.KEY_4));

        // Arrow/WASD navigation
        inputManager.addMapping("LevelUp_Up", new KeyTrigger(KeyInput.KEY_W), new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("LevelUp_Down", new KeyTrigger(KeyInput.KEY_S), new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("LevelUp_Confirm", new KeyTrigger(KeyInput.KEY_RETURN), new KeyTrigger(KeyInput.KEY_SPACE));

        inputManager.addListener(this,
            "LevelUp_Select1", "LevelUp_Select2", "LevelUp_Select3", "LevelUp_Select4",
            "LevelUp_Up", "LevelUp_Down", "LevelUp_Confirm");
    }

    private void clearInput() {
        InputManager inputManager = app.getInputManager();

        inputManager.deleteMapping("LevelUp_Select1");
        inputManager.deleteMapping("LevelUp_Select2");
        inputManager.deleteMapping("LevelUp_Select3");
        inputManager.deleteMapping("LevelUp_Select4");
        inputManager.deleteMapping("LevelUp_Up");
        inputManager.deleteMapping("LevelUp_Down");
        inputManager.deleteMapping("LevelUp_Confirm");

        inputManager.removeListener(this);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) {
            return;
        }

        switch (name) {
            case "LevelUp_Select1":
                selectedOption = 0;
                confirmSelection();
                break;
            case "LevelUp_Select2":
                selectedOption = 1;
                confirmSelection();
                break;
            case "LevelUp_Select3":
                selectedOption = 2;
                confirmSelection();
                break;
            case "LevelUp_Select4":
                selectedOption = 3;
                confirmSelection();
                break;
            case "LevelUp_Up":
                moveSelection(-1);
                break;
            case "LevelUp_Down":
                moveSelection(1);
                break;
            case "LevelUp_Confirm":
                confirmSelection();
                break;
        }
    }

    private void moveSelection(int direction) {
        selectedOption = (selectedOption + direction + 4) % 4;
        updateSelectionHighlight();
    }

    private void updateSelectionHighlight() {
        // Reset all options to unselected state
        setOptionHighlight(strengthBg, strengthText, selectedOption == 0);
        setOptionHighlight(agilityBg, agilityText, selectedOption == 1);
        setOptionHighlight(vitalityBg, vitalityText, selectedOption == 2);
        setOptionHighlight(dexterityBg, dexterityText, selectedOption == 3);
    }

    private void setOptionHighlight(Geometry bg, BitmapText text, boolean selected) {
        if (selected) {
            text.setColor(new ColorRGBA(1.0f, 0.85f, 0.0f, 1.0f)); // Gold
            Material mat = bg.getMaterial();
            mat.setColor("Color", new ColorRGBA(0.4f, 0.35f, 0.1f, 0.95f)); // Highlighted
        } else {
            text.setColor(ColorRGBA.White);
            Material mat = bg.getMaterial();
            mat.setColor("Color", new ColorRGBA(0.2f, 0.2f, 0.3f, 0.8f)); // Normal
        }
    }

    private void confirmSelection() {
        if (player != null) {
            // Apply the stat increase (1-indexed for the player method)
            player.levelUp(selectedOption + 1);
        }

        if (listener != null) {
            listener.onStatSelected(selectedOption + 1);
        }

        hide();
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        hide();
    }

    /**
     * Listener interface for level up events
     */
    public interface LevelUpListener {
        /**
         * Called when a stat is selected for upgrade
         * @param stat The stat that was selected (1=Strength, 2=Agility, 3=Vitality, 4=Dexterity)
         */
        void onStatSelected(int stat);
    }
}
