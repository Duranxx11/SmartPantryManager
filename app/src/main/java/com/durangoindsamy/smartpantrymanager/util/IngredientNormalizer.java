package com.durangoindsamy.smartpantrymanager.util;

import java.util.Locale;

/**
 * Converts ingredient names into a consistent form for matching.
 */
public final class IngredientNormalizer {

    private IngredientNormalizer() {
        // Utility class - no instances
    }

    public static String normalize(String name) {
        if (name == null) {
            return "";
        }

        String normalized = name.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");

        if (normalized.isEmpty()) {
            return "";
        }

        // Normalize common plural forms.
        int lastSpace = normalized.lastIndexOf(' ');

        String prefix = lastSpace >= 0
                ? normalized.substring(0, lastSpace + 1)
                : "";

        String lastWord = lastSpace >= 0
                ? normalized.substring(lastSpace + 1)
                : normalized;

        if (lastWord.length() > 4 && lastWord.endsWith("ies")) {
            lastWord = lastWord.substring(
                    0, lastWord.length() - 3) + "y";

        } else if (lastWord.length() > 4 &&
                (lastWord.endsWith("ches")
                        || lastWord.endsWith("shes")
                        || lastWord.endsWith("xes")
                        || lastWord.endsWith("zes")
                        || lastWord.endsWith("sses")
                        || lastWord.endsWith("oes"))) {

            lastWord = lastWord.substring(
                    0, lastWord.length() - 2);

        } else if (lastWord.length() > 3
                && lastWord.endsWith("s")
                && !lastWord.endsWith("ss")
                && !lastWord.endsWith("us")
                && !lastWord.endsWith("is")) {

            lastWord = lastWord.substring(
                    0, lastWord.length() - 1);
        }

        return prefix + lastWord;
    }
}