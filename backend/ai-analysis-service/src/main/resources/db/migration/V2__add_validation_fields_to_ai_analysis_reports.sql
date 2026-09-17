ALTER TABLE ai_analysis_reports
    MODIFY COLUMN summary TEXT,
    MODIFY COLUMN key_findings TEXT,
    MODIFY COLUMN risk_assessment TEXT,
    MODIFY COLUMN suggested_questions TEXT,
    ADD COLUMN validation_passed BOOLEAN,
    ADD COLUMN validation_status VARCHAR(50),
    ADD COLUMN validation_violations TEXT;