package com.finintel.financialanalytics.service;

import com.finintel.financialanalytics.dto.FinancialFindingResponse;
import com.finintel.financialanalytics.entity.FinancialFinding;
import com.finintel.financialanalytics.exception.ResourceNotFoundException;
import com.finintel.financialanalytics.repository.FinancialFindingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FinancialFindingQueryService {

    private final FinancialFindingRepository financialFindingRepository;

    public FinancialFindingQueryService(
            FinancialFindingRepository financialFindingRepository
    ) {
        this.financialFindingRepository = financialFindingRepository;
    }

    @Transactional(readOnly = true)
    public List<FinancialFindingResponse> getFindingsByTicker(String ticker) {
        List<FinancialFinding> findings =
                financialFindingRepository.findByTickerOrderByFiscalYearDesc(
                        ticker.toUpperCase()
                );

        if (findings.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No financial findings found for ticker: " + ticker.toUpperCase()
            );
        }

        return findings.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FinancialFindingResponse> getFindingsByTickerAndFiscalYear(
            String ticker,
            Integer fiscalYear
    ) {
        List<FinancialFinding> findings =
                financialFindingRepository.findByTickerAndFiscalYearOrderByFindingCodeAsc(
                        ticker.toUpperCase(),
                        fiscalYear
                );

        if (findings.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No financial findings found for ticker: "
                            + ticker.toUpperCase()
                            + " and fiscal year: "
                            + fiscalYear
            );
        }

        return findings.stream()
                .map(this::toResponse)
                .toList();
    }

    private FinancialFindingResponse toResponse(FinancialFinding finding) {
        return new FinancialFindingResponse(
                finding.getId(),
                finding.getMetricId(),
                finding.getCompanyId(),
                finding.getTicker(),
                finding.getFiscalYear(),
                finding.getPeriodType(),
                finding.getFindingCode(),
                finding.getSeverity(),
                finding.getObservedValue(),
                finding.getThresholdValue(),
                finding.getMessage(),
                finding.getCreatedAt(),
                finding.getUpdatedAt()
        );
    }
}