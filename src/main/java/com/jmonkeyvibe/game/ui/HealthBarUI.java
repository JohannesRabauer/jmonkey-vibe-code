package com.jmonkeyvibe.game.ui;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

/**
 * Player health bar UI displayed on the GUI layer
 */
public class HealthBarUI {

    private SimpleApplication app;
    private Node guiNode;
    private Node healthBarNode;

    private Geometry healthBarBackground;
    private Geometry healthBarFill;
    private BitmapText healthText;

    private float maxWidth;
    private float barHeight;
    private float currentHealthPercent = 1.0f;

    private static final int BAR_WIDTH = 200;
    private static final int BAR_HEIGHT = 20;
    private static final int MARGIN = 20;
    private static final int BORDER = 2;

    public HealthBarUI(SimpleApplication app) {
        this.app = app;
        this.guiNode = app.getGuiNode();
        this.healthBarNode = new Node("HealthBarUI");
        this.maxWidth = BAR_WIDTH - (BORDER * 2);
        this.barHeight = BAR_HEIGHT - (BORDER * 2);

        createHealthBarUI();
    }

    private void createHealthBarUI() {
        BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");

        int screenHeight = app.getCamera().getHeight();

        // Position in top-left corner
        int barX = MARGIN;
        int barY = screenHeight - MARGIN - BAR_HEIGHT;

        // Health bar background (red - shows damage)
        Quad bgQuad = new Quad(BAR_WIDTH, BAR_HEIGHT);
        healthBarBackground = new Geometry("HealthBarBackground", bgQuad);
        Material bgMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        bgMat.setColor("Color", new ColorRGBA(0.3f, 0.0f, 0.0f, 0.9f)); // Dark red background
        bgMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        healthBarBackground.setMaterial(bgMat);
        healthBarBackground.setLocalTranslation(barX, barY, 0);

        // Health bar fill (green - current health)
        Quad fillQuad = new Quad(maxWidth, barHeight);
        healthBarFill = new Geometry("HealthBarFill", fillQuad);
        Material fillMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        fillMat.setColor("Color", new ColorRGBA(0.0f, 0.8f, 0.0f, 1.0f)); // Bright green
        fillMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        healthBarFill.setMaterial(fillMat);
        healthBarFill.setLocalTranslation(barX + BORDER, barY + BORDER, 1);

        // Health text showing current/max
        healthText = new BitmapText(font);
        float textSize = font.getCharSet().getRenderedSize();
        healthText.setSize(textSize);
        healthText.setColor(ColorRGBA.White);
        healthText.setText("100 / 100");
        healthText.setLocalTranslation(barX + BAR_WIDTH + 10, barY + BAR_HEIGHT - 3, 1);

        // Label
        BitmapText healthLabel = new BitmapText(font);
        healthLabel.setSize(textSize);
        healthLabel.setColor(ColorRGBA.White);
        healthLabel.setText("HP:");
        healthLabel.setLocalTranslation(barX - 30, barY + BAR_HEIGHT - 3, 1);

        // Add to health bar node
        healthBarNode.attachChild(healthBarBackground);
        healthBarNode.attachChild(healthBarFill);
        healthBarNode.attachChild(healthText);
        healthBarNode.attachChild(healthLabel);
    }

    /**
     * Update the health bar display
     * @param currentHealth Current health value
     * @param maxHealth Maximum health value
     */
    public void update(float currentHealth, float maxHealth) {
        currentHealthPercent = Math.max(0, Math.min(1, currentHealth / maxHealth));

        // Update the fill bar width
        float newWidth = maxWidth * currentHealthPercent;

        // Recreate the fill geometry with new width
        Quad fillQuad = new Quad(Math.max(0.1f, newWidth), barHeight);
        healthBarFill.setMesh(fillQuad);

        // Update color based on health percentage (green -> yellow -> red)
        ColorRGBA healthColor;
        if (currentHealthPercent > 0.6f) {
            healthColor = new ColorRGBA(0.0f, 0.8f, 0.0f, 1.0f); // Green
        } else if (currentHealthPercent > 0.3f) {
            healthColor = new ColorRGBA(0.8f, 0.8f, 0.0f, 1.0f); // Yellow
        } else {
            healthColor = new ColorRGBA(0.8f, 0.0f, 0.0f, 1.0f); // Red
        }
        healthBarFill.getMaterial().setColor("Color", healthColor);

        // Update text
        healthText.setText((int)currentHealth + " / " + (int)maxHealth);
    }

    /**
     * Show the health bar UI
     */
    public void show() {
        if (!guiNode.hasChild(healthBarNode)) {
            guiNode.attachChild(healthBarNode);
        }
    }

    /**
     * Hide the health bar UI
     */
    public void hide() {
        if (guiNode.hasChild(healthBarNode)) {
            guiNode.detachChild(healthBarNode);
        }
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        hide();
        healthBarNode.detachAllChildren();
    }
}
