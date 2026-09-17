package com.finintel.financialanalytics.repository;

import com.finintel.financialanalytics.entity.FinancialMetric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FinancialMetricRepository extends JpaRepository<FinancialMetric, Long> {

    Optional<FinancialMetric> findByCompanyIdAndFiscalYearAndPeriodType(
            Long companyId,
            Integer fiscalYear,
            String periodType
    );

    List<FinancialMetric> findByTickerOrderByFiscalYearDesc(String ticker);
}