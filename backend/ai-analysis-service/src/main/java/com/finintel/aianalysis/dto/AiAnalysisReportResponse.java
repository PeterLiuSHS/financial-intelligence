package com.finintel.aianalysis.dto;

import java.time.LocalDateTime;

public record AiAnalysisReportResponse(
        Long id,
        String ticker,
        String companyName,
        String reportType,
        String summary,
        String keyFindings,
        String riskAssessment,
        String suggestedQuestions,
        String provider,
        String model,
        Boolean validationPassed,
        String validationStatus,
        String validationViolations,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}