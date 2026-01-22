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

/**
 * Player entity with movement and interaction
 */
public class Player {

    private Node spatial;
    private Vector3f position;
    private float health;
    private float maxHealth;

    // Leveling system
    private int level;
    private int experience;
    private int experienceToNextLevel;

    // Stats
    private int strength;      // Damage multiplier
    private int agility;       // Move speed
    private int vitality;      // Max health
    private int dexterity;     // Fire rate

    // Base values for stat calculations
    private static final float BASE_MAX_HEALTH = 100f;
    private static final float BASE_MOVE_SPEED = 7.0f;
    private static final float BASE_DAMAGE = 10f;
    private static final float BASE_FIRE_RATE = 0.15f;

    public Player(AssetManager assetManager) {
        this.spatial = new Node("Player");
        this.position = new Vector3f(0, 0, 0);

        // Initialize leveling
        this.level = 1;
        this.experience = 0;
        this.experienceToNextLevel = 100; // level * 100

        // Initialize base stats (all start at 1)
        this.strength = 1;
        this.agility = 1;
        this.vitality = 1;
        this.dexterity = 1;

        // Calculate max health based on vitality
        this.maxHealth = calculateMaxHealth();
        this.health = maxHealth;
        
        // Create player sprite with texture
        Quad quad = new Quad(1, 1);
        Geometry playerGeom = new Geometry("PlayerGeometry", quad);
        
        Texture playerTexture = assetManager.loadTexture("Textures/player.png");
        playerTexture.setWrap(Texture.WrapMode.Repeat);
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", playerTexture);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        playerGeom.setMaterial(mat);
        playerGeom.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Transparent);
        
        // Flip texture and center quad for top-down view
        quad.scaleTextureCoordinates(new com.jme3.math.Vector2f(1, -1));
        playerGeom.setLocalTranslation(-0.5f, 0, -0.5f); // Center the quad
        playerGeom.rotate(-FastMath.HALF_PI, 0, 0); // Rotate to face camera
        
        spatial.attachChild(playerGeom);
        spatial.setLocalTranslation(position);
    }
    
    public void setPosition(Vector3f position) {
        this.position.set(position);
        spatial.setLocalTranslation(position);
    }
    
    public Vector3f getPosition() {
        return position.clone();
    }
    
    public void move(Vector3f delta) {
        position.addLocal(delta);
        spatial.setLocalTranslation(position);
    }
    
    public Node getSpatial() {
        return spatial;
    }
    
    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }
    
    public void takeDamage(float damage) {
        health = Math.max(0, health - damage);
    }
    
    public void heal(float amount) {
        health = Math.min(maxHealth, health + amount);
    }
    
    public boolean isAlive() {
        return health > 0;
    }

    // Leveling methods
    public int getLevel() {
        return level;
    }

    public int getExperience() {
        return experience;
    }

    public int getExperienceToNextLevel() {
        return experienceToNextLevel;
    }

    /**
     * Add experience points and check for level up.
     * @param xp Experience points to add
     * @return true if leveled up, false otherwise
     */
    public boolean addExperience(int xp) {
        experience += xp;
        System.out.println("Gained " + xp + " XP! (" + experience + "/" + experienceToNextLevel + ")");

        if (experience >= experienceToNextLevel) {
            return true; // Level up available
        }
        return false;
    }

    /**
     * Perform level up. Called after player selects a stat.
     * @param stat The stat to increase (1=Strength, 2=Agility, 3=Vitality, 4=Dexterity)
     */
    public void levelUp(int stat) {
        // Carry over excess XP
        experience -= experienceToNextLevel;
        level++;
        experienceToNextLevel = level * 100;

        // Increase the selected stat
        switch (stat) {
            case 1:
                strength++;
                System.out.println("Strength increased to " + strength + "!");
                break;
            case 2:
                agility++;
                System.out.println("Agility increased to " + agility + "!");
                break;
            case 3:
                vitality++;
                float oldMaxHealth = maxHealth;
                maxHealth = calculateMaxHealth();
                // Heal to full on vitality increase
                health = maxHealth;
                System.out.println("Vitality increased to " + vitality + "! Max HP: " + (int)oldMaxHealth + " -> " + (int)maxHealth);
                break;
            case 4:
                dexterity++;
                System.out.println("Dexterity increased to " + dexterity + "!");
                break;
        }

        System.out.println("Level Up! Now level " + level);
    }

    // Stat getters
    public int getStrength() {
        return strength;
    }

    public int getAgility() {
        return agility;
    }

    public int getVitality() {
        return vitality;
    }

    public int getDexterity() {
        return dexterity;
    }

    // Calculated stat values
    private float calculateMaxHealth() {
        // Each vitality point adds 20 HP
        return BASE_MAX_HEALTH + (vitality - 1) * 20f;
    }

    /**
     * Get calculated move speed based on agility.
     * Each agility point adds 0.5 to move speed.
     */
    public float getMoveSpeed() {
        return BASE_MOVE_SPEED + (agility - 1) * 0.5f;
    }

    /**
     * Get damage multiplier based on strength.
     * Each strength point adds 15% damage.
     */
    public float getDamageMultiplier() {
        return 1.0f + (strength - 1) * 0.15f;
    }

    /**
     * Get fire cooldown based on dexterity.
     * Each dexterity point reduces cooldown by 10%.
     */
    public float getFireCooldown() {
        float reduction = 1.0f - (dexterity - 1) * 0.10f;
        // Minimum 30% of base fire rate
        return BASE_FIRE_RATE * Math.max(0.30f, reduction);
    }

    /**
     * Get base damage value (used by combat manager)
     */
    public float getBaseDamage() {
        return BASE_DAMAGE * getDamageMultiplier();
    }
}
