package com.scriptureflow.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.scriptureflow.data.db.entities.PreferenceEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the preferences table.
 * This table is used as a generic key-value store for application settings,
 * where values are typically stored as JSON strings.
 */
@Dao
interface PreferenceDao {

    /**
     * Inserts or replaces a preference entity.
     * This is an "upsert" operation.
     *
     * @param preference The [PreferenceEntity] to save.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preference: PreferenceEntity)

    /**
     * Retrieves a preference entity by its key, observing changes.
     *
     * @param key The unique key for the preference (e.g., "reading_settings").
     * @return A [Flow] emitting the [PreferenceEntity] if it exists, or null otherwise.
     */
    @Query("SELECT * FROM preferences WHERE key = :key")
    fun get(key: String): Flow<PreferenceEntity?>

    /**
     * Retrieves the string value of a preference for a one-time read.
     *
     * @param key The unique key for the preference.
     * @return The string value associated with the key, or null if the key does not exist.
     */
    @Query("SELECT value FROM preferences WHERE key = :key")
    suspend fun getValue(key: String): String?

    /**
     * Deletes a preference by its key.
     *
     * @param key The key of the preference to delete.
     */
    @Query("DELETE FROM preferences WHERE key = :key")
    suspend fun delete(key: String)

}
