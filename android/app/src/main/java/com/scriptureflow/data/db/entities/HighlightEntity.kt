package com.scriptureflow.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.scriptureflow.domain.model.Highlight
import com.scriptureflow.domain.model.HighlightColor
import com.scriptureflow.domain.model.VerseRange
import com.scriptureflow.domain.model.VerseRef

/**
 * Represents a highlight stored in the Room database.
 * This entity corresponds to the `highlights` table defined in the contract DDL.
 */
@Entity(
    tableName = "highlights",
    indices = [
        Index(value = ["start_book_id", "start_chapter", "start_verse"], name = "idx_highlights_start"),
        Index(value = ["end_book_id", "end_chapter", "end_verse"], name = "idx_highlights_end")
    ]
)
data class HighlightEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "start_book_id")
    val startBookId: String,

    @ColumnInfo(name = "start_chapter")
    val startChapter: Int,

    @ColumnInfo(name = "start_verse")
    val startVerse: Int,

    @ColumnInfo(name = "end_book_id")
    val endBookId: String,

    @ColumnInfo(name = "end_chapter")
    val endChapter: Int,

    @ColumnInfo(name = "end_verse")
    val endVerse: Int,

    /** Stores the HighlightColor enum as a string (e.g., "YELLOW"). */
    val color: String,

    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long,

    @ColumnInfo(name = "updated_at_epoch_ms")
    val updatedAtEpochMs: Long
)

/**
 * Converts a [HighlightEntity] from the database into a [Highlight] domain model.
 */
fun HighlightEntity.toDomainModel(): Highlight {
    val startRef = VerseRef(
        bookId = startBookId,
        chapter = startChapter,
        verse = startVerse
    )
    val endRef = VerseRef(
        bookId = endBookId,
        chapter = endChapter,
        verse = endVerse
    )
    return Highlight(
        id = id,
        range = VerseRange(start = startRef, end = endRef),
        color = HighlightColor.valueOf(color),
        createdAtEpochMs = createdAtEpochMs,
        updatedAtEpochMs = updatedAtEpochMs
    )
}

/**
 * Converts a [Highlight] domain model into a [HighlightEntity] for database storage.
 */
fun Highlight.toEntity(): HighlightEntity {
    return HighlightEntity(
        id = id,
        startBookId = range.start.bookId,
        startChapter = range.start.chapter,
        startVerse = range.start.verse,
        endBookId = range.end.bookId,
        endChapter = range.end.chapter,
        endVerse = range.end.verse,
        color = color.name, // Convert enum to its string name
        createdAtEpochMs = createdAtEpochMs,
        updatedAtEpochMs = updatedAtEpochMs
    )
}
