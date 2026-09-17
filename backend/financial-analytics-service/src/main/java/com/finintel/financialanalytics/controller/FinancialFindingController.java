package com.finintel.financialanalytics.controller;

import com.finintel.financialanalytics.dto.FinancialFindingResponse;
import com.finintel.financialanalytics.service.FinancialFindingQueryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics/companies/{ticker}/findings")
public class FinancialFindingController {

    private final FinancialFindingQueryService financialFindingQueryService;

    public FinancialFindingController(
            FinancialFindingQueryService financialFindingQueryService
    ) {
        this.financialFindingQueryService = financialFindingQueryService;
    }

    @GetMapping
    public List<FinancialFindingResponse> getFindingsByTicker(
            @PathVariable String ticker
    ) {
        return financialFindingQueryService.getFindingsByTicker(ticker);
    }

    @GetMapping("/{fiscalYear}")
    public List<FinancialFindingResponse> getFindingsByTickerAndFiscalYear(
            @PathVariable String ticker,
            @PathVariable Integer fiscalYear
    ) {
        return financialFindingQueryService.getFindingsByTickerAndFiscalYear(
                ticker,
                fiscalYear
        );
    }
}