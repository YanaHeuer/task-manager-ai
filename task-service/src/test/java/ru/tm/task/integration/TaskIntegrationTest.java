package ru.tm.task.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tm.task.client.AiServiceClient;
import ru.tm.task.dto.SummarizeResponse;
import ru.tm.task.dto.TaskCreateRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full integration test: spins up a real PostgreSQL in Docker (Testcontainers),
 * runs Flyway migrations, and verifies the whole request path through MockMvc.
 * ai-service is replaced with a mock bean so the test doesn't depend on an external API.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class TaskIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("taskdb_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("eureka.client.enabled", () -> "false");
        registry.add("spring.cloud.config.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiServiceClient aiServiceClient;

    @Test
    void fullLifecycle_createReadSummarize() throws Exception {
        when(aiServiceClient.summarize(any())).thenReturn(new SummarizeResponse("Final summary"));

        String body = objectMapper.writeValueAsString(new TaskCreateRequest("Integration test", "Task text"));

        String response = mockMvc.perform(post("/api/tasks").contentType("application/json").content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/tasks/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Integration test"));

        mockMvc.perform(post("/api/tasks/" + id + "/summarize"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiSummary").value("Final summary"));
    }
}
