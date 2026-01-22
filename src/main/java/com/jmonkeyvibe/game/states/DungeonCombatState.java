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
import com.jmonkeyvibe.game.audio.AudioManager;
import com.jmonkeyvibe.game.combat.CombatManager;
import com.jmonkeyvibe.game.input.GamepadManager;
import com.jmonkeyvibe.game.ui.HealthBarUI;
import com.jmonkeyvibe.game.ui.GameOverUI;
import com.jmonkeyvibe.game.world.DungeonGenerator;

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
    private HealthBarUI playerHealthBar;
    private GameOverUI gameOverUI;
    private DungeonGenerator dungeonGenerator;
    
    private boolean moveForward = false;
    private boolean moveBackward = false;
    private boolean moveLeft = false;
    private boolean moveRight = false;
    private boolean firing = false;

    // Gamepad support
    private GamepadManager gamepadManager;
    private Vector3f gamepadAimDirection = new Vector3f(0, 0, 1); // Default aim forward

    private Vector3f dungeonExitPosition;
    private static final float MOVE_SPEED = 7.0f;
    private static final float EXIT_DISTANCE = 2.0f;

    // Enemy spawning constants
    private static final int BASE_ENEMIES = 3;                    // Starting number of enemies in wave 1
    private static final int ENEMIES_PER_WAVE_INCREASE = 1;       // Additional enemies per wave
    private static final int MAX_ENEMIES_PER_WAVE = 15;           // Cap on enemies per wave
    private static final float MIN_ENEMY_SPAWN_DISTANCE = 8.0f;   // Minimum distance from player spawn
    private static final float MAX_ENEMY_SPAWN_DISTANCE = 25.0f;  // Maximum distance from player spawn
    private static final float MIN_ENEMY_SEPARATION = 3.0f;       // Minimum distance between enemies

    // Wave system
    private int currentWave = 0;
    private float waveTransitionDelay = 0f;
    private static final float WAVE_TRANSITION_TIME = 5.0f;       // Seconds between waves
    private boolean waveInProgress = false;
    private boolean gameOver = false;

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
        
        // Generate procedural dungeon and store for collision detection
        dungeonGenerator = new DungeonGenerator(this.app.getAssetManager());
        dungeonGenerator.generateDungeon(dungeonNode, 40, 40);
        
        // Attach combat manager node
        dungeonNode.attachChild(combatManager.getCombatNode());

        // Create player health bar UI
        playerHealthBar = new HealthBarUI(this.app);
        playerHealthBar.update(player.getHealth(), player.getMaxHealth());
        playerHealthBar.show();

        // Create game over UI
        gameOverUI = new GameOverUI(this.app);
        gameOverUI.setListener(new GameOverUI.GameOverListener() {
            @Override
            public void onTryAgain() {
                // Restart the dungeon
                ((com.jmonkeyvibe.game.Main) DungeonCombatState.this.app).restartDungeon();
            }

            @Override
            public void onExitToOverworld() {
                // Exit to overworld
                ((com.jmonkeyvibe.game.Main) DungeonCombatState.this.app).exitDungeon();
            }
        });

        // Start the first wave
        startNextWave();
        
        // Create dungeon exit portal
        createDungeonExit();
        
        this.app.getRootNode().attachChild(dungeonNode);

        System.out.println("Entered dungeon combat mode!");
        System.out.println("Survive the infinite waves!");
    }

    /**
     * Start the next wave of enemies
     */
    private void startNextWave() {
        currentWave++;
        waveInProgress = true;

        // Calculate number of enemies for this wave
        int enemyCount = Math.min(
            BASE_ENEMIES + (currentWave - 1) * ENEMIES_PER_WAVE_INCREASE,
            MAX_ENEMIES_PER_WAVE
        );

        System.out.println("========================================");
        System.out.println("         WAVE " + currentWave + " STARTING!");
        System.out.println("         Enemies: " + enemyCount);
        System.out.println("========================================");

        // Play wave start sound
        AudioManager.getInstance().playSound(AudioManager.SOUND_WAVE_START);

        spawnWaveEnemies(enemyCount);
    }

    /**
     * Spawn enemies for the current wave
     */
    private void spawnWaveEnemies(int enemyCount) {
        List<Vector3f> usedPositions = new ArrayList<>();
        Vector3f playerPos = player.getPosition();

        for (int i = 0; i < enemyCount; i++) {
            Vector3f position = generateRandomEnemyPosition(playerPos, usedPositions);
            if (position != null) {
                usedPositions.add(position);
                Enemy.EnemyType type = getRandomEnemyTypeForWave(currentWave);
                combatManager.spawnEnemy(type, position);
            }
        }
    }

    /**
     * Check if all enemies in the current wave are defeated
     * and start the next wave after a short delay
     */
    private void checkWaveCompletion(float tpf) {
        // Handle wave transition delay (must be checked BEFORE early return)
        if (!waveInProgress && waveTransitionDelay > 0) {
            waveTransitionDelay -= tpf;
            if (waveTransitionDelay <= 0) {
                startNextWave();
            }
            return;
        }

        if (!waveInProgress) {
            return;
        }

        // Check if all enemies are defeated
        if (combatManager.getEnemies().isEmpty()) {
            waveInProgress = false;
            waveTransitionDelay = WAVE_TRANSITION_TIME;
            System.out.println("Wave " + currentWave + " complete! Prepare for the next wave...");
        }
    }

    /**
     * Get a random enemy type based on the current wave.
     * Higher waves have higher chances of spawning tougher enemies.
     */
    private Enemy.EnemyType getRandomEnemyTypeForWave(int wave) {
        float roll = random.nextFloat();

        // Adjust probabilities based on wave number
        // Wave 1-2: Mostly goblins and skeletons
        // Wave 3-5: Start seeing more orcs
        // Wave 6+: Demons become more common

        float goblinChance;
        float skeletonChance;
        float orcChance;
        // Demon is the remainder

        if (wave <= 2) {
            goblinChance = 0.50f;
            skeletonChance = 0.35f;
            orcChance = 0.15f;
        } else if (wave <= 5) {
            goblinChance = 0.35f;
            skeletonChance = 0.30f;
            orcChance = 0.25f;
        } else if (wave <= 10) {
            goblinChance = 0.25f;
            skeletonChance = 0.25f;
            orcChance = 0.30f;
        } else {
            // Wave 11+: Tough enemies dominate
            goblinChance = 0.15f;
            skeletonChance = 0.20f;
            orcChance = 0.35f;
        }

        if (roll < goblinChance) {
            return Enemy.EnemyType.GOBLIN;
        } else if (roll < goblinChance + skeletonChance) {
            return Enemy.EnemyType.SKELETON;
        } else if (roll < goblinChance + skeletonChance + orcChance) {
            return Enemy.EnemyType.ORC;
        } else {
            return Enemy.EnemyType.DEMON;
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
        if (playerHealthBar != null) {
            playerHealthBar.cleanup();
        }
        if (gameOverUI != null) {
            gameOverUI.cleanup();
        }
    }

    @Override
    protected void onEnable() {
        setupInput();
        // Combat music is started by Main.enterDungeon()
    }

    @Override
    protected void onDisable() {
        clearInput();
    }

    @Override
    public void update(float tpf) {
        // If game over screen is showing, only update the UI
        if (gameOver) {
            if (gameOverUI != null) {
                gameOverUI.update();
            }
            return;
        }

        // Handle gamepad input
        handleGamepadInput(tpf);

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

        // Add gamepad left stick movement
        if (gamepadManager != null) {
            Vector2f leftStick = gamepadManager.getLeftStick();
            if (leftStick.lengthSquared() > 0) {
                // Left stick: X is left-right, Y is up-down
                // In our coordinate system: positive X is left, positive Z is forward
                moveDirection.addLocal(-leftStick.x, 0, leftStick.y);
            }
        }

        if (moveDirection.lengthSquared() > 0) {
            moveDirection.normalizeLocal();
            Vector3f movement = moveDirection.mult(MOVE_SPEED * tpf);
            movePlayerWithCollision(movement);
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

        // Update player health bar
        playerHealthBar.update(player.getHealth(), player.getMaxHealth());

        // Check for player death
        if (!player.isAlive()) {
            handlePlayerDeath();
            return;
        }

        // Check if wave is complete and start next wave
        checkWaveCompletion(tpf);

        // Check if player is near exit
        checkExitProximity();

        // Update camera to follow player
        Vector3f playerPos = player.getPosition();
        app.getCamera().setLocation(new Vector3f(playerPos.x, 50, playerPos.z));
    }

    /**
     * Move the player with collision detection.
     * Checks X and Z axes separately to allow sliding along walls.
     */
    private void movePlayerWithCollision(Vector3f movement) {
        Vector3f currentPos = player.getPosition();

        // Try moving in X direction first
        float newX = currentPos.x + movement.x;
        if (dungeonGenerator.isWalkable(newX, currentPos.z)) {
            currentPos.x = newX;
        }

        // Then try moving in Z direction
        float newZ = currentPos.z + movement.z;
        if (dungeonGenerator.isWalkable(currentPos.x, newZ)) {
            currentPos.z = newZ;
        }

        // Update player position
        player.setPosition(currentPos);
    }

    /**
     * Handle gamepad input for combat mode
     */
    private void handleGamepadInput(float tpf) {
        if (gamepadManager == null) {
            return;
        }

        // Right analog stick for aiming
        Vector2f rightStick = gamepadManager.getRightStick();
        if (rightStick.lengthSquared() > 0) {
            // Convert right stick to aim direction
            // In our coordinate system: positive X is left, positive Z is forward
            gamepadAimDirection = new Vector3f(-rightStick.x, 0, rightStick.y).normalizeLocal();
        }

        // Right trigger or A button for firing with gamepad
        boolean gamepadFiring = gamepadManager.isRightTriggerPressed() || gamepadManager.isAButtonPressed();
        if (gamepadFiring && rightStick.lengthSquared() > 0) {
            // Fire in the direction of the right stick
            Vector3f playerPos = player.getPosition();
            combatManager.fireProjectile(playerPos, gamepadAimDirection, 10f);
        }

        // Start button for exiting dungeon (ESC equivalent)
        if (gamepadManager.isStartButtonJustPressed()) {
            System.out.println("Exiting dungeon via gamepad...");
            ((com.jmonkeyvibe.game.Main) app).exitDungeon();
        }

        // B button also exits dungeon
        if (gamepadManager.isBButtonJustPressed()) {
            System.out.println("Exiting dungeon via gamepad...");
            ((com.jmonkeyvibe.game.Main) app).exitDungeon();
        }
    }

    /**
     * Set the gamepad manager for controller support
     */
    public void setGamepadManager(GamepadManager gamepadManager) {
        this.gamepadManager = gamepadManager;
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
        if (gameOver) {
            return; // Already showing game over
        }

        gameOver = true;

        System.out.println("========================================");
        System.out.println("           GAME OVER!");
        System.out.println("     You reached Wave " + currentWave);
        System.out.println("========================================");

        // Clear movement input so player stops
        moveForward = false;
        moveBackward = false;
        moveLeft = false;
        moveRight = false;
        firing = false;

        // Show game over screen
        if (gameOverUI != null) {
            gameOverUI.setGamepadManager(gamepadManager);
            gameOverUI.show(currentWave);
        }
    }
}
