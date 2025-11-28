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
                        horizontal = BrowserDimens.BrowserPaddingScreenEdge,
//                        bottom = 96.dp // Space for FAB
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

        // FLOATING TOP BAR (Rounded Pill)
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(
                    top = BrowserDimens.BrowserSpacingMedium,
                    start = BrowserDimens.BrowserPaddingScreenEdge,
                    end = BrowserDimens.BrowserPaddingScreenEdge
                )
                .fillMaxWidth(),
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
                // Back Button (Circular)
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
                    text = "Tabs (${tabs.size})",
                    style = BrowserTypography.BrowserFontHeadlineSmall,
                    color = BrowserColors.BrowserColorPrimaryText
                )

                // Clear All Button (Circular)
                IconButton(
                    onClick = { showCloseAllSheet = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            BrowserColors.BrowserColorPrimarySurface.copy(alpha = 0.9f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Rounded.DeleteSweep,
                        contentDescription = "Close All",
                        tint = BrowserColors.BrowserColorError
                    )
                }
            }
        }

        // FLOATING ACTION BUTTON (Circular, Accent Color)
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
            containerColor = BrowserColors.BrowserColorAccent,
            contentColor = BrowserColors.BrowserColorPrimarySurface,
            shape = CircleShape
        ) {
            Icon(
                Icons.Rounded.Add,
                contentDescription = "New Tab",
                modifier = Modifier.size(BrowserDimens.BrowserSizeIconLarge)
            )
        }
    }

    // Close All Confirmation Bottom Sheet
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