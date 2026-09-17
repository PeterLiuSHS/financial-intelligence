package com.finintel.financialanalytics.service;

import com.finintel.common.event.FinancialDataImportedEvent;
import com.finintel.financialanalytics.client.FinancialDataClient;
import com.finintel.financialanalytics.dto.FinancialStatementDto;
import com.finintel.financialanalytics.entity.FinancialMetric;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinancialAnalysisService {

    private final FinancialDataClient financialDataClient;
    private final FinancialMetricCalculationService calculationService;
    private final FinancialAnomalyDetectionService anomalyDetectionService;

    public FinancialAnalysisService(
            FinancialDataClient financialDataClient,
            FinancialMetricCalculationService calculationService,
            FinancialAnomalyDetectionService anomalyDetectionService
    ) {
        this.financialDataClient = financialDataClient;
        this.calculationService = calculationService;
        this.anomalyDetectionService = anomalyDetectionService;
    }

    public void analyzeImportedFinancialData(
            FinancialDataImportedEvent event
    ) {
        List<FinancialStatementDto> statements =
                financialDataClient.getFinancialStatements(event.ticker());

        List<FinancialMetric> metrics =
                calculationService.calculateAndSaveMetrics(statements);

        anomalyDetectionService.detectAndSaveFindings(metrics);

        System.out.println(
                "Calculated metrics and findings for "
                        + event.ticker()
                        + ". Metric records: "
                        + metrics.size()
        );
    }
}