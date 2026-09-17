package com.finintel.financialanalytics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "financial_metrics",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_company_year_period_metrics",
                        columnNames = {"company_id", "fiscal_year", "period_type"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long companyId;

    private String ticker;

    private Integer fiscalYear;

    private String periodType;

    private BigDecimal grossMargin;

    private BigDecimal operatingMargin;

    private BigDecimal netMargin;

    private BigDecimal currentRatio;

    private BigDecimal debtToAssets;

    private BigDecimal cashFlowMargin;

    private BigDecimal revenueGrowth;

    private BigDecimal netIncomeGrowth;

    private BigDecimal inventoryGrowth;

    private BigDecimal accountsReceivableGrowth;

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