package ru.tm.ai.service;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Wrapper around the Spring AI ChatClient (Groq, via its OpenAI-compatible API). Builds a
 * system prompt that constrains the model to a summarizer role and returns a short summary
 * of the text.
 */
@Slf4j
@Service
public class SummarizationService {

    private static final String SYSTEM_PROMPT = """
            You are an assistant that concisely and accurately summarizes the user's notes and tasks.
            Rules:
            - Respond in English.
            - No more than 3 sentences.
            - Keep only the key facts and the next step (action item), if any.
            - Do not add anything from yourself, do not invent facts.
            """;

    private final ChatClient chatClient;

    public SummarizationService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    @RateLimiter(name = "groqSummarize")
    public String summarize(String text) {
        log.debug("Summarization request, text length={}", text.length());
        String result = chatClient.prompt()
                .user(text)
                .call()
                .content();
        log.debug("Received summary from the model");
        return result;
    }
}
