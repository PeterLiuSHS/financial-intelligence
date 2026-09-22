package com.finintel.financialdata.controller;

import com.finintel.financialdata.dto.FinancialStatementResponse;
import com.finintel.financialdata.entity.Company;
import com.finintel.financialdata.entity.FinancialStatement;
import com.finintel.financialdata.entity.PeriodType;
import com.finintel.financialdata.repository.CompanyRepository;
import com.finintel.financialdata.repository.FinancialStatementRepository;
import com.finintel.financialdata.service.SecFinancialStatementImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FinancialStatementControllerTest {

    private MockMvc mockMvc;

    private SecFinancialStatementImportService importService;
    private CompanyRepository companyRepository;
    private FinancialStatementRepository financialStatementRepository;

    @BeforeEach
    void setUp() {
        importService =
                mock(SecFinancialStatementImportService.class);

        companyRepository =
                mock(CompanyRepository.class);

        financialStatementRepository =
                mock(FinancialStatementRepository.class);

        FinancialStatementController controller =
                new FinancialStatementController(
                        importService,
                        companyRepository,
                        financialStatementRepository
                );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void importAnnualStatementsFromSec_shouldReturn200AndStatements()
            throws Exception {

        FinancialStatementResponse statement2025 =
                createResponse(
                        101L,
                        2025,
                        new BigDecimal("1000")
                );

        FinancialStatementResponse statement2024 =
                createResponse(
                        102L,
                        2024,
                        new BigDecimal("900")
                );

        when(
                importService.importRecentAnnualStatements(
                        "AAPL",
                        2
                )
        ).thenReturn(
                List.of(
                        statement2025,
                        statement2024
                )
        );

        mockMvc.perform(
                        post(
                                "/api/companies/AAPL/financial-statements/import-from-sec"
                        )
                                .param("years", "2")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()").value(2)
                )
                .andExpect(
                        jsonPath("$[0].ticker")
                                .value("AAPL")
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
                        jsonPath("$[1].fiscalYear")
                                .value(2024)
                )
                .andExpect(
                        jsonPath("$[1].revenue")
                                .value(900)
                );

        verify(importService)
                .importRecentAnnualStatements(
                        "AAPL",
                        2
                );
    }

    @Test
    void importAnnualStatementsFromSec_withoutYears_shouldUseDefaultThree()
            throws Exception {

        when(
                importService.importRecentAnnualStatements(
                        "AAPL",
                        3
                )
        ).thenReturn(List.of());

        mockMvc.perform(
                        post(
                                "/api/companies/AAPL/financial-statements/import-from-sec"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()").value(0)
                );

        verify(importService)
                .importRecentAnnualStatements(
                        "AAPL",
                        3
                );
    }

    @Test
    void getFinancialStatements_shouldReturn200AndStatements()
            throws Exception {

        Company company = createCompany();

        FinancialStatement statement2025 =
                createStatement(
                        101L,
                        company,
                        2025,
                        "1000"
                );

        FinancialStatement statement2024 =
                createStatement(
                        102L,
                        company,
                        2024,
                        "900"
                );

        when(
                companyRepository.findByTickerIgnoreCase(
                        "AAPL"
                )
        ).thenReturn(
                Optional.of(company)
        );

        when(
                financialStatementRepository
                        .findByCompanyOrderByFiscalYearDesc(
                                company
                        )
        ).thenReturn(
                List.of(
                        statement2025,
                        statement2024
                )
        );

        mockMvc.perform(
                        get(
                                "/api/companies/AAPL/financial-statements"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()").value(2)
                )
                .andExpect(
                        jsonPath("$[0].id").value(101)
                )
                .andExpect(
                        jsonPath("$[0].companyId").value(1)
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
                        jsonPath("$[1].fiscalYear")
                                .value(2024)
                );

        verify(companyRepository)
                .findByTickerIgnoreCase("AAPL");

        verify(financialStatementRepository)
                .findByCompanyOrderByFiscalYearDesc(
                        company
                );
    }

    @Test
    void getFinancialStatements_whenNoStatementsExist_shouldReturnEmptyList()
            throws Exception {

        Company company = createCompany();

        when(
                companyRepository.findByTickerIgnoreCase(
                        "AAPL"
                )
        ).thenReturn(
                Optional.of(company)
        );

        when(
                financialStatementRepository
                        .findByCompanyOrderByFiscalYearDesc(
                                company
                        )
        ).thenReturn(List.of());

        mockMvc.perform(
                        get(
                                "/api/companies/AAPL/financial-statements"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()").value(0)
                );
    }

    private Company createCompany() {
        return Company.builder()
                .id(1L)
                .ticker("AAPL")
                .name("Apple Inc.")
                .cik("0000320193")
                .country("US")
                .build();
    }

    private FinancialStatement createStatement(
            Long id,
            Company company,
            int fiscalYear,
            String revenue
    ) {
        return FinancialStatement.builder()
                .id(id)
                .company(company)
                .fiscalYear(fiscalYear)
                .periodType(PeriodType.ANNUAL)
                .revenue(new BigDecimal(revenue))
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
                .build();
    }

    private FinancialStatementResponse createResponse(
            Long id,
            int fiscalYear,
            BigDecimal revenue
    ) {
        LocalDateTime now =
                LocalDateTime.of(
                        2026,
                        9,
                        22,
                        2,
                        0
                );

        return new FinancialStatementResponse(
                id,
                1L,
                "AAPL",
                "Apple Inc.",
                fiscalYear,
                PeriodType.ANNUAL,

                revenue,
                new BigDecimal("400"),
                new BigDecimal("300"),
                new BigDecimal("250"),

                new BigDecimal("3500"),
                new BigDecimal("2500"),
                new BigDecimal("1000"),
                new BigDecimal("500"),
                new BigDecimal("100"),
                new BigDecimal("200"),
                new BigDecimal("1500"),
                new BigDecimal("1200"),

                new BigDecimal("600"),
                new BigDecimal("150"),

                now,
                now
        );
    }
}