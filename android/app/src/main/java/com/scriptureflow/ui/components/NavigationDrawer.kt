package com.scriptureflow.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.scriptureflow.data.assets.BookAsset

/**
 * The content displayed inside the modal navigation drawer.
 * It manages the two-stage selection process: first a book, then a chapter.
 *
 * @param books The list of all Bible books to display.
 * @param onNavigateToChapter Callback invoked when a book and chapter are selected.
 *   It provides the book ID and the 1-based chapter number.
 */
@Composable
fun NavigationDrawerContent(
    books: List<BookAsset>,
    onNavigateToChapter: (bookId: String, chapter: Int) -> Unit
) {
    var selectedBook by remember { mutableStateOf<BookAsset?>(null) }

    ModalDrawerSheet {
        Crossfade(targetState = selectedBook, label = "DrawerContentCrossfade") { book ->
            if (book == null) {
                BookList(
                    books = books,
                    onBookSelected = { selectedBook = it }
                )
            } else {
                ChapterGrid(
                    book = book,
                    onChapterSelected = { chapter ->
                        onNavigateToChapter(book.bookId, chapter)
                    },
                    onBack = { selectedBook = null }
                )
            }
        }
    }
}

@Composable
private fun BookList(
    books: List<BookAsset>,
    onBookSelected: (BookAsset) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {
        Text(
            text = "Books",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        HorizontalDivider()
        LazyColumn {
            items(books, key = { it.bookId }) { book ->
                val bookSelectDescription = "Select book: ${book.name}" // For accessibility
                NavigationDrawerItem(
                    label = { Text(book.name) },
                    selected = false,
                    onClick = { onBookSelected(book) },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .semantics { contentDescription = bookSelectDescription }
                )
            }
        }
    }
}

@Composable
private fun ChapterGrid(
    book: BookAsset,
    onChapterSelected: (chapter: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chapters = (1..book.chapters.size).toList()
    val backButtonDescription = "Back to book list"

    Column(modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.semantics { contentDescription = backButtonDescription }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null // Handled by parent modifier
                )
            }
            Text(
                text = book.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        HorizontalDivider()
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 64.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chapters) { chapter ->
                val chapterSelectDescription = "Select chapter $chapter"
                OutlinedButton(
                    onClick = { onChapterSelected(chapter) },
                    modifier = Modifier
                        .aspectRatio(1f) // Makes the buttons square
                        .semantics { contentDescription = chapterSelectDescription },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = chapter.toString(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
