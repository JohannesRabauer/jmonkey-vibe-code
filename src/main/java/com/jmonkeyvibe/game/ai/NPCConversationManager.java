package com.jmonkeyvibe.game.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import com.jmonkeyvibe.game.entities.NPC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages NPC conversations using AI language models
 */
public class NPCConversationManager {
    
    private ChatLanguageModel chatModel;
    private Map<String, List<String>> conversationHistory;
    private static final int MAX_HISTORY_LENGTH = 10;
    
    public NPCConversationManager() {
        try {
            this.chatModel = AIModelFactory.createChatModel();
            this.conversationHistory = new HashMap<>();
            System.out.println("NPCConversationManager initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize AI model: " + e.getMessage());
            System.err.println("NPC conversations will use fallback dialogue");
            this.chatModel = null;
        }
    }
    
    /**
     * Start a conversation with an NPC
     */
    public String startConversation(NPC npc, String playerMessage) {
        if (chatModel == null) {
            return getFallbackDialogue(npc);
        }
        
        String npcId = npc.getName();
        
        // Initialize conversation history if needed
        conversationHistory.putIfAbsent(npcId, new ArrayList<>());
        List<String> history = conversationHistory.get(npcId);
        
        // Build context for the AI
        String systemPrompt = buildSystemPrompt(npc);
        String conversationContext = buildConversationContext(history, playerMessage);
        String fullPrompt = systemPrompt + "\n\n" + conversationContext;
        
        try {
            // Generate AI response
            String npcResponse = chatModel.generate(fullPrompt);
            
            // Update conversation history
            history.add("Player: " + playerMessage);
            history.add(npc.getName() + ": " + npcResponse);
            
            // Trim history if too long
            if (history.size() > MAX_HISTORY_LENGTH * 2) {
                history.subList(0, history.size() - MAX_HISTORY_LENGTH * 2).clear();
            }
            
            return npcResponse;
            
        } catch (Exception e) {
            System.err.println("AI dialogue generation failed: " + e.getMessage());
            return getFallbackDialogue(npc);
        }
    }
    
    /**
     * Build system prompt with NPC personality and context
     */
    private String buildSystemPrompt(NPC npc) {
        return String.format(
            "You are %s, an NPC in a fantasy RPG game. " +
            "Your personality is: %s. " +
            "You live in a procedurally generated world with dungeons, quests, and adventures. " +
            "Respond in character, keeping your responses concise (2-3 sentences). " +
            "You can offer quests, share information about the world, or just chat with the player. " +
            "Stay in character and be helpful to the adventurer.",
            npc.getName(),
            npc.getPersonality()
        );
    }
    
    /**
     * Build conversation context from history
     */
    private String buildConversationContext(List<String> history, String newMessage) {
        StringBuilder context = new StringBuilder();
        
        if (!history.isEmpty()) {
            context.append("Previous conversation:\n");
            for (String line : history) {
                context.append(line).append("\n");
            }
            context.append("\n");
        }
        
        context.append("Player: ").append(newMessage).append("\n");
        context.append("Respond as the NPC:");
        
        return context.toString();
    }
    
    /**
     * Fallback dialogue when AI is unavailable
     */
    private String getFallbackDialogue(NPC npc) {
        return String.format(
            "Greetings, traveler! I am %s. " +
            "The AI dialogue system is currently unavailable. " +
            "Please configure your AI provider (OPENAI or OLLAMA) to enable dynamic conversations.",
            npc.getName()
        );
    }
    
    /**
     * Clear conversation history for an NPC
     */
    public void clearHistory(String npcId) {
        conversationHistory.remove(npcId);
    }
    
    /**
     * Clear all conversation histories
     */
    public void clearAllHistory() {
        conversationHistory.clear();
    }
    
    /**
     * Check if AI model is available
     */
    public boolean isAIAvailable() {
        return chatModel != null;
    }
}
