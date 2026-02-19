package com.scriptureflow.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.scriptureflow.ui.screens.home.HomeScreen
import com.scriptureflow.ui.screens.reader.ReaderScreen
import com.scriptureflow.ui.screens.search.SearchScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.Home
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.Home) {
            HomeScreen(navController = navController)
        }

        composable(
            route = Routes.Reader.route,
            arguments = listOf(
                navArgument(Routes.Reader.ARG_BOOK_ID) { type = NavType.StringType },
                navArgument(Routes.Reader.ARG_CHAPTER) { type = NavType.IntType },
                navArgument(Routes.Reader.ARG_VERSE) {
                    type = NavType.IntType
                    defaultValue = 0 // 0 indicates no specific verse to scroll to
                }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString(Routes.Reader.ARG_BOOK_ID)
            val chapter = backStackEntry.arguments?.getInt(Routes.Reader.ARG_CHAPTER)
            val verse = backStackEntry.arguments?.getInt(Routes.Reader.ARG_VERSE)

            if (bookId != null && chapter != null && verse != null) {
                ReaderScreen(
                    navController = navController,
                    bookId = bookId,
                    chapter = chapter,
                    initialVerse = if (verse > 0) verse else null
                )
            } else {
                // This state should ideally not be reached with valid navigation calls.
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: Invalid arguments for Reader screen.")
                }
            }
        }

        composable(Routes.Search) {
            SearchScreen(navController = navController)
        }

        composable(Routes.Bookmarks) {
            // Placeholder UI for Bookmarks Screen
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Bookmarks Screen")
            }
        }

        composable(Routes.Settings) {
            // Placeholder UI for Settings Screen
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Settings Screen")
            }
        }
    }
}
