package com.finintel.financialdata.controller;

import com.finintel.financialdata.dto.SecFilingDto;
import com.finintel.financialdata.service.SecFilingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies/{ticker}/filings")
public class SecFilingController {

    private final SecFilingService secFilingService;

    public SecFilingController(SecFilingService secFilingService) {
        this.secFilingService = secFilingService;
    }

    @GetMapping("/latest-10-k")
    public ResponseEntity<SecFilingDto> getLatest10K(
            @PathVariable String ticker
    ) {
        SecFilingDto filing = secFilingService.getLatest10K(ticker);

        return ResponseEntity.ok(filing);
    }

    @GetMapping(
            value = "/latest-10-k/document",
            produces = "text/html"
    )
    public ResponseEntity<String> getLatest10KDocument(
            @PathVariable String ticker
    ) {
        String html =
                secFilingService.getLatest10KDocument(ticker);

        return ResponseEntity.ok(html);
    }

    @GetMapping(
            value = "/latest-10-k/text",
            produces = "text/plain"
    )
    public ResponseEntity<String> getLatest10KText(
            @PathVariable String ticker
    ) {
        String text =
                secFilingService.getLatest10KText(ticker);

        return ResponseEntity.ok(text);
    }

    @GetMapping(
            value = "/latest-10-k/risk-factors",
            produces = "text/plain"
    )
    public ResponseEntity<String> getLatest10KRiskFactors(
            @PathVariable String ticker
    ) {
        String riskFactors =
                secFilingService.getLatest10KRiskFactors(ticker);

        return ResponseEntity.ok(riskFactors);
    }
}
