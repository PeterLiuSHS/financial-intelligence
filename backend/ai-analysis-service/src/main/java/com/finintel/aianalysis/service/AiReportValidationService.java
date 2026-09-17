package com.finintel.aianalysis.service;

import com.finintel.aianalysis.dto.AiGeneratedReport;
import com.finintel.aianalysis.dto.AiOutputValidationResult;
import com.finintel.aianalysis.dto.FinancialFindingDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiReportValidationService {

    public AiOutputValidationResult validate(
            AiGeneratedReport report,
            List<FinancialFindingDto> findings
    ) {
        List<String> violations = new ArrayList<>();

        validateRequiredFields(report, violations);
        validateNoInvestmentAdvice(report, violations);
        validateFindingFaithfulness(report, findings, violations);

        return new AiOutputValidationResult(
                violations.isEmpty(),
                violations
        );
    }

    private void validateRequiredFields(
            AiGeneratedReport report,
            List<String> violations
    ) {
        if (isBlank(report.summary())) {
            violations.add("summary is missing");
        }

        if (isBlank(report.keyFindings())) {
            violations.add("keyFindings is missing");
        }

        if (isBlank(report.riskAssessment())) {
            violations.add("riskAssessment is missing");
        }

        if (isBlank(report.suggestedQuestions())) {
            violations.add("suggestedQuestions is missing");
        }
    }

    private void validateNoInvestmentAdvice(
            AiGeneratedReport report,
            List<String> violations
    ) {
        String text = allReportText(report).toLowerCase();

        List<String> bannedPhrases = List.of(
                "buy recommendation",
                "sell recommendation",
                "hold recommendation",
                "recommend buying",
                "recommend selling",
                "recommend holding",
                "you should buy",
                "you should sell",
                "you should hold",
                "investors should buy",
                "investors should sell",
                "investors should hold",
                "strong buy",
                "strong sell"
        );

        for (String phrase : bannedPhrases) {
            if (text.contains(phrase)) {
                violations.add("investment advice phrase detected: " + phrase);
            }
        }
    }

    private void validateFindingFaithfulness(
            AiGeneratedReport report,
            List<FinancialFindingDto> findings,
            List<String> violations
    ) {
        String text = allReportText(report).toUpperCase();

        List<String> triggeredFindingCodes = findings.stream()
                .map(FinancialFindingDto::findingCode)
                .map(String::toUpperCase)
                .toList();

        List<String> knownFindingCodes = List.of(
                "REVENUE_DECLINE",
                "SHARP_REVENUE_DECLINE",
                "LOW_CURRENT_RATIO",
                "INVENTORY_BUILDUP",
                "RECEIVABLES_BUILDUP",
                "WEAK_CASH_FLOW_MARGIN",
                "EARNINGS_CASHFLOW_DIVERGENCE"
        );

        for (String knownFindingCode : knownFindingCodes) {
            if (text.contains(knownFindingCode)
                    && !triggeredFindingCodes.contains(knownFindingCode)) {
                violations.add(
                        "unsupported finding code mentioned: " + knownFindingCode
                );
            }
        }
    }

    private String allReportText(AiGeneratedReport report) {
        return String.join(
                "\n",
                nullToEmpty(report.summary()),
                nullToEmpty(report.keyFindings()),
                nullToEmpty(report.riskAssessment()),
                nullToEmpty(report.suggestedQuestions())
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}