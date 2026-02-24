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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.scriptureflow.core.model.MatchRange
import com.scriptureflow.core.model.SearchResult
import com.scriptureflow.data.repo.ScriptureRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    repository: ScriptureRepository,
    onOpenReader: (verseId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    fun triggerSearch(newQuery: String) {
        searchJob?.cancel()
        if (newQuery.isBlank()) {
            isSearching = false
            results = emptyList()
            errorMessage = null
            return
        }

        searchJob = scope.launch {
            isSearching = true
            errorMessage = null
            // Small debounce for usability.
            delay(200)
            try {
                results = repository.search(newQuery, limit = 200)
            } catch (t: Throwable) {
                errorMessage = t.message ?: "Search failed"
                results = emptyList()
            } finally {
                isSearching = false
            }
        }
    }

    LaunchedEffect(Unit) {
        // Ensure asset verses are available; keeps first search responsive.
        try {
            repository.ensureVersesLoaded()
        } catch (_: Throwable) {
            // If this fails, the user will see errors upon searching.
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Search",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = query,
            onValueChange = {
                query = it
                triggerSearch(it)
            },
            singleLine = true,
            label = { Text("Search verses") },
            placeholder = { Text("e.g., beginning word") },
        )

        when {
            isSearching -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "Error",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            query.isNotBlank() && results.isEmpty() -> {
                Text(
                    text = "No results",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(
                items = results,
                key = { it.verse.id },
            ) { item ->
                SearchResultRow(
                    result = item,
                    onClick = { onOpenReader(item.verse.id) },
                )
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    result: SearchResult,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "${result.verse.ref.book} ${result.verse.ref.chapter}:${result.verse.ref.verse}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = highlightAnnotated(
                    text = result.verse.text,
                    ranges = result.matchRanges,
                ),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Open",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun highlightAnnotated(
    text: String,
    ranges: List<MatchRange>,
) = buildAnnotatedString {
    append(text)

    if (text.isEmpty() || ranges.isEmpty()) return@buildAnnotatedString

    val safe = ranges
        .asSequence()
        .mapNotNull { r ->
            val start = r.start.coerceIn(0, text.length)
            val end = r.end.coerceIn(0, text.length)
            if (end <= start) null else start to end
        }
        .sortedWith(compareBy<Pair<Int, Int>> { it.first }.thenBy { it.second })
        .toList()

    val highlightStyle = SpanStyle(
        background = MaterialTheme.colorScheme.tertiaryContainer,
        color = MaterialTheme.colorScheme.onTertiaryContainer,
        fontWeight = FontWeight.SemiBold,
    )

    for ((start, end) in safe) {
        addStyle(highlightStyle, start, end)
    }
}
