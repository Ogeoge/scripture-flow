package com.scriptureflow.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A stable reference to a specific verse.
 * Corresponds to the `VerseRef` data model in the contract.
 */
@Serializable
data class VerseRef(
    @SerialName("book_id")
    val bookId: String,
    val chapter: Int,
    val verse: Int
) : Comparable<VerseRef> {

    /**
     * Compares two verse references based on book (lexicographically),
     * then chapter, then verse.
     */
    override fun compareTo(other: VerseRef): Int {
        if (bookId != other.bookId) {
            return bookId.compareTo(other.bookId)
        }
        if (chapter != other.chapter) {
            return chapter.compareTo(other.chapter)
        }
        return verse.compareTo(other.verse)
    }

    /**
     * Formats the reference for display, e.g., "Genesis 1:1".
     * Requires the display name of the book.
     */
    fun toDisplayString(bookName: String): String {
        return "$bookName $chapter:$verse"
    }
}

/**
 * Inclusive range of verses in a single book.
 * Corresponds to the `VerseRange` data model in the contract.
 */
@Serializable
data class VerseRange(
    val start: VerseRef,
    val end: VerseRef
) {
    init {
        // Contract invariant: end must not be before start.
        require(start.bookId == end.bookId) { "Verse range must be within the same book." }
        require(start <= end) { "End of range cannot be before the start." }
    }

    /**
     * Formats the range for display.
     * e.g., "Genesis 1:1-5" or "Genesis 1:29 - 2:3".
     * Requires the display name of the book.
     */
    fun toDisplayString(bookName: String): String {
        return when {
            start == end -> start.toDisplayString(bookName)
            start.chapter == end.chapter -> "$bookName ${start.chapter}:${start.verse}-${end.verse}"
            else -> "$bookName ${start.chapter}:${start.verse} - ${end.chapter}:${end.verse}"
        }
    }
}
