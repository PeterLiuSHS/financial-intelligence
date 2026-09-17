package com.finintel.financialanalytics.service;

import com.finintel.financialanalytics.dto.FinancialMetricResponse;
import com.finintel.financialanalytics.entity.FinancialMetric;
import com.finintel.financialanalytics.exception.ResourceNotFoundException;
import com.finintel.financialanalytics.repository.FinancialMetricRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FinancialMetricQueryService {

    private final FinancialMetricRepository financialMetricRepository;

    public FinancialMetricQueryService(
            FinancialMetricRepository financialMetricRepository
    ) {
        this.financialMetricRepository = financialMetricRepository;
    }

    @Transactional(readOnly = true)
    public List<FinancialMetricResponse> getMetricsByTicker(String ticker) {
        List<FinancialMetric> metrics =
                financialMetricRepository.findByTickerOrderByFiscalYearDesc(
                        ticker.toUpperCase()
                );

        if (metrics.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No financial metrics found for ticker: " + ticker.toUpperCase()
            );
        }

        return metrics.stream()
                .map(this::toResponse)
                .toList();
    }

    private FinancialMetricResponse toResponse(FinancialMetric metric) {
        return new FinancialMetricResponse(
                metric.getId(),
                metric.getCompanyId(),
                metric.getTicker(),
                metric.getFiscalYear(),
                metric.getPeriodType(),

                metric.getGrossMargin(),
                metric.getOperatingMargin(),
                metric.getNetMargin(),
                metric.getCurrentRatio(),
                metric.getDebtToAssets(),
                metric.getCashFlowMargin(),

                metric.getRevenueGrowth(),
                metric.getNetIncomeGrowth(),
                metric.getInventoryGrowth(),
                metric.getAccountsReceivableGrowth(),

                metric.getCreatedAt(),
                metric.getUpdatedAt()
        );
    }
}