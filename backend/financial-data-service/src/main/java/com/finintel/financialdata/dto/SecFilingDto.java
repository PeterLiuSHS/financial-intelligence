package com.finintel.financialdata.dto;

public record SecFilingDto(
        String form,
        String filingDate,
        String accessionNumber,
        String primaryDocument
) {
}
