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
 * Non-Player Character with AI dialogue capabilities
 */
public class NPC {
    
    private Node spatial;
    private Vector3f position;
    private String name;
    private String personality; // Used for AI dialogue generation
    
    public NPC(AssetManager assetManager, String name) {
        this.name = name;
        this.spatial = new Node("NPC_" + name);
        this.position = new Vector3f(0, 0, 0);
        this.personality = "friendly and helpful";
        
        // Create NPC sprite with texture
        Quad quad = new Quad(1, 1);
        Geometry npcGeom = new Geometry("NPCGeometry_" + name, quad);
        
        Texture npcTexture = assetManager.loadTexture("Textures/npc.png");
        npcTexture.setWrap(Texture.WrapMode.Repeat);
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", npcTexture);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        npcGeom.setMaterial(mat);
        npcGeom.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Transparent);
        
        // Flip texture and center quad for top-down view
        quad.scaleTextureCoordinates(new com.jme3.math.Vector2f(1, -1));
        npcGeom.setLocalTranslation(-0.5f, 0, -0.5f); // Center the quad
        npcGeom.rotate(-FastMath.HALF_PI, 0, 0); // Rotate to face camera
        
        spatial.attachChild(npcGeom);
        spatial.setLocalTranslation(position);
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
}
