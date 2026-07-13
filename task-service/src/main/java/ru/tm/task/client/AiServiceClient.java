package ru.tm.task.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.tm.task.dto.SummarizeRequest;
import ru.tm.task.dto.SummarizeResponse;

/**
 * Client for ai-service. Load balancing goes through Eureka (service name instead of host:port).
 * Fault tolerance is provided by Resilience4j (see AiServiceClientFallback + application.yml).
 */
@FeignClient(name = "ai-service", fallback = AiServiceClientFallback.class)
public interface AiServiceClient {

    @PostMapping("/api/ai/summarize")
    SummarizeResponse summarize(@RequestBody SummarizeRequest request);
}
