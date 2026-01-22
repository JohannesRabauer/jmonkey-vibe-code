package com.jmonkeyvibe.game.entities;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

import java.util.Random;

/**
 * Non-Player Character with AI dialogue capabilities
 */
public class NPC {

    private Node spatial;
    private Vector3f position;
    private String name;
    private String personality; // Used for AI dialogue generation
    private NPCType npcType;

    // Random name pools for NPC generation
    private static final String[] FIRST_NAMES = {
        "Aldric", "Bran", "Cedric", "Dorian", "Elara", "Fiona", "Gareth", "Helena",
        "Ivar", "Jasper", "Kira", "Lyra", "Magnus", "Nora", "Orion", "Petra",
        "Quinn", "Roland", "Seren", "Theron", "Una", "Victor", "Willow", "Xander",
        "Yara", "Zephyr", "Astrid", "Boris", "Clara", "Drake"
    };

    private static final String[] TITLES = {
        "the Wise", "the Bold", "the Mysterious", "the Wanderer", "the Merchant",
        "the Healer", "the Scholar", "the Hunter", "the Blacksmith", "the Alchemist",
        "the Storyteller", "the Guardian", "the Traveler", "the Hermit", "the Sage",
        "of the Valley", "of the Mountains", "of the Forest", "the Elder", "the Young"
    };

    private static final String[] PERSONALITIES = {
        "friendly and helpful, always willing to share knowledge",
        "mysterious and cryptic, speaking in riddles",
        "grumpy but secretly kind-hearted",
        "overly enthusiastic and excitable about everything",
        "calm and wise, offering profound insights",
        "paranoid and suspicious of strangers",
        "jovial and loves to tell jokes and stories",
        "melancholic and philosophical about life",
        "pragmatic and business-minded",
        "curious and asks many questions about the traveler",
        "shy and speaks softly with hesitation",
        "boastful and loves to exaggerate their achievements",
        "nurturing and concerned about the hero's wellbeing",
        "eccentric and has unusual interests",
        "stern but fair, values honor above all"
    };

    private static final Random staticRandom = new Random();

    /**
     * NPC types with different visual appearances
     */
    public enum NPCType {
        VILLAGER(new ColorRGBA(0.8f, 0.7f, 0.5f, 1.0f), 1.0f),
        MERCHANT(new ColorRGBA(0.9f, 0.8f, 0.2f, 1.0f), 1.1f),
        GUARD(new ColorRGBA(0.5f, 0.5f, 0.7f, 1.0f), 1.2f),
        SAGE(new ColorRGBA(0.6f, 0.4f, 0.8f, 1.0f), 1.0f),
        TRAVELER(new ColorRGBA(0.4f, 0.6f, 0.4f, 1.0f), 1.0f),
        NOBLE(new ColorRGBA(0.8f, 0.2f, 0.3f, 1.0f), 1.15f);

        private final ColorRGBA tintColor;
        private final float size;

        NPCType(ColorRGBA tintColor, float size) {
            this.tintColor = tintColor;
            this.size = size;
        }

        public ColorRGBA getTintColor() { return tintColor; }
        public float getSize() { return size; }

        public static NPCType getRandomType() {
            NPCType[] types = values();
            return types[staticRandom.nextInt(types.length)];
        }
    }

    /**
     * Generate a random NPC name
     */
    public static String generateRandomName() {
        String firstName = FIRST_NAMES[staticRandom.nextInt(FIRST_NAMES.length)];
        // 60% chance to add a title
        if (staticRandom.nextFloat() < 0.6f) {
            String title = TITLES[staticRandom.nextInt(TITLES.length)];
            return firstName + " " + title;
        }
        return firstName;
    }

    /**
     * Generate a random personality
     */
    public static String generateRandomPersonality() {
        return PERSONALITIES[staticRandom.nextInt(PERSONALITIES.length)];
    }

    /**
     * Create NPC with specified name (uses random type)
     */
    public NPC(AssetManager assetManager, String name) {
        this(assetManager, name, NPCType.getRandomType());
    }

    /**
     * Create NPC with specified name and type
     */
    public NPC(AssetManager assetManager, String name, NPCType type) {
        this.name = name;
        this.npcType = type;
        this.spatial = new Node("NPC_" + name);
        this.position = new Vector3f(0, 0, 0);
        this.personality = generateRandomPersonality();
        
        // Create NPC sprite with texture and type-based appearance
        float size = npcType.getSize();
        Quad quad = new Quad(size, size);
        Geometry npcGeom = new Geometry("NPCGeometry_" + name, quad);

        Texture npcTexture = assetManager.loadTexture("Textures/npc.png");
        npcTexture.setWrap(Texture.WrapMode.Repeat);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", npcTexture);
        // Apply tint color based on NPC type
        mat.setColor("Color", npcType.getTintColor());
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        npcGeom.setMaterial(mat);
        npcGeom.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Transparent);

        // Flip texture and center quad for top-down view
        quad.scaleTextureCoordinates(new com.jme3.math.Vector2f(1, -1));
        npcGeom.setLocalTranslation(-size / 2, 0, -size / 2); // Center the quad
        npcGeom.rotate(-FastMath.HALF_PI, 0, 0); // Rotate to face camera

        spatial.attachChild(npcGeom);
        spatial.setLocalTranslation(position);
    }

    /**
     * Create a fully randomized NPC
     */
    public static NPC createRandomNPC(AssetManager assetManager) {
        String name = generateRandomName();
        NPCType type = NPCType.getRandomType();
        NPC npc = new NPC(assetManager, name, type);
        npc.setPersonality(generateRandomPersonality());
        return npc;
    }
    
    public void setPosition(Vector3f position) {
        this.position.set(position);
        spatial.setLocalTranslation(position);
    }
    
    public Vector3f getPosition() {
        return position.clone();
    }
    
    public Node getSpatial() {
        return spatial;
    }
    
    public String getName() {
        return name;
    }
    
    public String getPersonality() {
        return personality;
    }
    
    public void setPersonality(String personality) {
        this.personality = personality;
    }
    
    public float distanceTo(Vector3f otherPosition) {
        return position.distance(otherPosition);
    }

    public NPCType getNpcType() {
        return npcType;
    }
}
