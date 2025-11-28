package com.octane.browser.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.octane.browser.presentation.components.BookmarkItem
import com.octane.browser.presentation.components.EmptyState
import com.octane.browser.presentation.viewmodels.BookmarkViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
    bookmarkViewModel: BookmarkViewModel = koinViewModel()
) {
    val filteredBookmarks by bookmarkViewModel.filteredBookmarks.collectAsState()
    val searchQuery by bookmarkViewModel.searchQuery.collectAsState()

    var showSearchBar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (showSearchBar) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { bookmarkViewModel.updateSearchQuery(it) },
                    onClose = {
                        bookmarkViewModel.updateSearchQuery("")
                        showSearchBar = false
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("Bookmarks") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSearchBar = true }) {
                            Icon(Icons.Default.Search, "Search")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        if (filteredBookmarks.isEmpty()) {
            EmptyState(
                icon = Icons.Default.StarBorder,
                title = if (searchQuery.isEmpty()) "No Bookmarks" else "No Results",
                message = if (searchQuery.isEmpty())
                    "Tap the star icon in the browser to bookmark pages"
                else
                    "No bookmarks match your search"
            )
        } else {
            LazyColumn(
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredBookmarks, key = { it.id }) { bookmark ->
                    BookmarkItem(
                        bookmark = bookmark,
                        onClick = {
                            onOpenUrl(bookmark.url)
                        },
                        onDelete = {
                            bookmarkViewModel.removeBookmark(bookmark.id)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search bookmarks...") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
        },
        actions = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, "Clear")
                }
            }
        }
    )
}
