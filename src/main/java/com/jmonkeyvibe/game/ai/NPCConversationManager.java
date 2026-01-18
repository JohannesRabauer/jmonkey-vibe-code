package com.jmonkeyvibe.game.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.data.message.AiMessage;
import com.jmonkeyvibe.game.entities.NPC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Manages NPC conversations using AI language models
 */
public class NPCConversationManager {
    
    private ChatLanguageModel chatModel;
    private StreamingChatLanguageModel streamingChatModel;
    private Map<String, List<String>> conversationHistory;
    private static final int MAX_HISTORY_LENGTH = 10;
    
    public NPCConversationManager() {
        try {
            this.chatModel = AIModelFactory.createChatModel();
            this.streamingChatModel = AIModelFactory.createStreamingChatModel();
            this.conversationHistory = new HashMap<>();
            System.out.println("NPCConversationManager initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize AI model: " + e.getMessage());
            System.err.println("NPC conversations will use fallback dialogue");
            this.chatModel = null;
            this.streamingChatModel = null;
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
    
    /**
     * Start streaming conversation with an NPC
     */
    public void startStreamingConversation(NPC npc, String playerMessage, Consumer<String> onToken, Runnable onComplete) {
        if (streamingChatModel == null) {
            // Fallback to non-streaming
            String response = startConversation(npc, playerMessage);
            onToken.accept(response);
            onComplete.run();
            return;
        }
        
        String npcId = npc.getName();
        conversationHistory.putIfAbsent(npcId, new ArrayList<>());
        List<String> history = conversationHistory.get(npcId);
        
        String systemPrompt = buildSystemPrompt(npc);
        String conversationContext = buildConversationContext(history, playerMessage);
        String fullPrompt = systemPrompt + "\n\n" + conversationContext;
        
        StringBuilder fullResponse = new StringBuilder();
        
        streamingChatModel.generate(fullPrompt, new StreamingResponseHandler<AiMessage>() {
            @Override
            public void onNext(String token) {
                fullResponse.append(token);
                onToken.accept(token);
            }
            
            @Override
            public void onComplete(Response<AiMessage> response) {
                String responseText = response.content().text();
                // Update conversation history
                history.add("Player: " + playerMessage);
                history.add(npc.getName() + ": " + responseText);
                
                // Trim history if too long
                if (history.size() > MAX_HISTORY_LENGTH * 2) {
                    history.subList(0, history.size() - MAX_HISTORY_LENGTH * 2).clear();
                }
                
                onComplete.run();
            }
            
            @Override
            public void onError(Throwable error) {
                System.err.println("Streaming dialogue failed: " + error.getMessage());
                String fallback = getFallbackDialogue(npc);
                onToken.accept(fallback);
                onComplete.run();
            }
        });
    }
    
    /**
     * Generate response options for the player
     */
    public List<String> generateResponseOptions(NPC npc, String npcLastMessage) {
        List<String> options = new ArrayList<>();
        
        if (chatModel == null) {
            options.add("Tell me more about yourself");
            options.add("Do you have any quests for me?");
            options.add("Farewell");
            return options;
        }
        
        try {
            String prompt = String.format(
                "You are helping a player in an RPG game respond to an NPC named %s. " +
                "The NPC just said: \"%s\"\n\n" +
                "Generate 3 short, distinct response options for the player (each 5-10 words max). " +
                "Return them as a simple numbered list (1., 2., 3.) with no extra text. " +
                "Make one friendly, one curious/questioning, and one to end/continue differently.",
                npc.getName(),
                npcLastMessage
            );
            
            String response = chatModel.generate(prompt);
            
            // Parse the response into individual options
            String[] lines = response.split("\n");
            for (String line : lines) {
                line = line.trim();
                // Remove numbering like "1.", "2.", etc.
                line = line.replaceFirst("^\\d+\\.\\s*", "");
                line = line.replaceFirst("^-\\s*", "");
                if (!line.isEmpty() && options.size() < 3) {
                    options.add(line);
                }
            }
            
            // Ensure we have exactly 3 options
            while (options.size() < 3) {
                if (options.size() == 0) options.add("Tell me more");
                else if (options.size() == 1) options.add("What can you help me with?");
                else options.add("I must go now");
            }
            
        } catch (Exception e) {
            System.err.println("Failed to generate response options: " + e.getMessage());
            options.clear();
            options.add("Tell me more about yourself");
            options.add("Do you have any quests for me?");
            options.add("Farewell");
        }
        
        return options.subList(0, Math.min(3, options.size()));
    }
}
