package com.scriptureflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.scriptureflow.ui.navigation.AppNavGraph
import com.scriptureflow.ui.shell.DrawerScaffold
import com.scriptureflow.ui.theme.ScriptureFlowTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as App

        setContent {
            val prefs by app.readingPrefsStore.preferencesFlow.collectAsState(
                initial = app.readingPrefsStore.defaultPreferences,
            )

            ScriptureFlowTheme(themeMode = prefs.themeMode) {
                DrawerScaffold(
                    repository = app.scriptureRepository,
                ) { navController ->
                    AppNavGraph(
                        navController = navController,
                        repository = app.scriptureRepository,
                        readingPrefsStore = app.readingPrefsStore,
                    )
                }
            }
        }
    }
}
