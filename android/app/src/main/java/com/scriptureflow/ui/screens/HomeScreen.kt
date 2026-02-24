package com.scriptureflow.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scriptureflow.data.repo.ScriptureRepository
import com.scriptureflow.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    repository: ScriptureRepository,
    onOpenReader: (verseId: String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHighlights: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = rememberHomeViewModel(repository)

    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "ScriptureFlow",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        when (val s = state) {
            is HomeViewModel.UiState.Loading -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is HomeViewModel.UiState.Ready -> {
                VerseOfDayCard(
                    book = s.verse.ref.book,
                    chapter = s.verse.ref.chapter,
                    verse = s.verse.ref.verse,
                    text = s.verse.text,
                    isHighlighted = s.isHighlighted,
                    onRead = { onOpenReader(s.verse.id) },
                    onToggleHighlight = { viewModel.toggleHighlightForVerseOfDay() },
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onOpenSearch,
                    ) {
                        Text("Search")
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onOpenSettings,
                    ) {
                        Text("Settings")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onOpenBookmarks,
                    ) {
                        Text("Bookmarks")
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onOpenHighlights,
                    ) {
                        Text("Highlights")
                    }
                }

                if (s.lastErrorMessage != null) {
                    Text(
                        text = s.lastErrorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun VerseOfDayCard(
    book: String,
    chapter: Int,
    verse: Int,
    text: String,
    isHighlighted: Boolean,
    onRead: () -> Unit,
    onToggleHighlight: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) {
                // Soft highlight background.
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Verse of the Day",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "$book $chapter:$verse",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onRead,
                ) {
                    Text("Read")
                }

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onToggleHighlight,
                ) {
                    Text(if (isHighlighted) "Unhighlight" else "Highlight")
                }
            }
        }
    }
}

@Composable
private fun rememberHomeViewModel(repository: ScriptureRepository): HomeViewModel {
    // Minimal manual ViewModel creation to avoid DI setup in this first version.
    // This is safe because HomeScreen is hosted under MainActivity and the repository
    // is a stable singleton from Application.
    return androidx.lifecycle.viewmodel.compose.viewModel(
        factory = HomeViewModel.Factory(repository),
    )
}
