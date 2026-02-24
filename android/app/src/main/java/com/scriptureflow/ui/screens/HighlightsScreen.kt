package com.scriptureflow.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scriptureflow.core.model.Verse
import com.scriptureflow.data.db.entities.HighlightEntity
import com.scriptureflow.data.repo.ScriptureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HighlightsScreen(
    repository: ScriptureRepository,
    onOpenReader: (verseId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val highlights by repository.observeHighlights().collectAsState(initial = emptyList())

    var resolvedVerses by remember { mutableStateOf<Map<String, Verse?>>(emptyMap()) }
    var isResolving by remember { mutableStateOf(true) }

    LaunchedEffect(highlights) {
        isResolving = true
        resolvedVerses = withContext(Dispatchers.IO) {
            val map = LinkedHashMap<String, Verse?>(highlights.size)
            for (h in highlights) {
                map[h.verseId] = repository.getVerseById(h.verseId)
            }
            map
        }
        isResolving = false
    }

    var pendingRemove by remember { mutableStateOf<HighlightEntity?>(null) }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Highlights",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        if (isResolving && highlights.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        if (highlights.isEmpty()) {
            Text(
                text = "No highlights yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(
                    items = highlights,
                    key = { it.verseId },
                ) { h ->
                    val verse = resolvedVerses[h.verseId]

                    HighlightRow(
                        highlight = h,
                        verse = verse,
                        onOpen = { onOpenReader(h.verseId) },
                        onRemove = { pendingRemove = h },
                    )
                }
            }
        }
    }

    val toRemove = pendingRemove
    if (toRemove != null) {
        val scope = androidx.compose.runtime.rememberCoroutineScope()
        AlertDialog(
            onDismissRequest = { pendingRemove = null },
            title = { Text("Remove highlight?") },
            text = { Text("This will remove the highlight for ${toRemove.verseId}.") },
            confirmButton = {
                Button(
                    onClick = {
                        pendingRemove = null
                        // Fire-and-forget from UI; repository uses Room (local).
                        scope.launch {
                            repository.clearHighlight(toRemove.verseId)
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
private fun HighlightRow(
    highlight: HighlightEntity,
    verse: Verse?,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
) {
    val subtitle = if (verse != null) {
        "${verse.ref.book} ${verse.ref.chapter}:${verse.ref.verse}"
    } else {
        highlight.verseId
    }

    val previewText = verse?.text?.take(140)?.let { if (it.length < (verse.text.length)) "$it…" else it }
        ?: "(Verse not found in local assets)"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = previewText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            OutlinedButton(
                onClick = onRemove,
            ) {
                Text("Remove")
            }
        }

        Text(
            text = "Color: #${highlight.colorArgb.toUInt().toString(16).padStart(8, '0').uppercase()}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

