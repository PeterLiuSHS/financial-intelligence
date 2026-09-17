package com.finintel.financialdata.dto;

public record SecCompanyImportResponse(
        Long companyId,
        String ticker,
        String name,
        String cik,
        String message
) {
}