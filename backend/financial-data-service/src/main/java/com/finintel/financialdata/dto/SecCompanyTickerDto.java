package com.finintel.financialdata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SecCompanyTickerDto(
        @JsonProperty("cik_str")
        Integer cikStr,

        String ticker,

        String title
) {
}