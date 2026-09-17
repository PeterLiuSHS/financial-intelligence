CREATE TABLE financial_metrics (
                                   id BIGINT NOT NULL AUTO_INCREMENT,

                                   company_id BIGINT,
                                   ticker VARCHAR(50),
                                   fiscal_year INT,
                                   period_type VARCHAR(50),

                                   gross_margin DECIMAL(38, 6),
                                   operating_margin DECIMAL(38, 6),
                                   net_margin DECIMAL(38, 6),
                                   current_ratio DECIMAL(38, 6),
                                   debt_to_assets DECIMAL(38, 6),
                                   cash_flow_margin DECIMAL(38, 6),

                                   revenue_growth DECIMAL(38, 6),
                                   net_income_growth DECIMAL(38, 6),
                                   inventory_growth DECIMAL(38, 6),
                                   accounts_receivable_growth DECIMAL(38, 6),

                                   created_at DATETIME(6),
                                   updated_at DATETIME(6),

                                   PRIMARY KEY (id),

                                   CONSTRAINT uk_company_year_period_metrics
                                       UNIQUE (company_id, fiscal_year, period_type)
);