package com.jmonkeyvibe.game.world;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Procedural dungeon generator for combat areas
 */
public class DungeonGenerator {

    private AssetManager assetManager;
    private Random random;

    // Collision grid: 0 = wall (blocked), 1 = floor (walkable)
    private int[][] collisionGrid;

    private static final int ROOM_MIN_SIZE = 4;
    private static final int ROOM_MAX_SIZE = 10;
    private static final int MAX_ROOMS = 15;

    public DungeonGenerator(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.random = new Random();
    }

    /**
     * Generate a procedural dungeon layout
     * @return the collision grid (0 = wall, 1 = floor)
     */
    public int[][] generateDungeon(Node dungeonNode, int width, int height) {
        System.out.println("Generating dungeon: " + width + "x" + height);
        
        // Initialize dungeon grid (0 = wall, 1 = floor)
        int[][] grid = new int[width][height];
        
        // Generate rooms
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < MAX_ROOMS; i++) {
            Room room = createRandomRoom(width, height);
            
            // Check if room overlaps with existing rooms
            boolean overlaps = false;
            for (Room existingRoom : rooms) {
                if (room.intersects(existingRoom)) {
                    overlaps = true;
                    break;
                }
            }
            
            if (!overlaps) {
                rooms.add(room);
                carveRoom(grid, room);
                
                // Connect to previous room with corridor
                if (rooms.size() > 1) {
                    Room prevRoom = rooms.get(rooms.size() - 2);
                    carveCorridor(grid, prevRoom.centerX, prevRoom.centerZ, room.centerX, room.centerZ);
                }
            }
        }
        
        // Create dungeon geometry from grid
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < height; z++) {
                if (grid[x][z] == 1) {
                    createFloorTile(dungeonNode, x, z);
                } else {
                    createWallTile(dungeonNode, x, z);
                }
            }
        }

        // Store the collision grid for external access
        this.collisionGrid = grid;

        System.out.println("Dungeon generated with " + rooms.size() + " rooms");

        return grid;
    }

    /**
     * Get the collision grid for this dungeon
     * @return the collision grid (0 = wall, 1 = floor)
     */
    public int[][] getCollisionGrid() {
        return collisionGrid;
    }

    /**
     * Check if a position is walkable (not a wall)
     * @param x world X coordinate
     * @param z world Z coordinate
     * @return true if the position is walkable
     */
    public boolean isWalkable(float x, float z) {
        if (collisionGrid == null) {
            return true; // No collision data available
        }

        // Convert world coordinates to grid coordinates
        int gridX = Math.round(x);
        int gridZ = Math.round(z);

        // Check bounds
        if (gridX < 0 || gridX >= collisionGrid.length ||
            gridZ < 0 || gridZ >= collisionGrid[0].length) {
            return false; // Out of bounds = wall
        }

        return collisionGrid[gridX][gridZ] == 1;
    }
    
    private Room createRandomRoom(int mapWidth, int mapHeight) {
        int width = random.nextInt(ROOM_MAX_SIZE - ROOM_MIN_SIZE + 1) + ROOM_MIN_SIZE;
        int height = random.nextInt(ROOM_MAX_SIZE - ROOM_MIN_SIZE + 1) + ROOM_MIN_SIZE;
        int x = random.nextInt(mapWidth - width - 1);
        int z = random.nextInt(mapHeight - height - 1);
        
        return new Room(x, z, width, height);
    }
    
    private void carveRoom(int[][] grid, Room room) {
        for (int x = room.x; x < room.x + room.width; x++) {
            for (int z = room.z; z < room.z + room.height; z++) {
                if (x >= 0 && x < grid.length && z >= 0 && z < grid[0].length) {
                    grid[x][z] = 1;
                }
            }
        }
    }
    
    private void carveCorridor(int[][] grid, int x1, int z1, int x2, int z2) {
        // Horizontal corridor
        int startX = Math.min(x1, x2);
        int endX = Math.max(x1, x2);
        for (int x = startX; x <= endX; x++) {
            if (x >= 0 && x < grid.length && z1 >= 0 && z1 < grid[0].length) {
                grid[x][z1] = 1;
            }
        }
        
        // Vertical corridor
        int startZ = Math.min(z1, z2);
        int endZ = Math.max(z1, z2);
        for (int z = startZ; z <= endZ; z++) {
            if (x2 >= 0 && x2 < grid.length && z >= 0 && z < grid[0].length) {
                grid[x2][z] = 1;
            }
        }
    }
    
    private void createFloorTile(Node parent, int x, int z) {
        Quad quad = new Quad(1, 1);
        Geometry tile = new Geometry("Floor_" + x + "_" + z, quad);
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0.3f, 0.3f, 0.3f, 1.0f)); // Dark gray floor
        tile.setMaterial(mat);
        
        // Center and rotate for top-down view
        tile.setLocalTranslation(-0.5f, 0, -0.5f); // Center the quad
        tile.rotate(-FastMath.HALF_PI, 0, 0); // Rotate to face camera
        
        // Use container node for world positioning
        Node tileNode = new Node("FloorNode_" + x + "_" + z);
        tileNode.attachChild(tile);
        tileNode.setLocalTranslation(x, 0, z);
        
        parent.attachChild(tileNode);
    }
    
    private void createWallTile(Node parent, int x, int z) {
        Quad quad = new Quad(1, 1);
        Geometry tile = new Geometry("Wall_" + x + "_" + z, quad);
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0.1f, 0.1f, 0.1f, 1.0f)); // Almost black walls
        tile.setMaterial(mat);
        
        // Center and rotate for top-down view
        tile.setLocalTranslation(-0.5f, 0, -0.5f); // Center the quad
        tile.rotate(-FastMath.HALF_PI, 0, 0); // Rotate to face camera
        
        // Use container node for world positioning
        Node wallNode = new Node("WallNode_" + x + "_" + z);
        wallNode.attachChild(tile);
        wallNode.setLocalTranslation(x, 0, z);
        
        parent.attachChild(wallNode);
    }
    
    /**
     * Inner class representing a dungeon room
     */
    private static class Room {
        int x, z, width, height;
        int centerX, centerZ;
        
        Room(int x, int z, int width, int height) {
            this.x = x;
            this.z = z;
            this.width = width;
            this.height = height;
            this.centerX = x + width / 2;
            this.centerZ = z + height / 2;
        }
        
        boolean intersects(Room other) {
            return !(x + width < other.x || 
                    other.x + other.width < x ||
                    z + height < other.z ||
                    other.z + other.height < z);
        }
    }
}
