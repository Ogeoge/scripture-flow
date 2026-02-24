package com.scriptureflow.core.util

import java.util.Locale

/**
 * Text normalization helpers used by the offline search engine.
 *
 * Contract-aligned behavior:
 * - case-insensitive search
 * - partial-word matches (performed by substring matching in SearchEngine)
 * - multi-word queries: tokens are derived from the query and all must match
 */
object TextNormalize {

    /**
     * Lowercases in a locale-stable way for search.
     *
     * Note: We keep the string length unchanged to ensure indices returned by
     * SearchEngine (based on this lowered text) map 1:1 to the original verse text.
     */
    fun lowercaseForSearch(text: String): String = text.lowercase(Locale.ROOT)

    /**
     * Converts a raw query into tokens.
     *
     * - Lowercases
     * - Treats punctuation/symbols as separators
     * - Collapses whitespace
     */
    fun queryTokens(query: String): List<String> {
        val normalized = buildString(query.length) {
            for (ch in query) {
                append(if (isTokenChar(ch)) ch else ' ')
            }
        }
            .trim()
            .lowercase(Locale.ROOT)

        if (normalized.isEmpty()) return emptyList()

        return normalized
            .split(Regex("\\s+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    private fun isTokenChar(ch: Char): Boolean {
        // Letters/digits are part of tokens.
        // Keep apostrophe to allow queries like "lord's".
        return ch.isLetterOrDigit() || ch == '\''
    }
}
