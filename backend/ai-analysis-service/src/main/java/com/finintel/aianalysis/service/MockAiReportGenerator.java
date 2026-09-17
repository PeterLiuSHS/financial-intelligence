package com.finintel.aianalysis.service;

import com.finintel.aianalysis.dto.AiGeneratedReport;
import com.finintel.aianalysis.dto.FinancialFindingDto;
import com.finintel.aianalysis.dto.FinancialMetricDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MockAiReportGenerator {

    public AiGeneratedReport generateReport(
            String ticker,
            String companyName,
            List<FinancialMetricDto> metrics,
            List<FinancialFindingDto> findings
    ) {
        FinancialMetricDto latestMetric = metrics.get(0);

        String summary = "This mock report summarizes recent financial performance for "
                + companyName
                + " (" + ticker + ")"
                + ". The latest fiscal year analyzed is "
                + latestMetric.fiscalYear()
                + ".";

        String keyFindings = buildKeyFindings(findings);

        String riskAssessment = "Based on the rule-based findings, "
                + companyName
                + " shows "
                + findings.size()
                + " notable financial signals that may require further review.";

        String suggestedQuestions = """
            1. What explains the latest revenue growth trend?
            2. Are liquidity indicators improving or weakening?
            3. Do inventory or receivables trends suggest demand or collection issues?
            4. Is operating cash flow consistent with reported profitability?
            """;

        return new AiGeneratedReport(
                summary,
                keyFindings,
                riskAssessment,
                suggestedQuestions,
                "mock",
                "mock"
        );
    }

    private String buildKeyFindings(List<FinancialFindingDto> findings) {
        if (findings == null || findings.isEmpty()) {
            return "No rule-based findings were detected from the available metrics.";
        }

        StringBuilder builder = new StringBuilder();

        for (FinancialFindingDto finding : findings) {
            builder.append("- ")
                    .append(finding.fiscalYear())
                    .append(" ")
                    .append(finding.findingCode())
                    .append(" [")
                    .append(finding.severity())
                    .append("]: ")
                    .append(finding.message())
                    .append("\n");
        }

        return builder.toString();
    }
}