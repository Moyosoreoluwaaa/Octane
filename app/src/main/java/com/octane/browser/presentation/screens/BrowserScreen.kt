package com.octane.browser.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.octane.browser.presentation.components.AddressBar
import com.octane.browser.presentation.components.NavigationControls
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import com.octane.browser.webview.WebViewContainer
import org.koin.androidx.compose.koinViewModel

@Composable
fun BrowserScreen(
    onOpenTabManager: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    browserViewModel: BrowserViewModel = koinViewModel()
) {
    val webViewState by browserViewModel.webViewState.collectAsState()
    val isBookmarked by browserViewModel.isBookmarked.collectAsState()
    val tabs by browserViewModel.tabs.collectAsState()
    val showPhishingWarning by browserViewModel.showPhishingWarning.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen for navigation events
    LaunchedEffect(Unit) {
        browserViewModel.navigationEvent.collect { event ->
            when (event) {
                is BrowserViewModel.NavigationEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                is BrowserViewModel.NavigationEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                else -> { /* Handled by WebViewContainer */
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AddressBar(
                webViewState = webViewState,
                isBookmarked = isBookmarked,
                onNavigate = { browserViewModel.navigateToUrl(it) },
                onReload = { browserViewModel.reload() },
                onStop = { browserViewModel.stopLoading() },
                onBookmarkToggle = { browserViewModel.toggleBookmark() },
                onMenuClick = { showMenu = true }
            )
        },
        bottomBar = {
            NavigationControls(
                webViewState = webViewState,
                tabCount = tabs.size,
                onBack = { browserViewModel.goBack() },
                onForward = { browserViewModel.goForward() },
                onHome = { browserViewModel.navigateToUrl("about:blank") },
                onTabManager = onOpenTabManager
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            WebViewContainer(
                browserViewModel = browserViewModel
            )
        }
    }

    // Browser Menu
    if (showMenu) {
        BrowserMenu(
            onDismiss = { showMenu = false },
            onBookmarks = {
                showMenu = false
                onOpenBookmarks()
            },
            onHistory = {
                showMenu = false
                onOpenHistory()
            },
            onSettings = {
                showMenu = false
                onOpenSettings()
            },
            onNewTab = {
                showMenu = false
                browserViewModel.createNewTab()
            },
            onShare = {
                showMenu = false
                // TODO: Implement share
            }
        )
    }

    // Phishing Warning Dialog
    showPhishingWarning?.let { warning ->
        AlertDialog(
            onDismissRequest = { browserViewModel.dismissPhishingWarning() },
            title = { Text("⚠️ Security Warning") },
            text = { Text(warning) },
            confirmButton = {
                TextButton(onClick = { browserViewModel.dismissPhishingWarning() }) {
                    Text("Go Back")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { browserViewModel.proceedDespitePhishingWarning() }
                ) {
                    Text("Proceed Anyway")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrowserMenu(
    onDismiss: () -> Unit,
    onBookmarks: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onNewTab: () -> Unit,
    onShare: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            MenuItemButton(
                text = "New Tab",
                onClick = onNewTab
            )
            MenuItemButton(
                text = "Bookmarks",
                onClick = onBookmarks
            )
            MenuItemButton(
                text = "History",
                onClick = onHistory
            )
            MenuItemButton(
                text = "Share",
                onClick = onShare
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            MenuItemButton(
                text = "Settings",
                onClick = onSettings
            )
        }
    }
}

@Composable
private fun MenuItemButton(
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
