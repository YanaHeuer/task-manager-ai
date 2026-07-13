package ru.tm.task.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tm.task.client.AiServiceClient;
import ru.tm.task.dto.*;
import ru.tm.task.entity.Task;
import ru.tm.task.entity.TaskStatus;
import ru.tm.task.exception.TaskNotFoundException;
import ru.tm.task.repository.TaskRepository;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private AiServiceClient aiServiceClient;

    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private Task existingTask;

    @BeforeEach
    void setUp() {
        // MapStruct mappers are normally generated, but for a unit test without a Spring
        // context we use a simple manual implementation of the interface.
        taskMapper = task -> new TaskResponse(
                task.getId(), task.getTitle(), task.getContent(), task.getAiSummary(),
                task.getStatus(), task.getCreatedAt(), task.getUpdatedAt());
        taskService = new TaskService(taskRepository, aiServiceClient, taskMapper);

        existingTask = Task.builder()
                .id(1L)
                .title("Note")
                .content("A long note text for summarization")
                .status(TaskStatus.TODO)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void create_shouldSaveTaskWithTodoStatus() {
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setId(1L);
            t.setCreatedAt(Instant.now());
            t.setUpdatedAt(Instant.now());
            return t;
        });

        TaskResponse response = taskService.create(new TaskCreateRequest("Title", "Content"));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(TaskStatus.TODO);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getById(99L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void summarize_shouldStoreAiSummary() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(aiServiceClient.summarize(any(SummarizeRequest.class)))
                .thenReturn(new SummarizeResponse("Short summary"));

        TaskResponse response = taskService.summarize(1L);

        assertThat(response.aiSummary()).isEqualTo("Short summary");
        verify(aiServiceClient, times(1)).summarize(any(SummarizeRequest.class));
    }

    @Test
    void delete_shouldThrowWhenTaskMissing() {
        when(taskRepository.existsById(5L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.delete(5L))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository, never()).deleteById(any());
    }
}
