package com.finintel.financialanalytics.service;

import com.finintel.financialanalytics.entity.FinancialFinding;
import com.finintel.financialanalytics.entity.FinancialMetric;
import com.finintel.financialanalytics.entity.FindingSeverity;
import com.finintel.financialanalytics.repository.FinancialFindingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FinancialAnomalyDetectionService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private static final BigDecimal REVENUE_DECLINE_THRESHOLD =
            new BigDecimal("-0.02");

    private static final BigDecimal SHARP_REVENUE_DECLINE_THRESHOLD =
            new BigDecimal("-0.10");

    private static final BigDecimal LOW_CURRENT_RATIO_THRESHOLD =
            new BigDecimal("1.00");

    private static final BigDecimal INVENTORY_GROWTH_THRESHOLD =
            new BigDecimal("0.15");

    private static final BigDecimal RECEIVABLES_GROWTH_THRESHOLD =
            new BigDecimal("0.15");

    private static final BigDecimal CASH_FLOW_MARGIN_WEAK_THRESHOLD =
            new BigDecimal("0.10");

    private static final BigDecimal NET_MARGIN_DROP_THRESHOLD =
            new BigDecimal("-0.03");

    private final FinancialFindingRepository financialFindingRepository;

    public FinancialAnomalyDetectionService(
            FinancialFindingRepository financialFindingRepository
    ) {
        this.financialFindingRepository = financialFindingRepository;
    }

    @Transactional
    public void detectAndSaveFindings(List<FinancialMetric> metrics) {
        for (FinancialMetric metric : metrics) {
            detectForMetric(metric);
        }
    }

    private void detectForMetric(FinancialMetric metric) {
        detectRevenueDecline(metric);
        detectLowCurrentRatio(metric);
        detectInventoryBuildUp(metric);
        detectReceivablesBuildUp(metric);
        detectWeakCashFlowMargin(metric);
        detectEarningsCashFlowDivergence(metric);
    }

    private void detectRevenueDecline(FinancialMetric metric) {
        BigDecimal revenueGrowth = metric.getRevenueGrowth();

        if (revenueGrowth == null) {
            return;
        }

        if (revenueGrowth.compareTo(SHARP_REVENUE_DECLINE_THRESHOLD) <= 0) {
            saveOrUpdateFinding(
                    metric,
                    "SHARP_REVENUE_DECLINE",
                    FindingSeverity.HIGH,
                    revenueGrowth,
                    SHARP_REVENUE_DECLINE_THRESHOLD,
                    "Revenue declined by more than 10% compared with the previous fiscal year."
            );
            return;
        }

        if (revenueGrowth.compareTo(REVENUE_DECLINE_THRESHOLD) <= 0) {
            saveOrUpdateFinding(
                    metric,
                    "REVENUE_DECLINE",
                    FindingSeverity.MEDIUM,
                    revenueGrowth,
                    REVENUE_DECLINE_THRESHOLD,
                    "Revenue declined compared with the previous fiscal year."
            );
        }
    }

    private void detectLowCurrentRatio(FinancialMetric metric) {
        BigDecimal currentRatio = metric.getCurrentRatio();

        if (currentRatio == null) {
            return;
        }

        if (currentRatio.compareTo(LOW_CURRENT_RATIO_THRESHOLD) < 0) {
            saveOrUpdateFinding(
                    metric,
                    "LOW_CURRENT_RATIO",
                    FindingSeverity.MEDIUM,
                    currentRatio,
                    LOW_CURRENT_RATIO_THRESHOLD,
                    "Current ratio is below 1.0, which may indicate short-term liquidity pressure."
            );
        }
    }

    private void detectInventoryBuildUp(FinancialMetric metric) {
        BigDecimal inventoryGrowth = metric.getInventoryGrowth();
        BigDecimal revenueGrowth = metric.getRevenueGrowth();

        if (inventoryGrowth == null) {
            return;
        }

        boolean inventoryIncreasedFast =
                inventoryGrowth.compareTo(INVENTORY_GROWTH_THRESHOLD) >= 0;

        boolean revenueNotGrowingMuch =
                revenueGrowth == null || revenueGrowth.compareTo(new BigDecimal("0.05")) <= 0;

        if (inventoryIncreasedFast && revenueNotGrowingMuch) {
            saveOrUpdateFinding(
                    metric,
                    "INVENTORY_BUILDUP",
                    FindingSeverity.MEDIUM,
                    inventoryGrowth,
                    INVENTORY_GROWTH_THRESHOLD,
                    "Inventory grew materially while revenue did not grow at a similar pace."
            );
        }
    }

    private void detectReceivablesBuildUp(FinancialMetric metric) {
        BigDecimal receivablesGrowth = metric.getAccountsReceivableGrowth();
        BigDecimal revenueGrowth = metric.getRevenueGrowth();

        if (receivablesGrowth == null) {
            return;
        }

        boolean receivablesIncreasedFast =
                receivablesGrowth.compareTo(RECEIVABLES_GROWTH_THRESHOLD) >= 0;

        boolean revenueNotGrowingMuch =
                revenueGrowth == null || revenueGrowth.compareTo(new BigDecimal("0.05")) <= 0;

        if (receivablesIncreasedFast && revenueNotGrowingMuch) {
            saveOrUpdateFinding(
                    metric,
                    "RECEIVABLES_BUILDUP",
                    FindingSeverity.MEDIUM,
                    receivablesGrowth,
                    RECEIVABLES_GROWTH_THRESHOLD,
                    "Accounts receivable grew materially while revenue did not grow at a similar pace."
            );
        }
    }

    private void detectWeakCashFlowMargin(FinancialMetric metric) {
        BigDecimal cashFlowMargin = metric.getCashFlowMargin();

        if (cashFlowMargin == null) {
            return;
        }

        if (cashFlowMargin.compareTo(CASH_FLOW_MARGIN_WEAK_THRESHOLD) < 0) {
            saveOrUpdateFinding(
                    metric,
                    "WEAK_CASH_FLOW_MARGIN",
                    FindingSeverity.MEDIUM,
                    cashFlowMargin,
                    CASH_FLOW_MARGIN_WEAK_THRESHOLD,
                    "Operating cash flow margin is relatively weak compared with revenue."
            );
        }
    }

    private void detectEarningsCashFlowDivergence(FinancialMetric metric) {
        BigDecimal netMargin = metric.getNetMargin();
        BigDecimal cashFlowMargin = metric.getCashFlowMargin();

        if (netMargin == null || cashFlowMargin == null) {
            return;
        }

        BigDecimal gap = netMargin.subtract(cashFlowMargin);

        if (gap.compareTo(new BigDecimal("0.10")) >= 0) {
            saveOrUpdateFinding(
                    metric,
                    "EARNINGS_CASHFLOW_DIVERGENCE",
                    FindingSeverity.HIGH,
                    gap,
                    new BigDecimal("0.10"),
                    "Net margin is materially higher than operating cash flow margin, which may suggest weaker earnings quality."
            );
        }
    }

    private void saveOrUpdateFinding(
            FinancialMetric metric,
            String findingCode,
            FindingSeverity severity,
            BigDecimal observedValue,
            BigDecimal thresholdValue,
            String message
    ) {
        FinancialFinding finding = financialFindingRepository
                .findByMetricIdAndFindingCode(metric.getId(), findingCode)
                .orElseGet(() -> FinancialFinding.builder()
                        .metricId(metric.getId())
                        .companyId(metric.getCompanyId())
                        .ticker(metric.getTicker())
                        .fiscalYear(metric.getFiscalYear())
                        .periodType(metric.getPeriodType())
                        .findingCode(findingCode)
                        .build());

        finding.setSeverity(severity);
        finding.setObservedValue(observedValue);
        finding.setThresholdValue(thresholdValue);
        finding.setMessage(message);

        financialFindingRepository.save(finding);
    }
}