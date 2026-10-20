package com.durangoindsamy.smartpantrymanager.util;

import java.util.Locale;

/** Turns user-typed ingredient names into a consistent form for matching. */
public final class IngredientNormalizer {

    private IngredientNormalizer() { } // utility class - no instances

    public static String normalize(String name) {
        if (name == null) return "";
        return name.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " "); // collapse repeated spaces
    }
}