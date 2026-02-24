package com.scriptureflow.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scriptureflow.core.model.ReadingPreferences
import com.scriptureflow.core.model.Verse
import com.scriptureflow.data.prefs.ReadingPrefsStore
import com.scriptureflow.data.repo.ScriptureRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    repository: ScriptureRepository,
    readingPreferences: ReadingPreferences,
    verseId: String?,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isLoading by remember(verseId) { mutableStateOf(true) }
    var verse by remember(verseId) { mutableStateOf<Verse?>(null) }
    var isBookmarked by remember(verseId) { mutableStateOf(false) }
    var isHighlighted by remember(verseId) { mutableStateOf(false) }
    var errorMessage by remember(verseId) { mutableStateOf<String?>(null) }

    LaunchedEffect(verseId) {
        isLoading = true
        errorMessage = null

        if (verseId.isNullOrBlank()) {
            verse = null
            isBookmarked = false
            isHighlighted = false
            errorMessage = "Missing verse id."
            isLoading = false
            return@LaunchedEffect
        }

        try {
            repository.ensureVersesLoaded()
            verse = repository.getVerseById(verseId)
            if (verse == null) {
                errorMessage = "Verse not found: $verseId"
                isBookmarked = false
                isHighlighted = false
            } else {
                isBookmarked = repository.isBookmarked(verseId)
                isHighlighted = repository.isHighlighted(verseId)
            }
        } catch (t: Throwable) {
            errorMessage = t.message ?: "Failed to load verse."
            verse = null
            isBookmarked = false
            isHighlighted = false
        } finally {
            isLoading = false
        }
    }

    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Text(
                    text = verse?.let { "${it.ref.book} ${it.ref.chapter}:${it.ref.verse}" }
                        ?: "Reader",
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    // Avoid adding new resources; use a simple text-based icon.
                    Text(text = "←", style = MaterialTheme.typography.titleLarge)
                }
            },
            actions = {
                IconButton(onClick = onOpenSettings) {
                    Text(text = "⚙", style = MaterialTheme.typography.titleMedium)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )

        when {
            isLoading -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = errorMessage ?: "Error",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    OutlinedButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            }

            verse != null -> {
                val v = requireNotNull(verse)

                ReaderActionsRow(
                    isBookmarked = isBookmarked,
                    isHighlighted = isHighlighted,
                    onToggleBookmark = {
                        val id = v.id
                        if (isBookmarked) {
                            repository.removeBookmark(id)
                            isBookmarked = false
                        } else {
                            repository.addBookmark(id)
                            isBookmarked = true
                        }
                    },
                    onToggleHighlight = {
                        val id = v.id
                        if (isHighlighted) {
                            repository.clearHighlight(id)
                            isHighlighted = false
                        } else {
                            // Default highlight color (soft yellow) in ARGB.
                            repository.setHighlight(id, colorArgb = 0xFFFFF59D.toInt())
                            isHighlighted = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                )

                val textStyle = readerTextStyle(readingPreferences)

                SelectionContainer {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = v.text,
                            style = textStyle,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Reading settings are available in Settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderActionsRow(
    isBookmarked: Boolean,
    isHighlighted: Boolean,
    onToggleBookmark: suspend () -> Unit,
    onToggleHighlight: suspend () -> Unit,
    modifier: Modifier = Modifier,
) {
    var busy by remember { mutableStateOf(false) }
    var lastError by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val scope = androidx.compose.runtime.rememberCoroutineScope()

            Button(
                modifier = Modifier.weight(1f),
                enabled = !busy,
                onClick = {
                    lastError = null
                    busy = true
                    scope.launch {
                        try {
                            onToggleBookmark()
                        } catch (t: Throwable) {
                            lastError = t.message ?: "Failed to update bookmark."
                        } finally {
                            busy = false
                        }
                    }
                },
            ) {
                Text(if (isBookmarked) "Remove Bookmark" else "Bookmark")
            }

            OutlinedButton(
                modifier = Modifier.weight(1f),
                enabled = !busy,
                onClick = {
                    lastError = null
                    busy = true
                    scope.launch {
                        try {
                            onToggleHighlight()
                        } catch (t: Throwable) {
                            lastError = t.message ?: "Failed to update highlight."
                        } finally {
                            busy = false
                        }
                    }
                },
            ) {
                Text(if (isHighlighted) "Unhighlight" else "Highlight")
            }
        }

        if (lastError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = lastError ?: "Error",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun readerTextStyle(prefs: ReadingPreferences): TextStyle {
    val fontFamily = when (prefs.fontStyle) {
        ReadingPrefsStore.FONT_STYLE_SERIF -> FontFamily.Serif
        ReadingPrefsStore.FONT_STYLE_SANS -> FontFamily.SansSerif
        else -> FontFamily.Serif
    }

    val textAlign = when (prefs.textAlignment) {
        ReadingPrefsStore.TEXT_ALIGN_CENTER -> TextAlign.Center
        ReadingPrefsStore.TEXT_ALIGN_JUSTIFY -> TextAlign.Justify
        ReadingPrefsStore.TEXT_ALIGN_START -> TextAlign.Start
        else -> TextAlign.Start
    }

    val fontSize = prefs.fontSizeSp.coerceIn(12f, 42f)
    val lineHeightMultiplier = prefs.lineHeightMultiplier.coerceIn(1.0f, 2.0f)

    return MaterialTheme.typography.bodyLarge.copy(
        fontFamily = fontFamily,
        fontSize = fontSize.sp,
        lineHeight = (fontSize * lineHeightMultiplier).sp,
        textAlign = textAlign,
    )
}
