package com.finintel.aianalysis.client;

import com.finintel.aianalysis.config.AiProviderProperties;
import com.finintel.aianalysis.dto.GeminiGenerateContentResponse;
import com.finintel.aianalysis.exception.AiProviderException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class GeminiAiClient {

    private final RestClient restClient;
    private final AiProviderProperties aiProviderProperties;
    private final ObjectMapper objectMapper;

    public GeminiAiClient(
            RestClient restClient,
            AiProviderProperties aiProviderProperties,
            ObjectMapper objectMapper
    ) {
        this.restClient = restClient;
        this.aiProviderProperties = aiProviderProperties;
        this.objectMapper = objectMapper;
    }

    public String generateContent(String systemPrompt, String userPrompt) {
        AiProviderProperties.Gemini gemini =
                aiProviderProperties.gemini();

        if (gemini == null
                || gemini.apiKey() == null
                || gemini.apiKey().isBlank()) {
            throw new AiProviderException(
                    "Gemini API key is missing. Please set GEMINI_API_KEY."
            );
        }

        Map<String, Object> systemInstruction = new LinkedHashMap<>();
        systemInstruction.put(
                "parts",
                List.of(Map.of("text", systemPrompt))
        );

        Map<String, Object> userContent = new LinkedHashMap<>();
        userContent.put("role", "user");
        userContent.put(
                "parts",
                List.of(Map.of("text", userPrompt))
        );

        Map<String, Object> thinkingConfig = new LinkedHashMap<>();
        thinkingConfig.put("thinkingBudget", 256);

        Map<String, Object> generationConfig = new LinkedHashMap<>();
        generationConfig.put("temperature", 0.2);
        generationConfig.put("maxOutputTokens", 4096);
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.put("thinkingConfig", thinkingConfig);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("systemInstruction", systemInstruction);
        requestBody.put("contents", List.of(userContent));
        requestBody.put("generationConfig", generationConfig);

        try {
            GeminiGenerateContentResponse response = restClient.post()
                    .uri(gemini.baseUrl()
                            + "/models/"
                            + gemini.model()
                            + ":generateContent")
                    .header("x-goog-api-key", gemini.apiKey())
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(requestBody)
                    .exchange((request, clientResponse) -> {
                        String rawResponse = StreamUtils.copyToString(
                                clientResponse.getBody(),
                                StandardCharsets.UTF_8
                        );

                        System.out.println("========== Gemini Raw HTTP Response ==========");
                        System.out.println("Status: " + clientResponse.getStatusCode());
                        System.out.println("Model: " + gemini.model());
                        System.out.println("Raw body:");
                        System.out.println(rawResponse);
                        System.out.println("==============================================");

                        if (clientResponse.getStatusCode().isError()) {
                            throw new AiProviderException(
                                    "Gemini API returned error status "
                                            + clientResponse.getStatusCode()
                                            + ". Raw response: "
                                            + rawResponse
                            );
                        }

                        if (rawResponse == null || rawResponse.isBlank()) {
                            throw new AiProviderException(
                                    "Gemini API returned an empty raw response body."
                            );
                        }

                        try {
                            return objectMapper.readValue(
                                    rawResponse,
                                    GeminiGenerateContentResponse.class
                            );
                        } catch (JacksonException exception) {
                            throw new AiProviderException(
                                    "Failed to parse Gemini raw API response.",
                                    exception
                            );
                        }
                    });

            String content = extractText(response);

            System.out.println("========== Gemini Extracted Message Content ==========");
            System.out.println("Content length: " + content.length());
            System.out.println(content);
            System.out.println("======================================================");

            return content;

        } catch (RestClientException exception) {
            throw new AiProviderException(
                    "Failed to call Gemini generateContent API.",
                    exception
            );
        }
    }

    private String extractText(GeminiGenerateContentResponse response) {
        if (response == null
                || response.candidates() == null
                || response.candidates().isEmpty()
                || response.candidates().get(0).content() == null
                || response.candidates().get(0).content().parts() == null
                || response.candidates().get(0).content().parts().isEmpty()
                || response.candidates().get(0).content().parts().get(0).text() == null
                || response.candidates().get(0).content().parts().get(0).text().isBlank()) {
            throw new AiProviderException(
                    "Gemini returned an empty message content."
            );
        }

        return response.candidates().get(0).content().parts().get(0).text();
    }
}