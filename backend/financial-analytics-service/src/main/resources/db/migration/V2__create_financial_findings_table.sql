CREATE TABLE financial_findings (
                                    id BIGINT NOT NULL AUTO_INCREMENT,

                                    metric_id BIGINT,
                                    company_id BIGINT,
                                    ticker VARCHAR(50),
                                    fiscal_year INT,
                                    period_type VARCHAR(50),

                                    finding_code VARCHAR(100),
                                    severity VARCHAR(50),

                                    observed_value DECIMAL(38, 6),
                                    threshold_value DECIMAL(38, 6),

                                    message VARCHAR(1000),

                                    created_at DATETIME(6),
                                    updated_at DATETIME(6),

                                    PRIMARY KEY (id),

                                    CONSTRAINT uk_metric_finding_code
                                        UNIQUE (metric_id, finding_code)
);