package com.finintel.aianalysis.service;

import com.finintel.aianalysis.dto.AiContextNode;
import com.finintel.aianalysis.dto.AiContextPackage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AiFinancialPromptBuilder {

    public String buildSystemPrompt() {
        return """
                You are a financial analysis assistant for a backend financial intelligence platform.
                
                Use only the context nodes provided by the system.
                Do not invent financial facts, news, management commentary, market data, industry context, or company identity.
                Do not infer the company name from the ticker. Use only the company name provided by the system.
                Do not give investment advice.
                Do not recommend buying, selling, or holding any security.
                
                Important distinction:
                - Rule-based findings are the only system-confirmed risk signals.
                - Metrics are supporting numerical context.
                - Do not describe a metric as a risk, concern, warning, anomaly, red flag, or issue unless it appears in RULE_BASED_FINDINGS.
                - If a metric did not trigger a rule-based finding, describe it neutrally.
                
                Return only valid JSON with this exact structure:
                {
                  "summary": "...",
                  "keyFindings": "...",
                  "riskAssessment": "...",
                  "suggestedQuestions": "..."
                }
                
                Do not include markdown.
                Do not include code fences.
                Do not include any explanation outside the JSON object.
                The response must start with "{" and end with "}".
                Each field should be a plain English string.
                """;
    }

    public String buildUserPrompt(AiContextPackage contextPackage) {
        return """
                Generate a concise financial risk brief.

                Ticker: %s
                Company name: %s
                Report type: %s

                Context nodes:
                %s

                Requirements:
                - Use only the context nodes provided above.
                - Explain what the financial metrics suggest.
                - Explain the rule-based findings in business language.
                - Only call something a risk signal if it appears in RULE_BASED_FINDINGS.
                - Be careful and non-speculative.
                - Do not claim causality unless the provided data supports it.
                - Do not provide investment advice.
                - Keep each JSON field concise.
                - summary: maximum 80 words.
                - keyFindings: maximum 120 words.
                - riskAssessment: maximum 80 words.
                - suggestedQuestions: exactly 3 short questions.
                """.formatted(
                contextPackage.ticker(),
                contextPackage.companyName(),
                contextPackage.reportType(),
                formatContextNodes(contextPackage.nodes())
        );
    }

    private String formatContextNodes(List<AiContextNode> nodes) {
        StringBuilder builder = new StringBuilder();

        for (AiContextNode node : nodes) {
            builder.append("[").append(node.nodeType()).append("]\n")
                    .append("Title: ").append(node.title()).append("\n")
                    .append(node.content())
                    .append("\n\n");
        }

        return builder.toString();
    }
}