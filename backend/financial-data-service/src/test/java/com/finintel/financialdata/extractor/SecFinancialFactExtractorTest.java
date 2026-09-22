package com.finintel.financialdata.extractor;

import com.finintel.financialdata.dto.ExtractedFinancialStatement;
import com.finintel.financialdata.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SecFinancialFactExtractorTest {

    private SecFinancialFactExtractor extractor;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        extractor = new SecFinancialFactExtractor();
        objectMapper = new ObjectMapper();
    }

    @Test
    void extractLatestAnnualStatement_shouldExtractLatestFiscalYearAndFinancialValues()
            throws Exception {

        JsonNode companyFacts = objectMapper.readTree("""
                {
                  "facts": {
                    "us-gaap": {

                      "RevenueFromContractWithCustomerExcludingAssessedTax": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2024,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2024-11-01",
                              "val": 900
                            },
                            {
                              "fy": 2025,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2025-10-31",
                              "val": 1000
                            }
                          ]
                        }
                      },

                      "GrossProfit": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2025,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2025-10-31",
                              "val": 400
                            }
                          ]
                        }
                      },

                      "OperatingIncomeLoss": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2025,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2025-10-31",
                              "val": 300
                            }
                          ]
                        }
                      },

                      "NetIncomeLoss": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2025,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2025-10-31",
                              "val": 250
                            }
                          ]
                        }
                      },

                      "Assets": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2025,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2025-10-31",
                              "val": 3500
                            }
                          ]
                        }
                      },

                      "Liabilities": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2025,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2025-10-31",
                              "val": 2500
                            }
                          ]
                        }
                      },

                      "LongTermDebt": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2025,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2025-10-31",
                              "val": 1000
                            }
                          ]
                        }
                      },

                      "CashAndCashEquivalentsAtCarryingValue": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2025,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2025-10-31",
                              "val": 500
                            }
                          ]
                        }
                      },

                      "InventoryNet": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2025,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2025-10-31",
                              "val": 100
                            }
                          ]
                        }
                      },

                      "AccountsReceivableNetCurrent": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2025,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2025-10-31",
                              "val": 200
                            }
                          ]
                        }
                      },

                      "AssetsCurrent": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2025,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2025-10-31",
                              "val": 1500
                            }
                          ]
                        }
                      },

                      "LiabilitiesCurrent": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2025,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2025-10-31",
                              "val": 1200
                            }
                          ]
                        }
                      },

                      "NetCashProvidedByUsedInOperatingActivities": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2025,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2025-10-31",
                              "val": 600
                            }
                          ]
                        }
                      },

                      "PaymentsToAcquirePropertyPlantAndEquipment": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2025,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2025-10-31",
                              "val": 150
                            }
                          ]
                        }
                      }
                    }
                  }
                }
                """);

        ExtractedFinancialStatement result =
                extractor.extractLatestAnnualStatement(companyFacts);

        assertEquals(2025, result.fiscalYear());

        assertEquals(
                new BigDecimal("1000"),
                result.revenue()
        );

        assertEquals(
                new BigDecimal("400"),
                result.grossProfit()
        );

        assertEquals(
                new BigDecimal("300"),
                result.operatingIncome()
        );

        assertEquals(
                new BigDecimal("250"),
                result.netIncome()
        );

        assertEquals(
                new BigDecimal("3500"),
                result.totalAssets()
        );

        assertEquals(
                new BigDecimal("2500"),
                result.totalLiabilities()
        );

        assertEquals(
                new BigDecimal("1000"),
                result.totalDebt()
        );

        assertEquals(
                new BigDecimal("500"),
                result.cashAndCashEquivalents()
        );

        assertEquals(
                new BigDecimal("100"),
                result.inventory()
        );

        assertEquals(
                new BigDecimal("200"),
                result.accountsReceivable()
        );

        assertEquals(
                new BigDecimal("1500"),
                result.currentAssets()
        );

        assertEquals(
                new BigDecimal("1200"),
                result.currentLiabilities()
        );

        assertEquals(
                new BigDecimal("600"),
                result.operatingCashFlow()
        );

        assertEquals(
                new BigDecimal("150"),
                result.capitalExpenditure()
        );
    }

    @Test
    void extractLatestAnnualStatement_shouldIgnoreNonAnnualFacts()
            throws Exception {

        JsonNode companyFacts = objectMapper.readTree("""
                {
                  "facts": {
                    "us-gaap": {
                      "RevenueFromContractWithCustomerExcludingAssessedTax": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2026,
                              "form": "10-Q",
                              "fp": "Q1",
                              "filed": "2026-02-01",
                              "val": 2000
                            },
                            {
                              "fy": 2025,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2025-10-31",
                              "val": 1000
                            }
                          ]
                        }
                      }
                    }
                  }
                }
                """);

        ExtractedFinancialStatement result =
                extractor.extractLatestAnnualStatement(companyFacts);

        assertEquals(2025, result.fiscalYear());
        assertEquals(
                new BigDecimal("1000"),
                result.revenue()
        );
    }

    @Test
    void extractLatestAnnualStatement_shouldAccept10KAmendmentForm()
            throws Exception {

        JsonNode companyFacts = objectMapper.readTree("""
                {
                  "facts": {
                    "us-gaap": {
                      "RevenueFromContractWithCustomerExcludingAssessedTax": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2025,
                              "form": "10-K/A",
                              "fp": "FY",
                              "filed": "2025-11-10",
                              "val": 1100
                            }
                          ]
                        }
                      }
                    }
                  }
                }
                """);

        ExtractedFinancialStatement result =
                extractor.extractLatestAnnualStatement(companyFacts);

        assertEquals(2025, result.fiscalYear());
        assertEquals(
                new BigDecimal("1100"),
                result.revenue()
        );
    }

    @Test
    void extractLatestAnnualStatement_shouldUseRevenueFallbackConcept()
            throws Exception {

        JsonNode companyFacts = objectMapper.readTree("""
                {
                  "facts": {
                    "us-gaap": {
                      "Revenues": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2025,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2025-10-31",
                              "val": 1200
                            }
                          ]
                        }
                      }
                    }
                  }
                }
                """);

        ExtractedFinancialStatement result =
                extractor.extractLatestAnnualStatement(companyFacts);

        assertEquals(2025, result.fiscalYear());
        assertEquals(
                new BigDecimal("1200"),
                result.revenue()
        );
    }

    @Test
    void extractLatestAnnualStatement_shouldUseMostRecentlyFiledValueForSameFiscalYear()
            throws Exception {

        JsonNode companyFacts = objectMapper.readTree("""
                {
                  "facts": {
                    "us-gaap": {
                      "RevenueFromContractWithCustomerExcludingAssessedTax": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2025,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2025-10-01",
                              "val": 900
                            },
                            {
                              "fy": 2025,
                              "form": "10-K/A",
                              "fp": "FY",
                              "filed": "2025-11-15",
                              "val": 1000
                            }
                          ]
                        }
                      }
                    }
                  }
                }
                """);

        ExtractedFinancialStatement result =
                extractor.extractLatestAnnualStatement(companyFacts);

        assertEquals(
                new BigDecimal("1000"),
                result.revenue()
        );
    }

    @Test
    void extractLatestAnnualStatement_whenOptionalConceptIsMissing_shouldReturnNullForThatField()
            throws Exception {

        JsonNode companyFacts = objectMapper.readTree("""
                {
                  "facts": {
                    "us-gaap": {
                      "RevenueFromContractWithCustomerExcludingAssessedTax": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2025,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2025-10-31",
                              "val": 1000
                            }
                          ]
                        }
                      }
                    }
                  }
                }
                """);

        ExtractedFinancialStatement result =
                extractor.extractLatestAnnualStatement(companyFacts);

        assertEquals(2025, result.fiscalYear());

        assertEquals(
                new BigDecimal("1000"),
                result.revenue()
        );

        assertNull(result.grossProfit());
        assertNull(result.operatingIncome());
        assertNull(result.netIncome());
        assertNull(result.totalAssets());
        assertNull(result.totalLiabilities());
        assertNull(result.totalDebt());
        assertNull(result.cashAndCashEquivalents());
        assertNull(result.inventory());
        assertNull(result.accountsReceivable());
        assertNull(result.currentAssets());
        assertNull(result.currentLiabilities());
        assertNull(result.operatingCashFlow());
        assertNull(result.capitalExpenditure());
    }

    @Test
    void extractLatestAnnualStatement_whenNoAnnualRevenueFactExists_shouldThrowResourceNotFoundException()
            throws Exception {

        JsonNode companyFacts = objectMapper.readTree("""
                {
                  "facts": {
                    "us-gaap": {
                      "RevenueFromContractWithCustomerExcludingAssessedTax": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2025,
                              "form": "10-Q",
                              "fp": "Q1",
                              "filed": "2025-02-01",
                              "val": 300
                            }
                          ]
                        }
                      }
                    }
                  }
                }
                """);

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> extractor.extractLatestAnnualStatement(
                                companyFacts
                        )
                );

        assertEquals(
                "No annual revenue facts found in SEC company facts",
                exception.getMessage()
        );
    }

    @Test
    void extractRecentAnnualStatements_shouldReturnDistinctYearsInDescendingOrder()
            throws Exception {

        JsonNode companyFacts = objectMapper.readTree("""
                {
                  "facts": {
                    "us-gaap": {
                      "RevenueFromContractWithCustomerExcludingAssessedTax": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2023,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2023-11-01",
                              "val": 800
                            },
                            {
                              "fy": 2025,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2025-10-31",
                              "val": 1000
                            },
                            {
                              "fy": 2024,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2024-11-01",
                              "val": 900
                            },
                            {
                              "fy": 2025,
                              "form": "10-K/A",
                              "fp": "FY",
                              "filed": "2025-11-10",
                              "val": 1100
                            }
                          ]
                        }
                      }
                    }
                  }
                }
                """);

        List<ExtractedFinancialStatement> result =
                extractor.extractRecentAnnualStatements(
                        companyFacts,
                        3
                );

        assertEquals(3, result.size());

        assertEquals(
                List.of(2025, 2024, 2023),
                result.stream()
                        .map(ExtractedFinancialStatement::fiscalYear)
                        .toList()
        );

        /*
         * 2025 has two annual filings. The later filed value
         * should win.
         */
        assertEquals(
                new BigDecimal("1100"),
                result.get(0).revenue()
        );
    }

    @Test
    void extractRecentAnnualStatements_shouldRespectYearsLimit()
            throws Exception {

        JsonNode companyFacts = objectMapper.readTree("""
                {
                  "facts": {
                    "us-gaap": {
                      "RevenueFromContractWithCustomerExcludingAssessedTax": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2025,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2025-10-31",
                              "val": 1000
                            },
                            {
                              "fy": 2024,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2024-11-01",
                              "val": 900
                            },
                            {
                              "fy": 2023,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2023-11-01",
                              "val": 800
                            }
                          ]
                        }
                      }
                    }
                  }
                }
                """);

        List<ExtractedFinancialStatement> result =
                extractor.extractRecentAnnualStatements(
                        companyFacts,
                        2
                );

        assertEquals(2, result.size());

        assertEquals(2025, result.get(0).fiscalYear());
        assertEquals(2024, result.get(1).fiscalYear());
    }

    @Test
    void extractRecentAnnualStatements_shouldUseFallbackRevenueConceptsAcrossYears()
            throws Exception {

        JsonNode companyFacts = objectMapper.readTree("""
                {
                  "facts": {
                    "us-gaap": {

                      "RevenueFromContractWithCustomerExcludingAssessedTax": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2025,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2025-10-31",
                              "val": 1000
                            }
                          ]
                        }
                      },

                      "Revenues": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2024,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2024-11-01",
                              "val": 900
                            }
                          ]
                        }
                      },

                      "SalesRevenueNet": {
                        "units": {
                          "USD": [
                            {
                              "fy": 2023,
                              "form": "10-K",
                              "fp": "FY",
                              "filed": "2023-11-01",
                              "val": 800
                            }
                          ]
                        }
                      }
                    }
                  }
                }
                """);

        List<ExtractedFinancialStatement> result =
                extractor.extractRecentAnnualStatements(
                        companyFacts,
                        3
                );

        assertEquals(3, result.size());

        assertEquals(2025, result.get(0).fiscalYear());
        assertEquals(2024, result.get(1).fiscalYear());
        assertEquals(2023, result.get(2).fiscalYear());

        assertEquals(
                new BigDecimal("1000"),
                result.get(0).revenue()
        );

        assertEquals(
                new BigDecimal("900"),
                result.get(1).revenue()
        );

        assertEquals(
                new BigDecimal("800"),
                result.get(2).revenue()
        );
    }
}