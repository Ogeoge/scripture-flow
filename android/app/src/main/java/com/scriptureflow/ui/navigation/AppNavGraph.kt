package com.scriptureflow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.scriptureflow.data.prefs.ReadingPrefsStore
import com.scriptureflow.data.repo.ScriptureRepository
import com.scriptureflow.ui.screens.BookmarksScreen
import com.scriptureflow.ui.screens.HighlightsScreen
import com.scriptureflow.ui.screens.HomeScreen
import com.scriptureflow.ui.screens.ReaderScreen
import com.scriptureflow.ui.screens.SearchScreen
import com.scriptureflow.ui.screens.SettingsScreen

object Routes {
    const val HOME: String = "home"
    const val READER: String = "reader"
    const val SEARCH: String = "search"
    const val BOOKMARKS: String = "bookmarks"
    const val HIGHLIGHTS: String = "highlights"
    const val SETTINGS: String = "settings"

    const val ARG_VERSE_ID: String = "verseId"

    fun reader(verseId: String): String = "$READER/$verseId"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    repository: ScriptureRepository,
    readingPrefsStore: ReadingPrefsStore,
    modifier: Modifier = Modifier,
) {
    val prefs by readingPrefsStore.preferencesFlow.collectAsState(
        initial = readingPrefsStore.defaultPreferences,
    )

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier,
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                repository = repository,
                onOpenReader = { verseId ->
                    navController.navigate(Routes.reader(verseId))
                },
                onOpenSearch = { navController.navigate(Routes.SEARCH) },
                onOpenBookmarks = { navController.navigate(Routes.BOOKMARKS) },
                onOpenHighlights = { navController.navigate(Routes.HIGHLIGHTS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(
            route = "${Routes.READER}/{${Routes.ARG_VERSE_ID}}",
            arguments = listOf(
                navArgument(Routes.ARG_VERSE_ID) { type = NavType.StringType },
            ),
        ) { entry ->
            val verseId = entry.arguments?.getString(Routes.ARG_VERSE_ID)

            ReaderScreen(
                repository = repository,
                readingPreferences = prefs,
                verseId = verseId,
                onBack = { navController.popBackStack() },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                repository = repository,
                onOpenReader = { verseId -> navController.navigate(Routes.reader(verseId)) },
            )
        }

        composable(Routes.BOOKMARKS) {
            BookmarksScreen(
                repository = repository,
                onOpenReader = { verseId -> navController.navigate(Routes.reader(verseId)) },
            )
        }

        composable(Routes.HIGHLIGHTS) {
            HighlightsScreen(
                repository = repository,
                onOpenReader = { verseId -> navController.navigate(Routes.reader(verseId)) },
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                readingPrefsStore = readingPrefsStore,
                preferences = prefs,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

@Composable
fun rememberAppNavController(): NavHostController = rememberNavController()

@Composable
fun NavigateToReaderEffect(
    navController: NavHostController,
    verseId: String?,
) {
    LaunchedEffect(verseId) {
        if (!verseId.isNullOrBlank()) {
            navController.navigate(Routes.reader(verseId))
        }
    }
}
