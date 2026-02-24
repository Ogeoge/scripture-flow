package com.scriptureflow.data.repo

import android.content.Context
import com.scriptureflow.core.model.Verse
import com.scriptureflow.core.model.VerseRef
import com.scriptureflow.core.search.SearchEngine
import com.scriptureflow.data.assets.KjvAssetParser
import com.scriptureflow.data.db.AppDatabase
import com.scriptureflow.data.db.entities.BookmarkEntity
import com.scriptureflow.data.db.entities.HighlightEntity
import com.scriptureflow.data.prefs.ReadingPrefsStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.math.absoluteValue

/**
 * ScriptureRepository
 *
 * Offline-first coordinator for:
 * - loading verses from local assets (KJV JSON)
 * - providing drawer/navigation helper data (books -> chapters -> verses)
 * - local search (no network)
 * - Room-backed bookmarks/highlights
 * - deterministic Verse of the Day per local calendar date
 */
class ScriptureRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val readingPrefsStore: ReadingPrefsStore,
    private val assetParser: KjvAssetParser = KjvAssetParser(),
    private val searchEngine: SearchEngine = SearchEngine(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    private val cacheMutex = Mutex()

    @Volatile
    private var cachedVerses: List<Verse>? = null

    @Volatile
    private var cachedIndex: VerseIndex? = null

    suspend fun ensureVersesLoaded(): List<Verse> {
        cachedVerses?.let { return it }

        return cacheMutex.withLock {
            cachedVerses?.let { return it }

            val loaded = assetParser.loadVersesFromAsset(context)
            // Stable ordering helps determinism across restarts (and makes daily verse deterministic).
            val ordered = loaded.sortedWith(
                compareBy<Verse> { it.ref.book }.thenBy { it.ref.chapter }.thenBy { it.ref.verse },
            )

            cachedVerses = ordered
            cachedIndex = VerseIndex.build(ordered)

            ordered
        }
    }

    suspend fun getVerseById(verseId: String): Verse? {
        val verses = ensureVersesLoaded()
        return verses.firstOrNull { it.id == verseId }
    }

    suspend fun getVerseOfTheDay(date: LocalDate = LocalDate.now()): Verse {
        val verses = ensureVersesLoaded()
        require(verses.isNotEmpty()) { "No verses loaded from assets." }

        // Deterministic per local calendar date.
        // Seed derived solely from date and local verse corpus: date + corpus size.
        val seedString = "${date.year}-${date.monthValue}-${date.dayOfMonth}|${verses.size}"
        val idx = (seedString.hashCode().absoluteValue) % verses.size
        return verses[idx]
    }

    suspend fun search(query: String, limit: Int = 200) = withContext(ioDispatcher) {
        val verses = ensureVersesLoaded()
        searchEngine.search(verses = verses, query = query, limit = limit)
    }

    /** Drawer/navigation helpers */

    suspend fun getBooks(): List<String> {
        val index = ensureIndex()
        return index.books
    }

    suspend fun getChapters(book: String): List<Int> {
        val index = ensureIndex()
        return index.chaptersByBook[book].orEmpty()
    }

    suspend fun getVerses(book: String, chapter: Int): List<Verse> {
        val index = ensureIndex()
        val ids = index.verseIdsByBookChapter[book to chapter].orEmpty()
        // Use id->verse lookup for speed.
        return ids.mapNotNull { index.verseById[it] }
    }

    suspend fun getFirstVerseRef(book: String, chapter: Int): VerseRef? {
        val verses = getVerses(book, chapter)
        return verses.minByOrNull { it.ref.verse }?.ref
    }

    /** Bookmarks */

    fun observeBookmarks(): Flow<List<BookmarkEntity>> = database.bookmarkDao().observeAll()

    suspend fun isBookmarked(verseId: String): Boolean = database.bookmarkDao().isBookmarked(verseId)

    suspend fun addBookmark(verseId: String, nowEpochMs: Long = System.currentTimeMillis()) {
        database.bookmarkDao().upsert(
            BookmarkEntity(
                verseId = verseId,
                createdAtEpochMs = nowEpochMs,
            ),
        )
    }

    suspend fun removeBookmark(verseId: String) {
        database.bookmarkDao().deleteByVerseId(verseId)
    }

    /** Highlights */

    fun observeHighlights(): Flow<List<HighlightEntity>> = database.highlightDao().observeAll()

    suspend fun isHighlighted(verseId: String): Boolean = database.highlightDao().isHighlighted(verseId)

    suspend fun setHighlight(
        verseId: String,
        colorArgb: Int,
        nowEpochMs: Long = System.currentTimeMillis(),
    ) {
        database.highlightDao().upsert(
            HighlightEntity(
                verseId = verseId,
                colorArgb = colorArgb,
                createdAtEpochMs = nowEpochMs,
            ),
        )
    }

    suspend fun clearHighlight(verseId: String) {
        database.highlightDao().deleteByVerseId(verseId)
    }

    /** Reading preferences */

    fun readingPreferencesFlow() = readingPrefsStore.preferencesFlow

    private suspend fun ensureIndex(): VerseIndex {
        ensureVersesLoaded()
        return cachedIndex ?: cacheMutex.withLock {
            cachedIndex ?: VerseIndex.build(requireNotNull(cachedVerses)).also { cachedIndex = it }
        }
    }

    private data class VerseIndex(
        val books: List<String>,
        val chaptersByBook: Map<String, List<Int>>,
        val verseIdsByBookChapter: Map<Pair<String, Int>, List<String>>,
        val verseById: Map<String, Verse>,
    ) {
        companion object {
            fun build(verses: List<Verse>): VerseIndex {
                val verseById = verses.associateBy { it.id }

                val books = verses
                    .map { it.ref.book }
                    .distinct()
                    .sorted()

                val chaptersByBook = buildMap {
                    for (book in books) {
                        val chapters = verses
                            .asSequence()
                            .filter { it.ref.book == book }
                            .map { it.ref.chapter }
                            .distinct()
                            .sorted()
                            .toList()
                        put(book, chapters)
                    }
                }

                val verseIdsByBookChapter = buildMap {
                    for (v in verses) {
                        val key = v.ref.book to v.ref.chapter
                        val existing = get(key)
                        if (existing == null) {
                            put(key, mutableListOf(v.id))
                        } else {
                            @Suppress("UNCHECKED_CAST")
                            (existing as MutableList<String>).add(v.id)
                        }
                    }
                }.mapValues { (_, ids) ->
                    // Ensure verse ordering within chapter is stable by verse number.
                    ids.sortedWith(compareBy { id ->
                        // id format: "{book}|{chapter}|{verse}"; we only need verse
                        id.substringAfterLast('|').toIntOrNull() ?: Int.MAX_VALUE
                    })
                }

                return VerseIndex(
                    books = books,
                    chaptersByBook = chaptersByBook,
                    verseIdsByBookChapter = verseIdsByBookChapter,
                    verseById = verseById,
                )
            }
        }
    }
}
