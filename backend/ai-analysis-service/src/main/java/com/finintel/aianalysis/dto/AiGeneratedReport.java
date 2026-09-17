package com.finintel.aianalysis.dto;

public record AiGeneratedReport(
        String summary,
        String keyFindings,
        String riskAssessment,
        String suggestedQuestions,
        String provider,
        String model
) {
}