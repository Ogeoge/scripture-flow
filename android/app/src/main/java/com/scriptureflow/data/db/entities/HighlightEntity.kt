package com.scriptureflow.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for the contract table:
 * highlights(verse_id PRIMARY KEY, color_argb, created_at_epoch_ms)
 *
 * Contract invariant: one highlight per verse (unique verse_id).
 */
@Entity(tableName = "highlights")
data class HighlightEntity(
    @PrimaryKey
    @ColumnInfo(name = "verse_id")
    val verseId: String,

    @ColumnInfo(name = "color_argb")
    val colorArgb: Int,

    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long,
)
