package com.finintel.aianalysis.controller;

import com.finintel.aianalysis.dto.AiAnalysisReportResponse;
import com.finintel.aianalysis.dto.AiContextRequestOptions;
import com.finintel.aianalysis.service.AiAnalysisReportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai-analysis/companies/{ticker}/reports")
public class AiAnalysisReportController {

    private final AiAnalysisReportService aiAnalysisReportService;

    public AiAnalysisReportController(
            AiAnalysisReportService aiAnalysisReportService
    ) {
        this.aiAnalysisReportService = aiAnalysisReportService;
    }

    @PostMapping
    public AiAnalysisReportResponse generateReport(
            @PathVariable String ticker,
            @RequestParam(required = false) Integer maxNodes,
            @RequestParam(required = false) Integer maxMetricsYears,
            @RequestParam(required = false) Integer maxFindings,
            @RequestParam(required = false) Boolean includePreviousReports,
            @RequestParam(required = false) Integer maxPreviousReports
    ) {
        AiContextRequestOptions options = new AiContextRequestOptions(
                maxNodes,
                maxMetricsYears,
                maxFindings,
                includePreviousReports,
                maxPreviousReports
        );

        return aiAnalysisReportService.generateReport(ticker, options);
    }

    @GetMapping
    public List<AiAnalysisReportResponse> getReportsByTicker(
            @PathVariable String ticker
    ) {
        return aiAnalysisReportService.getReportsByTicker(ticker);
    }
}