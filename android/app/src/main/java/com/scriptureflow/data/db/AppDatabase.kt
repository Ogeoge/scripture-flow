package com.scriptureflow.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.scriptureflow.data.db.dao.BookmarkDao
import com.scriptureflow.data.db.dao.HighlightDao
import com.scriptureflow.data.db.entities.BookmarkEntity
import com.scriptureflow.data.db.entities.HighlightEntity

/**
 * Room database for ScriptureFlow.
 *
 * Contract-aligned tables:
 * - bookmarks(verse_id PRIMARY KEY, created_at_epoch_ms)
 * - highlights(verse_id PRIMARY KEY, color_argb, created_at_epoch_ms)
 *
 * Note: The contract also describes an optional cached verses table for fast search.
 * This first version keeps cached verses out of Room to minimize files/complexity;
 * search can operate directly on asset-loaded verses and/or a future verses cache.
 */
@Database(
    entities = [
        BookmarkEntity::class,
        HighlightEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(
    // No converters currently; kept for future growth.
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookmarkDao(): BookmarkDao

    abstract fun highlightDao(): HighlightDao

    companion object {
        private const val DB_NAME: String = "scriptureflow.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME,
                )
                    // Keep startup robust for a first version; migrations can be added later.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
