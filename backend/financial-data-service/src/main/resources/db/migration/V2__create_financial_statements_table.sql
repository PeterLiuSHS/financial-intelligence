CREATE TABLE financial_statements (
                                      id BIGINT NOT NULL AUTO_INCREMENT,
                                      company_id BIGINT NOT NULL,
                                      fiscal_year INT NOT NULL,
                                      period_type VARCHAR(255) NOT NULL,

                                      revenue DECIMAL(38, 2),
                                      gross_profit DECIMAL(38, 2),
                                      operating_income DECIMAL(38, 2),
                                      net_income DECIMAL(38, 2),

                                      total_assets DECIMAL(38, 2),
                                      total_liabilities DECIMAL(38, 2),
                                      total_debt DECIMAL(38, 2),
                                      cash_and_cash_equivalents DECIMAL(38, 2),
                                      inventory DECIMAL(38, 2),
                                      accounts_receivable DECIMAL(38, 2),
                                      current_assets DECIMAL(38, 2),
                                      current_liabilities DECIMAL(38, 2),

                                      operating_cash_flow DECIMAL(38, 2),
                                      capital_expenditure DECIMAL(38, 2),

                                      created_at DATETIME(6),
                                      updated_at DATETIME(6),

                                      PRIMARY KEY (id),

                                      CONSTRAINT fk_financial_statements_company
                                          FOREIGN KEY (company_id)
                                              REFERENCES companies(id),

                                      CONSTRAINT uk_company_year_period
                                          UNIQUE (company_id, fiscal_year, period_type)
);