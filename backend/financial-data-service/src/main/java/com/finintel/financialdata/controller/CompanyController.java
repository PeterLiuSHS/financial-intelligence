package com.finintel.financialdata.controller;

import com.finintel.financialdata.dto.CompanyResponse;
import com.finintel.financialdata.dto.CreateCompanyRequest;
import com.finintel.financialdata.dto.SecCompanyImportResponse;
import com.finintel.financialdata.service.CompanyService;
import com.finintel.financialdata.service.SecCompanyImportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;
    private final SecCompanyImportService secCompanyImportService;

    public CompanyController(
            CompanyService companyService,
            SecCompanyImportService secCompanyImportService
    ) {
        this.companyService = companyService;
        this.secCompanyImportService = secCompanyImportService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponse createCompany(
            @Valid @RequestBody CreateCompanyRequest request
    ) {
        return companyService.createCompany(request);
    }

    @PostMapping("/{ticker}/import-from-sec")
    public SecCompanyImportResponse importCompanyFromSec(
            @PathVariable String ticker
    ) {
        return secCompanyImportService.importCompanyFromSec(ticker);
    }

    @GetMapping("/{ticker}")
    public CompanyResponse getCompanyByTicker(
            @PathVariable String ticker
    ) {
        return companyService.getCompanyByTicker(ticker);
    }

    @GetMapping
    public List<CompanyResponse> getAllCompanies() {
        return companyService.getAllCompanies();
    }
}