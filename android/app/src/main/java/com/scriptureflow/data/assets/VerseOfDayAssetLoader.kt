package com.scriptureflow.data.assets

import android.content.Context
import com.scriptureflow.domain.model.VerseRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Data model for the entire Verse of the Day schedule asset.
 * Corresponds to `VerseOfTheDayScheduleAsset` in the contract.
 */
@Serializable
data class VerseOfTheDayScheduleAsset(
    val version: Int,
    val timezone: String,
    val days: List<VotdDay>
)

/**
 * Represents a single day's entry in the VOTD schedule.
 * Corresponds to `VotdDay` in the contract.
 */
@Serializable
data class VotdDay(
    @SerialName("day_of_year")
    val dayOfYear: Int,
    val ref: VerseRef
)

/**
 * Loads and provides access to the Verse of the Day (VOTD) schedule
 * from the JSON asset file.
 * This class is designed to be a singleton or provided via dependency injection.
 * It handles lazy, thread-safe loading of the VOTD schedule.
 */
class VerseOfDayAssetLoader(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var votdSchedule: VerseOfTheDayScheduleAsset? = null

    @Volatile
    private var dayMap: Map<Int, VerseRef>? = null
    private val mutex = Mutex()

    /**
     * Ensures the VOTD schedule is loaded from assets and parsed. This is a thread-safe,
     * idempotent operation.
     */
    private suspend fun ensureLoaded() {
        // Fast path check to avoid locking if already initialized.
        if (votdSchedule != null) return

        mutex.withLock {
            // Second check within the lock to handle race conditions.
            if (votdSchedule != null) return@withLock

            val schedule = withContext(Dispatchers.IO) {
                val jsonString = context.assets.open(VOTD_ASSET_PATH)
                    .bufferedReader()
                    .use { it.readText() }
                json.decodeFromString<VerseOfTheDayScheduleAsset>(jsonString)
            }

            votdSchedule = schedule
            dayMap = schedule.days.associateBy({ it.dayOfYear }, { it.ref })
        }
    }

    /**
     * Gets the Verse of the Day reference for a specific day of the year.
     *
     * @param dayOfYear The day of the year (1-based, e.g., 1 for Jan 1st).
     * @return The [VerseRef] for that day, or null if not found in the schedule.
     * @throws IOException if the asset file cannot be read.
     * @throws kotlinx.serialization.SerializationException if the JSON is malformed.
     */
    suspend fun getVerseRefForDayOfYear(dayOfYear: Int): VerseRef? {
        ensureLoaded()
        return dayMap?.get(dayOfYear)
    }

    /**
     * Returns the IANA timezone ID specified in the VOTD schedule asset.
     * This should be used by callers to correctly calculate the current day of the year.
     *
     * @return The timezone string (e.g., "UTC", "America/New_York").
     * @throws IOException if the asset file cannot be read.
     */
    suspend fun getTimezone(): String {
        ensureLoaded()
        // It's safe to use the non-null assertion because ensureLoaded guarantees initialization.
        return votdSchedule!!.timezone
    }

    companion object {
        private const val VOTD_ASSET_PATH = "votd/votd.json"
    }
}
