package com.scriptureflow.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Enum representing the available colors for highlights.
 * The names must match the case-sensitive values in the contract:
 * YELLOW, GREEN, BLUE, PINK, ORANGE, PURPLE.
 */
enum class HighlightColor {
    YELLOW,
    GREEN,
    BLUE,
    PINK,
    ORANGE,
    PURPLE
}

/**
 * Represents a user highlight for a verse range with a specific color.
 * This domain model corresponds to the `Highlight` data model in the contract.
 */
@Serializable
data class Highlight(
    /** Client-generated UUID. */
    val id: String,

    /** The highlighted verse range. */
    val range: VerseRange,

    /** The color of the highlight. */
    val color: HighlightColor,

    /** Creation timestamp in milliseconds since the Unix epoch. */
    @SerialName("created_at_epoch_ms")
    val createdAtEpochMs: Long,

    /** Last update timestamp in milliseconds since the Unix epoch. */
    @SerialName("updated_at_epoch_ms")
    val updatedAtEpochMs: Long
)
