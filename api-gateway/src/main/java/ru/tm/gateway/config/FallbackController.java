package ru.tm.gateway.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Returns a clear response when a service is unavailable and the circuit breaker
 * has tripped, instead of a raw connection error.
 *
 * Mapped with @RequestMapping (no method restriction) rather than @GetMapping:
 * Spring Cloud Gateway's forward: filter preserves the original HTTP method, so a
 * failed POST/PUT/DELETE would otherwise hit these endpoints and get 405 instead
 * of the intended fallback response.
 */
@RestController
public class FallbackController {

    @RequestMapping("/fallback/tasks")
    public ResponseEntity<Map<String, String>> taskServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "task-service is temporarily unavailable, please try again later"));
    }

    @RequestMapping("/fallback/ai")
    public ResponseEntity<Map<String, String>> aiServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "ai-service is temporarily unavailable, summarization will be performed later"));
    }
}
