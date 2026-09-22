package com.finintel.financialdata.controller;

import com.finintel.financialdata.dto.SecFilingDto;
import com.finintel.financialdata.service.SecFilingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SecFilingControllerTest {

    private MockMvc mockMvc;
    private SecFilingService secFilingService;

    @BeforeEach
    void setUp() {
        secFilingService = mock(SecFilingService.class);

        SecFilingController controller =
                new SecFilingController(secFilingService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void getLatest10K_shouldReturn200AndFilingMetadata()
            throws Exception {

        SecFilingDto filing =
                new SecFilingDto(
                        "10-K",
                        "2025-10-31",
                        "0000320193-25-000079",
                        "aapl-20250927.htm"
                );

        when(secFilingService.getLatest10K("AAPL"))
                .thenReturn(filing);

        mockMvc.perform(
                        get("/api/companies/AAPL/filings/latest-10-k")
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.form").value("10-K")
                )
                .andExpect(
                        jsonPath("$.filingDate")
                                .value("2025-10-31")
                )
                .andExpect(
                        jsonPath("$.accessionNumber")
                                .value("0000320193-25-000079")
                )
                .andExpect(
                        jsonPath("$.primaryDocument")
                                .value("aapl-20250927.htm")
                );

        verify(secFilingService)
                .getLatest10K("AAPL");
    }

    @Test
    void getLatest10KDocument_shouldReturn200AndHtml()
            throws Exception {

        String html = """
                <html>
                    <body>
                        <h1>Apple 2025 Form 10-K</h1>
                    </body>
                </html>
                """;

        when(secFilingService.getLatest10KDocument("AAPL"))
                .thenReturn(html);

        mockMvc.perform(
                        get(
                                "/api/companies/AAPL/filings/latest-10-k/document"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.TEXT_HTML
                        )
                )
                .andExpect(
                        content().string(html)
                );

        verify(secFilingService)
                .getLatest10KDocument("AAPL");
    }

    @Test
    void getLatest10KText_shouldReturn200AndPlainText()
            throws Exception {

        String text =
                "Apple Inc. 2025 Form 10-K annual report.";

        when(secFilingService.getLatest10KText("AAPL"))
                .thenReturn(text);

        mockMvc.perform(
                        get(
                                "/api/companies/AAPL/filings/latest-10-k/text"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.TEXT_PLAIN
                        )
                )
                .andExpect(
                        content().string(text)
                );

        verify(secFilingService)
                .getLatest10KText("AAPL");
    }

    @Test
    void getLatest10KRiskFactors_shouldReturn200AndRiskFactors()
            throws Exception {

        String riskFactors = """
                Item 1A. Risk Factors

                The Company is subject to supply chain,
                geopolitical and operational risks.
                """;

        when(secFilingService.getLatest10KRiskFactors("AAPL"))
                .thenReturn(riskFactors);

        mockMvc.perform(
                        get(
                                "/api/companies/AAPL/filings/latest-10-k/risk-factors"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.TEXT_PLAIN
                        )
                )
                .andExpect(
                        content().string(riskFactors)
                );

        verify(secFilingService)
                .getLatest10KRiskFactors("AAPL");
    }
}