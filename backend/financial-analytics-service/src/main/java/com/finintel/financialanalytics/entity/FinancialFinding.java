package com.finintel.financialanalytics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "financial_findings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_metric_finding_code",
                        columnNames = {"metric_id", "finding_code"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long metricId;

    private Long companyId;

    private String ticker;

    private Integer fiscalYear;

    private String periodType;

    private String findingCode;

    @Enumerated(EnumType.STRING)
    private FindingSeverity severity;

    @Column(precision = 38, scale = 6)
    private BigDecimal observedValue;

    @Column(precision = 38, scale = 6)
    private BigDecimal thresholdValue;

    @Column(length = 1000)
    private String message;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}