package com.scriptureflow.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.scriptureflow.data.db.entities.BookmarkEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the bookmarks table.
 */
@Dao
interface BookmarkDao {

    /**
     * Inserts a bookmark into the table. If the bookmark already exists, it replaces it.
     *
     * @param bookmark The bookmark to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookmarkEntity)

    /**
     * Updates an existing bookmark.
     *
     * @param bookmark The bookmark to update.
     */
    @Update
    suspend fun update(bookmark: BookmarkEntity)

    /**
     * Deletes a bookmark by its ID.
     *
     * @param id The ID of the bookmark to delete.
     */
    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * Deletes all bookmarks from the table.
     */
    @Query("DELETE FROM bookmarks")
    suspend fun deleteAll()

    /**
     * Retrieves a single bookmark by its ID.
     *
     * @param id The ID of the bookmark.
     * @return A Flow emitting the [BookmarkEntity] or null if not found.
     */
    @Query("SELECT * FROM bookmarks WHERE id = :id")
    fun getById(id: String): Flow<BookmarkEntity?>

    /**
     * Retrieves all bookmarks from the table, ordered by creation date descending.
     *
     * @return A Flow emitting a list of all [BookmarkEntity]s.
     */
    @Query("SELECT * FROM bookmarks ORDER BY created_at_epoch_ms DESC")
    fun getAll(): Flow<List<BookmarkEntity>>

    /**
     * Retrieves all bookmarks that are associated with a specific book.
     * This is useful for efficiently querying bookmarks relevant to the current reading context.
     * The query checks if the bookmark's range is within the given book.
     *
     * @param bookId The stable book identifier (e.g., "GEN").
     * @return A Flow emitting a list of relevant [BookmarkEntity]s.
     */
    @Query("SELECT * FROM bookmarks WHERE start_book_id = :bookId ORDER BY start_chapter, start_verse ASC")
    fun getBookmarksByBook(bookId: String): Flow<List<BookmarkEntity>>

    /**
     * Retrieves all bookmarks whose ranges intersect with a given chapter.
     * This allows the reader screen to display markers for bookmarks within the current chapter.
     *
     * @param bookId The stable book identifier.
     * @param chapter The 1-based chapter number.
     * @return A Flow emitting a list of bookmarks relevant to the given chapter.
     */
    @Query("""
        SELECT * FROM bookmarks
        WHERE start_book_id = :bookId
        AND start_chapter <= :chapter AND end_chapter >= :chapter
        ORDER BY start_verse ASC
    """)
    fun getBookmarksForChapter(bookId: String, chapter: Int): Flow<List<BookmarkEntity>>

}
