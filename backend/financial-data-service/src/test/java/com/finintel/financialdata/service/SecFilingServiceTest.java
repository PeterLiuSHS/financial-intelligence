package com.finintel.financialdata.service;

import com.finintel.financialdata.client.SecClient;
import com.finintel.financialdata.dto.SecCompanyTickerDto;
import com.finintel.financialdata.dto.SecFilingDto;
import com.finintel.financialdata.exception.ResourceNotFoundException;
import com.finintel.financialdata.parser.SecFilingParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecFilingServiceTest {

    @Mock
    private SecClient secClient;

    @Mock
    private SecFilingParser secFilingParser;

    private SecFilingService service;

    @BeforeEach
    void setUp() {
        service = new SecFilingService(
                secClient,
                secFilingParser
        );
    }

    @Test
    void getLatest10K_shouldReturnLatest10KFiling() {
        SecCompanyTickerDto company =
                new SecCompanyTickerDto(
                        320193,
                        "AAPL",
                        "Apple Inc."
                );

        JsonNode submissions = mock(JsonNode.class);
        JsonNode filings = mock(JsonNode.class);
        JsonNode recent = mock(JsonNode.class);

        JsonNode forms = mock(JsonNode.class);
        JsonNode filingDates = mock(JsonNode.class);
        JsonNode accessionNumbers = mock(JsonNode.class);
        JsonNode primaryDocuments = mock(JsonNode.class);

        JsonNode form0 = mock(JsonNode.class);
        JsonNode form1 = mock(JsonNode.class);

        JsonNode date1 = mock(JsonNode.class);
        JsonNode accession1 = mock(JsonNode.class);
        JsonNode document1 = mock(JsonNode.class);

        when(secClient.findCompanyByTicker("AAPL"))
                .thenReturn(company);

        when(secClient.formatCik(320193))
                .thenReturn("0000320193");

        when(secClient.getCompanySubmissions("0000320193"))
                .thenReturn(submissions);

        when(submissions.path("filings"))
                .thenReturn(filings);

        when(filings.path("recent"))
                .thenReturn(recent);

        when(recent.path("form"))
                .thenReturn(forms);

        when(recent.path("filingDate"))
                .thenReturn(filingDates);

        when(recent.path("accessionNumber"))
                .thenReturn(accessionNumbers);

        when(recent.path("primaryDocument"))
                .thenReturn(primaryDocuments);

        // Two filings: first is not 10-K, second is 10-K
        when(forms.size()).thenReturn(2);

        when(forms.get(0)).thenReturn(form0);
        when(forms.get(1)).thenReturn(form1);

        when(form0.asText()).thenReturn("8-K");
        when(form1.asText()).thenReturn("10-K");

        when(filingDates.get(1)).thenReturn(date1);
        when(accessionNumbers.get(1)).thenReturn(accession1);
        when(primaryDocuments.get(1)).thenReturn(document1);

        when(date1.asText())
                .thenReturn("2025-10-31");

        when(accession1.asText())
                .thenReturn("0000320193-25-000079");

        when(document1.asText())
                .thenReturn("aapl-20250927.htm");

        SecFilingDto result =
                service.getLatest10K("AAPL");

        assertNotNull(result);

        assertEquals(
                "10-K",
                result.form()
        );

        assertEquals(
                "2025-10-31",
                result.filingDate()
        );

        assertEquals(
                "0000320193-25-000079",
                result.accessionNumber()
        );

        assertEquals(
                "aapl-20250927.htm",
                result.primaryDocument()
        );

        verify(secClient)
                .findCompanyByTicker("AAPL");

        verify(secClient)
                .getCompanySubmissions("0000320193");
    }

    @Test
    void getLatest10K_whenNo10KExists_shouldThrowResourceNotFoundException() {
        SecCompanyTickerDto company =
                new SecCompanyTickerDto(
                        320193,
                        "AAPL",
                        "Apple Inc."
                );

        JsonNode submissions = mock(JsonNode.class);
        JsonNode filings = mock(JsonNode.class);
        JsonNode recent = mock(JsonNode.class);
        JsonNode forms = mock(JsonNode.class);

        JsonNode form0 = mock(JsonNode.class);
        JsonNode form1 = mock(JsonNode.class);

        when(secClient.findCompanyByTicker("AAPL"))
                .thenReturn(company);

        when(secClient.formatCik(320193))
                .thenReturn("0000320193");

        when(secClient.getCompanySubmissions("0000320193"))
                .thenReturn(submissions);

        when(submissions.path("filings"))
                .thenReturn(filings);

        when(filings.path("recent"))
                .thenReturn(recent);

        when(recent.path("form"))
                .thenReturn(forms);

        /*
         * These three are still called by the production code
         * before the loop begins.
         */
        when(recent.path("filingDate"))
                .thenReturn(mock(JsonNode.class));

        when(recent.path("accessionNumber"))
                .thenReturn(mock(JsonNode.class));

        when(recent.path("primaryDocument"))
                .thenReturn(mock(JsonNode.class));

        when(forms.size()).thenReturn(2);

        when(forms.get(0)).thenReturn(form0);
        when(forms.get(1)).thenReturn(form1);

        when(form0.asText()).thenReturn("8-K");
        when(form1.asText()).thenReturn("10-Q");

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> service.getLatest10K("AAPL")
                );

        assertEquals(
                "No 10-K filing found for ticker: AAPL",
                exception.getMessage()
        );
    }

    @Test
    void getLatest10KDocument_shouldDownloadCorrectFilingDocument() {
        SecCompanyTickerDto company =
                new SecCompanyTickerDto(
                        320193,
                        "AAPL",
                        "Apple Inc."
                );

        JsonNode submissions = mock(JsonNode.class);
        JsonNode filings = mock(JsonNode.class);
        JsonNode recent = mock(JsonNode.class);

        JsonNode forms = mock(JsonNode.class);
        JsonNode filingDates = mock(JsonNode.class);
        JsonNode accessionNumbers = mock(JsonNode.class);
        JsonNode primaryDocuments = mock(JsonNode.class);

        JsonNode form = mock(JsonNode.class);
        JsonNode filingDate = mock(JsonNode.class);
        JsonNode accession = mock(JsonNode.class);
        JsonNode primaryDocument = mock(JsonNode.class);

        when(secClient.findCompanyByTicker("AAPL"))
                .thenReturn(company);

        when(secClient.formatCik(320193))
                .thenReturn("0000320193");

        when(secClient.getCompanySubmissions("0000320193"))
                .thenReturn(submissions);

        when(submissions.path("filings"))
                .thenReturn(filings);

        when(filings.path("recent"))
                .thenReturn(recent);

        when(recent.path("form"))
                .thenReturn(forms);

        when(recent.path("filingDate"))
                .thenReturn(filingDates);

        when(recent.path("accessionNumber"))
                .thenReturn(accessionNumbers);

        when(recent.path("primaryDocument"))
                .thenReturn(primaryDocuments);

        when(forms.size()).thenReturn(1);
        when(forms.get(0)).thenReturn(form);
        when(form.asText()).thenReturn("10-K");

        when(filingDates.get(0))
                .thenReturn(filingDate);

        when(accessionNumbers.get(0))
                .thenReturn(accession);

        when(primaryDocuments.get(0))
                .thenReturn(primaryDocument);

        when(filingDate.asText())
                .thenReturn("2025-10-31");

        when(accession.asText())
                .thenReturn("0000320193-25-000079");

        when(primaryDocument.asText())
                .thenReturn("aapl-20250927.htm");

        when(secClient.getFilingDocument(
                "0000320193",
                "0000320193-25-000079",
                "aapl-20250927.htm"
        )).thenReturn("<html>Apple 10-K</html>");

        String result =
                service.getLatest10KDocument("AAPL");

        assertEquals(
                "<html>Apple 10-K</html>",
                result
        );

        verify(secClient).getFilingDocument(
                "0000320193",
                "0000320193-25-000079",
                "aapl-20250927.htm"
        );
    }

    @Test
    void getLatest10KText_shouldParseDownloadedHtml() {
        SecFilingService spyService =
                spy(new SecFilingService(
                        secClient,
                        secFilingParser
                ));

        String html =
                "<html><body>Apple filing</body></html>";

        String parsedText =
                "Apple filing";

        doReturn(html)
                .when(spyService)
                .getLatest10KDocument("AAPL");

        when(secFilingParser.extractText(html))
                .thenReturn(parsedText);

        String result =
                spyService.getLatest10KText("AAPL");

        assertEquals(
                parsedText,
                result
        );

        verify(secFilingParser)
                .extractText(html);
    }

    @Test
    void getLatest10KRiskFactors_shouldExtractRiskFactorsFromText() {
        SecFilingService spyService =
                spy(new SecFilingService(
                        secClient,
                        secFilingParser
                ));

        String filingText = """
                Item 1A. Risk Factors
                Supply chain disruptions may affect operations.
                Item 1B. Unresolved Staff Comments
                """;

        String riskFactors =
                "Supply chain disruptions may affect operations.";

        doReturn(filingText)
                .when(spyService)
                .getLatest10KText("AAPL");

        when(secFilingParser.extractRiskFactors(filingText))
                .thenReturn(riskFactors);

        String result =
                spyService.getLatest10KRiskFactors("AAPL");

        assertEquals(
                riskFactors,
                result
        );

        verify(secFilingParser)
                .extractRiskFactors(filingText);
    }
}