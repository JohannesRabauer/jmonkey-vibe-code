package com.jmonkeyvibe.game.quest;

/**
 * Represents a quest in the game
 */
public class Quest {
    
    private String id;
    private String title;
    private String description;
    private String type; // FETCH, KILL, ESCORT, EXPLORE, TALK
    private String objective;
    private int count; // For kill quests or multiple objectives
    private int progress;
    private int reward;
    private boolean completed;
    private boolean active;
    
    public Quest() {
        this.id = java.util.UUID.randomUUID().toString();
        this.progress = 0;
        this.completed = false;
        this.active = false;
        this.count = 1;
    }
    
    // Getters and setters
    
    public String getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getObjective() {
        return objective;
    }
    
    public void setObjective(String objective) {
        this.objective = objective;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public int getProgress() {
        return progress;
    }
    
    public void incrementProgress() {
        this.progress++;
        if (progress >= count) {
            this.completed = true;
        }
    }
    
    public int getReward() {
        return reward;
    }
    
    public void setReward(int reward) {
        this.reward = reward;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s - %s (Reward: %d gold)", 
            type, title, description, reward);
    }
}
