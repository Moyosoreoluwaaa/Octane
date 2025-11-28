package com.octane.browser.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
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

    // FLOATING DESIGN: Box with background + floating elements
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrowserColors.BrowserColorPrimaryBackground)
    ) {
        // Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 72.dp) // Space for floating top bar
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
                        horizontal = BrowserDimens.BrowserPaddingScreenEdge,
//                        bottom = BrowserDimens.BrowserSpacingXLarge
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

        // FLOATING TOP BAR (Rounded Pill)
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
                    .statusBarsPadding()
                    .padding(
                        top = BrowserDimens.BrowserSpacingMedium,
                        start = BrowserDimens.BrowserPaddingScreenEdge,
                        end = BrowserDimens.BrowserPaddingScreenEdge
                    )
            )
        } else {
            HistoryTopBar(
                onBack = onBack,
                onSearch = { showSearchBar = true },
                onClearAll = { showClearSheet = true },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(
                        top = BrowserDimens.BrowserSpacingMedium,
                        start = BrowserDimens.BrowserPaddingScreenEdge,
                        end = BrowserDimens.BrowserPaddingScreenEdge
                    )
            )
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

@Composable
private fun HistoryTopBar(
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BrowserDimens.BrowserShapeRoundedMedium),
        color = BrowserColors.BrowserColorPrimarySurface.copy(alpha = BrowserOpacity.BrowserOpacitySurfaceHigh),
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
            // Back Button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        BrowserColors.BrowserColorPrimarySurface.copy(alpha = 0.9f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = BrowserColors.BrowserColorPrimaryText
                )
            }

            // Title
            Text(
                text = "History",
                style = BrowserTypography.BrowserFontHeadlineSmall,
                color = BrowserColors.BrowserColorPrimaryText
            )

            // Actions Row
            Row(horizontalArrangement = Arrangement.spacedBy(BrowserDimens.BrowserSpacingSmall)) {
                IconButton(
                    onClick = onSearch,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            BrowserColors.BrowserColorPrimarySurface.copy(alpha = 0.9f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = BrowserColors.BrowserColorPrimaryText
                    )
                }

                IconButton(
                    onClick = onClearAll,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            BrowserColors.BrowserColorPrimarySurface.copy(alpha = 0.9f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Rounded.DeleteSweep,
                        contentDescription = "Clear History",
                        tint = BrowserColors.BrowserColorError
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
        shape = RoundedCornerShape(BrowserDimens.BrowserShapeRoundedMedium),
        color = BrowserColors.BrowserColorPrimarySurface.copy(alpha = BrowserOpacity.BrowserOpacitySurfaceHigh),
        shadowElevation = BrowserDimens.BrowserElevationMedium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = BrowserDimens.BrowserSpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = "Close Search",
                    tint = BrowserColors.BrowserColorPrimaryText
                )
            }

            Spacer(modifier = Modifier.width(BrowserDimens.BrowserSpacingSmall))

            // Search TextField
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = BrowserTypography.BrowserFontBodyMedium.copy(
                    color = BrowserColors.BrowserColorPrimaryText
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            "Search history...",
                            style = BrowserTypography.BrowserFontBodyMedium,
                            color = BrowserColors.BrowserColorSecondaryText
                        )
                    }
                    innerTextField()
                }
            )

            // Clear Button
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Clear",
                        modifier = Modifier.size(BrowserDimens.BrowserSizeIconMedium),
                        tint = BrowserColors.BrowserColorSecondaryText
                    )
                }
            }
        }
    }
}