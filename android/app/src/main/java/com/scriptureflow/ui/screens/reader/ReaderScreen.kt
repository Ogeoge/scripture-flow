package com.scriptureflow.ui.screens.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.scriptureflow.app.Routes
import com.scriptureflow.data.assets.ChapterAsset
import com.scriptureflow.data.assets.RangeAsset
import com.scriptureflow.data.assets.VerseAsset
import com.scriptureflow.data.repo.ReadingSettings
import kotlinx.coroutines.launch

// In a real app, this state would be provided by a ViewModel.
@Immutable
data class ReaderScreenState(
    val bookName: String,
    val chapterNumber: Int,
    val chapterContent: ChapterAsset?,
    val readingSettings: ReadingSettings,
    val isLoading: Boolean,
    val error: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    navController: NavController,
    bookId: String,
    chapter: Int,
    initialVerse: Int?
) {
    // In a real app, this would be collected from a ViewModel, e.g.:
    // val viewModel: ReaderViewModel = hiltViewModel()
    // val state by viewModel.uiState.collectAsState()

    // Using placeholder state for demonstration purposes.
    val state = ReaderScreenState(
        bookName = bookId, // Using bookId as a placeholder for the book name
        chapterNumber = chapter,
        chapterContent = ChapterAsset( // Example content
            chapter = chapter,
            verses = listOf(
                VerseAsset(verse = 1, text = "In the beginning God created the heaven and the earth."),
                VerseAsset(verse = 2, text = "And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters."),
                VerseAsset(
                    verse = 3,
                    text = "And God said, Let there be light: and there was light.",
                    // Example of red-letter ranges, assuming the text "Let there be light:"
                    redLetterRanges = listOf(RangeAsset(start = 14, end = 33))
                ),
                VerseAsset(verse = 4, text = "And God saw the light, that it was good: and God divided the light from the darkness.")
            )
        ),
        readingSettings = ReadingSettings.default(),
        isLoading = false,
        error = null
    )

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll to the initial verse when content is first loaded.
    LaunchedEffect(state.chapterContent, initialVerse) {
        if (state.chapterContent != null && initialVerse != null && initialVerse > 0) {
            val index = state.chapterContent.verses.indexOfFirst { it.verse == initialVerse }
            if (index != -1) {
                coroutineScope.launch {
                    listState.scrollToItem(index)
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text(text = "${state.bookName} ${state.chapterNumber}") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.Search) }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { /* TODO: Open overflow menu for settings/bookmarks */ }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More options")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.error != null -> {
                    Text(
                        text = "Error: ${state.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.chapterContent != null -> {
                    ChapterContent(
                        chapter = state.chapterContent,
                        settings = state.readingSettings,
                        listState = listState,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun ChapterContent(
    chapter: ChapterAsset,
    settings: ReadingSettings,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        itemsIndexed(
            items = chapter.verses,
            key = { _, verse -> verse.verse } // Stable key for performance
        ) { _, verse ->
            VerseItem(verse = verse, settings = settings)
            if (settings.paragraphSpacingDp > 0) {
                Spacer(modifier = Modifier.height(settings.paragraphSpacingDp.dp))
            }
        }
    }
}

@Composable
private fun VerseItem(
    verse: VerseAsset,
    settings: ReadingSettings
) {
    // TODO: Implement long-press gestures for selection.
    // TODO: Apply background color for highlights based on data from the repository.
    Text(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                append("${verse.verse} ")
            }
            val verseText = verse.text
            append(verseText)

            if (settings.redLetterEnabled) {
                val offset = "${verse.verse} ".length
                verse.redLetterRanges.forEach { range ->
                    if (range.start < range.end && range.end <= verseText.length) {
                        addStyle(
                            style = SpanStyle(color = Color.Red),
                            start = offset + range.start,
                            end = offset + range.end
                        )
                    }
                }
            }
        },
        fontFamily = if (settings.fontFamily == "SERIF") FontFamily.Serif else FontFamily.SansSerif,
        fontSize = settings.fontSizeSp.sp,
        lineHeight = (settings.fontSizeSp * settings.lineSpacingMultiplier).sp,
        modifier = Modifier.fillMaxWidth()
    )
}
