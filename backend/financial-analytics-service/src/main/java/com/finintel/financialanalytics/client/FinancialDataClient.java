package com.finintel.financialanalytics.client;

import com.finintel.financialanalytics.config.FinancialDataServiceProperties;
import com.finintel.financialanalytics.dto.FinancialStatementDto;
import com.finintel.financialanalytics.exception.ExternalServiceException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

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

    public List<FinancialStatementDto> getFinancialStatements(String ticker) {
        try {
            return restClient.get()
                    .uri(properties.baseUrl() + "/api/companies/{ticker}/financial-statements", ticker)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (RestClientException exception) {
            throw new ExternalServiceException(
                    "Failed to fetch financial statements from financial-data-service for ticker: "
                            + ticker,
                    exception
            );
        }
    }
}