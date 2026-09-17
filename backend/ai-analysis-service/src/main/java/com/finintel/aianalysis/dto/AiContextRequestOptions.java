package com.finintel.aianalysis.dto;

public record AiContextRequestOptions(
        Integer maxNodes,
        Integer maxMetricsYears,
        Integer maxFindings,
        Boolean includePreviousReports,
        Integer maxPreviousReports
) {
}