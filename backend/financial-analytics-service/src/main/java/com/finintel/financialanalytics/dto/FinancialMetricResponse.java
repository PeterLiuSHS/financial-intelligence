package com.finintel.financialanalytics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FinancialMetricResponse(
        Long id,
        Long companyId,
        String ticker,
        Integer fiscalYear,
        String periodType,

        BigDecimal grossMargin,
        BigDecimal operatingMargin,
        BigDecimal netMargin,
        BigDecimal currentRatio,
        BigDecimal debtToAssets,
        BigDecimal cashFlowMargin,

        BigDecimal revenueGrowth,
        BigDecimal netIncomeGrowth,
        BigDecimal inventoryGrowth,
        BigDecimal accountsReceivableGrowth,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
