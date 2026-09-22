package com.finintel.financialdata.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecFilingParserTest {

    private SecFilingParser parser;

    @BeforeEach
    void setUp() {
        parser = new SecFilingParser();
    }

    @Test
    void extractText_shouldExtractReadableTextFromHtml() {
        String html = """
                <html>
                    <head>
                        <style>
                            body { color: red; }
                        </style>
                    </head>
                    <body>
                        <h1>Apple Inc.</h1>
                        <p>Annual Report</p>
                        <script>
                            console.log("ignore");
                        </script>
                    </body>
                </html>
                """;

        String result = parser.extractText(html);

        assertTrue(result.contains("Apple Inc."));
        assertTrue(result.contains("Annual Report"));

        assertFalse(result.contains("console.log"));
        assertFalse(result.contains("color: red"));
    }

    @Test
    void extractText_shouldNormalizeRepeatedWhitespace() {
        String html = """
                <html>
                    <body>
                        <p>Apple       Inc.</p>
                        <p>Financial       Report</p>
                    </body>
                </html>
                """;

        String result = parser.extractText(html);

        assertFalse(result.contains("       "));
        assertTrue(result.contains("Apple"));
        assertTrue(result.contains("Financial"));
    }

    @Test
    void extractRiskFactors_shouldExtractSectionBetweenItem1AAndItem1B() {
        String text = """
                Item 1. Business

                Business information.

                Item 1A. Risk Factors

                Supply chain disruption may affect operations.
                Component shortages may increase costs.

                Item 1B. Unresolved Staff Comments

                Other content.
                """;

        String result = parser.extractRiskFactors(text);

        assertTrue(result.contains("Item 1A"));
        assertTrue(result.contains(
                "Supply chain disruption may affect operations."
        ));
        assertTrue(result.contains(
                "Component shortages may increase costs."
        ));

        assertFalse(result.contains(
                "Unresolved Staff Comments"
        ));
        assertFalse(result.contains(
                "Other content."
        ));
    }

    @Test
    void extractRiskFactors_shouldStopAtItem1CWhenItAppearsBeforeItem2() {
        String text = """
                Item 1A. Risk Factors

                Risk factor content.

                Item 1C. Cybersecurity

                Cybersecurity content.

                Item 2. Properties

                Property content.
                """;

        String result = parser.extractRiskFactors(text);

        assertTrue(result.contains(
                "Risk factor content."
        ));

        assertFalse(result.contains(
                "Cybersecurity content."
        ));

        assertFalse(result.contains(
                "Property content."
        ));
    }

    @Test
    void extractRiskFactors_shouldStopAtItem2WhenItem1BAnd1CAreAbsent() {
        String text = """
                Item 1A. Risk Factors

                Risk factor content.

                Item 2. Properties

                Property content.
                """;

        String result = parser.extractRiskFactors(text);

        assertTrue(result.contains(
                "Risk factor content."
        ));

        assertFalse(result.contains(
                "Property content."
        ));
    }

    @Test
    void extractRiskFactors_whenItem1AIsMissing_shouldThrowException() {
        String text = """
                Item 1. Business

                Business content.

                Item 2. Properties
                """;

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> parser.extractRiskFactors(text)
                );

        assertEquals(
                "Item 1A. Risk Factors section not found",
                exception.getMessage()
        );
    }

    @Test
    void extractRiskFactors_whenEndMarkerIsMissing_shouldThrowException() {
        String text = """
                Item 1A. Risk Factors

                Risk factor content with no later section.
                """;

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> parser.extractRiskFactors(text)
                );

        assertEquals(
                "Could not find the end of Item 1A. Risk Factors",
                exception.getMessage()
        );
    }
}