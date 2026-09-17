package com.finintel.financialdata.service;

import tools.jackson.databind.JsonNode;
import com.finintel.financialdata.client.SecClient;
import com.finintel.financialdata.dto.ExtractedFinancialStatement;
import com.finintel.financialdata.dto.FinancialStatementResponse;
import com.finintel.financialdata.entity.Company;
import com.finintel.financialdata.entity.FinancialStatement;
import com.finintel.financialdata.entity.PeriodType;
import com.finintel.financialdata.event.FinancialDataEventPublisher;
import com.finintel.financialdata.exception.ResourceNotFoundException;
import com.finintel.financialdata.extractor.SecFinancialFactExtractor;
import com.finintel.financialdata.repository.CompanyRepository;
import com.finintel.financialdata.repository.FinancialStatementRepository;
import com.finintel.common.event.FinancialDataImportedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SecFinancialStatementImportService {

    private final CompanyRepository companyRepository;
    private final FinancialStatementRepository financialStatementRepository;
    private final SecClient secClient;
    private final SecFinancialFactExtractor secFinancialFactExtractor;
    private final FinancialDataEventPublisher financialDataEventPublisher;

    public SecFinancialStatementImportService(
            CompanyRepository companyRepository,
            FinancialStatementRepository financialStatementRepository,
            SecClient secClient,
            SecFinancialFactExtractor secFinancialFactExtractor,
            FinancialDataEventPublisher financialDataEventPublisher
    ) {
        this.companyRepository = companyRepository;
        this.financialStatementRepository = financialStatementRepository;
        this.secClient = secClient;
        this.secFinancialFactExtractor = secFinancialFactExtractor;
        this.financialDataEventPublisher = financialDataEventPublisher;
    }

    @Transactional
    public List<FinancialStatementResponse> importRecentAnnualStatements(
            String ticker,
            int years
    ) {
        Company company = companyRepository.findByTickerIgnoreCase(ticker)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found with ticker: " + ticker
                                + ". Please import company from SEC first."
                ));

        if (company.getCik() == null || company.getCik().isBlank()) {
            throw new ResourceNotFoundException(
                    "Company does not have CIK. Please import company from SEC first."
            );
        }

        JsonNode companyFacts = secClient.getCompanyFacts(company.getCik());

        List<ExtractedFinancialStatement> extractedStatements =
                secFinancialFactExtractor.extractRecentAnnualStatements(
                        companyFacts,
                        years
                );

        List<FinancialStatement> savedStatements = extractedStatements.stream()
                .map(extracted -> saveOrUpdateAnnualStatement(company, extracted))
                .toList();

        List<Integer> fiscalYears = savedStatements.stream()
                .map(FinancialStatement::getFiscalYear)
                .toList();

        financialDataEventPublisher.publishFinancialDataImported(
                new FinancialDataImportedEvent(
                        company.getId(),
                        company.getTicker(),
                        "ANNUAL",
                        fiscalYears
                )
        );

        return savedStatements.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public FinancialStatementResponse importLatestAnnualStatement(String ticker) {
        return importRecentAnnualStatements(ticker, 1)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No annual financial statement imported for ticker: " + ticker
                ));
    }

    private FinancialStatement saveOrUpdateAnnualStatement(
            Company company,
            ExtractedFinancialStatement extracted
    ) {
        FinancialStatement statement = financialStatementRepository
                .findByCompanyAndFiscalYearAndPeriodType(
                        company,
                        extracted.fiscalYear(),
                        PeriodType.ANNUAL
                )
                .orElseGet(() -> FinancialStatement.builder()
                        .company(company)
                        .fiscalYear(extracted.fiscalYear())
                        .periodType(PeriodType.ANNUAL)
                        .build());

        statement.setRevenue(extracted.revenue());
        statement.setGrossProfit(extracted.grossProfit());
        statement.setOperatingIncome(extracted.operatingIncome());
        statement.setNetIncome(extracted.netIncome());

        statement.setTotalAssets(extracted.totalAssets());
        statement.setTotalLiabilities(extracted.totalLiabilities());
        statement.setTotalDebt(extracted.totalDebt());
        statement.setCashAndCashEquivalents(extracted.cashAndCashEquivalents());
        statement.setInventory(extracted.inventory());
        statement.setAccountsReceivable(extracted.accountsReceivable());
        statement.setCurrentAssets(extracted.currentAssets());
        statement.setCurrentLiabilities(extracted.currentLiabilities());

        statement.setOperatingCashFlow(extracted.operatingCashFlow());
        statement.setCapitalExpenditure(extracted.capitalExpenditure());

        return financialStatementRepository.save(statement);
    }

    private FinancialStatementResponse toResponse(FinancialStatement statement) {
        Company company = statement.getCompany();

        return new FinancialStatementResponse(
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
        );
    }
}