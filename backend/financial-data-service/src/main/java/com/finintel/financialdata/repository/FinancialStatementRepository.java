package com.finintel.financialdata.repository;

import com.finintel.financialdata.entity.Company;
import com.finintel.financialdata.entity.FinancialStatement;
import com.finintel.financialdata.entity.PeriodType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FinancialStatementRepository extends JpaRepository<FinancialStatement, Long> {

    List<FinancialStatement> findByCompanyOrderByFiscalYearDesc(Company company);

    Optional<FinancialStatement> findByCompanyAndFiscalYearAndPeriodType(
            Company company,
            Integer fiscalYear,
            PeriodType periodType
    );
}