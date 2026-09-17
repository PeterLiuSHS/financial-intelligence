package com.finintel.aianalysis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai")
public record AiProviderProperties(
        String provider,
        Gemini gemini
) {
    public record Gemini(
            String baseUrl,
            String model,
            String apiKey
    ) {
    }
}