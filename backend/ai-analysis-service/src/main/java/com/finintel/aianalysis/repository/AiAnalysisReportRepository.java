package com.finintel.aianalysis.repository;

import com.finintel.aianalysis.entity.AiAnalysisReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiAnalysisReportRepository extends JpaRepository<AiAnalysisReport, Long> {

    List<AiAnalysisReport> findByTickerOrderByCreatedAtDesc(String ticker);
}