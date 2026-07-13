package ru.tm.task.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tm.task.client.AiServiceClient;
import ru.tm.task.dto.*;
import ru.tm.task.entity.Task;
import ru.tm.task.entity.TaskStatus;
import ru.tm.task.exception.TaskNotFoundException;
import ru.tm.task.repository.TaskRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final AiServiceClient aiServiceClient;
    private final TaskMapper taskMapper;

    @Transactional
    public TaskResponse create(TaskCreateRequest request) {
        Task task = Task.builder()
                .title(request.title())
                .content(request.content())
                .status(TaskStatus.TODO)
                .build();

        Task saved = taskRepository.save(task);
        log.info("Task created id={}", saved.getId());
        return taskMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(Long id) {
        return taskMapper.toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getAll(TaskStatus status, Pageable pageable) {
        Page<Task> page = status != null
                ? taskRepository.findByStatus(status, pageable)
                : taskRepository.findAll(pageable);
        return page.map(taskMapper::toResponse);
    }

    @Transactional
    public TaskResponse update(Long id, TaskUpdateRequest request) {
        Task task = findEntity(id);
        task.setTitle(request.title());
        task.setContent(request.content());
        task.setStatus(request.status());
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }

    /**
     * Requests a short summary of the task content from ai-service and saves it.
     * Circuit breaker + retry protect against cascading failures if ai-service is unavailable;
     * when the circuit breaker is open, the fallback client is invoked (see AiServiceClientFallback).
     */
    @Transactional
    @CircuitBreaker(name = "aiService", fallbackMethod = "summarizeFallback")
    @Retry(name = "aiService")
    public TaskResponse summarize(Long id) {
        Task task = findEntity(id);
        SummarizeResponse response = aiServiceClient.summarize(new SummarizeRequest(task.getContent()));
        task.setAiSummary(response.summary());
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @SuppressWarnings("unused")
    private TaskResponse summarizeFallback(Long id, Throwable throwable) {
        log.warn("Summarization unavailable for task id={}: {}", id, throwable.getMessage());
        Task task = findEntity(id);
        task.setAiSummary("Summary temporarily unavailable, please try again later");
        return taskMapper.toResponse(taskRepository.save(task));
    }

    private Task findEntity(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    }
}
