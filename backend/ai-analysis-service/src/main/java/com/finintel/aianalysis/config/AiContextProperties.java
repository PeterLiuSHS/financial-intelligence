package com.finintel.aianalysis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.context")
public record AiContextProperties(
        Integer maxNodes,
        Integer maxMetricsYears,
        Integer maxFindings,
        Boolean includePreviousReports,
        Integer maxPreviousReports
) {
    public int resolvedMaxNodes() {
        return maxNodes == null ? 5 : maxNodes;
    }

    public int resolvedMaxMetricsYears() {
        return maxMetricsYears == null ? 3 : maxMetricsYears;
    }

    public int resolvedMaxFindings() {
        return maxFindings == null ? 10 : maxFindings;
    }

    public boolean resolvedIncludePreviousReports() {
        return includePreviousReports != null && includePreviousReports;
    }

    public int resolvedMaxPreviousReports() {
        return maxPreviousReports == null ? 2 : maxPreviousReports;
    }
}