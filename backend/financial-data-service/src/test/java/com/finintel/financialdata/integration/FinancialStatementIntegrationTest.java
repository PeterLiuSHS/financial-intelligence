package com.finintel.financialdata.integration;

import com.finintel.financialdata.entity.Company;
import com.finintel.financialdata.entity.FinancialStatement;
import com.finintel.financialdata.entity.PeriodType;
import com.finintel.financialdata.repository.CompanyRepository;
import com.finintel.financialdata.repository.FinancialStatementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FinancialStatementIntegrationTest
        extends AbstractIntegrationTest {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private FinancialStatementRepository financialStatementRepository;

    @BeforeEach
    void cleanDatabase() {
        financialStatementRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    void getFinancialStatements_shouldReturnStatementsFromMySql()
            throws Exception {

        Company company = companyRepository.save(
                Company.builder()
                        .ticker("AAPL")
                        .name("Apple Inc.")
                        .cik("0000320193")
                        .exchange("NASDAQ")
                        .sector("Technology")
                        .industry("Consumer Electronics")
                        .country("US")
                        .build()
        );

        FinancialStatement statement2025 =
                financialStatementRepository.save(
                        FinancialStatement.builder()
                                .company(company)
                                .fiscalYear(2025)
                                .periodType(PeriodType.ANNUAL)
                                .revenue(new BigDecimal("1000"))
                                .grossProfit(new BigDecimal("400"))
                                .operatingIncome(new BigDecimal("300"))
                                .netIncome(new BigDecimal("250"))
                                .totalAssets(new BigDecimal("3500"))
                                .totalLiabilities(new BigDecimal("2500"))
                                .totalDebt(new BigDecimal("1000"))
                                .cashAndCashEquivalents(
                                        new BigDecimal("500")
                                )
                                .inventory(new BigDecimal("100"))
                                .accountsReceivable(
                                        new BigDecimal("200")
                                )
                                .currentAssets(
                                        new BigDecimal("1500")
                                )
                                .currentLiabilities(
                                        new BigDecimal("1200")
                                )
                                .operatingCashFlow(
                                        new BigDecimal("600")
                                )
                                .capitalExpenditure(
                                        new BigDecimal("150")
                                )
                                .build()
                );

        FinancialStatement statement2024 =
                financialStatementRepository.save(
                        FinancialStatement.builder()
                                .company(company)
                                .fiscalYear(2024)
                                .periodType(PeriodType.ANNUAL)
                                .revenue(new BigDecimal("900"))
                                .grossProfit(new BigDecimal("350"))
                                .operatingIncome(new BigDecimal("270"))
                                .netIncome(new BigDecimal("220"))
                                .totalAssets(new BigDecimal("3300"))
                                .totalLiabilities(new BigDecimal("2400"))
                                .totalDebt(new BigDecimal("950"))
                                .cashAndCashEquivalents(
                                        new BigDecimal("450")
                                )
                                .inventory(new BigDecimal("90"))
                                .accountsReceivable(
                                        new BigDecimal("180")
                                )
                                .currentAssets(
                                        new BigDecimal("1400")
                                )
                                .currentLiabilities(
                                        new BigDecimal("1100")
                                )
                                .operatingCashFlow(
                                        new BigDecimal("550")
                                )
                                .capitalExpenditure(
                                        new BigDecimal("140")
                                )
                                .build()
                );

        assertNotNull(statement2025.getId());
        assertNotNull(statement2024.getId());

        mockMvc.perform(
                        get(
                                "/api/companies/AAPL/financial-statements"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$[0].ticker")
                                .value("AAPL")
                )
                .andExpect(
                        jsonPath("$[0].companyName")
                                .value("Apple Inc.")
                )
                .andExpect(
                        jsonPath("$[0].fiscalYear")
                                .value(2025)
                )
                .andExpect(
                        jsonPath("$[0].periodType")
                                .value("ANNUAL")
                )
                .andExpect(
                        jsonPath("$[0].revenue")
                                .value(1000)
                )
                .andExpect(
                        jsonPath("$[0].netIncome")
                                .value(250)
                )
                .andExpect(
                        jsonPath("$[1].fiscalYear")
                                .value(2024)
                )
                .andExpect(
                        jsonPath("$[1].revenue")
                                .value(900)
                );

        List<FinancialStatement> persisted =
                financialStatementRepository
                        .findByCompanyOrderByFiscalYearDesc(
                                company
                        );

        assertEquals(
                2,
                persisted.size()
        );

        assertEquals(
                2025,
                persisted.get(0).getFiscalYear()
        );

        assertEquals(
                2024,
                persisted.get(1).getFiscalYear()
        );
    }

    @Test
    void getFinancialStatements_whenCompanyExistsButHasNoStatements_shouldReturnEmptyList()
            throws Exception {

        companyRepository.save(
                Company.builder()
                        .ticker("MSFT")
                        .name("Microsoft Corporation")
                        .cik("0000789019")
                        .exchange("NASDAQ")
                        .sector("Technology")
                        .industry("Software")
                        .country("US")
                        .build()
        );

        mockMvc.perform(
                        get(
                                "/api/companies/MSFT/financial-statements"
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.length()")
                                .value(0)
                );
    }

    @Test
    void getFinancialStatements_whenCompanyDoesNotExist_shouldReturn404()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/companies/UNKNOWN/financial-statements"
                        )
                )
                .andExpect(
                        status().isNotFound()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Not Found")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Company not found with ticker: UNKNOWN"
                                )
                );
    }
}