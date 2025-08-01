package com.steam.modeni.controller;

import com.steam.modeni.config.OpenAiConfig;
import com.steam.modeni.dto.ChatRequest;
import com.steam.modeni.dto.OpenAiResponse;
import com.steam.modeni.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/openai")
@RequiredArgsConstructor
public class OpenAiController {

    private final OpenAiService openAiService;
    private final OpenAiConfig openAiConfig;

    @GetMapping("/config")
    public ResponseEntity<?> checkConfig() {
        String apiKey = openAiConfig.getApiKey();
        boolean isConfigured = apiKey != null && !apiKey.equals("test-key-placeholder");
        
        return ResponseEntity.ok(Map.of(
            "apiKeyConfigured", isConfigured,
            "apiKeyLength", apiKey != null ? apiKey.length() : 0,
            "apiKeyPrefix", apiKey != null && apiKey.length() > 8 ? apiKey.substring(0, 8) + "..." : "N/A"
        ));
    }

    @PostMapping("/chat")
    public ResponseEntity<?> generateChatCompletion(@RequestBody ChatRequest request) {
        try {
            String response = openAiService.generateSimpleResponse(request.getPrompt());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.badRequest().body("AI 응답 생성에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/chat/detailed")
    public ResponseEntity<?> generateDetailedChatCompletion(@RequestBody ChatRequest request) {
        try {
            OpenAiResponse response = openAiService.generateChatCompletion(request.getPrompt());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.badRequest().body("AI 응답 생성에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/chat/simple")
    public ResponseEntity<?> generateSimpleChat(@RequestBody String prompt) {
        try {
            String response = openAiService.generateSimpleResponse(prompt);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.badRequest().body("AI 응답 생성에 실패했습니다: " + e.getMessage());
        }
    }
} 