package com.steam.modeni.service;

import com.steam.modeni.config.OpenAiConfig;
import com.steam.modeni.dto.OpenAiRequest;
import com.steam.modeni.dto.OpenAiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final RestTemplate restTemplate;
    private final OpenAiConfig openAiConfig;

    @Value("${openai.api.url}")
    private String openaiApiUrl;

    public OpenAiResponse generateChatCompletion(String prompt) {
        try {
            OpenAiRequest request = new OpenAiRequest();
            request.setModel("gpt-3.5-turbo");
            request.setMessages(List.of(
                    new OpenAiRequest.Message("user", prompt)
            ));
            request.setTemperature(0.7);
            request.setMaxTokens(1000);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + openAiConfig.getApiKey());
            headers.set("Content-Type", "application/json");

            HttpEntity<OpenAiRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<OpenAiResponse> response = restTemplate.exchange(
                    openaiApiUrl,
                    HttpMethod.POST,
                    entity,
                    OpenAiResponse.class
            );

            log.info("OpenAI API 호출 성공: {}", response.getBody().getId());
            return response.getBody();
        } catch (Exception e) {
            log.error("OpenAI API 호출 실패: {}", e.getMessage());
            throw new RuntimeException("OpenAI API 호출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public String generateSimpleResponse(String prompt) {
        try {
            OpenAiResponse response = generateChatCompletion(prompt);
            if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            }
            return "응답을 생성할 수 없습니다.";
        } catch (Exception e) {
            log.error("응답 생성 실패: {}", e.getMessage());
            return "AI 응답 생성에 실패했습니다: " + e.getMessage();
        }
    }
} 