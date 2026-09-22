package com.finintel.financialdata.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sec.api")
public record SecApiProperties(
        String companyTickersUrl,
        String companyFactsUrlTemplate,
        String submissionsUrlTemplate,
        String filingDocumentUrlTemplate,
        String userAgent
) {
}