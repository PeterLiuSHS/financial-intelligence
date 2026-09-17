package com.finintel.financialdata.dto;

import java.time.LocalDateTime;

public record CompanyResponse(
        Long id,
        String ticker,
        String name,
        String cik,
        String exchange,
        String sector,
        String industry,
        String country,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}