package com.finintel.financialdata.service;

import com.finintel.financialdata.client.SecClient;
import com.finintel.financialdata.dto.SecCompanyImportResponse;
import com.finintel.financialdata.dto.SecCompanyTickerDto;
import com.finintel.financialdata.entity.Company;
import com.finintel.financialdata.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecCompanyImportServiceTest {

    @Mock
    private SecClient secClient;

    @Mock
    private CompanyRepository companyRepository;

    private SecCompanyImportService service;

    @BeforeEach
    void setUp() {
        service = new SecCompanyImportService(
                secClient,
                companyRepository
        );
    }

    @Test
    void importCompanyFromSec_whenCompanyDoesNotExist_shouldCreateCompany() {
        SecCompanyTickerDto secCompany =
                new SecCompanyTickerDto(
                        320193,
                        "aapl",
                        "Apple Inc."
                );

        when(secClient.findCompanyByTicker("AAPL"))
                .thenReturn(secCompany);

        when(secClient.formatCik(320193))
                .thenReturn("0000320193");

        when(companyRepository.findByTickerIgnoreCase("AAPL"))
                .thenReturn(Optional.empty());

        when(companyRepository.save(any(Company.class)))
                .thenAnswer(invocation -> {
                    Company company = invocation.getArgument(0);
                    company.setId(1L);
                    return company;
                });

        SecCompanyImportResponse result =
                service.importCompanyFromSec("AAPL");

        assertNotNull(result);
        assertEquals(1L, result.companyId());
        assertEquals("AAPL", result.ticker());
        assertEquals("Apple Inc.", result.name());
        assertEquals("0000320193", result.cik());
        assertEquals(
                "Company imported from SEC ticker mapping",
                result.message()
        );

        ArgumentCaptor<Company> captor =
                ArgumentCaptor.forClass(Company.class);

        verify(companyRepository).save(captor.capture());

        Company saved = captor.getValue();

        assertEquals("AAPL", saved.getTicker());
        assertEquals("Apple Inc.", saved.getName());
        assertEquals("0000320193", saved.getCik());
        assertEquals("US", saved.getCountry());
    }

    @Test
    void importCompanyFromSec_whenCompanyAlreadyExists_shouldUpdateCompany() {
        Company existingCompany = Company.builder()
                .id(1L)
                .ticker("AAPL")
                .name("Old Apple Name")
                .cik("OLD_CIK")
                .country(null)
                .build();

        SecCompanyTickerDto secCompany =
                new SecCompanyTickerDto(
                        320193,
                        "AAPL",
                        "Apple Inc."
                );

        when(secClient.findCompanyByTicker("AAPL"))
                .thenReturn(secCompany);

        when(secClient.formatCik(320193))
                .thenReturn("0000320193");

        when(companyRepository.findByTickerIgnoreCase("AAPL"))
                .thenReturn(Optional.of(existingCompany));

        when(companyRepository.save(existingCompany))
                .thenReturn(existingCompany);

        SecCompanyImportResponse result =
                service.importCompanyFromSec("AAPL");

        assertNotNull(result);

        assertEquals(1L, result.companyId());
        assertEquals("AAPL", result.ticker());
        assertEquals("Apple Inc.", result.name());
        assertEquals("0000320193", result.cik());

        assertEquals(
                "Apple Inc.",
                existingCompany.getName()
        );

        assertEquals(
                "0000320193",
                existingCompany.getCik()
        );

        assertEquals(
                "US",
                existingCompany.getCountry()
        );

        verify(companyRepository).save(existingCompany);
    }

    @Test
    void importCompanyFromSec_shouldNormalizeTickerReturnedBySec() {
        SecCompanyTickerDto secCompany =
                new SecCompanyTickerDto(
                        320193,
                        "aapl",
                        "Apple Inc."
                );

        when(secClient.findCompanyByTicker("aapl"))
                .thenReturn(secCompany);

        when(secClient.formatCik(320193))
                .thenReturn("0000320193");

        when(companyRepository.findByTickerIgnoreCase("AAPL"))
                .thenReturn(Optional.empty());

        when(companyRepository.save(any(Company.class)))
                .thenAnswer(invocation -> {
                    Company company = invocation.getArgument(0);
                    company.setId(1L);
                    return company;
                });

        SecCompanyImportResponse result =
                service.importCompanyFromSec("aapl");

        assertEquals("AAPL", result.ticker());

        verify(companyRepository)
                .findByTickerIgnoreCase("AAPL");
    }

    @Test
    void importCompanyFromSec_shouldUseSecClientToResolveCompanyAndCik() {
        SecCompanyTickerDto secCompany =
                new SecCompanyTickerDto(
                        320193,
                        "AAPL",
                        "Apple Inc."
                );

        when(secClient.findCompanyByTicker("AAPL"))
                .thenReturn(secCompany);

        when(secClient.formatCik(320193))
                .thenReturn("0000320193");

        when(companyRepository.findByTickerIgnoreCase("AAPL"))
                .thenReturn(Optional.empty());

        when(companyRepository.save(any(Company.class)))
                .thenAnswer(invocation -> {
                    Company company = invocation.getArgument(0);
                    company.setId(1L);
                    return company;
                });

        service.importCompanyFromSec("AAPL");

        verify(secClient).findCompanyByTicker("AAPL");
        verify(secClient).formatCik(320193);
    }
}