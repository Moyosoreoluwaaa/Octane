package com.octane.browser.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.octane.browser.design.BrowserColors
import com.octane.browser.design.BrowserDimens
import com.octane.browser.presentation.components.AddressBar
import com.octane.browser.presentation.components.BrowserMenu
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
                else -> { /* Handled by WebViewContainer */ }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // ✅ CHANGED
    ) {
        // Full Screen WebView Content
        WebViewContainer(
            browserViewModel = browserViewModel,
            modifier = Modifier.fillMaxSize()
        )

        // Snackbar at bottom
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        )

        // Floating Top Bar
        AddressBar(
            webViewState = webViewState,
            isBookmarked = isBookmarked,
            onNavigate = { browserViewModel.navigateToUrl(it) },
            onReload = { browserViewModel.reload() },
            onStop = { browserViewModel.stopLoading() },
            onBookmarkToggle = { browserViewModel.toggleBookmark() },
            onMenuClick = { showMenu = true },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(
                    start = BrowserDimens.BrowserPaddingScreenEdge,
                    end = BrowserDimens.BrowserPaddingScreenEdge
                )
        )

        // Floating Bottom Navigation
        NavigationControls(
            webViewState = webViewState,
            tabCount = tabs.size,
            onBack = { browserViewModel.goBack() },
            onForward = { browserViewModel.goForward() },
            onHome = { browserViewModel.navigateToUrl("about:blank") },
            onTabManager = onOpenTabManager,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(
                    bottom = BrowserDimens.BrowserPaddingBarBottom,
                    start = BrowserDimens.BrowserPaddingScreenEdge,
                    end = BrowserDimens.BrowserPaddingScreenEdge
                )
        )
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
            },
            onRefresh = {
                showMenu = false
                browserViewModel.reload()
            }
        )
    }

    // Phishing Warning Dialog
    showPhishingWarning?.let { warning ->
        AlertDialog(
            onDismissRequest = { browserViewModel.dismissPhishingWarning() },
            icon = {
                Icon(
                    Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = BrowserColors.BrowserColorWarning // Keep warning color as-is
                )
            },
            title = { Text("Security Warning") },
            text = { Text(warning) },
            confirmButton = {
                Button(
                    onClick = { browserViewModel.dismissPhishingWarning() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary // ✅ CHANGED
                    )
                ) {
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