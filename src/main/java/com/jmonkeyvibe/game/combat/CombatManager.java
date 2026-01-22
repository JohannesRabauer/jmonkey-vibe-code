package com.jmonkeyvibe.game.combat;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jmonkeyvibe.game.audio.AudioManager;
import com.jmonkeyvibe.game.entities.Enemy;
import com.jmonkeyvibe.game.entities.Player;
import com.jmonkeyvibe.game.entities.Projectile;
import com.jmonkeyvibe.game.world.DungeonGenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Manages combat mechanics, enemies, and projectiles
 */
public class CombatManager {

    /**
     * Listener interface for combat events
     */
    public interface CombatListener {
        /**
         * Called when an enemy is killed
         * @param enemyType The type of enemy that was killed
         * @param experienceAwarded The XP awarded for the kill
         */
        void onEnemyKilled(Enemy.EnemyType enemyType, int experienceAwarded);
    }

    private AssetManager assetManager;
    private Node combatNode;
    private List<Enemy> enemies;
    private List<Projectile> projectiles;
    private Random random;
    private DungeonGenerator dungeonGenerator;
    private CombatListener combatListener;

    // Fire rate limiting to prevent sound spam
    private float fireCooldown = 0f;
    private float currentFireRate = FIRE_RATE;
    private static final float FIRE_RATE = 0.15f; // Seconds between shots

    public CombatManager(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.combatNode = new Node("CombatNode");
        this.enemies = new ArrayList<>();
        this.projectiles = new ArrayList<>();
        this.random = new Random();
    }

    /**
     * Set the combat listener for receiving combat events
     */
    public void setCombatListener(CombatListener listener) {
        this.combatListener = listener;
    }

    /**
     * Set the fire rate (affected by player dexterity)
     */
    public void setFireRate(float fireRate) {
        this.currentFireRate = fireRate;
    }

    /**
     * Set the dungeon generator for collision detection
     */
    public void setDungeonGenerator(DungeonGenerator dungeonGenerator) {
        this.dungeonGenerator = dungeonGenerator;
    }
    
    public void update(float tpf) {
        // Update fire cooldown
        if (fireCooldown > 0) {
            fireCooldown -= tpf;
        }

        // Update all projectiles
        Iterator<Projectile> projIterator = projectiles.iterator();
        while (projIterator.hasNext()) {
            Projectile proj = projIterator.next();
            proj.update(tpf);

            // Remove inactive projectiles
            if (!proj.isActive()) {
                combatNode.detachChild(proj.getSpatial());
                projIterator.remove();
            }
        }

        // Update all enemies
        // Check collisions between projectiles and enemies
        // Track enemies that die this frame for XP rewards
        List<Enemy> killedEnemies = new ArrayList<>();
        AudioManager audioManager = AudioManager.getInstance();
        for (Enemy enemy : enemies) {
            for (Projectile proj : projectiles) {
                if (proj.checkCollision(enemy.getPosition(), 0.5f)) {
                    boolean wasAlive = enemy.isAlive();
                    enemy.takeDamage(proj.getDamage());

                    // Play appropriate sound
                    if (wasAlive && !enemy.isAlive()) {
                        // Enemy just died
                        audioManager.playSound(AudioManager.SOUND_ENEMY_DEATH);
                        killedEnemies.add(enemy);
                        System.out.println("Enemy killed!");
                    } else if (enemy.isAlive()) {
                        // Enemy hit but still alive
                        audioManager.playSound(AudioManager.SOUND_ENEMY_HIT);
                        System.out.println("Hit! Enemy health: " + enemy.getHealth());
                    }
                }
            }
        }

        // Notify listener of killed enemies and award XP
        if (combatListener != null) {
            for (Enemy enemy : killedEnemies) {
                int xp = enemy.getType().getExperienceValue();
                combatListener.onEnemyKilled(enemy.getType(), xp);
            }
        }

        // Remove dead enemies
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            if (!enemy.isAlive()) {
                combatNode.detachChild(enemy.getSpatial());
                enemyIterator.remove();
            }
        }
    }
    
    public void updateEnemies(float tpf, Vector3f playerPosition) {
        for (Enemy enemy : enemies) {
            if (dungeonGenerator != null) {
                // Move enemy with collision detection
                moveEnemyWithCollision(enemy, tpf, playerPosition);
            } else {
                // Fallback to original behavior if no dungeon generator
                enemy.update(tpf, playerPosition);
            }
        }
    }

    /**
     * Move an enemy towards the player with collision detection.
     * Checks X and Z axes separately to allow sliding along walls.
     */
    private void moveEnemyWithCollision(Enemy enemy, float tpf, Vector3f playerPosition) {
        Vector3f currentPos = enemy.getPosition();
        Vector3f direction = playerPosition.subtract(currentPos).normalizeLocal();
        float speed = enemy.getType().getSpeed();

        // Calculate intended movement
        float moveX = direction.x * speed * tpf;
        float moveZ = direction.z * speed * tpf;

        // Try moving in X direction first
        float newX = currentPos.x + moveX;
        boolean canMoveX = dungeonGenerator.isWalkable(newX, currentPos.z);

        // Try moving in Z direction
        float newZ = currentPos.z + moveZ;
        boolean canMoveZ = dungeonGenerator.isWalkable(currentPos.x, newZ);

        // Apply valid movements
        Vector3f newPos = currentPos.clone();
        if (canMoveX) {
            newPos.x = newX;
        }
        if (canMoveZ) {
            newPos.z = newZ;
        }

        // Update enemy position via its update method (also updates cooldowns)
        enemy.updateWithPosition(tpf, newPos);
    }

    /**
     * Process enemy attacks on the player.
     * Returns total damage dealt to the player this frame.
     */
    public float processEnemyAttacks(Player player) {
        float totalDamage = 0f;
        Vector3f playerPosition = player.getPosition();

        for (Enemy enemy : enemies) {
            float damage = enemy.tryAttackPlayer(playerPosition);
            if (damage > 0) {
                player.takeDamage(damage);
                totalDamage += damage;

                // Play player hit sound
                AudioManager.getInstance().playSound(AudioManager.SOUND_PLAYER_HIT);

                System.out.println("Enemy " + enemy.getType().name() + " attacked player for " + damage + " damage! Player health: " + player.getHealth());
            }
        }

        return totalDamage;
    }
    
    public void spawnEnemy(Enemy.EnemyType type, Vector3f position) {
        // Ensure spawn position is walkable
        Vector3f spawnPos = findWalkablePosition(position);
        Enemy enemy = new Enemy(assetManager, type, spawnPos);
        enemies.add(enemy);
        combatNode.attachChild(enemy.getSpatial());
    }

    /**
     * Find a walkable position near the given position.
     * If the given position is already walkable, returns it.
     * Otherwise, searches in a spiral pattern for a nearby walkable tile.
     */
    private Vector3f findWalkablePosition(Vector3f position) {
        if (dungeonGenerator == null || dungeonGenerator.isWalkable(position.x, position.z)) {
            return position;
        }

        // Search in a spiral pattern for a walkable position
        for (int radius = 1; radius <= 10; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    // Only check the outer ring of this radius
                    if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                        float testX = position.x + dx;
                        float testZ = position.z + dz;
                        if (dungeonGenerator.isWalkable(testX, testZ)) {
                            return new Vector3f(testX, position.y, testZ);
                        }
                    }
                }
            }
        }

        // Fallback to original position if no walkable position found
        System.out.println("Warning: Could not find walkable spawn position near " + position);
        return position;
    }
    
    public void fireProjectile(Vector3f startPosition, Vector3f direction, float damage) {
        // Check fire rate cooldown
        if (fireCooldown > 0) {
            return; // Still on cooldown
        }

        Projectile projectile = new Projectile(assetManager, startPosition, direction, damage);
        projectiles.add(projectile);
        combatNode.attachChild(projectile.getSpatial());

        // Play shooting sound
        AudioManager.getInstance().playSound(AudioManager.SOUND_PLAYER_SHOOT);

        // Reset cooldown (use player's dexterity-modified fire rate)
        fireCooldown = currentFireRate;
    }
    
    public Node getCombatNode() {
        return combatNode;
    }
    
    public List<Enemy> getEnemies() {
        return enemies;
    }
    
    public void clear() {
        for (Enemy enemy : enemies) {
            combatNode.detachChild(enemy.getSpatial());
        }
        for (Projectile proj : projectiles) {
            combatNode.detachChild(proj.getSpatial());
        }
        enemies.clear();
        projectiles.clear();
    }
}
