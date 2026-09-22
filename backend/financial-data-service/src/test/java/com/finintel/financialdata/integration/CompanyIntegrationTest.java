package com.finintel.financialdata.integration;

import com.finintel.financialdata.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CompanyIntegrationTest
        extends AbstractIntegrationTest {

    @Autowired
    private CompanyRepository companyRepository;

    @BeforeEach
    void cleanDatabase() {
        companyRepository.deleteAll();
    }

    @Test
    void createCompany_shouldPersistCompanyInMySql()
            throws Exception {

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
                        jsonPath("$.ticker")
                                .value("AAPL")
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Apple Inc.")
                )
                .andExpect(
                        jsonPath("$.cik")
                                .value("0000320193")
                )
                .andExpect(
                        jsonPath("$.country")
                                .value("US")
                );

        var company =
                companyRepository
                        .findByTickerIgnoreCase("AAPL");

        assertTrue(company.isPresent());

        assertEquals(
                "Apple Inc.",
                company.get().getName()
        );

        assertEquals(
                "0000320193",
                company.get().getCik()
        );

        assertEquals(
                "NASDAQ",
                company.get().getExchange()
        );
    }

    @Test
    void createThenGetCompany_shouldReturnPersistedCompany()
            throws Exception {

        String requestBody = """
                {
                  "ticker": "MSFT",
                  "name": "Microsoft Corporation",
                  "cik": "0000789019",
                  "exchange": "NASDAQ",
                  "sector": "Technology",
                  "industry": "Software",
                  "country": "US"
                }
                """;

        /*
         * First HTTP request:
         * Controller -> Service -> Repository -> MySQL
         */
        mockMvc.perform(
                        post("/api/companies")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isCreated()
                );

        /*
         * Second HTTP request:
         * MySQL -> Repository -> Service -> Controller
         */
        mockMvc.perform(
                        get("/api/companies/MSFT")
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.ticker")
                                .value("MSFT")
                )
                .andExpect(
                        jsonPath("$.name")
                                .value(
                                        "Microsoft Corporation"
                                )
                )
                .andExpect(
                        jsonPath("$.cik")
                                .value("0000789019")
                )
                .andExpect(
                        jsonPath("$.exchange")
                                .value("NASDAQ")
                )
                .andExpect(
                        jsonPath("$.industry")
                                .value("Software")
                );
    }

    @Test
    void createDuplicateCompany_shouldReturn409()
            throws Exception {

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
                .andExpect(
                        status().isCreated()
                );

        /*
         * Same ticker again.
         *
         * This now tests the real chain:
         *
         * CompanyController
         *      ->
         * CompanyService
         *      ->
         * CompanyRepository
         *      ->
         * DuplicateResourceException
         *      ->
         * GlobalExceptionHandler
         *      ->
         * HTTP 409
         */
        mockMvc.perform(
                        post("/api/companies")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isConflict()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(409)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Conflict")
                );
    }

    @Test
    void createCompany_withMissingTicker_shouldReturn400()
            throws Exception {

        String invalidRequest = """
                {
                  "name": "Apple Inc."
                }
                """;

        mockMvc.perform(
                        post("/api/companies")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(invalidRequest)
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Bad Request")
                );

        /*
         * Validation should fail before anything
         * is inserted into MySQL.
         */
        assertEquals(
                0,
                companyRepository.count()
        );
    }
}