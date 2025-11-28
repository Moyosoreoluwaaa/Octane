package com.octane.browser.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.octane.browser.design.*
import com.octane.browser.presentation.components.*
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import com.octane.browser.presentation.viewmodels.TabManagerViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TabManagerScreen(
    onBack: () -> Unit,
    browserViewModel: BrowserViewModel = koinViewModel(),
    tabManagerViewModel: TabManagerViewModel = koinViewModel()
) {
    val tabs by tabManagerViewModel.tabs.collectAsState()
    val activeTab by browserViewModel.activeTab.collectAsState()

    var showCloseAllSheet by remember { mutableStateOf(false) }

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
            if (tabs.isEmpty()) {
                EmptyState(
                    icon = Icons.Rounded.Tab,
                    title = "No Tabs",
                    message = "Tap + to create a new tab"
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(
                        horizontal = BrowserDimens.BrowserPaddingScreenEdge
                    ),
                    horizontalArrangement = Arrangement.spacedBy(BrowserDimens.BrowserSpacingUnit),
                    verticalArrangement = Arrangement.spacedBy(BrowserDimens.BrowserSpacingUnit),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(tabs, key = { it.id }) { tab ->
                        TabPreview(
                            tab = tab,
                            isActive = tab.id == activeTab?.id,
                            onClick = {
                                browserViewModel.switchToTab(tab.id)
                                onBack()
                            },
                            onClose = {
                                tabManagerViewModel.closeTab(tab.id)
                            }
                        )
                    }
                }
            }
        }

        // Floating Top Bar
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(
                    start = BrowserDimens.BrowserPaddingScreenEdge,
                    end = BrowserDimens.BrowserPaddingScreenEdge
                )
                .fillMaxWidth(),
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
                    text = "Tabs (${tabs.size})",
                    style = MaterialTheme.typography.headlineSmall, // ✅ CHANGED
                    color = MaterialTheme.colorScheme.onSurface // ✅ CHANGED
                )

                IconButton(
                    onClick = { showCloseAllSheet = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), // ✅ CHANGED
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Rounded.DeleteSweep,
                        contentDescription = "Close All",
                        tint = MaterialTheme.colorScheme.error // ✅ CHANGED
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = {
                tabManagerViewModel.createNewTab()
                onBack()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(
                    end = BrowserDimens.BrowserPaddingScreenEdge,
                    bottom = BrowserDimens.BrowserPaddingBarBottom
                ),
            containerColor = MaterialTheme.colorScheme.primary, // ✅ CHANGED
            contentColor = MaterialTheme.colorScheme.onPrimary, // ✅ CHANGED
            shape = CircleShape
        ) {
            Icon(
                Icons.Rounded.Add,
                contentDescription = "New Tab",
                modifier = Modifier.size(BrowserDimens.BrowserSizeIconLarge)
            )
        }
    }

    if (showCloseAllSheet) {
        ConfirmationBottomSheet(
            title = "Close All Tabs?",
            message = "This will close all ${tabs.size} tabs and create a new empty tab.",
            confirmText = "Close All",
            isDestructive = true,
            onDismiss = { showCloseAllSheet = false },
            onConfirm = {
                tabManagerViewModel.closeAllTabs()
                onBack()
            }
        )
    }
}