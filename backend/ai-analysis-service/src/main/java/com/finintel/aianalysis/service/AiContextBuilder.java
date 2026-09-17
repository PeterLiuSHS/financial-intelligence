package com.finintel.aianalysis.service;

import com.finintel.aianalysis.config.AiContextProperties;
import com.finintel.aianalysis.dto.AiContextNode;
import com.finintel.aianalysis.dto.AiContextPackage;
import com.finintel.aianalysis.dto.AiContextRequestOptions;
import com.finintel.aianalysis.dto.CompanyDto;
import com.finintel.aianalysis.dto.FinancialFindingDto;
import com.finintel.aianalysis.dto.FinancialMetricDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AiContextBuilder {

    private final AiContextProperties contextProperties;

    public AiContextBuilder(AiContextProperties contextProperties) {
        this.contextProperties = contextProperties;
    }

    public AiContextPackage buildFinancialRiskBriefContext(
            CompanyDto company,
            List<FinancialMetricDto> metrics,
            List<FinancialFindingDto> findings,
            AiContextRequestOptions options
    ) {
        int maxNodes = resolveMaxNodes(options);
        int maxMetricsYears = resolveMaxMetricsYears(options);
        int maxFindings = resolveMaxFindings(options);

        List<AiContextNode> nodes = new ArrayList<>();

        nodes.add(buildCompanyNode(company));
        nodes.add(buildMetricsNode(metrics, maxMetricsYears));
        nodes.add(buildFindingsNode(findings, maxFindings));

        List<AiContextNode> selectedNodes = nodes.stream()
                .sorted(Comparator.comparing(AiContextNode::priority))
                .limit(maxNodes)
                .toList();

        return new AiContextPackage(
                company.ticker(),
                company.name(),
                "FINANCIAL_RISK_BRIEF",
                selectedNodes
        );
    }

    private AiContextNode buildCompanyNode(CompanyDto company) {
        String content = """
                Ticker: %s
                Company name: %s
                CIK: %s
                Exchange: %s
                Sector: %s
                Industry: %s
                Country: %s
                """.formatted(
                company.ticker(),
                company.name(),
                company.cik(),
                company.exchange(),
                company.sector(),
                company.industry(),
                company.country()
        );

        return new AiContextNode(
                "COMPANY_PROFILE",
                "Company profile",
                content,
                1
        );
    }

    private AiContextNode buildMetricsNode(
            List<FinancialMetricDto> metrics,
            int maxMetricsYears
    ) {
        StringBuilder builder = new StringBuilder();

        metrics.stream()
                .limit(maxMetricsYears)
                .forEach(metric -> builder.append("""
                        Fiscal year: %s
                        - grossMargin: %s
                        - operatingMargin: %s
                        - netMargin: %s
                        - currentRatio: %s
                        - debtToAssets: %s
                        - cashFlowMargin: %s
                        - revenueGrowth: %s
                        - netIncomeGrowth: %s
                        - inventoryGrowth: %s
                        - accountsReceivableGrowth: %s
                        
                        """.formatted(
                        metric.fiscalYear(),
                        metric.grossMargin(),
                        metric.operatingMargin(),
                        metric.netMargin(),
                        metric.currentRatio(),
                        metric.debtToAssets(),
                        metric.cashFlowMargin(),
                        metric.revenueGrowth(),
                        metric.netIncomeGrowth(),
                        metric.inventoryGrowth(),
                        metric.accountsReceivableGrowth()
                )));

        return new AiContextNode(
                "FINANCIAL_METRICS",
                "Financial metrics",
                builder.toString(),
                2
        );
    }

    private AiContextNode buildFindingsNode(
            List<FinancialFindingDto> findings,
            int maxFindings
    ) {
        if (findings == null || findings.isEmpty()) {
            return new AiContextNode(
                    "RULE_BASED_FINDINGS",
                    "Rule-based findings",
                    "No rule-based findings were detected.",
                    3
            );
        }

        StringBuilder builder = new StringBuilder();

        findings.stream()
                .limit(maxFindings)
                .forEach(finding -> builder.append("""
                        Fiscal year: %s
                        - findingCode: %s
                        - severity: %s
                        - observedValue: %s
                        - thresholdValue: %s
                        - message: %s
                        
                        """.formatted(
                        finding.fiscalYear(),
                        finding.findingCode(),
                        finding.severity(),
                        finding.observedValue(),
                        finding.thresholdValue(),
                        finding.message()
                )));

        return new AiContextNode(
                "RULE_BASED_FINDINGS",
                "Rule-based findings",
                builder.toString(),
                3
        );
    }

    private int resolveMaxNodes(AiContextRequestOptions options) {
        if (options != null && options.maxNodes() != null && options.maxNodes() > 0) {
            return options.maxNodes();
        }

        return contextProperties.resolvedMaxNodes();
    }

    private int resolveMaxMetricsYears(AiContextRequestOptions options) {
        if (options != null
                && options.maxMetricsYears() != null
                && options.maxMetricsYears() > 0) {
            return options.maxMetricsYears();
        }

        return contextProperties.resolvedMaxMetricsYears();
    }

    private int resolveMaxFindings(AiContextRequestOptions options) {
        if (options != null
                && options.maxFindings() != null
                && options.maxFindings() > 0) {
            return options.maxFindings();
        }

        return contextProperties.resolvedMaxFindings();
    }
}