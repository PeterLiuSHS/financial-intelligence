package com.finintel.aianalysis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services.financial-data")
public record FinancialDataServiceProperties(
        String baseUrl
) {
}