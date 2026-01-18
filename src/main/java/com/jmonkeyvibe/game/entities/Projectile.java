package com.jmonkeyvibe.game.entities;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

/**
 * Projectile entity for combat
 */
public class Projectile {
    
    private Node spatial;
    private Vector3f position;
    private Vector3f velocity;
    private float damage;
    private float lifetime;
    private float maxLifetime;
    private boolean active;
    
    private static final float PROJECTILE_SPEED = 15f;
    private static final float MAX_LIFETIME = 3f; // seconds
    
    public Projectile(AssetManager assetManager, Vector3f startPosition, Vector3f direction, float damage) {
        this.position = startPosition.clone();
        this.velocity = direction.normalize().mult(PROJECTILE_SPEED);
        this.damage = damage;
        this.lifetime = 0;
        this.maxLifetime = MAX_LIFETIME;
        this.active = true;
        
        this.spatial = new Node("Projectile");
        
        // Create projectile sprite (small yellow square)
        Quad quad = new Quad(0.3f, 0.3f);
        Geometry projectileGeom = new Geometry("ProjectileGeometry", quad);
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Yellow);
        projectileGeom.setMaterial(mat);
        // Center and rotate for top-down view
        projectileGeom.setLocalTranslation(-0.15f, 0, -0.15f); // Center the quad
        projectileGeom.rotate(-FastMath.HALF_PI, 0, 0); // Rotate to face camera
        
        spatial.attachChild(projectileGeom);
        spatial.setLocalTranslation(position);
    }
    
    public void update(float tpf) {
        if (!active) return;
        
        // Update position
        position.addLocal(velocity.mult(tpf));
        spatial.setLocalTranslation(position);
        
        // Update lifetime
        lifetime += tpf;
        if (lifetime >= maxLifetime) {
            active = false;
        }
    }
    
    public boolean checkCollision(Vector3f targetPosition, float targetRadius) {
        if (!active) return false;
        
        float distance = position.distance(targetPosition);
        if (distance <= targetRadius) {
            active = false;
            return true;
        }
        return false;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void deactivate() {
        active = false;
    }
    
    public Node getSpatial() {
        return spatial;
    }
    
    public float getDamage() {
        return damage;
    }
    
    public Vector3f getPosition() {
        return position.clone();
    }
}
