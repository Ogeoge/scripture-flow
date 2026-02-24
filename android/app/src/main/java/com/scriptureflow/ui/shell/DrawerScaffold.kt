package com.scriptureflow.ui.shell

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.scriptureflow.core.model.Verse
import com.scriptureflow.data.repo.ScriptureRepository
import com.scriptureflow.ui.navigation.Routes
import com.scriptureflow.ui.navigation.rememberAppNavController
import kotlinx.coroutines.launch

/**
 * Material3 drawer scaffold with:
 * - hybrid selection flow: Book -> Chapter -> Verse
 * - links to Home/Search/Bookmarks/Highlights/Settings
 *
 * This is UI-only; it relies on [ScriptureRepository] for offline-first asset-loaded data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerScaffold(
    repository: ScriptureRepository,
    modifier: Modifier = Modifier,
    content: @Composable (navController: NavHostController) -> Unit,
) {
    val navController = rememberAppNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val selection = remember { DrawerSelectionState() }

    // Load books once drawer is used; also warms verse cache for the app.
    LaunchedEffect(Unit) {
        repository.ensureVersesLoaded()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                repository = repository,
                selection = selection,
                onNavigate = { route ->
                    scope.launch {
                        drawerState.close()
                    }
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onOpenReaderForVerseId = { verseId ->
                    scope.launch { drawerState.close() }
                    navController.navigate(Routes.reader(verseId)) {
                        launchSingleTop = true
                    }
                },
            )
        },
        modifier = modifier,
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(text = "ScriptureFlow") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            },
                        ) {
                            Icon(imageVector = Icons.Filled.Menu, contentDescription = "Open navigation drawer")
                        }
                    },
                )
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                content(navController)
            }
        }
    }
}

private class DrawerSelectionState {
    val selectedBook: MutableState<String?> = mutableStateOf(null)
    val selectedChapter: MutableState<Int?> = mutableStateOf(null)

    fun resetToBooks() {
        selectedBook.value = null
        selectedChapter.value = null
    }

    fun resetToChapters(book: String) {
        selectedBook.value = book
        selectedChapter.value = null
    }

    fun setChapter(chapter: Int) {
        selectedChapter.value = chapter
    }
}

@Composable
private fun DrawerContent(
    repository: ScriptureRepository,
    selection: DrawerSelectionState,
    onNavigate: (String) -> Unit,
    onOpenReaderForVerseId: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var books by remember { mutableStateOf<List<String>>(emptyList()) }
    var chapters by remember { mutableStateOf<List<Int>>(emptyList()) }
    var verses by remember { mutableStateOf<List<Verse>>(emptyList()) }

    val selectedBook = selection.selectedBook.value
    val selectedChapter = selection.selectedChapter.value

    LaunchedEffect(Unit) {
        books = repository.getBooks()
    }

    LaunchedEffect(selectedBook) {
        if (selectedBook.isNullOrBlank()) {
            chapters = emptyList()
            verses = emptyList()
        } else {
            chapters = repository.getChapters(selectedBook)
            selection.selectedChapter.value = null
            verses = emptyList()
        }
    }

    LaunchedEffect(selectedBook, selectedChapter) {
        if (!selectedBook.isNullOrBlank() && selectedChapter != null) {
            verses = repository.getVerses(selectedBook, selectedChapter)
        } else {
            verses = emptyList()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
    ) {
        Text(
            text = "Navigate",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        DrawerPrimaryLinks(
            onNavigate = onNavigate,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))
        Divider()
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Browse",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        when {
            selectedBook == null -> {
                BooksList(
                    books = books,
                    onSelectBook = { book -> selection.resetToChapters(book) },
                )
            }

            selectedChapter == null -> {
                ChaptersList(
                    book = selectedBook,
                    chapters = chapters,
                    onBack = { selection.resetToBooks() },
                    onSelectChapter = { ch -> selection.setChapter(ch) },
                )
            }

            else -> {
                VersesList(
                    book = selectedBook,
                    chapter = selectedChapter,
                    verses = verses,
                    onBack = { selection.resetToChapters(selectedBook) },
                    onOpenVerse = { verse -> onOpenReaderForVerseId(verse.id) },
                )
            }
        }
    }
}

@Composable
private fun DrawerPrimaryLinks(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        NavigationDrawerItem(
            label = { Text("Home") },
            selected = false,
            onClick = { onNavigate(Routes.HOME) },
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        )
        NavigationDrawerItem(
            label = { Text("Search") },
            selected = false,
            onClick = { onNavigate(Routes.SEARCH) },
            icon = { Icon(Icons.Filled.Search, contentDescription = null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        )
        NavigationDrawerItem(
            label = { Text("Bookmarks") },
            selected = false,
            onClick = { onNavigate(Routes.BOOKMARKS) },
            icon = { Icon(Icons.Filled.Bookmark, contentDescription = null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        )
        NavigationDrawerItem(
            label = { Text("Highlights") },
            selected = false,
            onClick = { onNavigate(Routes.HIGHLIGHTS) },
            icon = { Icon(Icons.Filled.Star, contentDescription = null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        )
        NavigationDrawerItem(
            label = { Text("Settings") },
            selected = false,
            onClick = { onNavigate(Routes.SETTINGS) },
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        )
    }
}

@Composable
private fun BooksList(
    books: List<String>,
    onSelectBook: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        items(books) { book ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectBook(book) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = book, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun ChaptersList(
    book: String,
    chapters: List<Int>,
    onBack: () -> Unit,
    onSelectChapter: (Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back to books")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = book, style = MaterialTheme.typography.titleSmall)
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(chapters) { chapter ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectChapter(chapter) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "Chapter $chapter", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun VersesList(
    book: String,
    chapter: Int,
    verses: List<Verse>,
    onBack: () -> Unit,
    onOpenVerse: (Verse) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back to chapters")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "$book $chapter", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(onClick = onBack) {
                Text(text = "Chapters")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(verses) { verse ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenVerse(verse) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = "${verse.ref.verse}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.width(32.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = verse.text,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                    )
                }
            }
        }
    }
}
