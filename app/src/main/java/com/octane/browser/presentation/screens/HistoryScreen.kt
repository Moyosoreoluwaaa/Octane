package com.octane.browser.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
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
import com.octane.browser.presentation.components.ConfirmationBottomSheet
import com.octane.browser.presentation.components.EmptyState
import com.octane.browser.presentation.components.HistoryItem
import com.octane.browser.presentation.viewmodels.HistoryViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
    historyViewModel: HistoryViewModel = koinViewModel()
) {
    val historyEntries by historyViewModel.historyEntries.collectAsState()
    val searchQuery by historyViewModel.searchQuery.collectAsState()

    var showSearchBar by remember { mutableStateOf(false) }
    var showClearSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (showSearchBar) {
                HistorySearchBar(
                    query = searchQuery,
                    onQueryChange = { historyViewModel.updateSearchQuery(it) },
                    onClose = {
                        historyViewModel.updateSearchQuery("")
                        showSearchBar = false
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("History") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSearchBar = true }) {
                            Icon(Icons.Default.Search, "Search")
                        }
                        IconButton(onClick = { showClearSheet = true }) {
                            Icon(Icons.Default.DeleteSweep, "Clear History")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        if (historyEntries.isEmpty()) {
            EmptyState(
                icon = Icons.Default.History,
                title = if (searchQuery.isEmpty()) "No History" else "No Results",
                message = if (searchQuery.isEmpty())
                    "Your browsing history will appear here"
                else
                    "No history entries match your search"
            )
        } else {
            LazyColumn(
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(historyEntries, key = { it.id }) { entry ->
                    HistoryItem(
                        entry = entry,
                        onClick = {
                            onOpenUrl(entry.url)
                        }
                    )
                }
            }
        }
    }

    // Clear History Bottom Sheet
    if (showClearSheet) {
        ConfirmationBottomSheet(
            title = "Clear History?",
            message = "This will permanently delete all your browsing history.",
            confirmText = "Clear",
            isDestructive = true,
            onDismiss = { showClearSheet = false },
            onConfirm = {
                historyViewModel.clearAllHistory()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistorySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search history...") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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