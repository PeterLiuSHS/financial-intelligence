package com.finintel.aianalysis.client;

import com.finintel.aianalysis.config.FinancialDataServiceProperties;
import com.finintel.aianalysis.dto.CompanyDto;
import com.finintel.aianalysis.exception.ExternalServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class FinancialDataClient {

    private final RestClient restClient;
    private final FinancialDataServiceProperties properties;

    public FinancialDataClient(
            RestClient restClient,
            FinancialDataServiceProperties properties
    ) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public CompanyDto getCompany(String ticker) {
        try {
            return restClient.get()
                    .uri(properties.baseUrl() + "/api/companies/{ticker}", ticker)
                    .retrieve()
                    .body(CompanyDto.class);
        } catch (RestClientException exception) {
            throw new ExternalServiceException(
                    "Failed to fetch company information from financial-data-service for ticker: "
                            + ticker.toUpperCase(),
                    exception
            );
        }
    }
}