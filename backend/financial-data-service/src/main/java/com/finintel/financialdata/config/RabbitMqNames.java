package com.finintel.financialdata.config;

public final class RabbitMqNames {

    private RabbitMqNames() {
    }

    public static final String FINANCIAL_EXCHANGE = "financial.exchange";

    public static final String FINANCIAL_DATA_IMPORTED_ROUTING_KEY =
            "financial.data.imported";

    public static final String FINANCIAL_DATA_IMPORTED_QUEUE =
            "financial.data.imported.queue";
}