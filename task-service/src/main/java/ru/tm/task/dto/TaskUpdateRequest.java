package ru.tm.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.tm.task.entity.TaskStatus;

public record TaskUpdateRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        @NotBlank(message = "Content must not be blank")
        String content,

        @NotNull(message = "Status is required")
        TaskStatus status
) {
}
