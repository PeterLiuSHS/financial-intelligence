package com.finintel.financialdata.extractor;

import tools.jackson.databind.JsonNode;
import com.finintel.financialdata.dto.ExtractedFinancialStatement;
import com.finintel.financialdata.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class SecFinancialFactExtractor {

    public ExtractedFinancialStatement extractLatestAnnualStatement(JsonNode companyFacts) {
        Integer latestFiscalYear = findLatestFiscalYear(
                companyFacts,
                List.of(
                        "RevenueFromContractWithCustomerExcludingAssessedTax",
                        "Revenues",
                        "SalesRevenueNet"
                )
        );

        return new ExtractedFinancialStatement(
                latestFiscalYear,

                extractValue(companyFacts, latestFiscalYear, List.of(
                        "RevenueFromContractWithCustomerExcludingAssessedTax",
                        "Revenues",
                        "SalesRevenueNet"
                )),

                extractValue(companyFacts, latestFiscalYear, List.of(
                        "GrossProfit"
                )),

                extractValue(companyFacts, latestFiscalYear, List.of(
                        "OperatingIncomeLoss"
                )),

                extractValue(companyFacts, latestFiscalYear, List.of(
                        "NetIncomeLoss",
                        "ProfitLoss"
                )),

                extractValue(companyFacts, latestFiscalYear, List.of(
                        "Assets"
                )),

                extractValue(companyFacts, latestFiscalYear, List.of(
                        "Liabilities"
                )),

                extractValue(companyFacts, latestFiscalYear, List.of(
                        "LongTermDebtAndFinanceLeaseObligations",
                        "LongTermDebt",
                        "LongTermDebtAndFinanceLeaseObligationsCurrent",
                        "LongTermDebtCurrent"
                )),

                extractValue(companyFacts, latestFiscalYear, List.of(
                        "CashAndCashEquivalentsAtCarryingValue",
                        "CashCashEquivalentsRestrictedCashAndRestrictedCashEquivalents"
                )),

                extractValue(companyFacts, latestFiscalYear, List.of(
                        "InventoryNet",
                        "InventoryFinishedGoodsNetOfReserves"
                )),

                extractValue(companyFacts, latestFiscalYear, List.of(
                        "AccountsReceivableNetCurrent",
                        "AccountsReceivableNet"
                )),

                extractValue(companyFacts, latestFiscalYear, List.of(
                        "AssetsCurrent"
                )),

                extractValue(companyFacts, latestFiscalYear, List.of(
                        "LiabilitiesCurrent"
                )),

                extractValue(companyFacts, latestFiscalYear, List.of(
                        "NetCashProvidedByUsedInOperatingActivities"
                )),

                extractValue(companyFacts, latestFiscalYear, List.of(
                        "PaymentsToAcquirePropertyPlantAndEquipment",
                        "PaymentsToAcquireProductiveAssets"
                ))
        );
    }

    public List<ExtractedFinancialStatement> extractRecentAnnualStatements(
            JsonNode companyFacts,
            int years
    ) {
        List<Integer> fiscalYears = findRecentFiscalYears(
                companyFacts,
                List.of(
                        "RevenueFromContractWithCustomerExcludingAssessedTax",
                        "Revenues",
                        "SalesRevenueNet"
                ),
                years
        );

        return fiscalYears.stream()
                .map(fiscalYear -> new ExtractedFinancialStatement(
                        fiscalYear,

                        extractValue(companyFacts, fiscalYear, List.of(
                                "RevenueFromContractWithCustomerExcludingAssessedTax",
                                "Revenues",
                                "SalesRevenueNet"
                        )),

                        extractValue(companyFacts, fiscalYear, List.of(
                                "GrossProfit"
                        )),

                        extractValue(companyFacts, fiscalYear, List.of(
                                "OperatingIncomeLoss"
                        )),

                        extractValue(companyFacts, fiscalYear, List.of(
                                "NetIncomeLoss",
                                "ProfitLoss"
                        )),

                        extractValue(companyFacts, fiscalYear, List.of(
                                "Assets"
                        )),

                        extractValue(companyFacts, fiscalYear, List.of(
                                "Liabilities"
                        )),

                        extractValue(companyFacts, fiscalYear, List.of(
                                "LongTermDebtAndFinanceLeaseObligations",
                                "LongTermDebt",
                                "LongTermDebtAndFinanceLeaseObligationsCurrent",
                                "LongTermDebtCurrent"
                        )),

                        extractValue(companyFacts, fiscalYear, List.of(
                                "CashAndCashEquivalentsAtCarryingValue",
                                "CashCashEquivalentsRestrictedCashAndRestrictedCashEquivalents"
                        )),

                        extractValue(companyFacts, fiscalYear, List.of(
                                "InventoryNet",
                                "InventoryFinishedGoodsNetOfReserves"
                        )),

                        extractValue(companyFacts, fiscalYear, List.of(
                                "AccountsReceivableNetCurrent",
                                "AccountsReceivableNet"
                        )),

                        extractValue(companyFacts, fiscalYear, List.of(
                                "AssetsCurrent"
                        )),

                        extractValue(companyFacts, fiscalYear, List.of(
                                "LiabilitiesCurrent"
                        )),

                        extractValue(companyFacts, fiscalYear, List.of(
                                "NetCashProvidedByUsedInOperatingActivities"
                        )),

                        extractValue(companyFacts, fiscalYear, List.of(
                                "PaymentsToAcquirePropertyPlantAndEquipment",
                                "PaymentsToAcquireProductiveAssets"
                        ))
                ))
                .toList();
    }

    private Integer findLatestFiscalYear(
            JsonNode companyFacts,
            List<String> conceptNames
    ) {
        Integer latestYear = null;

        for (String conceptName : conceptNames) {
            JsonNode facts = getUsdFacts(companyFacts, conceptName);

            if (facts == null || !facts.isArray()) {
                continue;
            }

            for (JsonNode fact : facts) {
                if (!isAnnualFact(fact)) {
                    continue;
                }

                if (!fact.has("fy")) {
                    continue;
                }

                int fiscalYear = fact.get("fy").asInt();

                if (latestYear == null || fiscalYear > latestYear) {
                    latestYear = fiscalYear;
                }
            }
        }

        if (latestYear == null) {
            throw new ResourceNotFoundException(
                    "No annual revenue facts found in SEC company facts"
            );
        }

        return latestYear;
    }

    private BigDecimal extractValue(
            JsonNode companyFacts,
            Integer fiscalYear,
            List<String> conceptNames
    ) {
        FactCandidate bestCandidate = null;

        for (String conceptName : conceptNames) {
            JsonNode facts = getUsdFacts(companyFacts, conceptName);

            if (facts == null || !facts.isArray()) {
                continue;
            }

            for (JsonNode fact : facts) {
                if (!isAnnualFact(fact)) {
                    continue;
                }

                if (!fact.has("fy") || fact.get("fy").asInt() != fiscalYear) {
                    continue;
                }

                if (!fact.has("val")) {
                    continue;
                }

                String filed = fact.has("filed") ? fact.get("filed").asText() : "";

                FactCandidate candidate = new FactCandidate(
                        new BigDecimal(fact.get("val").asText()),
                        filed
                );

                if (bestCandidate == null || candidate.filed().compareTo(bestCandidate.filed()) > 0) {
                    bestCandidate = candidate;
                }
            }
        }

        return bestCandidate == null ? null : bestCandidate.value();
    }

    private JsonNode getUsdFacts(JsonNode companyFacts, String conceptName) {
        return companyFacts
                .path("facts")
                .path("us-gaap")
                .path(conceptName)
                .path("units")
                .path("USD");
    }

    private boolean isAnnualFact(JsonNode fact) {
        String form = fact.has("form") ? fact.get("form").asText() : "";
        String fp = fact.has("fp") ? fact.get("fp").asText() : "";

        return form.startsWith("10-K") && "FY".equalsIgnoreCase(fp);
    }

    private record FactCandidate(
            BigDecimal value,
            String filed
    ) {
    }

    private List<Integer> findRecentFiscalYears(
            JsonNode companyFacts,
            List<String> conceptNames,
            int years
    ) {
        return conceptNames.stream()
                .map(conceptName -> getUsdFacts(companyFacts, conceptName))
                .filter(facts -> facts != null && facts.isArray())
                .flatMap(facts -> {
                    List<Integer> result = new java.util.ArrayList<>();

                    for (JsonNode fact : facts) {
                        if (!isAnnualFact(fact)) {
                            continue;
                        }

                        if (!fact.has("fy")) {
                            continue;
                        }

                        result.add(fact.get("fy").asInt());
                    }

                    return result.stream();
                })
                .distinct()
                .sorted(java.util.Comparator.reverseOrder())
                .limit(years)
                .toList();
    }
}