package com.finintel.financialdata.dto;

import java.math.BigDecimal;

public record ExtractedFinancialStatement(
        Integer fiscalYear,

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
        BigDecimal capitalExpenditure
) {
}