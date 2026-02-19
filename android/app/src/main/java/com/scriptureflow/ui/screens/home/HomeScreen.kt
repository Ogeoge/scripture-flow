package com.scriptureflow.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.scriptureflow.app.Routes
import com.scriptureflow.domain.model.VerseRef


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel() // Assumes a ViewModel factory is provided
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ScriptureFlow") },
                navigationIcon = {
                    // This icon would open a navigation drawer in a full implementation.
                    IconButton(onClick = { /* TODO: Open Drawer */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Open navigation menu")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.Search) }) {
                        Icon(Icons.Default.Search, contentDescription = "Search scripture")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ContinueReadingCard(
                state = uiState.lastLocationState,
                onNavigateToRef = { ref ->
                    navController.navigate(Routes.Reader.build(ref.bookId, ref.chapter, ref.verse))
                }
            )

            VerseOfTheDayCard(
                state = uiState.votdState,
                onNavigateToRef = { ref ->
                    navController.navigate(Routes.Reader.build(ref.bookId, ref.chapter, ref.verse))
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContinueReadingCard(
    state: LastLocationUiState,
    onNavigateToRef: (VerseRef) -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnabled = state is LastLocationUiState.Success
    val cardSemantics = if (isEnabled) "Continue reading from your last location" else ""

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = cardSemantics },
        onClick = {
            if (state is LastLocationUiState.Success) {
                onNavigateToRef(state.ref)
            }
        },
        enabled = isEnabled
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Continue Reading", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            when (state) {
                is LastLocationUiState.Loading -> CircularProgressIndicator()
                is LastLocationUiState.None -> Text(
                    "Select a chapter to begin reading.",
                    style = MaterialTheme.typography.bodyMedium
                )
                is LastLocationUiState.Success -> Text(
                    state.displayString,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerseOfTheDayCard(
    state: VotdUiState,
    onNavigateToRef: (VerseRef) -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnabled = state is VotdUiState.Success
    val cardSemantics = if (isEnabled) "View Verse of the Day" else ""

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = cardSemantics },
        onClick = {
            if (state is VotdUiState.Success) {
                onNavigateToRef(state.ref)
            }
        },
        enabled = isEnabled
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Verse of the Day", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            when (state) {
                is VotdUiState.Loading -> CircularProgressIndicator()
                is VotdUiState.Error -> Text(
                    state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                is VotdUiState.Success -> {
                    Text(
                        state.displayRef,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "\"${state.text}\",",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
