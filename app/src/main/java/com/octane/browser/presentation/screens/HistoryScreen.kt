package com.octane.browser.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.octane.browser.domain.models.HistoryEntry
import com.octane.browser.presentation.navigation.BrowserRoute // ✅ FIXED: Correct import
import com.octane.browser.presentation.viewmodels.HistoryViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = koinViewModel()
) {
    val history by viewModel.historyEntries.collectAsState() // ✅ FIXED: Use historyEntries

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearAllHistory() }) {
                        Icon(Icons.Default.Delete, "Clear All")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(history) { entry ->
                HistoryItem(
                    entry = entry,
                    onClick = {
                        // ✅ FIXED: Create new tab every time
                        navController.navigate(
                            BrowserRoute(
                                url = entry.url,
                                forceNewTab = true
                            )
                        )
                    },
                    onDelete = { viewModel.deleteHistoryEntry(entry.id) }
                )
            }
        }
    }
}

@Composable
fun HistoryItem(
    entry: HistoryEntry,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val dateText = remember(entry.visitedAt) {
        dateFormat.format(Date(entry.visitedAt))
    }

    ListItem(
        headlineContent = { Text(entry.title) },
        supportingContent = {
            Column {
                Text(entry.url)
                Text(
                    "$dateText • ${entry.visitCount} visit${if (entry.visitCount > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        leadingContent = {
            Icon(Icons.Default.History, contentDescription = null)
        },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete")
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}