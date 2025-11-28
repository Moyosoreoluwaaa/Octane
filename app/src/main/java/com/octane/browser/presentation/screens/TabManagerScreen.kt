package com.octane.browser.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.octane.browser.presentation.components.TabPreview
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import com.octane.browser.presentation.viewmodels.TabManagerViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabManagerScreen(
    onBack: () -> Unit,
    browserViewModel: BrowserViewModel = koinViewModel(),
    tabManagerViewModel: TabManagerViewModel = koinViewModel()
) {
    val tabs by tabManagerViewModel.tabs.collectAsState()
    val activeTab by browserViewModel.activeTab.collectAsState()

    var showCloseAllSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tabs (${tabs.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showCloseAllSheet = true }) {
                        Icon(Icons.Default.DeleteSweep, "Close All")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    tabManagerViewModel.createNewTab()
                    onBack()
                }
            ) {
                Icon(Icons.Default.Add, "New Tab")
            }
        }
    ) { paddingValues ->
        if (tabs.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Tab,
                title = "No Tabs",
                message = "Tap + to create a new tab"
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = paddingValues,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
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
