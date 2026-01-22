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
    private static final int CHOICE_HEIGHT = 35;
    private static final int PADDING = 15;
    private static final int SPACING = 8;
    private static final int MARGIN_TOP = 20;
    private static final int INPUT_HEIGHT = 35;
    private static final int NPC_NAME_HEIGHT = 25;
    private static final int DIALOG_TEXT_TOP_OFFSET = 50;
    
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
        
        // Calculate base positions from top
        int dialogX = (screenWidth - DIALOG_WIDTH) / 2;
        int dialogY = screenHeight - MARGIN_TOP - DIALOG_HEIGHT;
        
        // Dialog background
        Quad dialogQuad = new Quad(DIALOG_WIDTH, DIALOG_HEIGHT);
        dialogBackground = new Geometry("DialogBackground", dialogQuad);
        Material dialogMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        dialogMat.setColor("Color", new ColorRGBA(0.1f, 0.1f, 0.15f, 0.9f));
        dialogMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        dialogBackground.setMaterial(dialogMat);
        dialogBackground.setLocalTranslation(dialogX, dialogY, 0);
        
        // NPC name - positioned at top of dialog box
        npcNameText = new BitmapText(font);
        float npcNameSize = font.getCharSet().getRenderedSize() * 1.2f;
        npcNameText.setSize(npcNameSize);
        npcNameText.setColor(ColorRGBA.Yellow);
        npcNameText.setLocalTranslation(
            dialogX + PADDING,
            dialogY + DIALOG_HEIGHT - PADDING,
            1
        );

        // Dialog text - below NPC name with proper spacing
        dialogText = new BitmapText(font);
        float dialogTextSize = font.getCharSet().getRenderedSize();
        dialogText.setSize(dialogTextSize);
        dialogText.setColor(ColorRGBA.White);
        int dialogTextY = dialogY + DIALOG_HEIGHT - DIALOG_TEXT_TOP_OFFSET;
        int dialogTextHeight = DIALOG_HEIGHT - DIALOG_TEXT_TOP_OFFSET - PADDING;
        dialogText.setLocalTranslation(
            dialogX + PADDING,
            dialogTextY,
            1
        );
        dialogText.setBox(new com.jme3.font.Rectangle(0, 0, DIALOG_WIDTH - 2 * PADDING, dialogTextHeight));
        dialogText.setLineWrapMode(com.jme3.font.LineWrapMode.Word);
        
        // Prompt text - below dialog box
        float promptSize = font.getCharSet().getRenderedSize() * 0.9f;
        int promptY = dialogY - SPACING;
        promptText = new BitmapText(font);
        promptText.setSize(promptSize);
        promptText.setColor(ColorRGBA.LightGray);
        promptText.setText("Keys 1-3 or DPad+A to select | T or Y to type | ESC or B to close");
        promptText.setLocalTranslation(dialogX + PADDING, promptY, 1);

        // Choices - below prompt with proper spacing
        float choiceTextSize = font.getCharSet().getRenderedSize();
        int choicesStartY = promptY - (int)promptSize - SPACING;
        for (int i = 0; i < 3; i++) {
            int choiceY = choicesStartY - (i * (CHOICE_HEIGHT + SPACING));

            // Choice background - same width as dialog box
            Quad choiceQuad = new Quad(DIALOG_WIDTH, CHOICE_HEIGHT);
            Geometry choiceBg = new Geometry("ChoiceBg" + i, choiceQuad);
            Material choiceMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            choiceMat.setColor("Color", new ColorRGBA(0.2f, 0.2f, 0.3f, 0.8f));
            choiceMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            choiceBg.setMaterial(choiceMat);
            choiceBg.setLocalTranslation(dialogX, choiceY, 0);
            choiceBackgrounds.add(choiceBg);

            // Choice text - vertically centered in the box
            // BitmapText renders from baseline, so position at box bottom + offset to center
            BitmapText choiceText = new BitmapText(font);
            choiceText.setSize(choiceTextSize);
            choiceText.setColor(ColorRGBA.White);
            int textVerticalOffset = (int)((CHOICE_HEIGHT + choiceTextSize) / 2);
            choiceText.setLocalTranslation(
                dialogX + PADDING,
                choiceY + textVerticalOffset,
                1
            );
            choiceText.setBox(new com.jme3.font.Rectangle(0, 0, DIALOG_WIDTH - 2 * PADDING, CHOICE_HEIGHT));
            choiceText.setLineWrapMode(com.jme3.font.LineWrapMode.Word);
            choiceTexts.add(choiceText);
        }
        
        // Custom input - below all choices
        int lastChoiceY = choicesStartY - (2 * (CHOICE_HEIGHT + SPACING));
        int inputY = lastChoiceY - SPACING - INPUT_HEIGHT;

        // Custom input background - same width as dialog box
        Quad inputQuad = new Quad(DIALOG_WIDTH, INPUT_HEIGHT);
        customInputBackground = new Geometry("CustomInputBg", inputQuad);
        Material inputMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        inputMat.setColor("Color", new ColorRGBA(0.1f, 0.3f, 0.1f, 0.9f));
        inputMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        customInputBackground.setMaterial(inputMat);
        customInputBackground.setLocalTranslation(dialogX, inputY, 0);

        // Custom input text - vertically centered in the box
        float inputTextSize = font.getCharSet().getRenderedSize();
        customInputText = new BitmapText(font);
        customInputText.setSize(inputTextSize);
        customInputText.setColor(ColorRGBA.White);
        customInputText.setText("> ");
        int inputTextVerticalOffset = (int)((INPUT_HEIGHT + inputTextSize) / 2);
        customInputText.setLocalTranslation(
            dialogX + PADDING,
            inputY + inputTextVerticalOffset,
            1
        );
        
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

    /**
     * Highlight a choice visually (for gamepad navigation)
     */
    public void highlightChoice(int index) {
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

    /**
     * Get the number of available choices in the current dialog
     */
    public int getChoiceCount() {
        return currentChoices.size();
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
        promptText.setText("Keys 1-3 or DPad+A to select | T or Y to type | ESC or B to close");
    }
    
    public String getCurrentDialogText() {
        return currentDialogText.toString();
    }
}
