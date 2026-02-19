package com.scriptureflow.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.scriptureflow.data.db.entities.HighlightEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the highlights table.
 */
@Dao
interface HighlightDao {

    /**
     * Inserts a highlight into the table. If the highlight already exists, it replaces it.
     *
     * @param highlight The highlight to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(highlight: HighlightEntity)

    /**
     * Updates an existing highlight.
     *
     * @param highlight The highlight to update.
     */
    @Update
    suspend fun update(highlight: HighlightEntity)

    /**
     * Deletes a highlight by its ID.
     *
     * @param id The ID of the highlight to delete.
     */
    @Query("DELETE FROM highlights WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * Deletes all highlights from the table.
     */
    @Query("DELETE FROM highlights")
    suspend fun deleteAll()

    /**
     * Retrieves a single highlight by its ID.
     *
     * @param id The ID of the highlight.
     * @return A Flow emitting the [HighlightEntity] or null if not found.
     */
    @Query("SELECT * FROM highlights WHERE id = :id")
    fun getById(id: String): Flow<HighlightEntity?>

    /**
     * Retrieves all highlights from the table, ordered by creation date descending.
     *
     * @return A Flow emitting a list of all [HighlightEntity]s.
     */
    @Query("SELECT * FROM highlights ORDER BY created_at_epoch_ms DESC")
    fun getAll(): Flow<List<HighlightEntity>>

    /**
     * Retrieves all highlights whose ranges intersect with a given chapter.
     * This allows the reader screen to efficiently query and display highlight markers
     * for the currently viewed chapter.
     *
     * @param bookId The stable book identifier.
     * @param chapter The 1-based chapter number.
     * @return A Flow emitting a list of highlights relevant to the given chapter.
     */
    @Query("""
        SELECT * FROM highlights
        WHERE start_book_id = :bookId
        AND start_chapter <= :chapter AND end_chapter >= :chapter
        ORDER BY start_verse ASC
    """)
    fun getHighlightsForChapter(bookId: String, chapter: Int): Flow<List<HighlightEntity>>
}
