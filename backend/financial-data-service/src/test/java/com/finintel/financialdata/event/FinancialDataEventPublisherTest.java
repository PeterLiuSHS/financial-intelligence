package com.finintel.financialdata.event;

import com.finintel.common.event.FinancialDataImportedEvent;
import com.finintel.financialdata.config.RabbitMqNames;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FinancialDataEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private FinancialDataEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new FinancialDataEventPublisher(
                rabbitTemplate
        );
    }

    @Test
    void publishFinancialDataImported_shouldSendEventToCorrectExchangeAndRoutingKey() {
        FinancialDataImportedEvent event =
                new FinancialDataImportedEvent(
                        1L,
                        "AAPL",
                        "ANNUAL",
                        List.of(2025, 2024, 2023)
                );

        publisher.publishFinancialDataImported(event);

        verify(rabbitTemplate).convertAndSend(
                RabbitMqNames.FINANCIAL_EXCHANGE,
                RabbitMqNames.FINANCIAL_DATA_IMPORTED_ROUTING_KEY,
                event
        );
    }
}