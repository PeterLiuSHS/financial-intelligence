package com.finintel.financialanalytics;

import com.finintel.financialanalytics.config.FinancialDataServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(FinancialDataServiceProperties.class)
public class FinancialAnalyticsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinancialAnalyticsServiceApplication.class, args);
	}
}