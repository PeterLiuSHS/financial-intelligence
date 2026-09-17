package com.finintel.aianalysis.service;

import com.finintel.aianalysis.client.FinancialAnalyticsClient;
import com.finintel.aianalysis.client.FinancialDataClient;
import com.finintel.aianalysis.dto.AiAnalysisReportResponse;
import com.finintel.aianalysis.dto.AiContextPackage;
import com.finintel.aianalysis.dto.AiContextRequestOptions;
import com.finintel.aianalysis.dto.AiGeneratedReport;
import com.finintel.aianalysis.dto.AiOutputValidationResult;
import com.finintel.aianalysis.dto.CompanyDto;
import com.finintel.aianalysis.dto.FinancialFindingDto;
import com.finintel.aianalysis.dto.FinancialMetricDto;
import com.finintel.aianalysis.entity.AiAnalysisReport;
import com.finintel.aianalysis.exception.AiProviderException;
import com.finintel.aianalysis.exception.ResourceNotFoundException;
import com.finintel.aianalysis.repository.AiAnalysisReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AiAnalysisReportService {

    private final FinancialAnalyticsClient financialAnalyticsClient;
    private final FinancialDataClient financialDataClient;
    private final GeminiAiReportGenerator geminiAiReportGenerator;
    private final AiAnalysisReportRepository aiAnalysisReportRepository;
    private final AiReportValidationService aiReportValidationService;
    private final AiContextBuilder aiContextBuilder;

    public AiAnalysisReportService(
            FinancialAnalyticsClient financialAnalyticsClient,
            FinancialDataClient financialDataClient,
            GeminiAiReportGenerator geminiAiReportGenerator,
            AiAnalysisReportRepository aiAnalysisReportRepository,
            AiReportValidationService aiReportValidationService,
            AiContextBuilder aiContextBuilder
    ) {
        this.financialAnalyticsClient = financialAnalyticsClient;
        this.financialDataClient = financialDataClient;
        this.geminiAiReportGenerator = geminiAiReportGenerator;
        this.aiAnalysisReportRepository = aiAnalysisReportRepository;
        this.aiReportValidationService = aiReportValidationService;
        this.aiContextBuilder = aiContextBuilder;
    }

    @Transactional
    public AiAnalysisReportResponse generateReport(
            String ticker,
            AiContextRequestOptions options
    ) {
        String normalizedTicker = ticker.toUpperCase();

        CompanyDto company =
                financialDataClient.getCompany(normalizedTicker);

        List<FinancialMetricDto> metrics =
                financialAnalyticsClient.getMetrics(normalizedTicker);

        if (metrics == null || metrics.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No financial metrics available for ticker: " + normalizedTicker
            );
        }

        List<FinancialFindingDto> findings =
                financialAnalyticsClient.getFindings(normalizedTicker);

        AiContextPackage contextPackage =
                aiContextBuilder.buildFinancialRiskBriefContext(
                        company,
                        metrics,
                        findings,
                        options
                );

        AiGeneratedReport generatedReport =
                geminiAiReportGenerator.generateReport(contextPackage);

        AiOutputValidationResult validationResult =
                aiReportValidationService.validate(
                        generatedReport,
                        findings
                );

        if (!validationResult.valid()) {
            throw new AiProviderException(
                    "AI report validation failed: "
                            + String.join("; ", validationResult.violations())
            );
        }

        AiAnalysisReport report = AiAnalysisReport.builder()
                .ticker(normalizedTicker)
                .companyName(company.name())
                .reportType("FINANCIAL_RISK_BRIEF")
                .summary(generatedReport.summary())
                .keyFindings(generatedReport.keyFindings())
                .riskAssessment(generatedReport.riskAssessment())
                .suggestedQuestions(generatedReport.suggestedQuestions())
                .provider(generatedReport.provider())
                .model(generatedReport.model())
                .validationPassed(validationResult.valid())
                .validationStatus("PASSED")
                .validationViolations(String.join("; ", validationResult.violations()))
                .build();

        AiAnalysisReport savedReport =
                aiAnalysisReportRepository.save(report);

        return toResponse(savedReport);
    }

    @Transactional(readOnly = true)
    public List<AiAnalysisReportResponse> getReportsByTicker(String ticker) {
        String normalizedTicker = ticker.toUpperCase();

        List<AiAnalysisReport> reports =
                aiAnalysisReportRepository.findByTickerOrderByCreatedAtDesc(
                        normalizedTicker
                );

        if (reports.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No AI analysis reports found for ticker: " + normalizedTicker
            );
        }

        return reports.stream()
                .map(this::toResponse)
                .toList();
    }

    private AiAnalysisReportResponse toResponse(AiAnalysisReport report) {
        return new AiAnalysisReportResponse(
                report.getId(),
                report.getTicker(),
                report.getCompanyName(),
                report.getReportType(),
                report.getSummary(),
                report.getKeyFindings(),
                report.getRiskAssessment(),
                report.getSuggestedQuestions(),
                report.getProvider(),
                report.getModel(),
                report.getValidationPassed(),
                report.getValidationStatus(),
                report.getValidationViolations(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }
}