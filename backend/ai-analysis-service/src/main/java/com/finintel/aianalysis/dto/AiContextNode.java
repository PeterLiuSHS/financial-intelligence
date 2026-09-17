package com.finintel.aianalysis.dto;

public record AiContextNode(
        String nodeType,
        String title,
        String content,
        Integer priority
) {
}