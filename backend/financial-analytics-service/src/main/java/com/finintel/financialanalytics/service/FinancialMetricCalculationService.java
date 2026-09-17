package com.finintel.financialanalytics.service;

import com.finintel.financialanalytics.dto.FinancialStatementDto;
import com.finintel.financialanalytics.entity.FinancialMetric;
import com.finintel.financialanalytics.repository.FinancialMetricRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class FinancialMetricCalculationService {

    private static final int SCALE = 6;

    private final FinancialMetricRepository financialMetricRepository;

    public FinancialMetricCalculationService(
            FinancialMetricRepository financialMetricRepository
    ) {
        this.financialMetricRepository = financialMetricRepository;
    }

    @Transactional
    public List<FinancialMetric> calculateAndSaveMetrics(
            List<FinancialStatementDto> statements
    ) {
        List<FinancialStatementDto> sortedStatements = statements.stream()
                .sorted(Comparator.comparing(FinancialStatementDto::fiscalYear))
                .toList();

        return sortedStatements.stream()
                .map(statement -> {
                    FinancialStatementDto previous = findPreviousStatement(
                            sortedStatements,
                            statement.fiscalYear()
                    );

                    return calculateAndSaveMetric(statement, previous);
                })
                .toList();
    }

    private FinancialMetric calculateAndSaveMetric(
            FinancialStatementDto statement,
            FinancialStatementDto previous
    ) {
        FinancialMetric metric = financialMetricRepository
                .findByCompanyIdAndFiscalYearAndPeriodType(
                        statement.companyId(),
                        statement.fiscalYear(),
                        statement.periodType()
                )
                .orElseGet(() -> FinancialMetric.builder()
                        .companyId(statement.companyId())
                        .ticker(statement.ticker())
                        .fiscalYear(statement.fiscalYear())
                        .periodType(statement.periodType())
                        .build());

        metric.setGrossMargin(divide(statement.grossProfit(), statement.revenue()));
        metric.setOperatingMargin(divide(statement.operatingIncome(), statement.revenue()));
        metric.setNetMargin(divide(statement.netIncome(), statement.revenue()));
        metric.setCurrentRatio(divide(statement.currentAssets(), statement.currentLiabilities()));
        metric.setDebtToAssets(divide(statement.totalDebt(), statement.totalAssets()));
        metric.setCashFlowMargin(divide(statement.operatingCashFlow(), statement.revenue()));

        if (previous != null) {
            metric.setRevenueGrowth(growth(statement.revenue(), previous.revenue()));
            metric.setNetIncomeGrowth(growth(statement.netIncome(), previous.netIncome()));
            metric.setInventoryGrowth(growth(statement.inventory(), previous.inventory()));
            metric.setAccountsReceivableGrowth(
                    growth(statement.accountsReceivable(), previous.accountsReceivable())
            );
        }

        return financialMetricRepository.save(metric);
    }

    private FinancialStatementDto findPreviousStatement(
            List<FinancialStatementDto> statements,
            Integer fiscalYear
    ) {
        return statements.stream()
                .filter(statement -> statement.fiscalYear().equals(fiscalYear - 1))
                .findFirst()
                .orElse(null);
    }

    private BigDecimal growth(BigDecimal current, BigDecimal previous) {
        if (current == null || previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return current.subtract(previous)
                .divide(previous, SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal divide(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return numerator.divide(denominator, SCALE, RoundingMode.HALF_UP);
    }
}