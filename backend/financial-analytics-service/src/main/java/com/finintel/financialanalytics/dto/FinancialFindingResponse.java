package com.finintel.financialanalytics.dto;

import com.finintel.financialanalytics.entity.FindingSeverity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FinancialFindingResponse(
        Long id,
        Long metricId,
        Long companyId,
        String ticker,
        Integer fiscalYear,
        String periodType,
        String findingCode,
        FindingSeverity severity,
        BigDecimal observedValue,
        BigDecimal thresholdValue,
        String message,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}