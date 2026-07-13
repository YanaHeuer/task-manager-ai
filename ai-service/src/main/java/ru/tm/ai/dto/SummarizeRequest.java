package ru.tm.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record SummarizeRequest(
        @NotBlank(message = "Text to summarize is required")
        String text
) {
}
