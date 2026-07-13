package ru.tm.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import ru.tm.ai.controller.SummarizationController;
import ru.tm.ai.dto.SummarizeRequest;
import ru.tm.ai.service.SummarizationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test for the summarization service with a mocked ChatClient — no real Groq call is made,
 * which allows running the tests in CI without an API key or network access.
 */
@ExtendWith(MockitoExtension.class)
class SummarizationControllerTest {

    @Test
    void summarize_shouldReturnModelContent() {
        ChatClient.Builder builder = mock(ChatClient.Builder.class, RETURNS_DEEP_STUBS);
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);

        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);
        when(chatClient.prompt().user("source text").call().content())
                .thenReturn("Short summary of the text.");

        SummarizationService service = new SummarizationService(builder);
        SummarizationController controller = new SummarizationController(service);

        var response = controller.summarize(new SummarizeRequest("source text"));

        assertThat(response.summary()).isEqualTo("Short summary of the text.");
    }
}
