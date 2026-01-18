package com.jmonkeyvibe.game;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import com.jmonkeyvibe.game.states.ExplorationState;
import com.jmonkeyvibe.game.states.DungeonCombatState;

/**
 * Main entry point for the JMonkey Vibe Game
 * A 2D top-down RPG with AI-powered NPCs, procedural generation, and dungeon combat
 */
public class Main extends SimpleApplication {
    
    private ExplorationState explorationState;
    private DungeonCombatState dungeonCombatState;

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
        
        // Initialize game states
        explorationState = new ExplorationState();
        dungeonCombatState = new DungeonCombatState();
        
        // Start with exploration mode
        stateManager.attach(explorationState);
        
        System.out.println("JMonkey Vibe Game initialized!");
        System.out.println("AI Provider: " + System.getenv().getOrDefault("AI_PROVIDER", "OLLAMA (default)"));
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
    }

    /**
     * Switch to dungeon combat mode
     */
    public void enterDungeon() {
        stateManager.detach(explorationState);
        stateManager.attach(dungeonCombatState);
    }

    /**
     * Return to exploration mode
     */
    public void exitDungeon() {
        stateManager.detach(dungeonCombatState);
        stateManager.attach(explorationState);
    }
}
