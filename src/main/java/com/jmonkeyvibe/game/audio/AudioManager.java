package com.jmonkeyvibe.game.audio;

import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;

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
    }

    /**
     * Initialize the AudioManager with the asset manager.
     * This must be called before any audio can be played.
     */
    public void initialize(AssetManager assetManager) {
        this.assetManager = assetManager;
        loadAllAudio();
        System.out.println("AudioManager initialized");
    }

    /**
     * Load all audio files. Missing files are logged as warnings but don't crash.
     */
    private void loadAllAudio() {
        // Load background music
        explorationMusic = loadMusic("Sounds/Music/exploration.ogg", true);
        combatMusic = loadMusic("Sounds/Music/combat.ogg", true);

        // Load sound effects
        loadSoundEffect(SOUND_PLAYER_SHOOT, "Sounds/Effects/player_shoot.ogg");
        loadSoundEffect(SOUND_ENEMY_HIT, "Sounds/Effects/enemy_hit.ogg");
        loadSoundEffect(SOUND_ENEMY_DEATH, "Sounds/Effects/enemy_death.ogg");
        loadSoundEffect(SOUND_PLAYER_HIT, "Sounds/Effects/player_hit.ogg");
        loadSoundEffect(SOUND_WAVE_START, "Sounds/Effects/wave_start.ogg");
    }

    /**
     * Load a music track. Returns null if the file doesn't exist.
     */
    private AudioNode loadMusic(String path, boolean loop) {
        try {
            AudioNode music = new AudioNode(assetManager, path, AudioData.DataType.Stream);
            music.setLooping(loop);
            music.setPositional(false); // Non-positional (background music)
            music.setVolume(musicVolume);
            System.out.println("Loaded music: " + path);
            return music;
        } catch (AssetNotFoundException e) {
            logger.log(Level.WARNING, "Music file not found: " + path + " - Music will be silent");
            return null;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to load music: " + path + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Load a sound effect and store it by key.
     */
    private void loadSoundEffect(String key, String path) {
        try {
            AudioNode sound = new AudioNode(assetManager, path, AudioData.DataType.Buffer);
            sound.setLooping(false);
            sound.setPositional(false); // Non-positional for now
            sound.setVolume(effectsVolume);
            loadedSounds.put(key, sound);
            System.out.println("Loaded sound effect: " + key + " from " + path);
        } catch (AssetNotFoundException e) {
            logger.log(Level.WARNING, "Sound effect not found: " + path + " - Effect will be silent");
            loadedSounds.put(key, null); // Store null to mark as attempted
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to load sound effect: " + path + " - " + e.getMessage());
            loadedSounds.put(key, null);
        }
    }

    /**
     * Play background music for exploration mode.
     */
    public void playExplorationMusic() {
        switchMusic(explorationMusic);
    }

    /**
     * Play background music for combat mode.
     */
    public void playCombatMusic() {
        switchMusic(combatMusic);
    }

    /**
     * Switch to a different music track, fading out the current one.
     */
    private void switchMusic(AudioNode newMusic) {
        if (!musicEnabled) {
            return;
        }

        // Stop current music
        if (currentMusic != null && currentMusic.getStatus() == AudioSource.Status.Playing) {
            currentMusic.stop();
        }

        // Start new music
        if (newMusic != null) {
            newMusic.setVolume(musicVolume);
            newMusic.play();
            currentMusic = newMusic;
            System.out.println("Now playing: " + (newMusic == explorationMusic ? "exploration" : "combat") + " music");
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
        if (!effectsEnabled) {
            return;
        }

        AudioNode sound = loadedSounds.get(soundKey);
        if (sound != null) {
            // Create a new instance for playback to allow overlapping sounds
            sound.playInstance();
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
     * Clean up all audio resources.
     */
    public void cleanup() {
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

        System.out.println("AudioManager cleaned up");
    }
}
