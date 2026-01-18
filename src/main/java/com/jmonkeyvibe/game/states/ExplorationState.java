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

/**
 * Game state for exploration mode - top-down overworld navigation
 */
public class ExplorationState extends BaseAppState implements ActionListener {
    
    private SimpleApplication app;
    private Node worldNode;
    private Player player;
    private WorldGenerator worldGenerator;
    
    private boolean moveForward = false;
    private boolean moveBackward = false;
    private boolean moveLeft = false;
    private boolean moveRight = false;
    
    private static final float MOVE_SPEED = 5.0f;

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        this.worldNode = new Node("World");
        this.worldGenerator = new WorldGenerator(this.app.getAssetManager());
        
        System.out.println("Initializing exploration state...");
        
        // Generate initial world
        worldGenerator.generateOverworld(worldNode, 20, 20);
        System.out.println("World generated with tiles");
        
        // Create player
        player = new Player(this.app.getAssetManager());
        player.setPosition(new Vector3f(0, 0, 0));
        worldNode.attachChild(player.getSpatial());
        System.out.println("Player created at (0, 0, 0)");
        
        // Add some test NPCs
        createTestNPC(new Vector3f(5, 0, -5), "Village Elder");
        createTestNPC(new Vector3f(-5, 0, -5), "Mysterious Stranger");
        System.out.println("NPCs created");
        
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
    }

    private void setupInput() {
        app.getInputManager().addMapping("MoveForward", new KeyTrigger(KeyInput.KEY_W));
        app.getInputManager().addMapping("MoveBackward", new KeyTrigger(KeyInput.KEY_S));
        app.getInputManager().addMapping("MoveLeft", new KeyTrigger(KeyInput.KEY_A));
        app.getInputManager().addMapping("MoveRight", new KeyTrigger(KeyInput.KEY_D));
        app.getInputManager().addMapping("Interact", new KeyTrigger(KeyInput.KEY_E));
        
        app.getInputManager().addListener(this, 
            "MoveForward", "MoveBackward", "MoveLeft", "MoveRight", "Interact");
    }

    private void clearInput() {
        app.getInputManager().deleteMapping("MoveForward");
        app.getInputManager().deleteMapping("MoveBackward");
        app.getInputManager().deleteMapping("MoveLeft");
        app.getInputManager().deleteMapping("MoveRight");
        app.getInputManager().deleteMapping("Interact");
        app.getInputManager().removeListener(this);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
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
        }
    }

    private void checkNPCInteraction() {
        // TODO: Implement NPC interaction detection and dialogue triggering
        System.out.println("Checking for nearby NPCs to interact with...");
    }

    private void createTestNPC(Vector3f position, String name) {
        NPC npc = new NPC(app.getAssetManager(), name);
        npc.setPosition(position);
        worldNode.attachChild(npc.getSpatial());
    }
}
