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

import java.util.ArrayList;
import java.util.List;

/**
 * In-game dialog UI system
 */
public class DialogUI {
    
    private SimpleApplication app;
    private Node guiNode;
    private Node dialogNode;
    
    private BitmapText npcNameText;
    private BitmapText dialogText;
    private List<BitmapText> choiceTexts;
    private BitmapText promptText;
    private BitmapText customInputText;
    private Geometry customInputBackground;
    
    private Geometry dialogBackground;
    private List<Geometry> choiceBackgrounds;
    
    private boolean isVisible = false;
    private boolean isTypingCustomInput = false;
    private List<String> currentChoices;
    private int selectedChoice = 0;
    private StringBuilder currentDialogText = new StringBuilder();
    
    private static final int DIALOG_WIDTH = 600;
    private static final int DIALOG_HEIGHT = 200;
    private static final int CHOICE_HEIGHT = 40;
    private static final int PADDING = 10;
    
    public DialogUI(SimpleApplication app) {
        this.app = app;
        this.guiNode = app.getGuiNode();
        this.dialogNode = new Node("DialogUI");
        this.choiceTexts = new ArrayList<>();
        this.choiceBackgrounds = new ArrayList<>();
        this.currentChoices = new ArrayList<>();
        
        createDialogUI();
    }
    
    private void createDialogUI() {
        BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        
        int screenWidth = app.getCamera().getWidth();
        int screenHeight = app.getCamera().getHeight();
        
        // Dialog background
        Quad dialogQuad = new Quad(DIALOG_WIDTH, DIALOG_HEIGHT);
        dialogBackground = new Geometry("DialogBackground", dialogQuad);
        Material dialogMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        dialogMat.setColor("Color", new ColorRGBA(0.1f, 0.1f, 0.15f, 0.9f));
        dialogMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        dialogBackground.setMaterial(dialogMat);
        dialogBackground.setLocalTranslation(
            (screenWidth - DIALOG_WIDTH) / 2,
            screenHeight - DIALOG_HEIGHT - 20,
            0
        );
        
        // NPC name
        npcNameText = new BitmapText(font);
        npcNameText.setSize(font.getCharSet().getRenderedSize() * 1.2f);
        npcNameText.setColor(ColorRGBA.Yellow);
        npcNameText.setLocalTranslation(
            (screenWidth - DIALOG_WIDTH) / 2 + PADDING,
            screenHeight - 30,
            1
        );
        
        // Dialog text
        dialogText = new BitmapText(font);
        dialogText.setSize(font.getCharSet().getRenderedSize());
        dialogText.setColor(ColorRGBA.White);
        dialogText.setLocalTranslation(
            (screenWidth - DIALOG_WIDTH) / 2 + PADDING,
            screenHeight - 60,
            1
        );
        dialogText.setBox(new com.jme3.font.Rectangle(0, 0, DIALOG_WIDTH - 2 * PADDING, DIALOG_HEIGHT - 80));
        dialogText.setLineWrapMode(com.jme3.font.LineWrapMode.Word);
        
        // Prompt text
        promptText = new BitmapText(font);
        promptText.setSize(font.getCharSet().getRenderedSize() * 0.9f);
        promptText.setColor(ColorRGBA.LightGray);
        promptText.setText("Press 1-3 to select response, or T to type custom response");
        promptText.setLocalTranslation(
            (screenWidth - DIALOG_WIDTH) / 2 + PADDING,
            screenHeight - DIALOG_HEIGHT - 255,
            1
        );
        
        // Custom input background
        Quad inputQuad = new Quad(DIALOG_WIDTH - 2 * PADDING, 30);
        customInputBackground = new Geometry("CustomInputBg", inputQuad);
        Material inputMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        inputMat.setColor("Color", new ColorRGBA(0.1f, 0.3f, 0.1f, 0.9f));
        inputMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        customInputBackground.setMaterial(inputMat);
        customInputBackground.setLocalTranslation(
            (screenWidth - DIALOG_WIDTH) / 2 + PADDING,
            screenHeight - DIALOG_HEIGHT - 285,
            0
        );
        
        // Custom input text
        customInputText = new BitmapText(font);
        customInputText.setSize(font.getCharSet().getRenderedSize());
        customInputText.setColor(ColorRGBA.White);
        customInputText.setText("> ");
        customInputText.setLocalTranslation(
            (screenWidth - DIALOG_WIDTH) / 2 + PADDING + 5,
            screenHeight - DIALOG_HEIGHT - 285 + 20,
            1
        );
        
        // Create 3 choice slots
        for (int i = 0; i < 3; i++) {
            // Choice background
            Quad choiceQuad = new Quad(DIALOG_WIDTH - 2 * PADDING, CHOICE_HEIGHT);
            Geometry choiceBg = new Geometry("ChoiceBg" + i, choiceQuad);
            Material choiceMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            choiceMat.setColor("Color", new ColorRGBA(0.2f, 0.2f, 0.3f, 0.8f));
            choiceMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            choiceBg.setMaterial(choiceMat);
            choiceBg.setLocalTranslation(
                (screenWidth - DIALOG_WIDTH) / 2 + PADDING,
                screenHeight - DIALOG_HEIGHT - 50 - (i * (CHOICE_HEIGHT + 5)),
                0
            );
            choiceBackgrounds.add(choiceBg);
            
            // Choice text
            BitmapText choiceText = new BitmapText(font);
            choiceText.setSize(font.getCharSet().getRenderedSize());
            choiceText.setColor(ColorRGBA.White);
            choiceText.setLocalTranslation(
                (screenWidth - DIALOG_WIDTH) / 2 + PADDING + 5,
                screenHeight - DIALOG_HEIGHT - 50 - (i * (CHOICE_HEIGHT + 5)) + CHOICE_HEIGHT - 10,
                1
            );
            choiceText.setBox(new com.jme3.font.Rectangle(0, 0, DIALOG_WIDTH - 2 * PADDING - 10, CHOICE_HEIGHT));
            choiceText.setLineWrapMode(com.jme3.font.LineWrapMode.Word);
            choiceTexts.add(choiceText);
        }
        
        // Add to dialog node (but don't attach yet)
        dialogNode.attachChild(dialogBackground);
        dialogNode.attachChild(npcNameText);
        dialogNode.attachChild(dialogText);
        dialogNode.attachChild(promptText);
        dialogNode.attachChild(customInputBackground);
        dialogNode.attachChild(customInputText);
        for (Geometry bg : choiceBackgrounds) {
            dialogNode.attachChild(bg);
        }
        for (BitmapText text : choiceTexts) {
            dialogNode.attachChild(text);
        }
        
        // Hide custom input by default
        customInputBackground.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        customInputText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
    }
    
    public void show(String npcName, String dialog, List<String> choices) {
        npcNameText.setText(npcName);
        currentDialogText.setLength(0);
        currentDialogText.append(dialog);
        dialogText.setText(dialog);
        
        currentChoices.clear();
        currentChoices.addAll(choices);
        selectedChoice = 0;
        
        // Update choice texts
        for (int i = 0; i < 3; i++) {
            if (i < choices.size()) {
                choiceTexts.get(i).setText((i + 1) + ". " + choices.get(i));
                choiceTexts.get(i).setColor(ColorRGBA.White);
            } else {
                choiceTexts.get(i).setText("");
            }
        }
        
        updateChoiceHighlight();
        
        if (!isVisible) {
            guiNode.attachChild(dialogNode);
            isVisible = true;
        }
    }
    
    public void startStreamingDialog(String npcName, List<String> choices) {
        npcNameText.setText(npcName);
        currentDialogText.setLength(0);
        dialogText.setText("");
        
        currentChoices.clear();
        currentChoices.addAll(choices);
        selectedChoice = 0;
        
        // Update choice texts
        for (int i = 0; i < 3; i++) {
            if (i < choices.size()) {
                choiceTexts.get(i).setText((i + 1) + ". " + choices.get(i));
                choiceTexts.get(i).setColor(ColorRGBA.White);
            } else {
                choiceTexts.get(i).setText("");
            }
        }
        
        updateChoiceHighlight();
        
        if (!isVisible) {
            guiNode.attachChild(dialogNode);
            isVisible = true;
        }
    }
    
    public void appendStreamedText(String text) {
        currentDialogText.append(text);
        dialogText.setText(currentDialogText.toString());
    }
    
    public void setCustomInputVisible(boolean visible) {
        isTypingCustomInput = visible;
        if (visible) {
            customInputBackground.setCullHint(com.jme3.scene.Spatial.CullHint.Never);
            customInputText.setCullHint(com.jme3.scene.Spatial.CullHint.Never);
            customInputText.setText("> ");
            // Hide choices while typing
            for (Geometry bg : choiceBackgrounds) {
                bg.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
            }
            for (BitmapText text : choiceTexts) {
                text.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
            }
        } else {
            customInputBackground.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
            customInputText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
            // Show choices again
            for (Geometry bg : choiceBackgrounds) {
                bg.setCullHint(com.jme3.scene.Spatial.CullHint.Never);
            }
            for (BitmapText text : choiceTexts) {
                text.setCullHint(com.jme3.scene.Spatial.CullHint.Never);
            }
        }
    }
    
    public void updateCustomInputText(String text) {
        customInputText.setText("> " + text + "_");
    }
    
    public void hide() {
        if (isVisible) {
            guiNode.detachChild(dialogNode);
            isVisible = false;
        }
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    public boolean isTypingCustomInput() {
        return isTypingCustomInput;
    }
    
    public void selectChoice(int index) {
        if (index >= 0 && index < currentChoices.size()) {
            selectedChoice = index;
            updateChoiceHighlight();
        }
    }
    
    public String getSelectedChoice() {
        if (selectedChoice >= 0 && selectedChoice < currentChoices.size()) {
            return currentChoices.get(selectedChoice);
        }
        return null;
    }
    
    public int getSelectedChoiceIndex() {
        return selectedChoice;
    }
    
    private void updateChoiceHighlight() {
        for (int i = 0; i < choiceTexts.size(); i++) {
            if (i == selectedChoice && i < currentChoices.size()) {
                choiceTexts.get(i).setColor(ColorRGBA.Yellow);
                // Update background color
                Material mat = choiceBackgrounds.get(i).getMaterial();
                mat.setColor("Color", new ColorRGBA(0.4f, 0.4f, 0.1f, 0.9f));
            } else if (i < currentChoices.size()) {
                choiceTexts.get(i).setColor(ColorRGBA.White);
                Material mat = choiceBackgrounds.get(i).getMaterial();
                mat.setColor("Color", new ColorRGBA(0.2f, 0.2f, 0.3f, 0.8f));
            }
        }
    }
    
    public void showCustomInputPrompt() {
        promptText.setText("Type your response and press ENTER (or ESC to cancel)");
    }
    
    public void showChoicePrompt() {
        promptText.setText("Press 1-3 to select response, or T to type custom response");
    }
    
    public String getCurrentDialogText() {
        return currentDialogText.toString();
    }
}
