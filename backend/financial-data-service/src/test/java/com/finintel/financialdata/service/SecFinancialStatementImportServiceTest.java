package com.finintel.financialdata.service;

import com.finintel.common.event.FinancialDataImportedEvent;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecFinancialStatementImportServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private FinancialStatementRepository financialStatementRepository;

    @Mock
    private SecClient secClient;

    @Mock
    private SecFinancialFactExtractor secFinancialFactExtractor;

    @Mock
    private FinancialDataEventPublisher financialDataEventPublisher;

    private SecFinancialStatementImportService service;

    @BeforeEach
    void setUp() {
        service = new SecFinancialStatementImportService(
                companyRepository,
                financialStatementRepository,
                secClient,
                secFinancialFactExtractor,
                financialDataEventPublisher
        );
    }

    @Test
    void importRecentAnnualStatements_shouldImportSavePublishAndReturnStatements() {
        Company company = createCompany();

        JsonNode companyFacts = mock(JsonNode.class);

        ExtractedFinancialStatement fy2025 = createExtractedStatement(
                2025,
                "1000",
                "400",
                "300",
                "250"
        );

        ExtractedFinancialStatement fy2024 = createExtractedStatement(
                2024,
                "900",
                "350",
                "270",
                "220"
        );

        when(companyRepository.findByTickerIgnoreCase("AAPL"))
                .thenReturn(Optional.of(company));

        when(secClient.getCompanyFacts("0000320193"))
                .thenReturn(companyFacts);

        when(secFinancialFactExtractor.extractRecentAnnualStatements(
                companyFacts, 2))
                .thenReturn(List.of(fy2025, fy2024));

        when(financialStatementRepository
                .findByCompanyAndFiscalYearAndPeriodType(
                        eq(company),
                        anyInt(),
                        eq(PeriodType.ANNUAL)))
                .thenReturn(Optional.empty());

        when(financialStatementRepository.save(any(FinancialStatement.class)))
                .thenAnswer(invocation -> {
                    FinancialStatement statement = invocation.getArgument(0);

                    if (statement.getFiscalYear() == 2025) {
                        statement.setId(101L);
                    } else {
                        statement.setId(102L);
                    }

                    return statement;
                });

        List<FinancialStatementResponse> result =
                service.importRecentAnnualStatements("AAPL", 2);

        assertEquals(2, result.size());

        assertEquals(2025, result.get(0).fiscalYear());
        assertEquals(2024, result.get(1).fiscalYear());

        assertEquals(new BigDecimal("1000"), result.get(0).revenue());
        assertEquals(new BigDecimal("900"), result.get(1).revenue());

        verify(secClient).getCompanyFacts("0000320193");

        verify(secFinancialFactExtractor)
                .extractRecentAnnualStatements(companyFacts, 2);

        verify(financialStatementRepository, times(2))
                .save(any(FinancialStatement.class));

        ArgumentCaptor<FinancialDataImportedEvent> eventCaptor =
                ArgumentCaptor.forClass(FinancialDataImportedEvent.class);

        verify(financialDataEventPublisher)
                .publishFinancialDataImported(eventCaptor.capture());

        FinancialDataImportedEvent event = eventCaptor.getValue();

        assertEquals(1L, event.companyId());
        assertEquals("AAPL", event.ticker());
        assertEquals("ANNUAL", event.periodType());
        assertEquals(List.of(2025, 2024), event.fiscalYears());
    }

    @Test
    void importRecentAnnualStatements_whenStatementAlreadyExists_shouldUpdateExistingStatement() {
        Company company = createCompany();

        JsonNode companyFacts = mock(JsonNode.class);

        ExtractedFinancialStatement extracted =
                createExtractedStatement(
                        2025,
                        "1000",
                        "400",
                        "300",
                        "250"
                );

        FinancialStatement existing = FinancialStatement.builder()
                .id(50L)
                .company(company)
                .fiscalYear(2025)
                .periodType(PeriodType.ANNUAL)
                .revenue(new BigDecimal("500"))
                .build();

        when(companyRepository.findByTickerIgnoreCase("AAPL"))
                .thenReturn(Optional.of(company));

        when(secClient.getCompanyFacts("0000320193"))
                .thenReturn(companyFacts);

        when(secFinancialFactExtractor.extractRecentAnnualStatements(
                companyFacts, 1))
                .thenReturn(List.of(extracted));

        when(financialStatementRepository
                .findByCompanyAndFiscalYearAndPeriodType(
                        company,
                        2025,
                        PeriodType.ANNUAL))
                .thenReturn(Optional.of(existing));

        when(financialStatementRepository.save(existing))
                .thenReturn(existing);

        List<FinancialStatementResponse> result =
                service.importRecentAnnualStatements("AAPL", 1);

        assertEquals(1, result.size());

        assertEquals(new BigDecimal("1000"), existing.getRevenue());
        assertEquals(new BigDecimal("400"), existing.getGrossProfit());
        assertEquals(new BigDecimal("300"), existing.getOperatingIncome());
        assertEquals(new BigDecimal("250"), existing.getNetIncome());

        assertEquals(50L, result.get(0).id());

        verify(financialStatementRepository).save(existing);
    }

    @Test
    void importRecentAnnualStatements_whenCompanyDoesNotExist_shouldThrowResourceNotFoundException() {
        when(companyRepository.findByTickerIgnoreCase("UNKNOWN"))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.importRecentAnnualStatements(
                        "UNKNOWN",
                        3
                )
        );

        assertEquals(
                "Company not found with ticker: UNKNOWN. Please import company from SEC first.",
                exception.getMessage()
        );

        verifyNoInteractions(
                secClient,
                secFinancialFactExtractor,
                financialDataEventPublisher
        );

        verify(financialStatementRepository, never())
                .save(any());
    }

    @Test
    void importRecentAnnualStatements_whenCompanyHasNullCik_shouldThrowResourceNotFoundException() {
        Company company = createCompany();
        company.setCik(null);

        when(companyRepository.findByTickerIgnoreCase("AAPL"))
                .thenReturn(Optional.of(company));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.importRecentAnnualStatements(
                        "AAPL",
                        3
                )
        );

        assertEquals(
                "Company does not have CIK. Please import company from SEC first.",
                exception.getMessage()
        );

        verifyNoInteractions(
                secClient,
                secFinancialFactExtractor,
                financialDataEventPublisher
        );
    }

    @Test
    void importRecentAnnualStatements_whenCompanyHasBlankCik_shouldThrowResourceNotFoundException() {
        Company company = createCompany();
        company.setCik("   ");

        when(companyRepository.findByTickerIgnoreCase("AAPL"))
                .thenReturn(Optional.of(company));

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.importRecentAnnualStatements(
                        "AAPL",
                        3
                )
        );

        verifyNoInteractions(
                secClient,
                secFinancialFactExtractor,
                financialDataEventPublisher
        );
    }

    @Test
    void importLatestAnnualStatement_shouldImportOneYearAndReturnFirstStatement() {
        Company company = createCompany();

        JsonNode companyFacts = mock(JsonNode.class);

        ExtractedFinancialStatement extracted =
                createExtractedStatement(
                        2025,
                        "1000",
                        "400",
                        "300",
                        "250"
                );

        when(companyRepository.findByTickerIgnoreCase("AAPL"))
                .thenReturn(Optional.of(company));

        when(secClient.getCompanyFacts("0000320193"))
                .thenReturn(companyFacts);

        when(secFinancialFactExtractor.extractRecentAnnualStatements(
                companyFacts, 1))
                .thenReturn(List.of(extracted));

        when(financialStatementRepository
                .findByCompanyAndFiscalYearAndPeriodType(
                        company,
                        2025,
                        PeriodType.ANNUAL))
                .thenReturn(Optional.empty());

        when(financialStatementRepository.save(any(FinancialStatement.class)))
                .thenAnswer(invocation -> {
                    FinancialStatement statement = invocation.getArgument(0);
                    statement.setId(101L);
                    return statement;
                });

        FinancialStatementResponse result =
                service.importLatestAnnualStatement("AAPL");

        assertEquals(101L, result.id());
        assertEquals(2025, result.fiscalYear());
        assertEquals("AAPL", result.ticker());

        verify(secFinancialFactExtractor)
                .extractRecentAnnualStatements(companyFacts, 1);
    }

    @Test
    void importLatestAnnualStatement_whenExtractorReturnsNothing_shouldThrowResourceNotFoundException() {
        Company company = createCompany();

        JsonNode companyFacts = mock(JsonNode.class);

        when(companyRepository.findByTickerIgnoreCase("AAPL"))
                .thenReturn(Optional.of(company));

        when(secClient.getCompanyFacts("0000320193"))
                .thenReturn(companyFacts);

        when(secFinancialFactExtractor.extractRecentAnnualStatements(
                companyFacts, 1))
                .thenReturn(List.of());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.importLatestAnnualStatement("AAPL")
        );

        assertEquals(
                "No annual financial statement imported for ticker: AAPL",
                exception.getMessage()
        );

        verify(financialStatementRepository, never())
                .save(any());
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

    private ExtractedFinancialStatement createExtractedStatement(
            int fiscalYear,
            String revenue,
            String grossProfit,
            String operatingIncome,
            String netIncome
    ) {
        return new ExtractedFinancialStatement(
                fiscalYear,

                new BigDecimal(revenue),
                new BigDecimal(grossProfit),
                new BigDecimal(operatingIncome),
                new BigDecimal(netIncome),

                new BigDecimal("3500"),
                new BigDecimal("2500"),
                new BigDecimal("1000"),
                new BigDecimal("500"),
                new BigDecimal("100"),
                new BigDecimal("200"),
                new BigDecimal("1500"),
                new BigDecimal("1200"),

                new BigDecimal("600"),
                new BigDecimal("150")
        );
    }
}