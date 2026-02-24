package com.scriptureflow.data.assets

import android.content.Context
import com.scriptureflow.core.model.Verse
import com.scriptureflow.core.model.VerseRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Loads KJV JSON from app assets.
 *
 * Offline-first invariant: bible text must be loaded from local assets.
 *
 * Expected schema (see assets/bibles/kjv_sample.json):
 * {
 *   "version": "kjv",
 *   "books": [
 *     {"name":"Genesis","chapters":[{"chapter":1,"verses":[{"verse":1,"text":"..."}]}]}
 *   ]
 * }
 */
class KjvAssetParser(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    },
) {

    suspend fun loadVersesFromAsset(
        context: Context,
        assetPath: String = DEFAULT_ASSET_PATH,
    ): List<Verse> = withContext(Dispatchers.IO) {
        val text = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        val parsed = json.decodeFromString(KjvBibleAsset.serializer(), text)
        flatten(parsed)
    }

    private fun flatten(bible: KjvBibleAsset): List<Verse> {
        val out = ArrayList<Verse>(1024)

        for (book in bible.books) {
            val bookName = book.name
            for (chapter in book.chapters) {
                val chapterNum = chapter.chapter
                for (v in chapter.verses) {
                    val verseNum = v.verse
                    val ref = VerseRef(book = bookName, chapter = chapterNum, verse = verseNum)
                    val id = Verse.makeId(book = bookName, chapter = chapterNum, verse = verseNum)
                    out.add(Verse(id = id, ref = ref, text = v.text))
                }
            }
        }

        return out
    }

    @Serializable
    private data class KjvBibleAsset(
        @SerialName("version") val version: String,
        @SerialName("books") val books: List<KjvBookAsset>,
    )

    @Serializable
    private data class KjvBookAsset(
        @SerialName("name") val name: String,
        @SerialName("chapters") val chapters: List<KjvChapterAsset>,
    )

    @Serializable
    private data class KjvChapterAsset(
        @SerialName("chapter") val chapter: Int,
        @SerialName("verses") val verses: List<KjvVerseAsset>,
    )

    @Serializable
    private data class KjvVerseAsset(
        @SerialName("verse") val verse: Int,
        @SerialName("text") val text: String,
    )

    companion object {
        const val DEFAULT_ASSET_PATH: String = "bibles/kjv_sample.json"
    }
}
