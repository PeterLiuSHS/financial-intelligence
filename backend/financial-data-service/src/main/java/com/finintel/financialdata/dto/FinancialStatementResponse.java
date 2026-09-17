package com.finintel.financialdata.dto;

import com.finintel.financialdata.entity.PeriodType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FinancialStatementResponse(
        Long id,
        Long companyId,
        String ticker,
        String companyName,
        Integer fiscalYear,
        PeriodType periodType,

        BigDecimal revenue,
        BigDecimal grossProfit,
        BigDecimal operatingIncome,
        BigDecimal netIncome,

        BigDecimal totalAssets,
        BigDecimal totalLiabilities,
        BigDecimal totalDebt,
        BigDecimal cashAndCashEquivalents,
        BigDecimal inventory,
        BigDecimal accountsReceivable,
        BigDecimal currentAssets,
        BigDecimal currentLiabilities,

        BigDecimal operatingCashFlow,
        BigDecimal capitalExpenditure,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}