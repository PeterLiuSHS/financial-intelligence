package com.finintel.financialanalytics.controller;

import com.finintel.financialanalytics.dto.FinancialMetricResponse;
import com.finintel.financialanalytics.service.FinancialMetricQueryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics/companies/{ticker}/metrics")
public class FinancialMetricController {

    private final FinancialMetricQueryService financialMetricQueryService;

    public FinancialMetricController(
            FinancialMetricQueryService financialMetricQueryService
    ) {
        this.financialMetricQueryService = financialMetricQueryService;
    }

    @GetMapping
    public List<FinancialMetricResponse> getMetricsByTicker(
            @PathVariable String ticker
    ) {
        return financialMetricQueryService.getMetricsByTicker(ticker);
    }
}