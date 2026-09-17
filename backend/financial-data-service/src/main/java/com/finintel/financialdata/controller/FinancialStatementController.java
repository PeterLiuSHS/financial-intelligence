package com.finintel.financialdata.controller;

import com.finintel.financialdata.dto.FinancialStatementResponse;
import com.finintel.financialdata.entity.PeriodType;
import com.finintel.financialdata.exception.ResourceNotFoundException;
import com.finintel.financialdata.repository.CompanyRepository;
import com.finintel.financialdata.repository.FinancialStatementRepository;
import com.finintel.financialdata.service.SecFinancialStatementImportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies/{ticker}/financial-statements")
public class FinancialStatementController {

    private final SecFinancialStatementImportService secFinancialStatementImportService;
    private final CompanyRepository companyRepository;
    private final FinancialStatementRepository financialStatementRepository;

    public FinancialStatementController(
            SecFinancialStatementImportService secFinancialStatementImportService,
            CompanyRepository companyRepository,
            FinancialStatementRepository financialStatementRepository
    ) {
        this.secFinancialStatementImportService = secFinancialStatementImportService;
        this.companyRepository = companyRepository;
        this.financialStatementRepository = financialStatementRepository;
    }

    @PostMapping("/import-from-sec")
    public List<FinancialStatementResponse> importAnnualStatementsFromSec(
            @PathVariable String ticker,
            @RequestParam(defaultValue = "3") int years
    ) {
        return secFinancialStatementImportService.importRecentAnnualStatements(
                ticker,
                years
        );
    }

    @GetMapping
    public List<FinancialStatementResponse> getFinancialStatements(
            @PathVariable String ticker
    ) {
        var company = companyRepository.findByTickerIgnoreCase(ticker)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found with ticker: " + ticker
                ));

        return financialStatementRepository.findByCompanyOrderByFiscalYearDesc(company)
                .stream()
                .map(statement -> new FinancialStatementResponse(
                        statement.getId(),
                        company.getId(),
                        company.getTicker(),
                        company.getName(),
                        statement.getFiscalYear(),
                        statement.getPeriodType(),

                        statement.getRevenue(),
                        statement.getGrossProfit(),
                        statement.getOperatingIncome(),
                        statement.getNetIncome(),

                        statement.getTotalAssets(),
                        statement.getTotalLiabilities(),
                        statement.getTotalDebt(),
                        statement.getCashAndCashEquivalents(),
                        statement.getInventory(),
                        statement.getAccountsReceivable(),
                        statement.getCurrentAssets(),
                        statement.getCurrentLiabilities(),

                        statement.getOperatingCashFlow(),
                        statement.getCapitalExpenditure(),

                        statement.getCreatedAt(),
                        statement.getUpdatedAt()
                ))
                .toList();
    }
}