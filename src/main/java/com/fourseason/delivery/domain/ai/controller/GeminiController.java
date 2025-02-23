package com.fourseason.delivery.domain.ai.controller;

import com.fourseason.delivery.domain.ai.service.GeminiService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {
    private final GeminiService geminiService;

    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/recommend")
    public String recommendMenuDescription(@RequestBody String prompt) {
        return geminiService.getMenuRecommendation(prompt);
    }
}
