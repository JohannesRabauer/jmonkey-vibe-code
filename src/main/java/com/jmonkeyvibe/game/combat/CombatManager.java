package com.jmonkeyvibe.game.combat;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jmonkeyvibe.game.entities.Enemy;
import com.jmonkeyvibe.game.entities.Player;
import com.jmonkeyvibe.game.entities.Projectile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Manages combat mechanics, enemies, and projectiles
 */
public class CombatManager {
    
    private AssetManager assetManager;
    private Node combatNode;
    private List<Enemy> enemies;
    private List<Projectile> projectiles;
    private Random random;
    
    public CombatManager(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.combatNode = new Node("CombatNode");
        this.enemies = new ArrayList<>();
        this.projectiles = new ArrayList<>();
        this.random = new Random();
    }
    
    public void update(float tpf) {
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
        for (Enemy enemy : enemies) {
            for (Projectile proj : projectiles) {
                if (proj.checkCollision(enemy.getPosition(), 0.5f)) {
                    enemy.takeDamage(proj.getDamage());
                    System.out.println("Hit! Enemy health: " + (enemy.isAlive() ? "alive" : "dead"));
                }
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
            enemy.update(tpf, playerPosition);
        }
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
                System.out.println("Enemy " + enemy.getType().name() + " attacked player for " + damage + " damage! Player health: " + player.getHealth());
            }
        }

        return totalDamage;
    }
    
    public void spawnEnemy(Enemy.EnemyType type, Vector3f position) {
        Enemy enemy = new Enemy(assetManager, type, position);
        enemies.add(enemy);
        combatNode.attachChild(enemy.getSpatial());
    }
    
    public void fireProjectile(Vector3f startPosition, Vector3f direction, float damage) {
        Projectile projectile = new Projectile(assetManager, startPosition, direction, damage);
        projectiles.add(projectile);
        combatNode.attachChild(projectile.getSpatial());
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
