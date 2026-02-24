package com.scriptureflow.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerseRef(
    @SerialName("book") val book: String,
    @SerialName("chapter") val chapter: Int,
    @SerialName("verse") val verse: Int,
)

@Serializable
data class Verse(
    @SerialName("id") val id: String,
    @SerialName("ref") val ref: VerseRef,
    @SerialName("text") val text: String,
) {
    companion object {
        fun makeId(book: String, chapter: Int, verse: Int): String = "$book|$chapter|$verse"
    }
}

@Serializable
data class MatchRange(
    @SerialName("start") val start: Int,
    @SerialName("end") val end: Int,
)

@Serializable
data class SearchResult(
    @SerialName("verse") val verse: Verse,
    @SerialName("match_ranges") val matchRanges: List<MatchRange>,
)

@Serializable
data class Bookmark(
    @SerialName("verse_id") val verseId: String,
    @SerialName("created_at_epoch_ms") val createdAtEpochMs: Long,
)

@Serializable
data class Highlight(
    @SerialName("verse_id") val verseId: String,
    @SerialName("color_argb") val colorArgb: Int,
    @SerialName("created_at_epoch_ms") val createdAtEpochMs: Long,
)

@Serializable
data class ReadingPreferences(
    @SerialName("font_size_sp") val fontSizeSp: Float,
    @SerialName("line_height_multiplier") val lineHeightMultiplier: Float,
    @SerialName("font_style") val fontStyle: String,
    @SerialName("theme_mode") val themeMode: String,
    @SerialName("text_alignment") val textAlignment: String,
)

@Serializable
data class HealthResponse(
    @SerialName("status") val status: String,
)

@Serializable
data class ApiError(
    @SerialName("error") val error: ApiErrorDetails,
)

@Serializable
data class ApiErrorDetails(
    @SerialName("code") val code: String,
    @SerialName("message") val message: String,
    @SerialName("request_id") val requestId: String? = null,
)
