package com.finintel.financialdata.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class SecFilingParser {

    public String extractText(String html) {

        Document document = Jsoup.parse(html);

        // Remove content that is not useful for human-readable filing text
        document.select("script, style").remove();

        // SEC Inline XBRL often contains hidden metadata/facts
        document.select("[style*=display:none]").remove();

        preserveBlockBoundaries(document);

        String text;

        if (document.body() != null) {
            text = document.body().wholeText();
        } else {
            text = document.wholeText();
        }

        return normalizeText(text);
    }

    public String extractRiskFactors(String text) {

        String startMarker = "Item 1A. Risk Factors";

        int start = text.indexOf(startMarker);

        if (start == -1) {
            throw new IllegalStateException(
                    "Item 1A. Risk Factors section not found"
            );
        }

        int item1B = text.indexOf(
                "Item 1B",
                start + startMarker.length()
        );

        int item1C = text.indexOf(
                "Item 1C",
                start + startMarker.length()
        );

        int item2 = text.indexOf(
                "Item 2",
                start + startMarker.length()
        );

        int end = findFirstPositive(
                item1B,
                item1C,
                item2
        );

        if (end == -1) {
            throw new IllegalStateException(
                    "Could not find the end of Item 1A. Risk Factors"
            );
        }

        return text.substring(start, end).trim();
    }

    private void preserveBlockBoundaries(Document document) {

        document.select(
                "p, div, section, article, " +
                        "h1, h2, h3, h4, h5, h6, " +
                        "tr, li"
        ).forEach(element -> {
            element.prependText("\n");
            element.appendText("\n");
        });
    }

    private int findFirstPositive(int... indexes) {

        int result = -1;

        for (int index : indexes) {
            if (index != -1 && (result == -1 || index < result)) {
                result = index;
            }
        }

        return result;
    }

    private String normalizeText(String text) {

        return text
                .replace('\u00A0', ' ')
                .replaceAll("[ \\t]+", " ")
                .replaceAll(" *\\n *", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}