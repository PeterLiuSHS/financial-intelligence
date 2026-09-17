CREATE TABLE companies (
                           id BIGINT NOT NULL AUTO_INCREMENT,
                           ticker VARCHAR(255) NOT NULL,
                           name VARCHAR(255) NOT NULL,
                           cik VARCHAR(255),
                           exchange VARCHAR(255),
                           sector VARCHAR(255),
                           industry VARCHAR(255),
                           country VARCHAR(255),
                           created_at DATETIME(6),
                           updated_at DATETIME(6),
                           PRIMARY KEY (id),
                           CONSTRAINT uk_company_ticker UNIQUE (ticker)
);