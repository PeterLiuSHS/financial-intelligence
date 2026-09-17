package com.finintel.aianalysis.dto;

import java.util.List;

public record AiContextPackage(
        String ticker,
        String companyName,
        String reportType,
        List<AiContextNode> nodes
) {
}