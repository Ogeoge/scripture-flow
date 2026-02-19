package com.scriptureflow.data.assets

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Loads and provides access to the Bible data from the JSON asset file.
 * This class is designed to be a singleton or provided via dependency injection.
 * It handles lazy, thread-safe loading and parsing of the Bible asset.
 */
class BibleAssetLoader(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    // Volatile to ensure visibility of the initialized bibleAsset across threads.
    @Volatile
    private var bibleAsset: BibleAsset? = null
    private val mutex = Mutex()

    /**
     * Returns the full Bible asset, loading and parsing it from assets if not already cached.
     * This operation is thread-safe.
     *
     * @return The fully parsed [BibleAsset].
     * @throws IOException if the asset file cannot be read.
     * @throws kotlinx.serialization.SerializationException if the JSON is malformed.
     */
    suspend fun getBible(): BibleAsset {
        // Fast path check to avoid locking if already initialized.
        bibleAsset?.let { return it }

        return mutex.withLock {
            // Second check within the lock to handle race conditions where another coroutine
            // might have finished loading while this one was waiting for the mutex.
            bibleAsset?.let { return it }

            val loadedAsset = withContext(Dispatchers.IO) {
                val jsonString = context.assets.open(BIBLE_ASSET_PATH)
                    .bufferedReader()
                    .use { it.readText() }
                json.decodeFromString<BibleAsset>(jsonString)
            }
            bibleAsset = loadedAsset
            loadedAsset
        }
    }

    /**
     * Returns the list of all books in canonical order.
     */
    suspend fun getBooks(): List<BookAsset> {
        return getBible().books
    }

    /**
     * Retrieves a specific chapter from a book.
     * @param bookId The stable book identifier (e.g., "GEN").
     * @param chapter The 1-based chapter number.
     * @return The [ChapterAsset] or null if the book or chapter is not found.
     */
    suspend fun getChapter(bookId: String, chapter: Int): ChapterAsset? {
        val bible = getBible()
        // A simple find is performant enough for a list of 66 books.
        val book = bible.books.find { it.bookId.equals(bookId, ignoreCase = true) }
        // A find on chapters is also fast.
        return book?.chapters?.find { it.chapter == chapter }
    }

    companion object {
        private const val BIBLE_ASSET_PATH = "bible/kjv.json"
    }
}
