package com.scriptureflow.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a user bookmark for a verse range, with an optional note.
 * This domain model corresponds to the `Bookmark` data model in the contract.
 * It is also used for serialization when interacting with the local database preferences.
 */
@Serializable
data class Bookmark(
    /** Client-generated UUID. */
    val id: String,

    /** The bookmarked verse range. */
    val range: VerseRange,

    /** Optional user-provided note. */
    val note: String? = null,

    /** Creation timestamp in milliseconds since the Unix epoch. */
    @SerialName("created_at_epoch_ms")
    val createdAtEpochMs: Long,

    /** Last update timestamp in milliseconds since the Unix epoch. */
    @SerialName("updated_at_epoch_ms")
    val updatedAtEpochMs: Long
)
