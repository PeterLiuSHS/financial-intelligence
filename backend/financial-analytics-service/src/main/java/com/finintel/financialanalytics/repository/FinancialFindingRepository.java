package com.finintel.financialanalytics.repository;

import com.finintel.financialanalytics.entity.FinancialFinding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FinancialFindingRepository extends JpaRepository<FinancialFinding, Long> {

    Optional<FinancialFinding> findByMetricIdAndFindingCode(
            Long metricId,
            String findingCode
    );

    List<FinancialFinding> findByTickerOrderByFiscalYearDesc(
            String ticker
    );

    List<FinancialFinding> findByTickerAndFiscalYearOrderByFindingCodeAsc(
            String ticker,
            Integer fiscalYear
    );
}