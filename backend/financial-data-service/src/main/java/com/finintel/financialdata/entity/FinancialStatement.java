package com.finintel.financialdata.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "financial_statements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_company_year_period",
                        columnNames = {"company_id", "fiscal_year", "period_type"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false)
    private PeriodType periodType;

    private BigDecimal revenue;
    private BigDecimal grossProfit;
    private BigDecimal operatingIncome;
    private BigDecimal netIncome;

    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal totalDebt;
    private BigDecimal cashAndCashEquivalents;
    private BigDecimal inventory;
    private BigDecimal accountsReceivable;
    private BigDecimal currentAssets;
    private BigDecimal currentLiabilities;

    private BigDecimal operatingCashFlow;
    private BigDecimal capitalExpenditure;

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