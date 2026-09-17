package com.finintel.aianalysis;

import com.finintel.aianalysis.config.AiContextProperties;
import com.finintel.aianalysis.config.AiProviderProperties;
import com.finintel.aianalysis.config.FinancialAnalyticsServiceProperties;
import com.finintel.aianalysis.config.FinancialDataServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        FinancialAnalyticsServiceProperties.class,
        FinancialDataServiceProperties.class,
        AiProviderProperties.class,
        AiContextProperties.class
})
public class AiAnalysisServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAnalysisServiceApplication.class, args);
    }
}