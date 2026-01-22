package com.jmonkeyvibe.game.states;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jmonkeyvibe.game.world.WorldGenerator;
import com.jmonkeyvibe.game.entities.Player;
import com.jmonkeyvibe.game.entities.NPC;
import com.jmonkeyvibe.game.ai.NPCConversationManager;
import com.jmonkeyvibe.game.ui.DialogUI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Game state for exploration mode - top-down overworld navigation
 */
public class ExplorationState extends BaseAppState implements ActionListener {
    
    private SimpleApplication app;
    private Node worldNode;
    private Player player;
    private WorldGenerator worldGenerator;
    private NPCConversationManager conversationManager;
    private List<NPC> npcs;
    private DialogUI dialogUI;
    
    private boolean moveForward = false;
    private boolean moveBackward = false;
    private boolean moveLeft = false;
    private boolean moveRight = false;
    
    private NPC currentTalkingNPC = null;
    private String lastNPCMessage = "";
    private boolean isTypingCustomResponse = false;
    private StringBuilder customResponseBuffer = new StringBuilder();
    private List<Vector3f> dungeonPortals = new ArrayList<>();
    private com.jme3.font.BitmapText controlsTooltip;
    
    private static final float MOVE_SPEED = 5.0f;
    private static final float INTERACTION_DISTANCE = 3.0f;
    private static final float PORTAL_DISTANCE = 2.0f;

    // Randomization constants
    private static final int MIN_NPCS = 3;
    private static final int MAX_NPCS = 7;
    private static final int MIN_PORTALS = 2;
    private static final int MAX_PORTALS = 4;
    private static final float WORLD_SPAWN_RADIUS = 15.0f;
    private static final float MIN_SPAWN_DISTANCE = 3.0f;

    private Random random;

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        this.worldNode = new Node("World");
        this.worldGenerator = new WorldGenerator(this.app.getAssetManager());
        this.npcs = new ArrayList<>();
        this.conversationManager = new NPCConversationManager();
        this.dialogUI = new DialogUI(this.app);
        this.random = new Random();

        System.out.println("Initializing exploration state...");

        // Generate initial world
        worldGenerator.generateOverworld(worldNode, 20, 20);
        System.out.println("World generated with tiles");

        // Create player
        player = new Player(this.app.getAssetManager());
        player.setPosition(new Vector3f(0, 0, 0));
        worldNode.attachChild(player.getSpatial());
        System.out.println("Player created at (0, 0, 0)");

        // Spawn random NPCs
        spawnRandomNPCs();
        System.out.println("NPCs created: " + npcs.size());

        // Create random dungeon portals
        spawnRandomPortals();
        System.out.println("Dungeon portals created: " + dungeonPortals.size());
        
        // Create controls tooltip UI
        createControlsTooltip();
        
        this.app.getRootNode().attachChild(worldNode);
        System.out.println("World attached to root node. Total children: " + this.app.getRootNode().getChildren().size());
    }

    @Override
    protected void cleanup(Application app) {
        this.app.getRootNode().detachChild(worldNode);
    }

    @Override
    protected void onEnable() {
        setupInput();
    }

    @Override
    protected void onDisable() {
        clearInput();
    }

    @Override
    public void update(float tpf) {
        // Update player movement
        Vector3f moveDirection = new Vector3f();
        
        if (moveForward) {
            moveDirection.addLocal(0, 0, 1);  // W = up (positive Z)
        }
        if (moveBackward) {
            moveDirection.addLocal(0, 0, -1);  // S = down (negative Z)
        }
        if (moveLeft) {
            moveDirection.addLocal(1, 0, 0);  // A = left (positive X)
        }
        if (moveRight) {
            moveDirection.addLocal(-1, 0, 0);  // D = right (negative X)
        }
        
        if (moveDirection.lengthSquared() > 0) {
            moveDirection.normalizeLocal();
            player.move(moveDirection.mult(MOVE_SPEED * tpf));
        }
        
        // Update camera to follow player (keep it high above)
        Vector3f playerPos = player.getPosition();
        app.getCamera().setLocation(new Vector3f(playerPos.x, 100, playerPos.z));
        app.getCamera().lookAt(new Vector3f(playerPos.x, 0, playerPos.z), Vector3f.UNIT_Z);
        
        // Check for nearby dungeon portals
        updatePortalProximity();
    }

    private void setupInput() {
        app.getInputManager().addMapping("MoveForward", new KeyTrigger(KeyInput.KEY_W));
        app.getInputManager().addMapping("MoveBackward", new KeyTrigger(KeyInput.KEY_S));
        app.getInputManager().addMapping("MoveLeft", new KeyTrigger(KeyInput.KEY_A));
        app.getInputManager().addMapping("MoveRight", new KeyTrigger(KeyInput.KEY_D));
        app.getInputManager().addMapping("Interact", new KeyTrigger(KeyInput.KEY_E));
        app.getInputManager().addMapping("Choice1", new KeyTrigger(KeyInput.KEY_1));
        app.getInputManager().addMapping("Choice2", new KeyTrigger(KeyInput.KEY_2));
        app.getInputManager().addMapping("Choice3", new KeyTrigger(KeyInput.KEY_3));
        app.getInputManager().addMapping("TypeCustom", new KeyTrigger(KeyInput.KEY_T));
        app.getInputManager().addMapping("Escape", new KeyTrigger(KeyInput.KEY_ESCAPE));
        app.getInputManager().addMapping("EnterDungeon", new KeyTrigger(KeyInput.KEY_F));
        
        app.getInputManager().addListener(this, 
            "MoveForward", "MoveBackward", "MoveLeft", "MoveRight", "Interact",
            "Choice1", "Choice2", "Choice3", "TypeCustom", "Escape", "EnterDungeon");
        
        // Add raw input listener for text typing
        app.getInputManager().addRawInputListener(new com.jme3.input.RawInputListener() {
            @Override
            public void onKeyEvent(com.jme3.input.event.KeyInputEvent evt) {
                if (isTypingCustomResponse && evt.isPressed()) {
                    handleTextInput(evt);
                }
            }
            @Override public void beginInput() {}
            @Override public void endInput() {}
            @Override public void onMouseMotionEvent(com.jme3.input.event.MouseMotionEvent evt) {}
            @Override public void onMouseButtonEvent(com.jme3.input.event.MouseButtonEvent evt) {}
            @Override public void onJoyAxisEvent(com.jme3.input.event.JoyAxisEvent evt) {}
            @Override public void onJoyButtonEvent(com.jme3.input.event.JoyButtonEvent evt) {}
            @Override public void onTouchEvent(com.jme3.input.event.TouchEvent evt) {}
        });
    }

    private void clearInput() {
        app.getInputManager().deleteMapping("MoveForward");
        app.getInputManager().deleteMapping("MoveBackward");
        app.getInputManager().deleteMapping("MoveLeft");
        app.getInputManager().deleteMapping("MoveRight");
        app.getInputManager().deleteMapping("Interact");
        app.getInputManager().deleteMapping("Choice1");
        app.getInputManager().deleteMapping("Choice2");
        app.getInputManager().deleteMapping("Choice3");
        app.getInputManager().deleteMapping("TypeCustom");
        app.getInputManager().deleteMapping("Escape");
        app.getInputManager().removeListener(this);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        // Don't process movement if typing
        if (isTypingCustomResponse && !name.equals("Escape")) {
            return;
        }
        
        switch (name) {
            case "MoveForward":
                moveForward = isPressed;
                break;
            case "MoveBackward":
                moveBackward = isPressed;
                break;
            case "MoveLeft":
                moveLeft = isPressed;
                break;
            case "MoveRight":
                moveRight = isPressed;
                break;
            case "Interact":
                if (isPressed) {
                    checkNPCInteraction();
                }
                break;
            case "Choice1":
                if (isPressed && dialogUI.isVisible()) {
                    selectDialogChoice(0);
                }
                break;
            case "Choice2":
                if (isPressed && dialogUI.isVisible()) {
                    selectDialogChoice(1);
                }
                break;
            case "Choice3":
                if (isPressed && dialogUI.isVisible()) {
                    selectDialogChoice(2);
                }
                break;
            case "TypeCustom":
                if (isPressed && dialogUI.isVisible()) {
                    startCustomResponse();
                }
                break;
            case "Escape":
                if (isPressed) {
                    if (isTypingCustomResponse) {
                        cancelCustomResponse();
                    } else if (dialogUI.isVisible()) {
                        closeDialog();
                    }
                }
                break;
            case "EnterDungeon":
                if (isPressed) {
                    checkDungeonEntrance();
                }
                break;
        }
    }

    private void checkNPCInteraction() {
        // If already in dialog, close it
        if (dialogUI.isVisible()) {
            closeDialog();
            return;
        }
        
        Vector3f playerPos = player.getPosition();
        NPC closestNPC = null;
        float closestDistance = Float.MAX_VALUE;
        
        // Find the closest NPC within interaction distance
        for (NPC npc : npcs) {
            float distance = playerPos.distance(npc.getPosition());
            if (distance < INTERACTION_DISTANCE && distance < closestDistance) {
                closestDistance = distance;
                closestNPC = npc;
            }
        }
        
        if (closestNPC != null) {
            final NPC npc = closestNPC;
            currentTalkingNPC = npc;
            
            // Generate response options immediately
            List<String> choices = new ArrayList<>();
            choices.add("Tell me more...");
            choices.add("What can you help with?");
            choices.add("I must go now");
            
            // Show dialog UI with loading state
            dialogUI.startStreamingDialog(npc.getName(), choices);
            
            // Start streaming conversation
            conversationManager.startStreamingConversation(
                npc,
                "Hello! I'd like to talk to you.",
                token -> {
                    // Stream each token to the UI
                    app.enqueue(() -> {
                        dialogUI.appendStreamedText(token);
                        return null;
                    });
                },
                () -> {
                    // On complete, generate proper response options
                    app.enqueue(() -> {
                        lastNPCMessage = ""; // Will be in conversation history
                        List<String> properChoices = conversationManager.generateResponseOptions(npc, "greeting");
                        // Update choices
                        dialogUI.show(npc.getName(), dialogUI.getCurrentDialogText(), properChoices);
                        return null;
                    });
                }
            );
            
            System.out.println("\n=== Talking to " + npc.getName() + " ===");
        } else {
            System.out.println("No NPCs nearby. Move closer and press E to interact.");
        }
    }
    
    private void selectDialogChoice(int choiceIndex) {
        dialogUI.selectChoice(choiceIndex);
        String selectedResponse = dialogUI.getSelectedChoice();
        
        if (selectedResponse != null && currentTalkingNPC != null) {
            System.out.println("Player: " + selectedResponse);
            
            // Check if player is saying goodbye
            String lowerResponse = selectedResponse.toLowerCase();
            if (lowerResponse.contains("goodbye") || lowerResponse.contains("bye") || 
                lowerResponse.contains("see you") || lowerResponse.contains("farewell") ||
                lowerResponse.contains("leave") || lowerResponse.contains("must go")) {
                closeDialog();
                return;
            }
            
            // Clear current dialog and show loading
            List<String> loadingChoices = new ArrayList<>();
            loadingChoices.add("...");
            loadingChoices.add("...");
            loadingChoices.add("...");
            dialogUI.startStreamingDialog(currentTalkingNPC.getName(), loadingChoices);
            
            // Start streaming conversation
            conversationManager.startStreamingConversation(
                currentTalkingNPC,
                selectedResponse,
                token -> {
                    app.enqueue(() -> {
                        dialogUI.appendStreamedText(token);
                        return null;
                    });
                },
                () -> {
                    app.enqueue(() -> {
                        String currentText = dialogUI.getCurrentDialogText();
                        List<String> choices = conversationManager.generateResponseOptions(currentTalkingNPC, currentText);
                        dialogUI.show(currentTalkingNPC.getName(), currentText, choices);
                        return null;
                    });
                }
            );
        }
    }
    
    private void startCustomResponse() {
        isTypingCustomResponse = true;
        customResponseBuffer.setLength(0);
        dialogUI.setCustomInputVisible(true);
        dialogUI.updateCustomInputText("");
        dialogUI.showCustomInputPrompt();
        System.out.println("\nType your custom response (press ENTER to send, ESC to cancel):");
    }
    
    private void cancelCustomResponse() {
        isTypingCustomResponse = false;
        customResponseBuffer.setLength(0);
        dialogUI.setCustomInputVisible(false);
        dialogUI.showChoicePrompt();
        System.out.println("Custom input cancelled");
    }
    
    private void handleTextInput(com.jme3.input.event.KeyInputEvent evt) {
        char ch = evt.getKeyChar();
        int keyCode = evt.getKeyCode();
        
        if (keyCode == KeyInput.KEY_RETURN) {
            // Send custom response
            String customResponse = customResponseBuffer.toString().trim();
            if (!customResponse.isEmpty() && currentTalkingNPC != null) {
                System.out.println("Player: " + customResponse);
                
                // Check if player is saying goodbye
                String lowerResponse = customResponse.toLowerCase();
                if (lowerResponse.contains("goodbye") || lowerResponse.contains("bye") || 
                    lowerResponse.contains("see you") || lowerResponse.contains("farewell") ||
                    lowerResponse.contains("leave") || lowerResponse.contains("must go")) {
                    isTypingCustomResponse = false;
                    customResponseBuffer.setLength(0);
                    closeDialog();
                    return;
                }
                
                // Clear input and hide it
                isTypingCustomResponse = false;
                customResponseBuffer.setLength(0);
                dialogUI.setCustomInputVisible(false);
                
                // Start streaming response
                List<String> loadingChoices = new ArrayList<>();
                loadingChoices.add("...");
                loadingChoices.add("...");
                loadingChoices.add("...");
                dialogUI.startStreamingDialog(currentTalkingNPC.getName(), loadingChoices);
                
                conversationManager.startStreamingConversation(
                    currentTalkingNPC,
                    customResponse,
                    token -> {
                        app.enqueue(() -> {
                            dialogUI.appendStreamedText(token);
                            return null;
                        });
                    },
                    () -> {
                        app.enqueue(() -> {
                            String currentText = dialogUI.getCurrentDialogText();
                            List<String> choices = conversationManager.generateResponseOptions(currentTalkingNPC, currentText);
                            dialogUI.show(currentTalkingNPC.getName(), currentText, choices);
                            dialogUI.showChoicePrompt();
                            return null;
                        });
                    }
                );
            } else {
                isTypingCustomResponse = false;
                customResponseBuffer.setLength(0);
                dialogUI.setCustomInputVisible(false);
                dialogUI.showChoicePrompt();
            }
        } else if (keyCode == KeyInput.KEY_BACK && customResponseBuffer.length() > 0) {
            // Backspace
            customResponseBuffer.setLength(customResponseBuffer.length() - 1);
            dialogUI.updateCustomInputText(customResponseBuffer.toString());
        } else if (Character.isLetterOrDigit(ch) || Character.isWhitespace(ch) || "!?.,'\"()-".indexOf(ch) >= 0) {
            // Add character
            customResponseBuffer.append(ch);
            dialogUI.updateCustomInputText(customResponseBuffer.toString());
        }
    }
    
    private void closeDialog() {
        dialogUI.hide();
        currentTalkingNPC = null;
        lastNPCMessage = "";
        isTypingCustomResponse = false;
        customResponseBuffer.setLength(0);
        System.out.println("Dialog closed");
    }

    private void createTestNPC(Vector3f position, String name) {
        NPC npc = new NPC(app.getAssetManager(), name);
        npc.setPosition(position);
        worldNode.attachChild(npc.getSpatial());
        npcs.add(npc);
    }

    /**
     * Spawn a random number of NPCs at random positions in the world
     */
    private void spawnRandomNPCs() {
        int npcCount = MIN_NPCS + random.nextInt(MAX_NPCS - MIN_NPCS + 1);
        List<Vector3f> usedPositions = new ArrayList<>();

        for (int i = 0; i < npcCount; i++) {
            Vector3f position = generateRandomPosition(usedPositions);
            if (position != null) {
                usedPositions.add(position);
                NPC npc = NPC.createRandomNPC(app.getAssetManager());
                npc.setPosition(position);
                worldNode.attachChild(npc.getSpatial());
                npcs.add(npc);
                System.out.println("  Spawned NPC: " + npc.getName() + " (" + npc.getNpcType() + ") at " + position);
            }
        }
    }

    /**
     * Spawn random dungeon portals at random positions
     */
    private void spawnRandomPortals() {
        int portalCount = MIN_PORTALS + random.nextInt(MAX_PORTALS - MIN_PORTALS + 1);
        List<Vector3f> usedPositions = new ArrayList<>();

        // Collect NPC positions to avoid spawning portals on them
        for (NPC npc : npcs) {
            usedPositions.add(npc.getPosition());
        }

        for (int i = 0; i < portalCount; i++) {
            Vector3f position = generateRandomPosition(usedPositions);
            if (position != null) {
                usedPositions.add(position);
                createDungeonPortal(position);
                System.out.println("  Spawned dungeon portal at " + position);
            }
        }
    }

    /**
     * Generate a random position that doesn't overlap with existing positions
     */
    private Vector3f generateRandomPosition(List<Vector3f> usedPositions) {
        int maxAttempts = 50;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // Generate random position within world bounds, avoiding center (player spawn)
            float angle = random.nextFloat() * FastMath.TWO_PI;
            float distance = MIN_SPAWN_DISTANCE + random.nextFloat() * (WORLD_SPAWN_RADIUS - MIN_SPAWN_DISTANCE);
            float x = FastMath.cos(angle) * distance;
            float z = FastMath.sin(angle) * distance;
            Vector3f candidatePos = new Vector3f(x, 0, z);

            // Check if position is far enough from all used positions
            boolean validPosition = true;
            for (Vector3f usedPos : usedPositions) {
                if (candidatePos.distance(usedPos) < MIN_SPAWN_DISTANCE) {
                    validPosition = false;
                    break;
                }
            }

            if (validPosition) {
                return candidatePos;
            }
        }

        return null; // Could not find valid position
    }
    
    private void createDungeonPortal(Vector3f position) {
        // Create visual portal marker
        Quad portalQuad = new Quad(2, 2);
        Geometry portalGeom = new Geometry("DungeonPortal", portalQuad);
        
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0.5f, 0.0f, 0.8f, 0.7f)); // Purple
        mat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        portalGeom.setMaterial(mat);
        portalGeom.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Transparent);
        
        // Center and rotate for top-down view
        portalGeom.setLocalTranslation(-1, 0, -1);
        portalGeom.rotate(-FastMath.HALF_PI, 0, 0);
        
        Node portalNode = new Node("PortalNode");
        portalNode.attachChild(portalGeom);
        portalNode.setLocalTranslation(position);
        worldNode.attachChild(portalNode);
        
        dungeonPortals.add(position);
    }
    
    private void createControlsTooltip() {
        com.jme3.font.BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        controlsTooltip = new com.jme3.font.BitmapText(font);
        controlsTooltip.setSize(font.getCharSet().getRenderedSize() * 0.8f);
        controlsTooltip.setColor(ColorRGBA.White);
        controlsTooltip.setText(
            "EXPLORATION: WASD=Move | E=Talk to NPC | F=Enter Dungeon\n" +
            "DIALOG: 1-3=Choose | T=Custom Response | ESC=Close\n" +
            "COMBAT: WASD=Move | Mouse=Aim+Shoot | ESC=Exit Dungeon"
        );
        controlsTooltip.setLocalTranslation(10, app.getCamera().getHeight() - 10, 0);
        app.getGuiNode().attachChild(controlsTooltip);
    }
    
    private void updatePortalProximity() {
        Vector3f playerPos = player.getPosition();
        for (Vector3f portalPos : dungeonPortals) {
            float distance = playerPos.distance(portalPos);
            if (distance < PORTAL_DISTANCE) {
                // Player is near a portal
                return;
            }
        }
    }
    
    private void checkDungeonEntrance() {
        if (dialogUI.isVisible()) {
            return; // Don't enter dungeon during dialog
        }
        
        Vector3f playerPos = player.getPosition();
        for (Vector3f portalPos : dungeonPortals) {
            float distance = playerPos.distance(portalPos);
            if (distance < PORTAL_DISTANCE) {
                System.out.println("Entering dungeon...");
                ((com.jmonkeyvibe.game.Main) app).enterDungeon();
                return;
            }
        }
        System.out.println("No dungeon portal nearby. Look for purple portals!");
    }
}

