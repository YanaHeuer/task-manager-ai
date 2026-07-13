package ru.tm.task.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tm.task.dto.TaskCreateRequest;
import ru.tm.task.dto.TaskResponse;
import ru.tm.task.dto.TaskUpdateRequest;
import ru.tm.task.entity.TaskStatus;
import ru.tm.task.service.TaskService;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request));
    }

    @GetMapping("/{id}")
    public TaskResponse getById(@PathVariable Long id) {
        return taskService.getById(id);
    }

    @GetMapping
    public Page<TaskResponse> getAll(@RequestParam(required = false) TaskStatus status, Pageable pageable) {
        return taskService.getAll(status, pageable);
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskUpdateRequest request) {
        return taskService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        taskService.delete(id);
    }

    /**
     * Triggers AI summarization of the task content via ai-service.
     */
    @PostMapping("/{id}/summarize")
    public TaskResponse summarize(@PathVariable Long id) {
        return taskService.summarize(id);
    }
}
