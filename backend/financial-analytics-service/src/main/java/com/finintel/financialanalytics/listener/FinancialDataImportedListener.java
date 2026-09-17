package com.finintel.financialanalytics.listener;

import com.finintel.common.event.FinancialDataImportedEvent;
import com.finintel.financialanalytics.config.RabbitMqNames;
import com.finintel.financialanalytics.service.FinancialAnalysisService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class FinancialDataImportedListener {

    private final FinancialAnalysisService financialAnalysisService;

    public FinancialDataImportedListener(
            FinancialAnalysisService financialAnalysisService
    ) {
        this.financialAnalysisService = financialAnalysisService;
    }

    @RabbitListener(queues = RabbitMqNames.FINANCIAL_DATA_IMPORTED_QUEUE)
    public void handleFinancialDataImported(
            FinancialDataImportedEvent event
    ) {
        System.out.println("Received financial.data.imported event:");
        System.out.println("Ticker: " + event.ticker());
        System.out.println("Fiscal Years: " + event.fiscalYears());

        financialAnalysisService.analyzeImportedFinancialData(event);
    }
}