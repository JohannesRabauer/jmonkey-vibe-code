package com.jmonkeyvibe.game;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import com.jmonkeyvibe.game.audio.AudioManager;
import com.jmonkeyvibe.game.input.GamepadManager;
import com.jmonkeyvibe.game.states.ExplorationState;
import com.jmonkeyvibe.game.states.DungeonCombatState;
import com.jmonkeyvibe.game.states.IntroStoryState;

/**
 * Main entry point for the JMonkey Vibe Game
 * A 2D top-down RPG with AI-powered NPCs, procedural generation, and dungeon combat
 */
public class Main extends SimpleApplication {
    
    private IntroStoryState introStoryState;
    private ExplorationState explorationState;
    private DungeonCombatState dungeonCombatState;
    private GamepadManager gamepadManager;
    private AudioManager audioManager;

    public static void main(String[] args) {
        Main app = new Main();
        
        // Configure application settings
        AppSettings settings = new AppSettings(true);
        settings.setTitle("JMonkey Vibe - AI RPG Adventure");
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setVSync(true);
        settings.setFrameRate(60);
        settings.setSamples(4); // Anti-aliasing
        settings.setUseJoysticks(true); // Enable gamepad/controller support
        
        app.setSettings(settings);
        app.setShowSettings(false); // Skip settings dialog
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Disable the default fly camera for 2D gameplay
        flyCam.setEnabled(false);

        // Disable default ESC key mapping (which closes the app)
        inputManager.deleteMapping("SIMPLEAPP_Exit");

        // Set up orthographic camera for 2D top-down view
        setupOrthographicCamera();

        // Initialize gamepad/controller support
        gamepadManager = new GamepadManager();
        gamepadManager.initialize(inputManager);

        // Set up audio listener for jMonkeyEngine audio system
        // The listener must be positioned where sounds should be "heard" from
        listener.setLocation(cam.getLocation());
        listener.setRotation(cam.getRotation());

        // Initialize audio manager with rootNode so AudioNodes are in the scene graph
        // CRITICAL: AudioNodes must be attached to the scene graph for jME audio to work!
        audioManager = AudioManager.getInstance();
        audioManager.initialize(assetManager, rootNode);

        // Initialize game states
        introStoryState = new IntroStoryState();
        explorationState = new ExplorationState();
        dungeonCombatState = new DungeonCombatState();

        // Pass gamepad manager to states for controller support
        introStoryState.setGamepadManager(gamepadManager);
        explorationState.setGamepadManager(gamepadManager);
        dungeonCombatState.setGamepadManager(gamepadManager);

        // Start with intro story screen
        stateManager.attach(introStoryState);

        System.out.println("JMonkey Vibe Game initialized!");
        System.out.println("AI Provider: " + System.getenv().getOrDefault("AI_PROVIDER", "OLLAMA (default)"));
        System.out.println("Gamepad support enabled - connect a controller to use it!");
        System.out.println("Audio system initialized - add audio files to src/main/resources/Sounds/");
    }

    /**
     * Configure orthographic camera for 2D top-down rendering
     */
    private void setupOrthographicCamera() {
        cam.setParallelProjection(true);
        
        // Define frustum for 2D rendering (units in world space)
        float aspect = (float) cam.getWidth() / cam.getHeight();
        float viewHeight = 15f; // View height in world units
        float viewWidth = viewHeight * aspect;
        
        cam.setFrustum(
            -1000f,  // near (negative for parallel projection)
            1000f,   // far
            -viewWidth / 2,   // left
            viewWidth / 2,    // right
            viewHeight / 2,   // top
            -viewHeight / 2   // bottom
        );
        
        // Position camera high above looking straight down
        cam.setLocation(new Vector3f(0, 100, 0));
        cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Z);
        
        System.out.println("Camera setup complete - Looking down at world from (0, 100, 0)");
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Game loop updates handled by AppStates

        // Update audio listener position to match camera
        // This ensures sounds are "heard" from the camera's position
        listener.setLocation(cam.getLocation());
        listener.setRotation(cam.getRotation());
    }

    /**
     * Start the exploration mode (called from intro story)
     */
    public void startExploration() {
        stateManager.attach(explorationState);
        // Start exploration music
        audioManager.playExplorationMusic();
    }

    /**
     * Switch to dungeon combat mode
     */
    public void enterDungeon() {
        stateManager.detach(explorationState);
        stateManager.attach(dungeonCombatState);
        // Switch to combat music
        audioManager.playCombatMusic();
    }

    /**
     * Return to exploration mode
     */
    public void exitDungeon() {
        stateManager.detach(dungeonCombatState);
        stateManager.attach(explorationState);
        // Switch back to exploration music
        audioManager.playExplorationMusic();
    }

    /**
     * Restart the current dungeon (reset and re-enter)
     */
    public void restartDungeon() {
        stateManager.detach(dungeonCombatState);
        // Create a fresh dungeon state
        dungeonCombatState = new DungeonCombatState();
        dungeonCombatState.setGamepadManager(gamepadManager);
        stateManager.attach(dungeonCombatState);
        // Keep combat music playing
        audioManager.playCombatMusic();
    }

    @Override
    public void destroy() {
        // Clean up audio resources
        if (audioManager != null) {
            audioManager.cleanup();
        }
        super.destroy();
    }
}
