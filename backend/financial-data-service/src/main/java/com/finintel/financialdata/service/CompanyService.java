package com.finintel.financialdata.service;

import com.finintel.financialdata.dto.CompanyResponse;
import com.finintel.financialdata.dto.CreateCompanyRequest;
import com.finintel.financialdata.entity.Company;
import com.finintel.financialdata.exception.DuplicateResourceException;
import com.finintel.financialdata.exception.ResourceNotFoundException;
import com.finintel.financialdata.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional
    public CompanyResponse createCompany(CreateCompanyRequest request) {
        if (companyRepository.existsByTickerIgnoreCase(request.ticker())) {
            throw new DuplicateResourceException(
                    "Company already exists with ticker: " + request.ticker()
            );
        }

        Company company = Company.builder()
                .ticker(request.ticker().toUpperCase())
                .name(request.name())
                .cik(request.cik())
                .exchange(request.exchange())
                .sector(request.sector())
                .industry(request.industry())
                .country(request.country())
                .build();

        Company savedCompany = companyRepository.save(company);

        return toResponse(savedCompany);
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompanyByTicker(String ticker) {
        Company company = companyRepository.findByTickerIgnoreCase(ticker)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found with ticker: " + ticker
                ));

        return toResponse(company);
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> getAllCompanies() {
        return companyRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CompanyResponse toResponse(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getTicker(),
                company.getName(),
                company.getCik(),
                company.getExchange(),
                company.getSector(),
                company.getIndustry(),
                company.getCountry(),
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }
}