package com.jmonkeyvibe.game.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.time.Duration;

/**
 * Factory for creating AI chat models based on environment configuration
 * Supports both OpenAI API and local Ollama models
 */
public class AIModelFactory {
    
    private static final String AI_PROVIDER = System.getenv().getOrDefault("AI_PROVIDER", "OLLAMA");
    private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String OPENAI_BASE_URL = System.getenv("OPENAI_BASE_URL");
    private static final String OLLAMA_BASE_URL = System.getenv().getOrDefault("OLLAMA_BASE_URL", "http://localhost:11434");
    private static final String OLLAMA_MODEL = System.getenv().getOrDefault("OLLAMA_MODEL", "llama3.1");
    
    /**
     * Create a chat model based on the AI_PROVIDER environment variable
     * @return ChatLanguageModel instance (OpenAI or Ollama)
     */
    public static ChatLanguageModel createChatModel() {
        System.out.println("Initializing AI model with provider: " + AI_PROVIDER);
        
        return switch (AI_PROVIDER.toUpperCase()) {
            case "OPENAI" -> createOpenAIModel();
            case "OLLAMA" -> createOllamaModel();
            default -> {
                System.out.println("Unknown AI provider '" + AI_PROVIDER + "', defaulting to Ollama");
                yield createOllamaModel();
            }
        };
    }
    
    /**
     * Create an OpenAI chat model
     */
    private static ChatLanguageModel createOpenAIModel() {
        if (OPENAI_API_KEY == null || OPENAI_API_KEY.isEmpty()) {
            throw new IllegalStateException(
                "AI_PROVIDER is set to OPENAI but OPENAI_API_KEY environment variable is not set. " +
                "Either set OPENAI_API_KEY or change AI_PROVIDER to OLLAMA."
            );
        }
        
        var builder = OpenAiChatModel.builder()
            .apiKey(OPENAI_API_KEY)
            .modelName("gpt-4o-mini") // Using GPT-4o-mini for cost efficiency
            .temperature(0.7)
            .timeout(Duration.ofSeconds(30))
            .logRequests(false)
            .logResponses(false);
        
        if (OPENAI_BASE_URL != null && !OPENAI_BASE_URL.isEmpty()) {
            builder.baseUrl(OPENAI_BASE_URL);
        }
        
        System.out.println("Using OpenAI model: gpt-4o-mini");
        return builder.build();
    }
    
    /**
     * Create an Ollama chat model (local, free)
     */
    private static ChatLanguageModel createOllamaModel() {
        System.out.println("Using Ollama model: " + OLLAMA_MODEL + " at " + OLLAMA_BASE_URL);
        System.out.println("Make sure Ollama is running locally: ollama serve");
        System.out.println("Pull the model if needed: ollama pull " + OLLAMA_MODEL);
        
        return OllamaChatModel.builder()
            .baseUrl(OLLAMA_BASE_URL)
            .modelName(OLLAMA_MODEL)
            .temperature(0.7)
            .timeout(Duration.ofSeconds(60))
            .build();
    }
    
    /**
     * Test if the AI model is accessible
     */
    public static boolean testConnection() {
        try {
            ChatLanguageModel model = createChatModel();
            String response = model.generate("Hello");
            return response != null && !response.isEmpty();
        } catch (Exception e) {
            System.err.println("AI model connection test failed: " + e.getMessage());
            return false;
        }
    }
}
