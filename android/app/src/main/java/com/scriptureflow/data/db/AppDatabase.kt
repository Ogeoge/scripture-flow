package com.scriptureflow.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.scriptureflow.data.db.dao.BookmarkDao
import com.scriptureflow.data.db.dao.HighlightDao
import com.scriptureflow.data.db.dao.PreferenceDao
import com.scriptureflow.data.db.entities.BookmarkEntity
import com.scriptureflow.data.db.entities.HighlightEntity
import com.scriptureflow.data.db.entities.PreferenceEntity

/**
 * Room database for ScriptureFlow.
 *
 * This database provides DAOs for accessing user-specific data like bookmarks, highlights,
 * and preferences (including settings and last reading location).
 */
@Database(
    entities = [
        BookmarkEntity::class,
        HighlightEntity::class,
        PreferenceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookmarkDao(): BookmarkDao
    abstract fun highlightDao(): HighlightDao
    abstract fun preferenceDao(): PreferenceDao

    companion object {
        // Volatile annotation ensures that the INSTANCE variable is always up-to-date
        // and visible to all execution threads. Changes made by one thread are immediately
        // visible to all other threads.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns a singleton instance of the database.
         *
         * @param context The application context.
         * @return The singleton AppDatabase instance.
         */
        fun getDatabase(context: Context): AppDatabase {
            // If the INSTANCE is not null, then return it, otherwise create the database.
            // This pattern is thread-safe.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scriptureflow_database"
                )
                // Add migrations here if the schema changes in the future.
                .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
