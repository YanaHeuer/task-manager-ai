package ru.tm.task.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tm.task.dto.SummarizeRequest;
import ru.tm.task.dto.SummarizeResponse;

/**
 * If ai-service is unavailable (timeout, 5xx, open circuit breaker),
 * return a stub instead of failing the user's request.
 */
@Slf4j
@Component
public class AiServiceClientFallback implements AiServiceClient {

    @Override
    public SummarizeResponse summarize(SummarizeRequest request) {
        log.warn("ai-service is unavailable, returning fallback summary");
        return new SummarizeResponse("Summary temporarily unavailable (AI service is not responding)");
    }
}
