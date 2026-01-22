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
import com.jmonkeyvibe.game.input.GamepadManager;

/**
 * Game Over UI screen displayed when the player dies in the dungeon.
 * Shows final wave reached and options to retry or exit to overworld.
 */
public class GameOverUI implements ActionListener {

    private SimpleApplication app;
    private Node guiNode;
    private Node gameOverNode;

    private BitmapText gameOverText;
    private BitmapText waveText;
    private BitmapText tryAgainText;
    private BitmapText exitText;
    private BitmapText instructionsText;

    private Geometry darkOverlay;
    private Geometry tryAgainBackground;
    private Geometry exitBackground;

    private boolean isVisible = false;
    private int selectedOption = 0; // 0 = Try Again, 1 = Exit to Overworld
    private int waveReached = 0;

    private GameOverListener listener;
    private GamepadManager gamepadManager;

    // UI layout constants
    private static final int OPTION_WIDTH = 300;
    private static final int OPTION_HEIGHT = 50;
    private static final int OPTION_SPACING = 20;

    public GameOverUI(SimpleApplication app) {
        this.app = app;
        this.guiNode = app.getGuiNode();
        this.gameOverNode = new Node("GameOverUI");

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
        overlayMat.setColor("Color", new ColorRGBA(0f, 0f, 0f, 0.8f));
        overlayMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        darkOverlay.setMaterial(overlayMat);
        darkOverlay.setLocalTranslation(0, 0, -1);

        // "GAME OVER" title - large and prominent
        gameOverText = new BitmapText(font);
        float titleSize = font.getCharSet().getRenderedSize() * 3.0f;
        gameOverText.setSize(titleSize);
        gameOverText.setColor(new ColorRGBA(0.9f, 0.1f, 0.1f, 1.0f)); // Red color
        gameOverText.setText("GAME OVER");
        float gameOverWidth = gameOverText.getLineWidth();
        gameOverText.setLocalTranslation(
            (screenWidth - gameOverWidth) / 2,
            screenHeight * 0.7f,
            1
        );

        // Wave reached text
        waveText = new BitmapText(font);
        float waveSize = font.getCharSet().getRenderedSize() * 1.5f;
        waveText.setSize(waveSize);
        waveText.setColor(ColorRGBA.White);
        waveText.setText("You reached Wave 1");
        float waveWidth = waveText.getLineWidth();
        waveText.setLocalTranslation(
            (screenWidth - waveWidth) / 2,
            screenHeight * 0.55f,
            1
        );

        // Calculate center positions for options
        int centerX = screenWidth / 2;
        int optionsStartY = (int)(screenHeight * 0.40f);

        // "Try Again" option background
        Quad tryAgainQuad = new Quad(OPTION_WIDTH, OPTION_HEIGHT);
        tryAgainBackground = new Geometry("TryAgainBg", tryAgainQuad);
        Material tryAgainMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        tryAgainMat.setColor("Color", new ColorRGBA(0.3f, 0.3f, 0.1f, 0.9f));
        tryAgainMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        tryAgainBackground.setMaterial(tryAgainMat);
        tryAgainBackground.setLocalTranslation(
            centerX - OPTION_WIDTH / 2,
            optionsStartY - OPTION_HEIGHT,
            0
        );

        // "Try Again" text
        tryAgainText = new BitmapText(font);
        float optionTextSize = font.getCharSet().getRenderedSize() * 1.2f;
        tryAgainText.setSize(optionTextSize);
        tryAgainText.setColor(ColorRGBA.Yellow);
        tryAgainText.setText("1. Try Again");
        float tryAgainTextWidth = tryAgainText.getLineWidth();
        tryAgainText.setLocalTranslation(
            centerX - tryAgainTextWidth / 2,
            optionsStartY - (OPTION_HEIGHT - optionTextSize) / 2,
            1
        );

        // "Exit to Overworld" option background
        Quad exitQuad = new Quad(OPTION_WIDTH, OPTION_HEIGHT);
        exitBackground = new Geometry("ExitBg", exitQuad);
        Material exitMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        exitMat.setColor("Color", new ColorRGBA(0.2f, 0.2f, 0.3f, 0.8f));
        exitMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        exitBackground.setMaterial(exitMat);
        exitBackground.setLocalTranslation(
            centerX - OPTION_WIDTH / 2,
            optionsStartY - OPTION_HEIGHT * 2 - OPTION_SPACING,
            0
        );

        // "Exit to Overworld" text
        exitText = new BitmapText(font);
        exitText.setSize(optionTextSize);
        exitText.setColor(ColorRGBA.White);
        exitText.setText("2. Exit to Overworld");
        float exitTextWidth = exitText.getLineWidth();
        exitText.setLocalTranslation(
            centerX - exitTextWidth / 2,
            optionsStartY - OPTION_HEIGHT - OPTION_SPACING - (OPTION_HEIGHT - optionTextSize) / 2,
            1
        );

        // Instructions text
        instructionsText = new BitmapText(font);
        float instructionSize = font.getCharSet().getRenderedSize() * 0.9f;
        instructionsText.setSize(instructionSize);
        instructionsText.setColor(ColorRGBA.LightGray);
        instructionsText.setText("Press 1/2, W/S + Enter, or use D-pad + A to select");
        float instructionsWidth = instructionsText.getLineWidth();
        instructionsText.setLocalTranslation(
            (screenWidth - instructionsWidth) / 2,
            screenHeight * 0.15f,
            1
        );

        // Add all elements to the game over node
        gameOverNode.attachChild(darkOverlay);
        gameOverNode.attachChild(gameOverText);
        gameOverNode.attachChild(waveText);
        gameOverNode.attachChild(tryAgainBackground);
        gameOverNode.attachChild(tryAgainText);
        gameOverNode.attachChild(exitBackground);
        gameOverNode.attachChild(exitText);
        gameOverNode.attachChild(instructionsText);
    }

    /**
     * Show the game over screen with the wave reached
     */
    public void show(int waveReached) {
        this.waveReached = waveReached;
        this.selectedOption = 0; // Default to "Try Again"

        // Update wave text
        waveText.setText("You reached Wave " + waveReached);
        // Re-center the wave text
        int screenWidth = app.getCamera().getWidth();
        float waveWidth = waveText.getLineWidth();
        waveText.setLocalTranslation(
            (screenWidth - waveWidth) / 2,
            waveText.getLocalTranslation().y,
            1
        );

        updateSelectionHighlight();

        if (!isVisible) {
            guiNode.attachChild(gameOverNode);
            setupInput();
            isVisible = true;
        }
    }

    /**
     * Hide the game over screen
     */
    public void hide() {
        if (isVisible) {
            guiNode.detachChild(gameOverNode);
            clearInput();
            isVisible = false;
        }
    }

    /**
     * Check if the game over screen is visible
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Set the listener for game over events
     */
    public void setListener(GameOverListener listener) {
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

        // Handle B button to exit to overworld
        if (gamepadManager.isBButtonJustPressed()) {
            selectedOption = 1;
            confirmSelection();
        }
    }

    private void setupInput() {
        InputManager inputManager = app.getInputManager();

        // Number keys for direct selection
        inputManager.addMapping("GameOver_Select1", new KeyTrigger(KeyInput.KEY_1));
        inputManager.addMapping("GameOver_Select2", new KeyTrigger(KeyInput.KEY_2));

        // Arrow/WASD navigation
        inputManager.addMapping("GameOver_Up", new KeyTrigger(KeyInput.KEY_W), new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("GameOver_Down", new KeyTrigger(KeyInput.KEY_S), new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("GameOver_Confirm", new KeyTrigger(KeyInput.KEY_RETURN), new KeyTrigger(KeyInput.KEY_SPACE));

        inputManager.addListener(this,
            "GameOver_Select1", "GameOver_Select2",
            "GameOver_Up", "GameOver_Down", "GameOver_Confirm");
    }

    private void clearInput() {
        InputManager inputManager = app.getInputManager();

        inputManager.deleteMapping("GameOver_Select1");
        inputManager.deleteMapping("GameOver_Select2");
        inputManager.deleteMapping("GameOver_Up");
        inputManager.deleteMapping("GameOver_Down");
        inputManager.deleteMapping("GameOver_Confirm");

        inputManager.removeListener(this);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) {
            return;
        }

        switch (name) {
            case "GameOver_Select1":
                selectedOption = 0;
                confirmSelection();
                break;
            case "GameOver_Select2":
                selectedOption = 1;
                confirmSelection();
                break;
            case "GameOver_Up":
                moveSelection(-1);
                break;
            case "GameOver_Down":
                moveSelection(1);
                break;
            case "GameOver_Confirm":
                confirmSelection();
                break;
        }
    }

    private void moveSelection(int direction) {
        selectedOption = (selectedOption + direction + 2) % 2;
        updateSelectionHighlight();
    }

    private void updateSelectionHighlight() {
        if (selectedOption == 0) {
            // Try Again selected
            tryAgainText.setColor(ColorRGBA.Yellow);
            exitText.setColor(ColorRGBA.White);

            Material tryAgainMat = tryAgainBackground.getMaterial();
            tryAgainMat.setColor("Color", new ColorRGBA(0.4f, 0.4f, 0.1f, 0.9f));

            Material exitMat = exitBackground.getMaterial();
            exitMat.setColor("Color", new ColorRGBA(0.2f, 0.2f, 0.3f, 0.8f));
        } else {
            // Exit to Overworld selected
            tryAgainText.setColor(ColorRGBA.White);
            exitText.setColor(ColorRGBA.Yellow);

            Material tryAgainMat = tryAgainBackground.getMaterial();
            tryAgainMat.setColor("Color", new ColorRGBA(0.2f, 0.2f, 0.3f, 0.8f));

            Material exitMat = exitBackground.getMaterial();
            exitMat.setColor("Color", new ColorRGBA(0.4f, 0.4f, 0.1f, 0.9f));
        }
    }

    private void confirmSelection() {
        if (listener != null) {
            if (selectedOption == 0) {
                listener.onTryAgain();
            } else {
                listener.onExitToOverworld();
            }
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
     * Listener interface for game over screen events
     */
    public interface GameOverListener {
        /**
         * Called when player selects "Try Again"
         */
        void onTryAgain();

        /**
         * Called when player selects "Exit to Overworld"
         */
        void onExitToOverworld();
    }
}
