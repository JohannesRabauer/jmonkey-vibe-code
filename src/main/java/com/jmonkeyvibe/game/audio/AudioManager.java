package com.jmonkeyvibe.game.audio;

import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.scene.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages all audio in the game including background music and sound effects.
 * Uses jMonkeyEngine's AudioNode for audio playback.
 *
 * This manager gracefully handles missing audio files by logging warnings
 * and continuing without crashing the game.
 *
 * IMPORTANT: AudioNodes must be attached to a node in the scene graph for
 * jMonkeyEngine's audio renderer to process them. This manager attaches
 * all audio nodes to a dedicated audioNode which must be attached to rootNode.
 *
 * Required audio files (place in src/main/resources/Sounds/):
 * - Music/exploration.ogg - Ambient background music for exploration mode
 * - Music/combat.ogg - Intense background music for dungeon combat
 * - Effects/player_shoot.ogg - Sound when player fires a projectile
 * - Effects/enemy_hit.ogg - Sound when enemy takes damage
 * - Effects/enemy_death.ogg - Sound when enemy dies
 * - Effects/player_hit.ogg - Sound when player takes damage
 * - Effects/wave_start.ogg - Sound when a new wave begins
 */
public class AudioManager {

    private static final Logger logger = Logger.getLogger(AudioManager.class.getName());

    private AssetManager assetManager;
    private Node audioNode; // Node to hold all audio nodes in the scene graph
    private Map<String, AudioNode> loadedSounds;

    // Background music nodes
    private AudioNode explorationMusic;
    private AudioNode combatMusic;
    private AudioNode currentMusic;

    // Volume settings
    private float musicVolume = 0.5f;
    private float effectsVolume = 0.8f;
    private boolean musicEnabled = true;
    private boolean effectsEnabled = true;

    // Sound effect keys
    public static final String SOUND_PLAYER_SHOOT = "player_shoot";
    public static final String SOUND_ENEMY_HIT = "enemy_hit";
    public static final String SOUND_ENEMY_DEATH = "enemy_death";
    public static final String SOUND_PLAYER_HIT = "player_hit";
    public static final String SOUND_WAVE_START = "wave_start";

    // Music keys
    public static final String MUSIC_EXPLORATION = "exploration";
    public static final String MUSIC_COMBAT = "combat";

    // Singleton instance
    private static AudioManager instance;

    /**
     * Get the singleton instance of AudioManager.
     * Must call initialize() before using.
     */
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    private AudioManager() {
        this.loadedSounds = new HashMap<>();
        this.audioNode = new Node("AudioManagerNode");
    }

    /**
     * Initialize the AudioManager with the asset manager and root node.
     * This must be called before any audio can be played.
     *
     * @param assetManager The asset manager to load audio files
     * @param rootNode The root node to attach audio nodes to (required for audio to play!)
     */
    public void initialize(AssetManager assetManager, Node rootNode) {
        System.out.println("[AudioManager] initialize() called");
        this.assetManager = assetManager;

        // CRITICAL: Attach our audio node to the scene graph
        // Without this, AudioNodes won't be processed by jMonkeyEngine's audio renderer
        if (rootNode != null) {
            rootNode.attachChild(audioNode);
            System.out.println("[AudioManager] Audio node attached to root node");
        } else {
            System.out.println("[AudioManager] WARNING: rootNode is null! Audio may not play!");
        }

        loadAllAudio();
        System.out.println("[AudioManager] Initialization complete");
    }

    /**
     * @deprecated Use initialize(AssetManager, Node) instead
     */
    @Deprecated
    public void initialize(AssetManager assetManager) {
        System.out.println("[AudioManager] WARNING: Using deprecated initialize() without rootNode!");
        System.out.println("[AudioManager] Audio may not play! Call initialize(assetManager, rootNode) instead.");
        this.assetManager = assetManager;
        loadAllAudio();
        System.out.println("[AudioManager] Initialization complete (but audio nodes not attached to scene graph!)");
    }

    /**
     * Load all audio files. Missing files are logged as warnings but don't crash.
     */
    private void loadAllAudio() {
        System.out.println("[AudioManager] Loading audio files...");

        // Load background music
        System.out.println("[AudioManager] Loading exploration music...");
        explorationMusic = loadMusic("Sounds/Music/exploration.ogg", true);

        System.out.println("[AudioManager] Loading combat music...");
        combatMusic = loadMusic("Sounds/Music/combat.ogg", true);

        // Load sound effects
        System.out.println("[AudioManager] Loading sound effects...");
        loadSoundEffect(SOUND_PLAYER_SHOOT, "Sounds/Effects/player_shoot.ogg");
        loadSoundEffect(SOUND_ENEMY_HIT, "Sounds/Effects/enemy_hit.ogg");
        loadSoundEffect(SOUND_ENEMY_DEATH, "Sounds/Effects/enemy_death.ogg");
        loadSoundEffect(SOUND_PLAYER_HIT, "Sounds/Effects/player_hit.ogg");
        loadSoundEffect(SOUND_WAVE_START, "Sounds/Effects/wave_start.ogg");

        System.out.println("[AudioManager] All audio files loaded");
    }

    /**
     * Load a music track. Returns null if the file doesn't exist.
     * The AudioNode is attached to the audioNode so it's in the scene graph.
     */
    private AudioNode loadMusic(String path, boolean loop) {
        try {
            System.out.println("[AudioManager] Attempting to load music: " + path);
            AudioNode music = new AudioNode(assetManager, path, AudioData.DataType.Stream);
            music.setLooping(loop);
            music.setPositional(false); // Non-positional (background music)
            music.setVolume(musicVolume);

            // CRITICAL: Attach to scene graph for jMonkeyEngine audio renderer
            audioNode.attachChild(music);
            System.out.println("[AudioManager] SUCCESS: Loaded and attached music: " + path);
            System.out.println("[AudioManager]   - Volume: " + musicVolume);
            System.out.println("[AudioManager]   - Looping: " + loop);
            System.out.println("[AudioManager]   - Positional: false");
            return music;
        } catch (AssetNotFoundException e) {
            System.out.println("[AudioManager] ERROR: Music file not found: " + path);
            logger.log(Level.WARNING, "Music file not found: " + path + " - Music will be silent");
            return null;
        } catch (Exception e) {
            System.out.println("[AudioManager] ERROR: Failed to load music: " + path + " - " + e.getMessage());
            e.printStackTrace();
            logger.log(Level.WARNING, "Failed to load music: " + path + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Load a sound effect and store it by key.
     * The AudioNode is attached to the audioNode so it's in the scene graph.
     */
    private void loadSoundEffect(String key, String path) {
        try {
            System.out.println("[AudioManager] Attempting to load sound effect: " + key + " from " + path);
            AudioNode sound = new AudioNode(assetManager, path, AudioData.DataType.Buffer);
            sound.setLooping(false);
            sound.setPositional(false); // Non-positional for now
            sound.setVolume(effectsVolume);

            // CRITICAL: Attach to scene graph for jMonkeyEngine audio renderer
            audioNode.attachChild(sound);

            loadedSounds.put(key, sound);
            System.out.println("[AudioManager] SUCCESS: Loaded and attached sound effect: " + key);
        } catch (AssetNotFoundException e) {
            System.out.println("[AudioManager] ERROR: Sound effect not found: " + path);
            logger.log(Level.WARNING, "Sound effect not found: " + path + " - Effect will be silent");
            loadedSounds.put(key, null); // Store null to mark as attempted
        } catch (Exception e) {
            System.out.println("[AudioManager] ERROR: Failed to load sound effect: " + path + " - " + e.getMessage());
            e.printStackTrace();
            logger.log(Level.WARNING, "Failed to load sound effect: " + path + " - " + e.getMessage());
            loadedSounds.put(key, null);
        }
    }

    /**
     * Play background music for exploration mode.
     */
    public void playExplorationMusic() {
        System.out.println("[AudioManager] playExplorationMusic() called");
        switchMusic(explorationMusic);
    }

    /**
     * Play background music for combat mode.
     */
    public void playCombatMusic() {
        System.out.println("[AudioManager] playCombatMusic() called");
        switchMusic(combatMusic);
    }

    /**
     * Switch to a different music track, fading out the current one.
     */
    private void switchMusic(AudioNode newMusic) {
        System.out.println("[AudioManager] switchMusic() called");
        System.out.println("[AudioManager]   - musicEnabled: " + musicEnabled);
        System.out.println("[AudioManager]   - newMusic is null: " + (newMusic == null));

        if (!musicEnabled) {
            System.out.println("[AudioManager] Music is disabled, not playing");
            return;
        }

        // Stop current music
        if (currentMusic != null) {
            AudioSource.Status status = currentMusic.getStatus();
            System.out.println("[AudioManager]   - Current music status: " + status);
            if (status == AudioSource.Status.Playing) {
                currentMusic.stop();
                System.out.println("[AudioManager] Stopped current music");
            }
        }

        // Start new music
        if (newMusic != null) {
            newMusic.setVolume(musicVolume);
            System.out.println("[AudioManager] Starting music with volume: " + musicVolume);
            System.out.println("[AudioManager]   - AudioNode positional: " + newMusic.isPositional());
            System.out.println("[AudioManager]   - AudioNode looping: " + newMusic.isLooping());

            newMusic.play();
            currentMusic = newMusic;

            // Verify it started
            AudioSource.Status statusAfter = newMusic.getStatus();
            System.out.println("[AudioManager] Music started! Status after play(): " + statusAfter);
            System.out.println("[AudioManager] Now playing: " + (newMusic == explorationMusic ? "exploration" : "combat") + " music");
        } else {
            System.out.println("[AudioManager] WARNING: newMusic is null, nothing to play!");
        }
    }

    /**
     * Stop all music.
     */
    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
    }

    /**
     * Pause current music.
     */
    public void pauseMusic() {
        if (currentMusic != null && currentMusic.getStatus() == AudioSource.Status.Playing) {
            currentMusic.pause();
        }
    }

    /**
     * Resume paused music.
     */
    public void resumeMusic() {
        if (currentMusic != null && currentMusic.getStatus() == AudioSource.Status.Paused) {
            currentMusic.play();
        }
    }

    /**
     * Play a sound effect by key.
     */
    public void playSound(String soundKey) {
        System.out.println("[AudioManager] playSound() called for: " + soundKey);
        System.out.println("[AudioManager]   - effectsEnabled: " + effectsEnabled);

        if (!effectsEnabled) {
            System.out.println("[AudioManager] Effects disabled, not playing");
            return;
        }

        AudioNode sound = loadedSounds.get(soundKey);
        if (sound != null) {
            System.out.println("[AudioManager] Playing sound effect: " + soundKey);
            System.out.println("[AudioManager]   - Volume: " + sound.getVolume());
            System.out.println("[AudioManager]   - Positional: " + sound.isPositional());
            // Create a new instance for playback to allow overlapping sounds
            sound.playInstance();
            System.out.println("[AudioManager] playInstance() called for: " + soundKey);
        } else {
            System.out.println("[AudioManager] WARNING: Sound not found in loadedSounds: " + soundKey);
        }
    }

    /**
     * Play a positional sound effect at a specific location.
     */
    public void playSoundAtPosition(String soundKey, float x, float y, float z) {
        if (!effectsEnabled) {
            return;
        }

        AudioNode sound = loadedSounds.get(soundKey);
        if (sound != null) {
            // For positional audio, we'd need to create instances
            // For now, just play non-positional
            sound.playInstance();
        }
    }

    // Volume control methods

    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0f, Math.min(1f, volume));
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setEffectsVolume(float volume) {
        this.effectsVolume = Math.max(0f, Math.min(1f, volume));
        for (AudioNode sound : loadedSounds.values()) {
            if (sound != null) {
                sound.setVolume(effectsVolume);
            }
        }
    }

    public float getEffectsVolume() {
        return effectsVolume;
    }

    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled) {
            stopMusic();
        }
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void setEffectsEnabled(boolean enabled) {
        this.effectsEnabled = enabled;
    }

    public boolean isEffectsEnabled() {
        return effectsEnabled;
    }

    /**
     * Get the audio node that should be attached to the scene graph.
     * This is needed if you need to manually manage the audio node.
     */
    public Node getAudioNode() {
        return audioNode;
    }

    /**
     * Clean up all audio resources.
     */
    public void cleanup() {
        System.out.println("[AudioManager] cleanup() called");
        stopMusic();

        if (explorationMusic != null) {
            explorationMusic.stop();
        }
        if (combatMusic != null) {
            combatMusic.stop();
        }

        for (AudioNode sound : loadedSounds.values()) {
            if (sound != null) {
                sound.stop();
            }
        }
        loadedSounds.clear();

        // Detach audio node from scene graph
        if (audioNode.getParent() != null) {
            audioNode.removeFromParent();
        }

        System.out.println("[AudioManager] Cleaned up");
    }
}
