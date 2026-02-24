package com.scriptureflow.core.search

import com.scriptureflow.core.model.MatchRange
import com.scriptureflow.core.model.SearchResult
import com.scriptureflow.core.model.Verse
import com.scriptureflow.core.util.TextNormalize

/**
 * Offline-only search.
 *
 * Contract requirements:
 * - case-insensitive
 * - partial-word matches
 * - multi-word queries: all tokens must match within verse.text
 * - matchRanges refer to indices in original verse.text (start inclusive, end exclusive)
 */
class SearchEngine(
    private val normalizer: TextNormalize = TextNormalize,
) {

    fun search(
        verses: List<Verse>,
        query: String,
        limit: Int = 200,
    ): List<SearchResult> {
        val tokens = normalizer.queryTokens(query)
        if (tokens.isEmpty()) return emptyList()

        val results = ArrayList<SearchResult>(minOf(limit, 64))

        for (verse in verses) {
            val matchRanges = findAllTokenMatches(verseText = verse.text, tokens = tokens)
            if (matchRanges != null) {
                results.add(SearchResult(verse = verse, matchRanges = matchRanges))
                if (results.size >= limit) break
            }
        }

        return results
    }

    /**
     * Returns merged match ranges if ALL tokens match at least once, else null.
     */
    private fun findAllTokenMatches(
        verseText: String,
        tokens: List<String>,
    ): List<MatchRange>? {
        val lowered = normalizer.lowercaseForSearch(verseText)

        val allRanges = ArrayList<MatchRange>(tokens.size * 2)

        for (token in tokens) {
            var fromIndex = 0
            var foundAny = false

            while (fromIndex <= lowered.length) {
                val idx = lowered.indexOf(token, startIndex = fromIndex)
                if (idx < 0) break

                foundAny = true
                val start = idx
                val end = idx + token.length
                allRanges.add(MatchRange(start = start, end = end))

                // allow overlapping matches by advancing one char
                fromIndex = idx + 1
            }

            if (!foundAny) return null
        }

        return mergeRanges(allRanges)
    }

    private fun mergeRanges(ranges: List<MatchRange>): List<MatchRange> {
        if (ranges.isEmpty()) return emptyList()

        val sorted = ranges.sortedWith(compareBy<MatchRange> { it.start }.thenBy { it.end })
        val merged = ArrayList<MatchRange>(sorted.size)

        var currentStart = sorted[0].start
        var currentEnd = sorted[0].end

        for (i in 1 until sorted.size) {
            val r = sorted[i]
            if (r.start <= currentEnd) {
                currentEnd = maxOf(currentEnd, r.end)
            } else {
                merged.add(MatchRange(start = currentStart, end = currentEnd))
                currentStart = r.start
                currentEnd = r.end
            }
        }

        merged.add(MatchRange(start = currentStart, end = currentEnd))
        return merged
    }
}
