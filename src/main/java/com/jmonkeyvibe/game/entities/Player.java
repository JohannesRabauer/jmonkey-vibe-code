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
 * Player entity with movement and interaction
 */
public class Player {
    
    private Node spatial;
    private Vector3f position;
    private float health;
    private float maxHealth;
    
    public Player(AssetManager assetManager) {
        this.spatial = new Node("Player");
        this.position = new Vector3f(0, 0, 0);
        this.maxHealth = 100f;
        this.health = maxHealth;
        
        // Create player sprite (simple colored quad for now)
        Quad quad = new Quad(1, 1);
        Geometry playerGeom = new Geometry("PlayerGeometry", quad);
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        playerGeom.setMaterial(mat);
        
        // Center and rotate for top-down view
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
    
    public void takeDamage(float damage) {
        health = Math.max(0, health - damage);
    }
    
    public void heal(float amount) {
        health = Math.min(maxHealth, health + amount);
    }
    
    public boolean isAlive() {
        return health > 0;
    }
}
