package com.jmonkeyvibe.game.world;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import java.util.Random;

/**
 * Procedural world generation for exploration mode
 */
public class WorldGenerator {
    
    private AssetManager assetManager;
    private Random random;
    
    public WorldGenerator(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.random = new Random();
    }
    
    /**
     * Generate a procedural overworld with tiles
     */
    public void generateOverworld(Node worldNode, int width, int height) {
        System.out.println("Generating overworld: " + width + "x" + height);
        
        for (int x = -width / 2; x < width / 2; x++) {
            for (int z = -height / 2; z < height / 2; z++) {
                createTile(worldNode, x, z, getRandomTileType());
            }
        }
    }
    
    private void createTile(Node parent, int x, int z, TileType type) {
        Quad quad = new Quad(1, 1);
        Geometry tile = new Geometry("Tile_" + x + "_" + z, quad);
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", type.getColor());
        tile.setMaterial(mat);
        
        // Center quad at origin, then rotate to face up on XZ plane
        tile.setLocalTranslation(-0.5f, 0, -0.5f); // Center the quad
        tile.rotate(-FastMath.HALF_PI, 0, 0); // Rotate to face camera
        
        // Use container node for world positioning
        Node tileNode = new Node("TileNode_" + x + "_" + z);
        tileNode.attachChild(tile);
        tileNode.setLocalTranslation(x, 0, z);
        
        parent.attachChild(tileNode);
    }
    
    private TileType getRandomTileType() {
        float rand = random.nextFloat();
        if (rand < 0.6f) {
            return TileType.GRASS;
        } else if (rand < 0.8f) {
            return TileType.DIRT;
        } else if (rand < 0.9f) {
            return TileType.STONE;
        } else {
            return TileType.WATER;
        }
    }
    
    /**
     * Tile types with associated colors
     */
    private enum TileType {
        GRASS(new ColorRGBA(0.2f, 0.6f, 0.2f, 1.0f)),
        DIRT(new ColorRGBA(0.5f, 0.35f, 0.2f, 1.0f)),
        STONE(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f)),
        WATER(new ColorRGBA(0.2f, 0.4f, 0.8f, 1.0f));
        
        private final ColorRGBA color;
        
        TileType(ColorRGBA color) {
            this.color = color;
        }
        
        public ColorRGBA getColor() {
            return color;
        }
    }
}
