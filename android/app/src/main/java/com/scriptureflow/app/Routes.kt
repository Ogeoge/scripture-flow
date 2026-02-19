package com.scriptureflow.app

/**
 * Centralized definitions for navigation routes and arguments.
 */
object Routes {
    const val Home = "home"
    const val Search = "search"
    const val Bookmarks = "bookmarks"
    const val Settings = "settings"

    object Reader {
        const val ARG_BOOK_ID = "bookId"
        const val ARG_CHAPTER = "chapter"
        const val ARG_VERSE = "verse"

        /**
         * Route definition for the reader screen.
         * - `bookId`: Required book identifier (e.g., "GEN").
         * - `chapter`: Required 1-based chapter number.
         * - `verse`: Optional 1-based verse number to scroll to.
         */
        const val route = "reader/{$ARG_BOOK_ID}/{$ARG_CHAPTER}?$ARG_VERSE={$ARG_VERSE}"

        /**
         * Builds the navigation route for the Reader screen.
         *
         * @param bookId The stable book identifier (e.g., "GEN").
         * @param chapter The 1-based chapter number.
         * @param verse An optional 1-based verse number to initially scroll to.
         * @return A complete navigation route string.
         */
        fun build(bookId: String, chapter: Int, verse: Int? = null): String {
            val baseRoute = "reader/$bookId/$chapter"
            return if (verse != null && verse > 0) {
                "$baseRoute?$ARG_VERSE=$verse"
            } else {
                // The NavHost definition has a default value of 0, which we use to indicate
                // no specific verse scroll is needed.
                "$baseRoute?$ARG_VERSE=0"
            }
        }
    }
}
