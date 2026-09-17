package com.finintel.aianalysis.client;

import com.finintel.aianalysis.config.FinancialAnalyticsServiceProperties;
import com.finintel.aianalysis.dto.FinancialFindingDto;
import com.finintel.aianalysis.dto.FinancialMetricDto;
import com.finintel.aianalysis.exception.ExternalServiceException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class FinancialAnalyticsClient {

    private final RestClient restClient;
    private final FinancialAnalyticsServiceProperties properties;

    public FinancialAnalyticsClient(
            RestClient restClient,
            FinancialAnalyticsServiceProperties properties
    ) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public List<FinancialMetricDto> getMetrics(String ticker) {
        try {
            return restClient.get()
                    .uri(properties.baseUrl() + "/api/analytics/companies/{ticker}/metrics", ticker)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (RestClientException exception) {
            throw new ExternalServiceException(
                    "Failed to fetch financial metrics from financial-analytics-service for ticker: "
                            + ticker.toUpperCase(),
                    exception
            );
        }
    }

    public List<FinancialFindingDto> getFindings(String ticker) {
        try {
            return restClient.get()
                    .uri(properties.baseUrl() + "/api/analytics/companies/{ticker}/findings", ticker)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (RestClientException exception) {
            throw new ExternalServiceException(
                    "Failed to fetch financial findings from financial-analytics-service for ticker: "
                            + ticker.toUpperCase(),
                    exception
            );
        }
    }
}