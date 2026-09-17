package com.finintel.financialdata.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCompanyRequest(

        @NotBlank(message = "Ticker is required")
        String ticker,

        @NotBlank(message = "Company name is required")
        String name,

        String cik,

        String exchange,

        String sector,

        String industry,

        String country
) {
}