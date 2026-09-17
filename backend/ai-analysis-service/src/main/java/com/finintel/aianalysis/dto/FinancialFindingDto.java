package com.finintel.aianalysis.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FinancialFindingDto(
        Long id,
        Long metricId,
        Long companyId,
        String ticker,
        Integer fiscalYear,
        String periodType,
        String findingCode,
        String severity,
        BigDecimal observedValue,
        BigDecimal thresholdValue,
        String message,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}