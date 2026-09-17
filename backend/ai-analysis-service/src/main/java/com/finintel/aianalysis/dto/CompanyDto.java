package com.finintel.aianalysis.dto;

import java.time.LocalDateTime;

public record CompanyDto(
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