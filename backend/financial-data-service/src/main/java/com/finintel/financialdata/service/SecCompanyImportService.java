package com.finintel.financialdata.service;

import com.finintel.financialdata.client.SecClient;
import com.finintel.financialdata.dto.SecCompanyImportResponse;
import com.finintel.financialdata.dto.SecCompanyTickerDto;
import com.finintel.financialdata.entity.Company;
import com.finintel.financialdata.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecCompanyImportService {

    private final SecClient secClient;
    private final CompanyRepository companyRepository;

    public SecCompanyImportService(
            SecClient secClient,
            CompanyRepository companyRepository
    ) {
        this.secClient = secClient;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public SecCompanyImportResponse importCompanyFromSec(String ticker) {
        SecCompanyTickerDto secCompany = secClient.findCompanyByTicker(ticker);

        String normalizedTicker = secCompany.ticker().toUpperCase();
        String formattedCik = secClient.formatCik(secCompany.cikStr());

        Company company = companyRepository.findByTickerIgnoreCase(normalizedTicker)
                .map(existingCompany -> {
                    existingCompany.setName(secCompany.title());
                    existingCompany.setCik(formattedCik);
                    existingCompany.setCountry("US");
                    return existingCompany;
                })
                .orElseGet(() -> Company.builder()
                        .ticker(normalizedTicker)
                        .name(secCompany.title())
                        .cik(formattedCik)
                        .country("US")
                        .build());

        Company savedCompany = companyRepository.save(company);

        return new SecCompanyImportResponse(
                savedCompany.getId(),
                savedCompany.getTicker(),
                savedCompany.getName(),
                savedCompany.getCik(),
                "Company imported from SEC ticker mapping"
        );
    }
}