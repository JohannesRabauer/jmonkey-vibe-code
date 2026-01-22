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
import com.jmonkeyvibe.game.entities.Enemy;
import com.jmonkeyvibe.game.combat.CombatManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    // Enemy spawning constants
    private static final int MIN_ENEMIES = 3;
    private static final int MAX_ENEMIES = 8;
    private static final float MIN_ENEMY_SPAWN_DISTANCE = 8.0f;  // Minimum distance from player spawn
    private static final float MAX_ENEMY_SPAWN_DISTANCE = 25.0f; // Maximum distance from player spawn
    private static final float MIN_ENEMY_SEPARATION = 3.0f;      // Minimum distance between enemies

    private Random random;

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        this.dungeonNode = new Node("Dungeon");
        this.combatManager = new CombatManager(this.app.getAssetManager());
        this.random = new Random();

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
        // Randomly determine number of enemies
        int enemyCount = MIN_ENEMIES + random.nextInt(MAX_ENEMIES - MIN_ENEMIES + 1);
        List<Vector3f> usedPositions = new ArrayList<>();
        Vector3f playerSpawn = new Vector3f(10, 0, 10);

        System.out.println("Spawning " + enemyCount + " random enemies...");

        for (int i = 0; i < enemyCount; i++) {
            Vector3f position = generateRandomEnemyPosition(playerSpawn, usedPositions);
            if (position != null) {
                usedPositions.add(position);
                Enemy.EnemyType type = getRandomEnemyType();
                combatManager.spawnEnemy(type, position);
                System.out.println("  Spawned " + type + " at " + position);
            }
        }
    }

    /**
     * Get a random enemy type with weighted probability
     * Common enemies (Goblin, Skeleton) appear more often than rare ones (Orc, Demon)
     */
    private Enemy.EnemyType getRandomEnemyType() {
        float roll = random.nextFloat();
        if (roll < 0.4f) {
            return Enemy.EnemyType.GOBLIN;     // 40% chance
        } else if (roll < 0.7f) {
            return Enemy.EnemyType.SKELETON;   // 30% chance
        } else if (roll < 0.9f) {
            return Enemy.EnemyType.ORC;        // 20% chance
        } else {
            return Enemy.EnemyType.DEMON;      // 10% chance
        }
    }

    /**
     * Generate a random position for an enemy that doesn't overlap with existing positions
     * and maintains minimum distance from player spawn
     */
    private Vector3f generateRandomEnemyPosition(Vector3f playerSpawn, List<Vector3f> usedPositions) {
        int maxAttempts = 50;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // Generate random position within dungeon bounds
            float angle = random.nextFloat() * com.jme3.math.FastMath.TWO_PI;
            float distance = MIN_ENEMY_SPAWN_DISTANCE + random.nextFloat() * (MAX_ENEMY_SPAWN_DISTANCE - MIN_ENEMY_SPAWN_DISTANCE);

            float x = playerSpawn.x + com.jme3.math.FastMath.cos(angle) * distance;
            float z = playerSpawn.z + com.jme3.math.FastMath.sin(angle) * distance;

            // Keep within reasonable dungeon bounds (0-40 based on dungeon size)
            x = Math.max(2, Math.min(38, x));
            z = Math.max(2, Math.min(38, z));

            Vector3f candidatePos = new Vector3f(x, 0, z);

            // Check if position is far enough from all used positions
            boolean validPosition = true;
            for (Vector3f usedPos : usedPositions) {
                if (candidatePos.distance(usedPos) < MIN_ENEMY_SEPARATION) {
                    validPosition = false;
                    break;
                }
            }

            // Also ensure minimum distance from player spawn
            if (validPosition && candidatePos.distance(playerSpawn) < MIN_ENEMY_SPAWN_DISTANCE) {
                validPosition = false;
            }

            if (validPosition) {
                return candidatePos;
            }
        }

        return null; // Could not find valid position
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

        // Process enemy attacks on player
        combatManager.processEnemyAttacks(player);

        // Check for player death
        if (!player.isAlive()) {
            handlePlayerDeath();
            return;
        }

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

    private void handlePlayerDeath() {
        System.out.println("Player died! Game Over!");
        // Exit dungeon and return to main game
        ((com.jmonkeyvibe.game.Main) app).exitDungeon();
    }
}
