package com.finintel.financialdata.controller;

import com.finintel.financialdata.dto.CompanyResponse;
import com.finintel.financialdata.dto.SecCompanyImportResponse;
import com.finintel.financialdata.service.CompanyService;
import com.finintel.financialdata.service.SecCompanyImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CompanyControllerTest {

    private MockMvc mockMvc;

    private CompanyService companyService;
    private SecCompanyImportService secCompanyImportService;

    @BeforeEach
    void setUp() {
        companyService = mock(CompanyService.class);
        secCompanyImportService = mock(SecCompanyImportService.class);

        CompanyController controller =
                new CompanyController(
                        companyService,
                        secCompanyImportService
                );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

    }

    @Test
    void createCompany_shouldReturn201AndCreatedCompany()
            throws Exception {

        LocalDateTime now =
                LocalDateTime.of(
                        2026,
                        9,
                        22,
                        1,
                        30
                );

        CompanyResponse response =
                new CompanyResponse(
                        1L,
                        "AAPL",
                        "Apple Inc.",
                        "0000320193",
                        "NASDAQ",
                        "Technology",
                        "Consumer Electronics",
                        "US",
                        now,
                        now
                );

        when(companyService.createCompany(any()))
                .thenReturn(response);

        String requestBody = """
                {
                  "ticker": "AAPL",
                  "name": "Apple Inc.",
                  "cik": "0000320193",
                  "exchange": "NASDAQ",
                  "sector": "Technology",
                  "industry": "Consumer Electronics",
                  "country": "US"
                }
                """;

        mockMvc.perform(
                        post("/api/companies")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.id").value(1)
                )
                .andExpect(
                        jsonPath("$.ticker").value("AAPL")
                )
                .andExpect(
                        jsonPath("$.name").value("Apple Inc.")
                )
                .andExpect(
                        jsonPath("$.cik").value("0000320193")
                )
                .andExpect(
                        jsonPath("$.exchange").value("NASDAQ")
                )
                .andExpect(
                        jsonPath("$.sector").value("Technology")
                )
                .andExpect(
                        jsonPath("$.industry")
                                .value("Consumer Electronics")
                )
                .andExpect(
                        jsonPath("$.country").value("US")
                );

        verify(companyService)
                .createCompany(any());
    }

    @Test
    void createCompany_whenTickerMissing_shouldReturn400()
            throws Exception {

        String requestBody = """
                {
                  "name": "Apple Inc."
                }
                """;

        mockMvc.perform(
                        post("/api/companies")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(companyService);
    }

    @Test
    void createCompany_whenNameMissing_shouldReturn400()
            throws Exception {

        String requestBody = """
                {
                  "ticker": "AAPL"
                }
                """;

        mockMvc.perform(
                        post("/api/companies")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(companyService);
    }

    @Test
    void getCompanyByTicker_shouldReturn200AndCompany()
            throws Exception {

        LocalDateTime now =
                LocalDateTime.of(
                        2026,
                        9,
                        22,
                        1,
                        30
                );

        CompanyResponse response =
                new CompanyResponse(
                        1L,
                        "AAPL",
                        "Apple Inc.",
                        "0000320193",
                        "NASDAQ",
                        "Technology",
                        "Consumer Electronics",
                        "US",
                        now,
                        now
                );

        when(companyService.getCompanyByTicker("AAPL"))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/companies/AAPL")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(1)
                )
                .andExpect(
                        jsonPath("$.ticker").value("AAPL")
                )
                .andExpect(
                        jsonPath("$.name").value("Apple Inc.")
                )
                .andExpect(
                        jsonPath("$.country").value("US")
                );

        verify(companyService)
                .getCompanyByTicker("AAPL");
    }

    @Test
    void getAllCompanies_shouldReturn200AndCompanyList()
            throws Exception {

        LocalDateTime now =
                LocalDateTime.of(
                        2026,
                        9,
                        22,
                        1,
                        30
                );

        CompanyResponse apple =
                new CompanyResponse(
                        1L,
                        "AAPL",
                        "Apple Inc.",
                        "0000320193",
                        "NASDAQ",
                        "Technology",
                        "Consumer Electronics",
                        "US",
                        now,
                        now
                );

        CompanyResponse microsoft =
                new CompanyResponse(
                        2L,
                        "MSFT",
                        "Microsoft Corporation",
                        "0000789019",
                        "NASDAQ",
                        "Technology",
                        "Software",
                        "US",
                        now,
                        now
                );

        when(companyService.getAllCompanies())
                .thenReturn(
                        List.of(
                                apple,
                                microsoft
                        )
                );

        mockMvc.perform(
                        get("/api/companies")
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
                        jsonPath("$[1].ticker")
                                .value("MSFT")
                );

        verify(companyService)
                .getAllCompanies();
    }

    @Test
    void importCompanyFromSec_shouldReturn200AndImportResponse()
            throws Exception {

        SecCompanyImportResponse response =
                new SecCompanyImportResponse(
                        1L,
                        "AAPL",
                        "Apple Inc.",
                        "0000320193",
                        "Company imported successfully"
                );

        when(
                secCompanyImportService
                        .importCompanyFromSec("AAPL")
        ).thenReturn(response);

        mockMvc.perform(
                        post(
                                "/api/companies/AAPL/import-from-sec"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.companyId").value(1)
                )
                .andExpect(
                        jsonPath("$.ticker").value("AAPL")
                )
                .andExpect(
                        jsonPath("$.name").value("Apple Inc.")
                )
                .andExpect(
                        jsonPath("$.cik")
                                .value("0000320193")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Company imported successfully"
                                )
                );

        verify(secCompanyImportService)
                .importCompanyFromSec("AAPL");
    }
}