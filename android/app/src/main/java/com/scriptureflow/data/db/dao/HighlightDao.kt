package com.scriptureflow.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.scriptureflow.data.db.entities.HighlightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HighlightDao {

    /**
     * Contract: highlights.verse_id is unique (PRIMARY KEY).
     * Insert replaces to allow idempotent highlighting.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HighlightEntity)

    @Query("DELETE FROM highlights WHERE verse_id = :verseId")
    suspend fun deleteByVerseId(verseId: String)

    @Query("SELECT * FROM highlights WHERE verse_id = :verseId LIMIT 1")
    suspend fun getByVerseId(verseId: String): HighlightEntity?

    @Query("SELECT * FROM highlights ORDER BY created_at_epoch_ms DESC")
    fun observeAll(): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights ORDER BY created_at_epoch_ms DESC")
    suspend fun getAllOnce(): List<HighlightEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM highlights WHERE verse_id = :verseId)")
    suspend fun isHighlighted(verseId: String): Boolean
}
