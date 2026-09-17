package com.finintel.financialdata.event;

import com.finintel.common.event.FinancialDataImportedEvent;
import com.finintel.financialdata.config.RabbitMqNames;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class FinancialDataEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public FinancialDataEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishFinancialDataImported(
            FinancialDataImportedEvent event
    ) {
        rabbitTemplate.convertAndSend(
                RabbitMqNames.FINANCIAL_EXCHANGE,
                RabbitMqNames.FINANCIAL_DATA_IMPORTED_ROUTING_KEY,
                event
        );

        System.out.println("Published financial.data.imported event: " + event);
    }
}