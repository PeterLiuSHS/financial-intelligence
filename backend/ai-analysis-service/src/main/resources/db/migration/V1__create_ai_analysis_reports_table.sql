CREATE TABLE ai_analysis_reports (
                                     id BIGINT NOT NULL AUTO_INCREMENT,

                                     ticker VARCHAR(50),
                                     company_name VARCHAR(255),
                                     report_type VARCHAR(100),

                                     summary VARCHAR(2000),
                                     key_findings VARCHAR(4000),
                                     risk_assessment VARCHAR(4000),
                                     suggested_questions VARCHAR(4000),

                                     provider VARCHAR(100),
                                     model VARCHAR(100),

                                     created_at DATETIME(6),
                                     updated_at DATETIME(6),

                                     PRIMARY KEY (id)
);