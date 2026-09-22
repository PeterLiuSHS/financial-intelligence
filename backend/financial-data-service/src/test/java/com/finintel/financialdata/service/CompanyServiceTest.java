package com.finintel.financialdata.service;

import com.finintel.financialdata.dto.CompanyResponse;
import com.finintel.financialdata.dto.CreateCompanyRequest;
import com.finintel.financialdata.entity.Company;
import com.finintel.financialdata.exception.DuplicateResourceException;
import com.finintel.financialdata.exception.ResourceNotFoundException;
import com.finintel.financialdata.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    private CompanyService companyService;

    @BeforeEach
    void setUp() {
        companyService = new CompanyService(companyRepository);
    }

    @Test
    void createCompany_shouldSaveAndReturnCompany() {
        CreateCompanyRequest request = new CreateCompanyRequest(
                "aapl",
                "Apple Inc.",
                "0000320193",
                "NASDAQ",
                "Technology",
                "Consumer Electronics",
                "US"
        );

        when(companyRepository.existsByTickerIgnoreCase("aapl"))
                .thenReturn(false);

        Company savedCompany = Company.builder()
                .id(1L)
                .ticker("AAPL")
                .name("Apple Inc.")
                .cik("0000320193")
                .exchange("NASDAQ")
                .sector("Technology")
                .industry("Consumer Electronics")
                .country("US")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(companyRepository.save(any(Company.class)))
                .thenReturn(savedCompany);

        CompanyResponse result = companyService.createCompany(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("AAPL", result.ticker());
        assertEquals("Apple Inc.", result.name());
        assertEquals("0000320193", result.cik());
        assertEquals("NASDAQ", result.exchange());
        assertEquals("Technology", result.sector());
        assertEquals("Consumer Electronics", result.industry());
        assertEquals("US", result.country());

        ArgumentCaptor<Company> captor =
                ArgumentCaptor.forClass(Company.class);

        verify(companyRepository).save(captor.capture());

        Company companyToSave = captor.getValue();

        assertEquals("AAPL", companyToSave.getTicker());
        assertEquals("Apple Inc.", companyToSave.getName());
        assertEquals("0000320193", companyToSave.getCik());
    }

    @Test
    void createCompany_whenTickerAlreadyExists_shouldThrowDuplicateResourceException() {
        CreateCompanyRequest request = new CreateCompanyRequest(
                "AAPL",
                "Apple Inc.",
                "0000320193",
                "NASDAQ",
                "Technology",
                "Consumer Electronics",
                "US"
        );

        when(companyRepository.existsByTickerIgnoreCase("AAPL"))
                .thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> companyService.createCompany(request)
        );

        assertEquals(
                "Company already exists with ticker: AAPL",
                exception.getMessage()
        );

        verify(companyRepository, never()).save(any());
    }

    @Test
    void getCompanyByTicker_shouldReturnCompany() {
        Company company = Company.builder()
                .id(1L)
                .ticker("AAPL")
                .name("Apple Inc.")
                .cik("0000320193")
                .exchange("NASDAQ")
                .sector("Technology")
                .industry("Consumer Electronics")
                .country("US")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(companyRepository.findByTickerIgnoreCase("AAPL"))
                .thenReturn(Optional.of(company));

        CompanyResponse result =
                companyService.getCompanyByTicker("AAPL");

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("AAPL", result.ticker());
        assertEquals("Apple Inc.", result.name());
        assertEquals("0000320193", result.cik());

        verify(companyRepository)
                .findByTickerIgnoreCase("AAPL");
    }

    @Test
    void getCompanyByTicker_whenCompanyDoesNotExist_shouldThrowResourceNotFoundException() {
        when(companyRepository.findByTickerIgnoreCase("MSFT"))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> companyService.getCompanyByTicker("MSFT")
        );

        assertEquals(
                "Company not found with ticker: MSFT",
                exception.getMessage()
        );
    }

    @Test
    void getAllCompanies_shouldReturnAllCompanies() {
        Company apple = Company.builder()
                .id(1L)
                .ticker("AAPL")
                .name("Apple Inc.")
                .build();

        Company microsoft = Company.builder()
                .id(2L)
                .ticker("MSFT")
                .name("Microsoft Corporation")
                .build();

        when(companyRepository.findAll())
                .thenReturn(List.of(apple, microsoft));

        List<CompanyResponse> result =
                companyService.getAllCompanies();

        assertEquals(2, result.size());

        assertEquals("AAPL", result.get(0).ticker());
        assertEquals("Apple Inc.", result.get(0).name());

        assertEquals("MSFT", result.get(1).ticker());
        assertEquals(
                "Microsoft Corporation",
                result.get(1).name()
        );

        verify(companyRepository).findAll();
    }

    @Test
    void getAllCompanies_whenRepositoryIsEmpty_shouldReturnEmptyList() {
        when(companyRepository.findAll())
                .thenReturn(List.of());

        List<CompanyResponse> result =
                companyService.getAllCompanies();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}