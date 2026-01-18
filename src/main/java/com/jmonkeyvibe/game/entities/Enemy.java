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
    }
    
    public void update(float tpf, Vector3f playerPosition) {
        // Simple AI: move towards player
        Vector3f direction = playerPosition.subtract(position).normalize();
        position.addLocal(direction.mult(speed * tpf));
        spatial.setLocalTranslation(position);
    }
    
    public void takeDamage(float damage) {
        health = Math.max(0, health - damage);
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
    
    /**
     * Enemy types with different stats
     */
    public enum EnemyType {
        GOBLIN(30f, 3f, 5f, 0.8f, ColorRGBA.Red),
        SKELETON(50f, 2.5f, 8f, 1.0f, ColorRGBA.White),
        ORC(80f, 2f, 12f, 1.2f, new ColorRGBA(0.4f, 0.6f, 0.2f, 1f)),
        DEMON(120f, 3.5f, 15f, 1.5f, new ColorRGBA(0.6f, 0.1f, 0.1f, 1f));
        
        private final float health;
        private final float speed;
        private final float damage;
        private final float size;
        private final ColorRGBA color;
        
        EnemyType(float health, float speed, float damage, float size, ColorRGBA color) {
            this.health = health;
            this.speed = speed;
            this.damage = damage;
            this.size = size;
            this.color = color;
        }
        
        public float getHealth() { return health; }
        public float getSpeed() { return speed; }
        public float getDamage() { return damage; }
        public float getSize() { return size; }
        public ColorRGBA getColor() { return color; }
    }
}
