package com.finintel.financialdata.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public TopicExchange financialExchange() {
        return new TopicExchange(RabbitMqNames.FINANCIAL_EXCHANGE);
    }

    @Bean
    public Queue financialDataImportedQueue() {
        return new Queue(RabbitMqNames.FINANCIAL_DATA_IMPORTED_QUEUE, true);
    }

    @Bean
    public Binding financialDataImportedBinding(
            Queue financialDataImportedQueue,
            TopicExchange financialExchange
    ) {
        return BindingBuilder
                .bind(financialDataImportedQueue)
                .to(financialExchange)
                .with(RabbitMqNames.FINANCIAL_DATA_IMPORTED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}