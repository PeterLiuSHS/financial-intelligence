package com.finintel.aianalysis.dto;

import java.util.List;

public record GeminiGenerateContentResponse(
        List<Candidate> candidates
) {
    public record Candidate(
            Content content,
            String finishReason
    ) {
    }

    public record Content(
            List<Part> parts,
            String role
    ) {
    }

    public record Part(
            String text
    ) {
    }
}