package com.scriptureflow.domain.search

import com.scriptureflow.data.assets.BibleAsset
import com.scriptureflow.domain.model.VerseRef

/**
 * Represents a single search result, containing the verse reference, the full verse text,
 * and the ranges within the text that match the search query.
 *
 * @property ref The stable reference to the verse.
 * @property verseText The full, original text of the verse.
 * @property matchRanges A list of half-open [IntRange]s indicating the start (inclusive)
 *                       and end (exclusive) indices of query matches within [verseText].
 */
data class SearchResult(
    val ref: VerseRef,
    val verseText: String,
    val matchRanges: List<IntRange>
)

/**
 * A pure Kotlin search engine designed for offline full-text search across Bible assets.
 * It is stateless and can be used as a singleton or instantiated as needed.
 *
 * The search logic adheres to the contract:
 * - Case-insensitive matching (Unicode-aware).
 * - Partial-word (substring) matching.
 * - Returns match ranges for UI highlighting.
 */
class SearchEngine {

    /**
     * Searches the entire provided Bible asset for a given query string.
     *
     * @param query The search term. The search is a substring match.
     * @param bible The fully loaded [BibleAsset] to search within.
     * @return A list of [SearchResult]s for every verse that contains the query.
     *         Returns an empty list if the query is blank or no matches are found.
     */
    fun search(query: String, bible: BibleAsset): List<SearchResult> {
        if (query.isBlank()) {
            return emptyList()
        }

        val results = mutableListOf<SearchResult>()
        // Normalize the query once for efficiency.
        // Kotlin's lowercase() is Unicode-aware as per its documentation.
        val normalizedQuery = query.lowercase()

        bible.books.forEach { book ->
            book.chapters.forEach { chapter ->
                chapter.verses.forEach { verse ->
                    val matchRanges = findMatchesInText(verse.text, normalizedQuery)
                    if (matchRanges.isNotEmpty()) {
                        results.add(
                            SearchResult(
                                ref = VerseRef(book.bookId, chapter.chapter, verse.verse),
                                verseText = verse.text,
                                matchRanges = matchRanges
                            )
                        )
                    }
                }
            }
        }
        return results
    }

    /**
     * Finds all occurrences of a normalized query within a text, returning their ranges.
     *
     * @param text The text to search within (e.g., a verse).
     * @param normalizedQuery The query string, already converted to lowercase.
     * @return A list of [IntRange]s for each match.
     */
    private fun findMatchesInText(text: String, normalizedQuery: String): List<IntRange> {
        val normalizedText = text.lowercase()
        val matchRanges = mutableListOf<IntRange>()
        var currentIndex = 0

        while (currentIndex < normalizedText.length) {
            val matchIndex = normalizedText.indexOf(normalizedQuery, startIndex = currentIndex)
            if (matchIndex == -1) {
                // No more matches found in the remaining text.
                break
            }

            val range = matchIndex until (matchIndex + normalizedQuery.length)
            matchRanges.add(range)

            // Advance the search past the current match to find subsequent occurrences.
            currentIndex = range.last + 1
        }
        return matchRanges
    }
}
