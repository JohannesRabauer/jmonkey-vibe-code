package com.jmonkeyvibe.game.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Procedural quest generation system
 */
public class QuestGenerator {
    
    private Random random;
    private static final String[] QUEST_TYPES = {
        "FETCH", "KILL", "ESCORT", "EXPLORE", "TALK"
    };
    
    private static final String[] QUEST_ITEMS = {
        "Ancient Sword", "Magic Crystal", "Healing Herb", "Lost Letter", "Golden Key"
    };
    
    private static final String[] QUEST_ENEMIES = {
        "Goblin Chief", "Dark Wizard", "Giant Spider", "Skeleton Warrior", "Troll King"
    };
    
    private static final String[] QUEST_LOCATIONS = {
        "Ancient Ruins", "Dark Forest", "Abandoned Mine", "Cursed Temple", "Mountain Peak"
    };
    
    public QuestGenerator() {
        this.random = new Random();
    }
    
    /**
     * Generate a random quest
     */
    public Quest generateQuest() {
        String type = QUEST_TYPES[random.nextInt(QUEST_TYPES.length)];
        
        return switch (type) {
            case "FETCH" -> generateFetchQuest();
            case "KILL" -> generateKillQuest();
            case "ESCORT" -> generateEscortQuest();
            case "EXPLORE" -> generateExploreQuest();
            case "TALK" -> generateTalkQuest();
            default -> generateFetchQuest();
        };
    }
    
    private Quest generateFetchQuest() {
        String item = QUEST_ITEMS[random.nextInt(QUEST_ITEMS.length)];
        String location = QUEST_LOCATIONS[random.nextInt(QUEST_LOCATIONS.length)];
        
        Quest quest = new Quest();
        quest.setTitle("Retrieve the " + item);
        quest.setDescription("Find and bring back the " + item + " from the " + location + ".");
        quest.setType("FETCH");
        quest.setObjective(item);
        quest.setReward(random.nextInt(50) + 50); // 50-100 gold
        
        return quest;
    }
    
    private Quest generateKillQuest() {
        String enemy = QUEST_ENEMIES[random.nextInt(QUEST_ENEMIES.length)];
        int count = random.nextInt(5) + 1;
        
        Quest quest = new Quest();
        quest.setTitle("Defeat the " + enemy);
        quest.setDescription("Hunt down and defeat " + count + " " + enemy + "(s) that have been terrorizing the area.");
        quest.setType("KILL");
        quest.setObjective(enemy);
        quest.setCount(count);
        quest.setReward(random.nextInt(75) + 75); // 75-150 gold
        
        return quest;
    }
    
    private Quest generateEscortQuest() {
        String location = QUEST_LOCATIONS[random.nextInt(QUEST_LOCATIONS.length)];
        
        Quest quest = new Quest();
        quest.setTitle("Escort to " + location);
        quest.setDescription("Safely escort a merchant to the " + location + ".");
        quest.setType("ESCORT");
        quest.setObjective(location);
        quest.setReward(random.nextInt(60) + 40); // 40-100 gold
        
        return quest;
    }
    
    private Quest generateExploreQuest() {
        String location = QUEST_LOCATIONS[random.nextInt(QUEST_LOCATIONS.length)];
        
        Quest quest = new Quest();
        quest.setTitle("Explore the " + location);
        quest.setDescription("Venture into the " + location + " and discover its secrets.");
        quest.setType("EXPLORE");
        quest.setObjective(location);
        quest.setReward(random.nextInt(50) + 30); // 30-80 gold
        
        return quest;
    }
    
    private Quest generateTalkQuest() {
        String[] npcs = {"Wise Sage", "Village Chief", "Mysterious Hermit", "Royal Guard"};
        String npc = npcs[random.nextInt(npcs.length)];
        
        Quest quest = new Quest();
        quest.setTitle("Speak with " + npc);
        quest.setDescription("Find and talk to " + npc + " to gather important information.");
        quest.setType("TALK");
        quest.setObjective(npc);
        quest.setReward(random.nextInt(40) + 20); // 20-60 gold
        
        return quest;
    }
}
