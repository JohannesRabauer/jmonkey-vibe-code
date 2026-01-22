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
 * Enemy entity for combat encounters
 */
public class Enemy {
    
    private Node spatial;
    private Vector3f position;
    private float health;
    private float maxHealth;
    private float speed;
    private float damage;
    private EnemyType type;
    private float attackCooldown;
    private static final float ATTACK_COOLDOWN_TIME = 1.0f; // 1 second between attacks
    private static final float ATTACK_RANGE = 1.5f; // Distance at which enemy can attack

    // Health bar components
    private Node healthBarNode;
    private Geometry healthBarBackground;
    private Geometry healthBarFill;
    private float healthBarWidth;
    private static final float HEALTH_BAR_HEIGHT = 0.1f;
    private static final float HEALTH_BAR_Y_OFFSET = 0.3f; // Height above the enemy
    
    public Enemy(AssetManager assetManager, EnemyType type, Vector3f position) {
        this.type = type;
        this.position = position.clone();
        this.maxHealth = type.getHealth();
        this.health = maxHealth;
        this.speed = type.getSpeed();
        this.damage = type.getDamage();
        
        this.spatial = new Node("Enemy_" + type.name());
        
        // Create enemy sprite with texture
        Quad quad = new Quad(type.getSize(), type.getSize());
        Geometry enemyGeom = new Geometry("EnemyGeometry", quad);
        
        Texture enemyTexture = assetManager.loadTexture("Textures/enemy.png");
        enemyTexture.setWrap(Texture.WrapMode.Repeat);
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", enemyTexture);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        enemyGeom.setMaterial(mat);
        enemyGeom.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Transparent);
        
        // Flip texture and center quad for top-down view
        quad.scaleTextureCoordinates(new com.jme3.math.Vector2f(1, -1));
        enemyGeom.setLocalTranslation(-type.getSize() / 2, 0, -type.getSize() / 2); // Center the quad
        enemyGeom.rotate(-FastMath.HALF_PI, 0, 0); // Rotate to face camera
        
        spatial.attachChild(enemyGeom);
        spatial.setLocalTranslation(position);

        // Create health bar above enemy
        createHealthBar(assetManager);
    }

    private void createHealthBar(AssetManager assetManager) {
        healthBarNode = new Node("HealthBar");
        healthBarWidth = type.getSize() * 0.8f;

        // Background (red - shows damage)
        Quad bgQuad = new Quad(healthBarWidth, HEALTH_BAR_HEIGHT);
        healthBarBackground = new Geometry("HealthBarBg", bgQuad);
        Material bgMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        bgMat.setColor("Color", new ColorRGBA(0.5f, 0.0f, 0.0f, 0.9f));
        bgMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        healthBarBackground.setMaterial(bgMat);
        healthBarBackground.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Transparent);

        // Fill (green - current health)
        Quad fillQuad = new Quad(healthBarWidth, HEALTH_BAR_HEIGHT);
        healthBarFill = new Geometry("HealthBarFill", fillQuad);
        Material fillMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        fillMat.setColor("Color", new ColorRGBA(0.0f, 0.8f, 0.0f, 1.0f));
        fillMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        healthBarFill.setMaterial(fillMat);
        healthBarFill.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Transparent);

        // Center the health bar above the enemy
        float xOffset = -healthBarWidth / 2;
        healthBarBackground.setLocalTranslation(xOffset, 0, 0);
        healthBarFill.setLocalTranslation(xOffset, 0, 0.01f); // Slightly in front

        // Rotate to face camera (billboard effect for top-down view)
        healthBarBackground.rotate(-FastMath.HALF_PI, 0, 0);
        healthBarFill.rotate(-FastMath.HALF_PI, 0, 0);

        healthBarNode.attachChild(healthBarBackground);
        healthBarNode.attachChild(healthBarFill);

        // Position health bar above the enemy
        healthBarNode.setLocalTranslation(0, HEALTH_BAR_Y_OFFSET, 0);

        spatial.attachChild(healthBarNode);
    }

    private void updateHealthBar() {
        float healthPercent = Math.max(0, health / maxHealth);

        // Update the fill bar width
        float newWidth = healthBarWidth * healthPercent;
        Quad fillQuad = new Quad(Math.max(0.01f, newWidth), HEALTH_BAR_HEIGHT);
        healthBarFill.setMesh(fillQuad);

        // Update color based on health percentage
        ColorRGBA healthColor;
        if (healthPercent > 0.6f) {
            healthColor = new ColorRGBA(0.0f, 0.8f, 0.0f, 1.0f); // Green
        } else if (healthPercent > 0.3f) {
            healthColor = new ColorRGBA(0.8f, 0.8f, 0.0f, 1.0f); // Yellow
        } else {
            healthColor = new ColorRGBA(0.8f, 0.0f, 0.0f, 1.0f); // Red
        }
        healthBarFill.getMaterial().setColor("Color", healthColor);
    }
    
    public void update(float tpf, Vector3f playerPosition) {
        // Simple AI: move towards player
        Vector3f direction = playerPosition.subtract(position).normalize();
        position.addLocal(direction.mult(speed * tpf));
        spatial.setLocalTranslation(position);

        // Update attack cooldown
        if (attackCooldown > 0) {
            attackCooldown -= tpf;
        }
    }

    /**
     * Update enemy with a specific position (used for collision-aware movement).
     * This allows external collision checking before setting the position.
     */
    public void updateWithPosition(float tpf, Vector3f newPosition) {
        position.set(newPosition);
        spatial.setLocalTranslation(position);

        // Update attack cooldown
        if (attackCooldown > 0) {
            attackCooldown -= tpf;
        }
    }

    /**
     * Check if enemy can attack the player and return damage if so.
     * Returns damage dealt, or 0 if not attacking.
     */
    public float tryAttackPlayer(Vector3f playerPosition) {
        float distanceToPlayer = position.distance(playerPosition);

        // Check if in range and off cooldown
        if (distanceToPlayer <= ATTACK_RANGE && attackCooldown <= 0) {
            attackCooldown = ATTACK_COOLDOWN_TIME;
            return damage;
        }
        return 0f;
    }

    public float getAttackRange() {
        return ATTACK_RANGE;
    }
    
    public void takeDamage(float damage) {
        health = Math.max(0, health - damage);
        updateHealthBar();
    }
    
    public boolean isAlive() {
        return health > 0;
    }
    
    public Node getSpatial() {
        return spatial;
    }
    
    public Vector3f getPosition() {
        return position.clone();
    }
    
    public float getDamage() {
        return damage;
    }
    
    public EnemyType getType() {
        return type;
    }

    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }
    
    /**
     * Enemy types with different stats
     */
    public enum EnemyType {
        GOBLIN(30f, 3f, 5f, 0.8f, ColorRGBA.Red, 10),
        SKELETON(50f, 2.5f, 8f, 1.0f, ColorRGBA.White, 15),
        ORC(80f, 2f, 12f, 1.2f, new ColorRGBA(0.4f, 0.6f, 0.2f, 1f), 25),
        DEMON(120f, 3.5f, 15f, 1.5f, new ColorRGBA(0.6f, 0.1f, 0.1f, 1f), 40);

        private final float health;
        private final float speed;
        private final float damage;
        private final float size;
        private final ColorRGBA color;
        private final int experienceValue;

        EnemyType(float health, float speed, float damage, float size, ColorRGBA color, int experienceValue) {
            this.health = health;
            this.speed = speed;
            this.damage = damage;
            this.size = size;
            this.color = color;
            this.experienceValue = experienceValue;
        }

        public float getHealth() { return health; }
        public float getSpeed() { return speed; }
        public float getDamage() { return damage; }
        public float getSize() { return size; }
        public ColorRGBA getColor() { return color; }
        public int getExperienceValue() { return experienceValue; }
    }
}
