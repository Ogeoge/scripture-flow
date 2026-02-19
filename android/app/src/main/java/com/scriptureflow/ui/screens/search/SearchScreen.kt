package com.scriptureflow.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.scriptureflow.app.Routes
import com.scriptureflow.domain.model.VerseRef

// Dummy data class for demonstration purposes until SearchEngine is implemented
data class SearchResult(
    val verseRef: VerseRef,
    val bookName: String, // e.g., "Genesis"
    val verseText: String,
    val matchRanges: List<IntRange>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController
    // In a real app, a ViewModel would be injected here
) {
    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchPerformed by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val performSearch = {
        keyboardController?.hide()
        if (query.isNotBlank()) {
            isSearching = true
            searchPerformed = true
            // In a real app, this would call viewModel.search(query)
            // This would be a coroutine launch. For now, we'll use dummy data.
            searchResults = getDummySearchResults(query)
            isSearching = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search scripture...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { performSearch() }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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
                isSearching -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                searchPerformed && searchResults.isEmpty() -> {
                    Text(
                        text = "No results found for \"$query\"",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                searchResults.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(searchResults) { result ->
                            SearchResultItem(
                                result = result,
                                onClick = {
                                    navController.navigate(
                                        Routes.Reader.build(
                                            bookId = result.verseRef.bookId,
                                            chapter = result.verseRef.chapter,
                                            verse = result.verseRef.verse
                                        )
                                    )
                                }
                            )
                            Divider()
                        }
                    }
                }
                else -> {
                    Text(
                        text = "Enter a term to search the Bible.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(result: SearchResult, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = result.verseRef.toDisplayString(result.bookName),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        HighlightedText(
            fullText = result.verseText,
            ranges = result.matchRanges
        )
    }
}

@Composable
private fun HighlightedText(
    fullText: String,
    ranges: List<IntRange>,
    modifier: Modifier = Modifier
) {
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        val sortedRanges = ranges.sortedBy { it.first }

        sortedRanges.forEach { range ->
            if (range.first > lastIndex) {
                append(fullText.substring(lastIndex, range.first))
            }
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            ) {
                append(fullText.substring(range))
            }
            lastIndex = range.last + 1
        }

        if (lastIndex < fullText.length) {
            append(fullText.substring(lastIndex))
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
    )
}

// Dummy function to provide sample search results
private fun getDummySearchResults(query: String): List<SearchResult> {
    if (query.equals("love", ignoreCase = true)) {
        return listOf(
            SearchResult(
                verseRef = VerseRef("JHN", 3, 16),
                bookName = "John",
                verseText = "For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life.",
                matchRanges = listOf(11..15) // index of "loved"
            ),
            SearchResult(
                verseRef = VerseRef("1JN", 4, 8),
                bookName = "1 John",
                verseText = "He that loveth not knoweth not God; for God is love.",
                matchRanges = listOf(8..13, 46..49) // "loveth", "love"
            )
        )
    }
    return emptyList()
}
