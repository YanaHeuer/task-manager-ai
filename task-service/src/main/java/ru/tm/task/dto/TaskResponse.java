package ru.tm.task.dto;

import ru.tm.task.entity.TaskStatus;

import java.time.Instant;

public record TaskResponse(
        Long id,
        String title,
        String content,
        String aiSummary,
        TaskStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
