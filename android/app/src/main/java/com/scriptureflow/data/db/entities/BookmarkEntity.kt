package com.scriptureflow.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.scriptureflow.domain.model.Bookmark
import com.scriptureflow.domain.model.VerseRange
import com.scriptureflow.domain.model.VerseRef

/**
 * Represents a bookmark stored in the Room database.
 * This entity corresponds to the `bookmarks` table defined in the contract DDL.
 */
@Entity(
    tableName = "bookmarks",
    indices = [
        Index(value = ["start_book_id", "start_chapter", "start_verse"], name = "idx_bookmarks_start"),
        Index(value = ["end_book_id", "end_chapter", "end_verse"], name = "idx_bookmarks_end")
    ]
)
data class BookmarkEntity(
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

    val note: String?,

    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long,

    @ColumnInfo(name = "updated_at_epoch_ms")
    val updatedAtEpochMs: Long
)

/**
 * Converts a [BookmarkEntity] from the database into a [Bookmark] domain model.
 */
fun BookmarkEntity.toDomainModel(): Bookmark {
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
    return Bookmark(
        id = id,
        range = VerseRange(start = startRef, end = endRef),
        note = note,
        createdAtEpochMs = createdAtEpochMs,
        updatedAtEpochMs = updatedAtEpochMs
    )
}

/**
 * Converts a [Bookmark] domain model into a [BookmarkEntity] for database storage.
 */
fun Bookmark.toEntity(): BookmarkEntity {
    return BookmarkEntity(
        id = id,
        startBookId = range.start.bookId,
        startChapter = range.start.chapter,
        startVerse = range.start.verse,
        endBookId = range.end.bookId,
        endChapter = range.end.chapter,
        endVerse = range.end.verse,
        note = note,
        createdAtEpochMs = createdAtEpochMs,
        updatedAtEpochMs = updatedAtEpochMs
    )
}
