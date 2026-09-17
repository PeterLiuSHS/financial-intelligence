package com.finintel.aianalysis.dto;

import java.util.List;

public record AiOutputValidationResult(
        boolean valid,
        List<String> violations
) {
}