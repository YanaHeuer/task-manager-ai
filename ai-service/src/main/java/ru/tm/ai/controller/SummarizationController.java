package ru.tm.ai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tm.ai.dto.SummarizeRequest;
import ru.tm.ai.dto.SummarizeResponse;
import ru.tm.ai.service.SummarizationService;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class SummarizationController {

    private final SummarizationService summarizationService;

    @PostMapping("/summarize")
    public SummarizeResponse summarize(@Valid @RequestBody SummarizeRequest request) {
        String summary = summarizationService.summarize(request.text());
        return new SummarizeResponse(summary);
    }
}
