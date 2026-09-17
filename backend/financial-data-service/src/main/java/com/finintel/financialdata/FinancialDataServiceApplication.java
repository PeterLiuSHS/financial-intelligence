package com.finintel.financialdata;

import com.finintel.financialdata.config.SecApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SecApiProperties.class)
public class FinancialDataServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancialDataServiceApplication.class, args);
    }

}
