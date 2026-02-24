package com.scriptureflow.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for the contract table:
 * bookmarks(verse_id PRIMARY KEY, created_at_epoch_ms)
 *
 * Contract invariant: one bookmark per verse (unique verse_id).
 */
@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey
    @ColumnInfo(name = "verse_id")
    val verseId: String,

    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long,
)
