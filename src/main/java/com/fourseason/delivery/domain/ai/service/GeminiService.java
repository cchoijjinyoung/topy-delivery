package com.fourseason.delivery.domain.ai.service;

import com.fourseason.delivery.domain.ai.entity.AiResponse;
import com.fourseason.delivery.domain.ai.repository.AiResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final AiResponseRepository aiResponseRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=";

    public String getMenuRecommendation(String prompt) {
        String url = GEMINI_URL + apiKey;
        prompt += " 답변을 최대한 간결하게 50자 이하로.";

        // 요청 Body
        Map<String, Object> requestBody = Map.of(
            "contents", new Object[]{
                Map.of("parts", new Object[]{Map.of("text", prompt)})
            }
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

        AiResponse aiResponse = AiResponse.addOf(extractResponseText(response.getBody()));
        aiResponseRepository.save(aiResponse);

        return aiResponse.getResponse();
    }

    private String extractResponseText(Map responseBody) {
        if (responseBody != null && responseBody.containsKey("candidates")) {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            return candidates.get(0).get("content").toString();
        }
        return "추천 결과를 가져올 수 없습니다.";
    }
}
