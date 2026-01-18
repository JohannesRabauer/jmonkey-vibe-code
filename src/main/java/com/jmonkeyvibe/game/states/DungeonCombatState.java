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
    
    private Vector3f dungeonExitPosition;
    private static final float MOVE_SPEED = 7.0f;
    private static final float EXIT_DISTANCE = 2.0f;

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
        
        // Create dungeon exit portal
        createDungeonExit();
        
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
            moveDirection.addLocal(0, 0, 1);
        }
        if (moveBackward) {
            moveDirection.addLocal(0, 0, -1);
        }
        if (moveLeft) {
            moveDirection.addLocal(1, 0, 0);
        }
        if (moveRight) {
            moveDirection.addLocal(-1, 0, 0);
        }
        
        if (moveDirection.lengthSquared() > 0) {
            moveDirection.normalizeLocal();
            player.move(moveDirection.mult(MOVE_SPEED * tpf));
        }
        
        // Handle mouse aiming and shooting
        if (firing) {
            // Get mouse position in screen coordinates
            Vector2f mousePos = app.getInputManager().getCursorPosition();
            Vector3f playerPos = player.getPosition();
            
            // Calculate world position of mouse cursor
            // Screen center corresponds to player position
            float screenCenterX = app.getCamera().getWidth() / 2f;
            float screenCenterY = app.getCamera().getHeight() / 2f;
            
            // Calculate offset from screen center
            float offsetX = mousePos.x - screenCenterX;
            float offsetY = mousePos.y - screenCenterY;
            
            // Convert to world coordinates (adjust scale based on camera frustum)
            float worldScale = 15f / app.getCamera().getHeight(); // Based on viewHeight
            Vector3f aimDirection = new Vector3f(
                -offsetX * worldScale,
                0,
                offsetY * worldScale
            ).normalizeLocal();
            
            combatManager.fireProjectile(playerPos, aimDirection, 10f);
        }
        
        // Update combat manager and enemies
        combatManager.update(tpf);
        combatManager.updateEnemies(tpf, player.getPosition());
        
        // Check if player is near exit
        checkExitProximity();
        
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
                    System.out.println("Exiting dungeon...");
                    ((com.jmonkeyvibe.game.Main) app).exitDungeon();
                }
                break;
        }
    }
    
    private void createDungeonExit() {
        // Create exit portal at dungeon entrance
        dungeonExitPosition = new Vector3f(10, 0, 10);
        
        com.jme3.scene.shape.Quad exitQuad = new com.jme3.scene.shape.Quad(2, 2);
        com.jme3.scene.Geometry exitGeom = new com.jme3.scene.Geometry("DungeonExit", exitQuad);
        
        com.jme3.material.Material mat = new com.jme3.material.Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new com.jme3.math.ColorRGBA(0.0f, 0.8f, 0.0f, 0.7f)); // Green exit portal
        mat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        exitGeom.setMaterial(mat);
        exitGeom.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Transparent);
        
        // Center and rotate for top-down view
        exitGeom.setLocalTranslation(-1, 0, -1);
        exitGeom.rotate(-com.jme3.math.FastMath.HALF_PI, 0, 0);
        
        com.jme3.scene.Node exitNode = new com.jme3.scene.Node("ExitPortalNode");
        exitNode.attachChild(exitGeom);
        exitNode.setLocalTranslation(dungeonExitPosition);
        dungeonNode.attachChild(exitNode);
        
        System.out.println("Dungeon exit created at " + dungeonExitPosition);
    }
    
    private void checkExitProximity() {
        if (dungeonExitPosition != null) {
            float distance = player.getPosition().distance(dungeonExitPosition);
            if (distance < EXIT_DISTANCE) {
                // Player is near exit - could add visual feedback
            }
        }
    }
}
