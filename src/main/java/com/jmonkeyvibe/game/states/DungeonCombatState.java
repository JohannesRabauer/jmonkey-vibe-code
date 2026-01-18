package com.jmonkeyvibe.game.states;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jmonkeyvibe.game.entities.Player;
import com.jmonkeyvibe.game.combat.CombatManager;

/**
 * Game state for dungeon combat mode - twin-stick action combat
 */
public class DungeonCombatState extends BaseAppState implements ActionListener {
    
    private SimpleApplication app;
    private Node dungeonNode;
    private Player player;
    private CombatManager combatManager;
    
    private boolean moveForward = false;
    private boolean moveBackward = false;
    private boolean moveLeft = false;
    private boolean moveRight = false;
    private boolean firing = false;
    
    private static final float MOVE_SPEED = 7.0f;

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        this.dungeonNode = new Node("Dungeon");
        this.combatManager = new CombatManager(this.app.getAssetManager());
        
        // Create player for combat
        player = new Player(this.app.getAssetManager());
        player.setPosition(new Vector3f(10, 0, 10));
        dungeonNode.attachChild(player.getSpatial());
        
        // Generate procedural dungeon
        com.jmonkeyvibe.game.world.DungeonGenerator dungeonGen = 
            new com.jmonkeyvibe.game.world.DungeonGenerator(this.app.getAssetManager());
        dungeonGen.generateDungeon(dungeonNode, 40, 40);
        
        // Attach combat manager node
        dungeonNode.attachChild(combatManager.getCombatNode());
        
        // Spawn some enemies
        spawnInitialEnemies();
        
        this.app.getRootNode().attachChild(dungeonNode);
        
        System.out.println("Entered dungeon combat mode!");
    }
    
    private void spawnInitialEnemies() {
        // Spawn enemies at various positions
        combatManager.spawnEnemy(com.jmonkeyvibe.game.entities.Enemy.EnemyType.GOBLIN, 
            new Vector3f(15, 0, 15));
        combatManager.spawnEnemy(com.jmonkeyvibe.game.entities.Enemy.EnemyType.SKELETON, 
            new Vector3f(20, 0, 10));
        combatManager.spawnEnemy(com.jmonkeyvibe.game.entities.Enemy.EnemyType.GOBLIN, 
            new Vector3f(12, 0, 18));
    }

    @Override
    protected void cleanup(Application app) {
        this.app.getRootNode().detachChild(dungeonNode);
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
        // Update player movement (WASD)
        Vector3f moveDirection = new Vector3f();
        
        if (moveForward) {
            moveDirection.addLocal(0, 0, -1);
        }
        if (moveBackward) {
            moveDirection.addLocal(0, 0, 1);
        }
        if (moveLeft) {
            moveDirection.addLocal(-1, 0, 0);
        }
        if (moveRight) {
            moveDirection.addLocal(1, 0, 0);
        }
        
        if (moveDirection.lengthSquared() > 0) {
            moveDirection.normalizeLocal();
            player.move(moveDirection.mult(MOVE_SPEED * tpf));
        }
        
        // Handle mouse aiming and shooting
        if (firing) {
            Vector2f mousePos = app.getInputManager().getCursorPosition();
            Vector3f playerPos = player.getPosition();
            
            // Calculate aim direction from player to mouse cursor (simplified)
            // For now, just shoot in random directions for testing
            Vector3f aimDirection = new Vector3f(
                (float) Math.cos(System.currentTimeMillis() / 1000.0),
                0,
                (float) Math.sin(System.currentTimeMillis() / 1000.0)
            );
            
            combatManager.fireProjectile(playerPos, aimDirection, 10f);
        }
        
        // Update combat manager and enemies
        combatManager.update(tpf);
        combatManager.updateEnemies(tpf, player.getPosition());
        
        // Update camera to follow player
        Vector3f playerPos = player.getPosition();
        app.getCamera().setLocation(new Vector3f(playerPos.x, 50, playerPos.z));
    }

    private void setupInput() {
        app.getInputManager().addMapping("MoveForward", new KeyTrigger(KeyInput.KEY_W));
        app.getInputManager().addMapping("MoveBackward", new KeyTrigger(KeyInput.KEY_S));
        app.getInputManager().addMapping("MoveLeft", new KeyTrigger(KeyInput.KEY_A));
        app.getInputManager().addMapping("MoveRight", new KeyTrigger(KeyInput.KEY_D));
        app.getInputManager().addMapping("Fire", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        app.getInputManager().addMapping("ExitDungeon", new KeyTrigger(KeyInput.KEY_ESCAPE));
        
        app.getInputManager().addListener(this, 
            "MoveForward", "MoveBackward", "MoveLeft", "MoveRight", "Fire", "ExitDungeon");
    }

    private void clearInput() {
        app.getInputManager().deleteMapping("MoveForward");
        app.getInputManager().deleteMapping("MoveBackward");
        app.getInputManager().deleteMapping("MoveLeft");
        app.getInputManager().deleteMapping("MoveRight");
        app.getInputManager().deleteMapping("Fire");
        app.getInputManager().deleteMapping("ExitDungeon");
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
            case "Fire":
                firing = isPressed;
                break;
            case "ExitDungeon":
                if (isPressed) {
                    // TODO: Implement proper dungeon exit
                    System.out.println("Exiting dungeon...");
                }
                break;
        }
    }
}
