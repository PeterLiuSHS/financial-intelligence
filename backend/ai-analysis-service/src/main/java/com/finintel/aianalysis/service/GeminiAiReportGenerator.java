package com.finintel.aianalysis.service;

import com.finintel.aianalysis.client.GeminiAiClient;
import com.finintel.aianalysis.config.AiProviderProperties;
import com.finintel.aianalysis.dto.AiContextPackage;
import com.finintel.aianalysis.dto.AiGeneratedReport;
import com.finintel.aianalysis.exception.AiProviderException;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class GeminiAiReportGenerator {

    private final GeminiAiClient geminiAiClient;
    private final AiFinancialPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;
    private final AiProviderProperties aiProviderProperties;

    public GeminiAiReportGenerator(
            GeminiAiClient geminiAiClient,
            AiFinancialPromptBuilder promptBuilder,
            ObjectMapper objectMapper,
            AiProviderProperties aiProviderProperties
    ) {
        this.geminiAiClient = geminiAiClient;
        this.promptBuilder = promptBuilder;
        this.objectMapper = objectMapper;
        this.aiProviderProperties = aiProviderProperties;
    }

    public AiGeneratedReport generateReport(AiContextPackage contextPackage) {
        String systemPrompt = promptBuilder.buildSystemPrompt();

        String userPrompt = promptBuilder.buildUserPrompt(contextPackage);

        String content = geminiAiClient.generateContent(
                systemPrompt,
                userPrompt
        );

        GeminiStructuredReport structuredReport =
                parseStructuredReport(content);

        return new AiGeneratedReport(
                structuredReport.summary(),
                structuredReport.keyFindings(),
                structuredReport.riskAssessment(),
                structuredReport.suggestedQuestions(),
                "gemini",
                aiProviderProperties.gemini().model()
        );
    }

    private GeminiStructuredReport parseStructuredReport(String content) {
        try {
            String cleanedContent = extractJsonObject(content);

            return objectMapper.readValue(
                    cleanedContent,
                    GeminiStructuredReport.class
            );

        } catch (JacksonException exception) {
            throw new AiProviderException(
                    "Failed to parse Gemini response as structured JSON.",
                    exception
            );
        }
    }

    private String extractJsonObject(String content) {
        if (content == null || content.isBlank()) {
            throw new AiProviderException("Gemini returned empty content.");
        }

        String cleaned = content.trim();

        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring("```json".length()).trim();
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring("```".length()).trim();
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - "```".length()).trim();
        }

        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');

        if (start == -1) {
            throw new AiProviderException(
                    "Gemini response does not contain a JSON object start."
            );
        }

        if (end == -1 || end <= start) {
            throw new AiProviderException(
                    "Gemini response appears truncated before completing a JSON object."
            );
        }

        return cleaned.substring(start, end + 1);
    }

    private record GeminiStructuredReport(
            String summary,
            String keyFindings,
            String riskAssessment,
            String suggestedQuestions
    ) {
    }
}