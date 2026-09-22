package com.finintel.financialdata.service;

import com.finintel.financialdata.client.SecClient;
import com.finintel.financialdata.dto.SecCompanyTickerDto;
import com.finintel.financialdata.dto.SecFilingDto;
import com.finintel.financialdata.exception.ResourceNotFoundException;
import com.finintel.financialdata.parser.SecFilingParser;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
public class SecFilingService {

    private final SecClient secClient;
    private final SecFilingParser secFilingParser;

    public SecFilingService(SecClient secClient, SecFilingParser secFilingParser) {
        this.secClient = secClient;
        this.secFilingParser = secFilingParser;
    }

    public SecFilingDto getLatest10K(String ticker) {

        SecCompanyTickerDto company = secClient.findCompanyByTicker(ticker);

        String cik = secClient.formatCik(company.cikStr());

        JsonNode submissions = secClient.getCompanySubmissions(cik);

        JsonNode recent = submissions.path("filings").path("recent");

        JsonNode forms = recent.path("form");
        JsonNode filingDates = recent.path("filingDate");
        JsonNode accessionNumbers = recent.path("accessionNumber");
        JsonNode primaryDocuments = recent.path("primaryDocument");

        for (int i=0; i<forms.size(); i++){

            if ("10-K".equals(forms.get(i).asText())){
                return new SecFilingDto(
                        forms.get(i).asText(),
                        filingDates.get(i).asText(),
                        accessionNumbers.get(i).asText(),
                        primaryDocuments.get(i).asText()
                );
            }
        }

        throw new ResourceNotFoundException(
                "No 10-K filing found for ticker: " + ticker
        );
    }

    public String getLatest10KDocument(String ticker) {

        SecCompanyTickerDto company = secClient.findCompanyByTicker(ticker);

        String cik = secClient.formatCik(company.cikStr());

        SecFilingDto filing = getLatest10K(ticker);

        return secClient.getFilingDocument(
                cik,
                filing.accessionNumber(),
                filing.primaryDocument()
        );
    }

    public String getLatest10KText(String ticker) {

        String html = getLatest10KDocument(ticker);

        return secFilingParser.extractText(html);
    }

    public String getLatest10KRiskFactors(String ticker) {

        String text =
                getLatest10KText(ticker);

        return secFilingParser.extractRiskFactors(text);
    }
}
