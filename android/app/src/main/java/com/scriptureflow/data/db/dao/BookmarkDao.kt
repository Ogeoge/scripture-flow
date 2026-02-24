package com.scriptureflow.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.scriptureflow.data.db.entities.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    /**
     * Contract: bookmarks.verse_id is unique (PRIMARY KEY).
     * Insert replaces to allow idempotent bookmarking.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE verse_id = :verseId")
    suspend fun deleteByVerseId(verseId: String)

    @Query("SELECT * FROM bookmarks WHERE verse_id = :verseId LIMIT 1")
    suspend fun getByVerseId(verseId: String): BookmarkEntity?

    @Query("SELECT * FROM bookmarks ORDER BY created_at_epoch_ms DESC")
    fun observeAll(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY created_at_epoch_ms DESC")
    suspend fun getAllOnce(): List<BookmarkEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE verse_id = :verseId)")
    suspend fun isBookmarked(verseId: String): Boolean
}
