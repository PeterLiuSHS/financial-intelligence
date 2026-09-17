package com.finintel.aianalysis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services.financial-analytics")
public record FinancialAnalyticsServiceProperties(
        String baseUrl
) {
}