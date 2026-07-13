package ru.tm.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import ru.tm.task.dto.TaskCreateRequest;
import ru.tm.task.dto.TaskResponse;
import ru.tm.task.entity.TaskStatus;
import ru.tm.task.service.TaskService;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @Test
    void createTask_shouldReturn201() throws Exception {
        TaskResponse response = new TaskResponse(1L, "Title", "Text", null,
                TaskStatus.TODO, Instant.now(), Instant.now());
        when(taskService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/tasks")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new TaskCreateRequest("Title", "Text"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void createTask_shouldReturn400WhenTitleBlank() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new TaskCreateRequest("", "Text"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.title").exists());
    }

    @Test
    void getById_shouldReturn404WhenMissing() throws Exception {
        when(taskService.getById(42L)).thenThrow(new ru.tm.task.exception.TaskNotFoundException(42L));

        mockMvc.perform(get("/api/tasks/42"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_shouldReturnPage() throws Exception {
        TaskResponse t = new TaskResponse(1L, "A", "B", null, TaskStatus.TODO, Instant.now(), Instant.now());
        when(taskService.getAll(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(t)));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }
}
