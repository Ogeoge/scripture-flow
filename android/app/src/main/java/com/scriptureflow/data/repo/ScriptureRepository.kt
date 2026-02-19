package com.scriptureflow.data.repo

import com.scriptureflow.data.assets.BibleAssetLoader
import com.scriptureflow.data.assets.BookAsset
import com.scriptureflow.data.assets.ChapterAsset
import com.scriptureflow.data.assets.VerseOfDayAssetLoader
import com.scriptureflow.data.db.dao.BookmarkDao
import com.scriptureflow.data.db.dao.HighlightDao
import com.scriptureflow.data.db.dao.PreferenceDao
import com.scriptureflow.data.db.entities.PreferenceEntity
import com.scriptureflow.data.db.entities.toDomainModel
import com.scriptureflow.data.db.entities.toEntity
import com.scriptureflow.domain.model.Bookmark
import com.scriptureflow.domain.model.Highlight
import com.scriptureflow.domain.model.VerseRef
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

/**
 * Serializable data model for reader display settings, corresponding to the contract.
 */
@Serializable
data class ReadingSettings(
    @SerialName("font_size_sp")
    val fontSizeSp: Float,
    @SerialName("line_spacing_multiplier")
    val lineSpacingMultiplier: Float,
    @SerialName("paragraph_spacing_dp")
    val paragraphSpacingDp: Float,
    @SerialName("font_family")
    val fontFamily: String,
    @SerialName("red_letter_enabled")
    val redLetterEnabled: Boolean,
    @SerialName("theme_override")
    val themeOverride: String
) {
    companion object {
        fun default() = ReadingSettings(
            fontSizeSp = 18.0f,
            lineSpacingMultiplier = 1.2f,
            paragraphSpacingDp = 8.0f,
            fontFamily = "SERIF", // or "SANS"
            redLetterEnabled = true,
            themeOverride = "SYSTEM" // or "LIGHT", "DARK"
        )
    }
}

/**
 * Serializable data model for the last reading location, corresponding to the contract.
 */
@Serializable
data class LastLocation(
    val ref: VerseRef,
    @SerialName("updated_at_epoch_ms")
    val updatedAtEpochMs: Long
)


/**
 * Single source of truth for all app data. It abstracts away the origin of the data,
 * whether it's from local assets (Bible text) or a local database (user data).
 */
class ScriptureRepository(
    private val bibleAssetLoader: BibleAssetLoader,
    private val verseOfDayAssetLoader: VerseOfDayAssetLoader,
    private val bookmarkDao: BookmarkDao,
    private val highlightDao: HighlightDao,
    private val preferenceDao: PreferenceDao
) {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    // --- Bible Asset Methods ---

    suspend fun getBooks(): List<BookAsset> = bibleAssetLoader.getBooks()

    suspend fun getChapter(bookId: String, chapter: Int): ChapterAsset? {
        return bibleAssetLoader.getChapter(bookId, chapter)
    }

    // --- Verse of the Day Methods ---

    suspend fun getVerseRefForDayOfYear(dayOfYear: Int): VerseRef? {
        return verseOfDayAssetLoader.getVerseRefForDayOfYear(dayOfYear)
    }

    // --- Bookmark Methods ---

    fun getAllBookmarks(): Flow<List<Bookmark>> = bookmarkDao.getAll().map {
        it.map { entity -> entity.toDomainModel() }
    }

    fun getBookmarksForChapter(bookId: String, chapter: Int): Flow<List<Bookmark>> {
        return bookmarkDao.getBookmarksForChapter(bookId, chapter).map {
            it.map { entity -> entity.toDomainModel() }
        }
    }

    suspend fun addOrUpdateBookmark(bookmark: Bookmark) {
        bookmarkDao.insert(bookmark.toEntity())
    }

    suspend fun deleteBookmark(id: String) {
        bookmarkDao.deleteById(id)
    }

    // --- Highlight Methods ---

    fun getHighlightsForChapter(bookId: String, chapter: Int): Flow<List<Highlight>> {
        return highlightDao.getHighlightsForChapter(bookId, chapter).map {
            it.map { entity -> entity.toDomainModel() }
        }
    }

    suspend fun addOrUpdateHighlight(highlight: Highlight) {
        highlightDao.insert(highlight.toEntity())
    }

    suspend fun deleteHighlight(id: String) {
        highlightDao.deleteById(id)
    }

    // --- Preferences Methods (Settings & Last Location) ---

    fun getReadingSettings(): Flow<ReadingSettings> {
        return preferenceDao.get(KEY_READING_SETTINGS).map { entity ->
            if (entity?.value != null) {
                try {
                    json.decodeFromString<ReadingSettings>(entity.value)
                } catch (e: Exception) {
                    ReadingSettings.default()
                }
            } else {
                ReadingSettings.default()
            }
        }
    }

    suspend fun saveReadingSettings(settings: ReadingSettings) {
        val jsonValue = json.encodeToString(settings)
        val entity = PreferenceEntity(KEY_READING_SETTINGS, jsonValue, System.currentTimeMillis())
        preferenceDao.insert(entity)
    }

    fun getLastLocation(): Flow<LastLocation?> {
        return preferenceDao.get(KEY_LAST_LOCATION).map { entity ->
            if (entity?.value != null) {
                try {
                    json.decodeFromString<LastLocation>(entity.value)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }

    suspend fun saveLastLocation(lastLocation: LastLocation) {
        val jsonValue = json.encodeToString(lastLocation)
        val entity = PreferenceEntity(KEY_LAST_LOCATION, jsonValue, System.currentTimeMillis())
        preferenceDao.insert(entity)
    }

    companion object {
        private const val KEY_READING_SETTINGS = "reading_settings"
        private const val KEY_LAST_LOCATION = "last_location"
    }
}
