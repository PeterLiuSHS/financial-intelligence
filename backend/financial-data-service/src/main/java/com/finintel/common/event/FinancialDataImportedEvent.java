package com.finintel.common.event;

import java.util.List;

public record FinancialDataImportedEvent(
        Long companyId,
        String ticker,
        String periodType,
        List<Integer> fiscalYears
) {
}