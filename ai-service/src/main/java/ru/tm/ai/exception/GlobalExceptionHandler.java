package ru.tm.ai.exception;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(body("Invalid request: text must not be blank", HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<Map<String, Object>> handleRateLimit(RequestNotPermitted ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(body("AI request rate limit exceeded, please try again shortly", HttpStatus.TOO_MANY_REQUESTS));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(body("Error calling AI provider: " + ex.getMessage(), HttpStatus.BAD_GATEWAY));
    }

    private Map<String, Object> body(String message, HttpStatus status) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("timestamp", Instant.now().toString());
        map.put("status", status.value());
        map.put("message", message);
        return map;
    }
}
