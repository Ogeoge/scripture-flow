package com.scriptureflow.data.assets

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data classes for parsing the Bible JSON asset file (`bible/kjv.json`).
 * These models directly correspond to the asset structure defined in the contract.
 */

@Serializable
data class BibleAsset(
    @SerialName("translation_id")
    val translationId: String,
    val books: List<BookAsset>
)

@Serializable
data class BookAsset(
    @SerialName("book_id")
    val bookId: String,
    val name: String,
    val chapters: List<ChapterAsset>
)

@Serializable
data class ChapterAsset(
    val chapter: Int,
    val verses: List<VerseAsset>
)

@Serializable
data class VerseAsset(
    val verse: Int,
    val text: String,
    /** Optional list of UTF-16 code-unit [start,end) ranges within 'text' to render in red. */
    @SerialName("red_letter_ranges")
    val redLetterRanges: List<RangeAsset> = emptyList()
)

/**
 * Represents a half-open character range for highlighting within a string.
 * Corresponds to the `Range` model in the contract. Renamed to avoid conflicts.
 */
@Serializable
data class RangeAsset(
    /** Start index (inclusive). */
    val start: Int,
    /** End index (exclusive). */
    val end: Int
)
