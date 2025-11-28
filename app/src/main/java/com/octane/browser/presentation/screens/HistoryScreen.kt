package com.octane.browser.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.octane.browser.design.*
import com.octane.browser.presentation.components.*
import com.octane.browser.presentation.viewmodels.HistoryViewModel
import org.koin.androidx.compose.koinViewModel

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // ✅ CHANGED
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp)
        ) {
            if (historyEntries.isEmpty()) {
                EmptyState(
                    icon = Icons.Rounded.History,
                    title = if (searchQuery.isEmpty()) "No History" else "No Results",
                    message = if (searchQuery.isEmpty())
                        "Your browsing history will appear here"
                    else
                        "No history entries match your search"
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = BrowserDimens.BrowserPaddingScreenEdge
                    ),
                    verticalArrangement = Arrangement.spacedBy(BrowserDimens.BrowserSpacingUnit),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(historyEntries, key = { it.id }) { entry ->
                        HistoryItem(
                            entry = entry,
                            onClick = {
                                onOpenUrl(entry.url)
                                onBack()
                            }
                        )
                    }
                }
            }
        }

        if (showSearchBar) {
            HistorySearchBar(
                query = searchQuery,
                onQueryChange = { historyViewModel.updateSearchQuery(it) },
                onClose = {
                    historyViewModel.updateSearchQuery("")
                    showSearchBar = false
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)


            )
        } else {
            HistoryTopBar(
                onBack = onBack,
                onSearch = { showSearchBar = true },
                onClearAll = { showClearSheet = true },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                        start = BrowserDimens.BrowserPaddingScreenEdge,
                        end = BrowserDimens.BrowserPaddingScreenEdge
                    )
            )
        }
    }

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

@Composable
private fun HistoryTopBar(
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium, // ✅ CHANGED
        color = MaterialTheme.colorScheme.surface.copy(alpha = BrowserOpacity.BrowserOpacitySurfaceHigh), // ✅ CHANGED
        shadowElevation = BrowserDimens.BrowserElevationMedium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = BrowserDimens.BrowserSpacingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), // ✅ CHANGED
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface // ✅ CHANGED
                )
            }

            Text(
                text = "History",
                style = MaterialTheme.typography.headlineSmall, // ✅ CHANGED
                color = MaterialTheme.colorScheme.onSurface // ✅ CHANGED
            )

            Row(horizontalArrangement = Arrangement.spacedBy(BrowserDimens.BrowserSpacingSmall)) {
                IconButton(
                    onClick = onSearch,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), // ✅ CHANGED
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface // ✅ CHANGED
                    )
                }

                IconButton(
                    onClick = onClearAll,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), // ✅ CHANGED
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Rounded.DeleteSweep,
                        contentDescription = "Clear History",
                        tint = MaterialTheme.colorScheme.error // ✅ CHANGED
                    )
                }
            }
        }
    }
}

@Composable
private fun HistorySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium, // ✅ CHANGED
        color = MaterialTheme.colorScheme.surface.copy(alpha = BrowserOpacity.BrowserOpacitySurfaceHigh), // ✅ CHANGED
        shadowElevation = BrowserDimens.BrowserElevationMedium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = BrowserDimens.BrowserSpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = "Close Search",
                    tint = MaterialTheme.colorScheme.onSurface // ✅ CHANGED
                )
            }

            Spacer(modifier = Modifier.width(BrowserDimens.BrowserSpacingSmall))

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy( // ✅ CHANGED
                    color = MaterialTheme.colorScheme.onSurface // ✅ CHANGED
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            "Search history...",
                            style = MaterialTheme.typography.bodyMedium, // ✅ CHANGED
                            color = MaterialTheme.colorScheme.onSurfaceVariant // ✅ CHANGED
                        )
                    }
                    innerTextField()
                }
            )

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Clear",
                        modifier = Modifier.size(BrowserDimens.BrowserSizeIconMedium),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant // ✅ CHANGED
                    )
                }
            }
        }
    }
}