package com.scriptureflow.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.scriptureflow.core.model.Verse
import com.scriptureflow.data.db.entities.BookmarkEntity
import com.scriptureflow.data.repo.ScriptureRepository
import kotlinx.coroutines.launch

@Composable
fun BookmarksScreen(
    repository: ScriptureRepository,
    onOpenReader: (verseId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val bookmarks by repository.observeBookmarks().collectAsState(initial = emptyList())

    // Ensure the bible corpus is loaded so we can resolve verse_id -> Verse.
    var isLoadingVerses by remember { mutableStateOf(true) }
    var versesById by remember { mutableStateOf<Map<String, Verse>>(emptyMap()) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoadingVerses = true
        loadError = null
        try {
            val verses = repository.ensureVersesLoaded()
            versesById = verses.associateBy { it.id }
        } catch (t: Throwable) {
            loadError = t.message ?: "Failed to load verses"
            versesById = emptyMap()
        } finally {
            isLoadingVerses = false
        }
    }

    val scope = rememberCoroutineScope()
    var pendingRemove by remember { mutableStateOf<BookmarkEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Bookmarks",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        if (loadError != null) {
            Text(
                text = loadError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        when {
            isLoadingVerses -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            bookmarks.isEmpty() -> {
                Text(
                    text = "No bookmarks yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(
                        items = bookmarks,
                        key = { it.verseId },
                    ) { b ->
                        val verse = versesById[b.verseId]
                        BookmarkRow(
                            verseId = b.verseId,
                            verse = verse,
                            onOpen = { onOpenReader(b.verseId) },
                            onRemove = { pendingRemove = b },
                        )
                    }
                }
            }
        }
    }

    val toRemove = pendingRemove
    if (toRemove != null) {
        AlertDialog(
            onDismissRequest = { pendingRemove = null },
            title = { Text("Remove bookmark?") },
            text = { Text("This will remove the bookmark for ${toRemove.verseId}.") },
            confirmButton = {
                Button(
                    onClick = {
                        pendingRemove = null
                        scope.launch {
                            repository.removeBookmark(toRemove.verseId)
                        }
                    },
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingRemove = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun BookmarkRow(
    verseId: String,
    verse: Verse?,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = verse?.let { "${it.ref.book} ${it.ref.chapter}:${it.ref.verse}" } ?: verseId,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                IconButton(onClick = onRemove) {
                    Text(
                        text = "Remove",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            if (verse != null) {
                Text(
                    text = verse.text,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Text(
                    text = "Verse not found in local corpus.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
