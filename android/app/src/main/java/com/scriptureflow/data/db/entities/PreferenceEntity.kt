package com.scriptureflow.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a generic key-value pair stored in the Room database.
 * This entity is used for storing application preferences, such as reading settings
 * and the last reading location.
 *
 * This entity corresponds to the `preferences` table defined in the contract DDL.
 */
@Entity(tableName = "preferences")
data class PreferenceEntity(
    /**
     * The unique key for the preference. Should be a stable constant.
     * e.g., "reading_settings", "last_location".
     */
    @PrimaryKey
    val key: String,

    /**
     * The value of the preference, typically stored as a JSON string.
     */
    val value: String,

    /**
     * The timestamp of the last update in milliseconds since the Unix epoch.
     */
    @ColumnInfo(name = "updated_at_epoch_ms")
    val updatedAtEpochMs: Long
)
