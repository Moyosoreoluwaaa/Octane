package com.octane.browser.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.octane.browser.design.BrowserColors
import com.octane.browser.design.BrowserDimens
import com.octane.browser.presentation.components.BrowserAddressBar
import com.octane.browser.presentation.components.BrowserMenu
import com.octane.browser.presentation.components.NavigationControls
import com.octane.browser.presentation.navigation.HomeRoute
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import com.octane.browser.webview.WebViewContainer
import kotlinx.coroutines.delay
import timber.log.Timber

/**
 * ✅ FINAL FIX: Prevent navigation loops
 *
 * Back Button Logic:
 * 1. Close menu if open
 * 2. WebView back if possible
 * 3. Pop to HomeRoute (NOT navigate, to avoid loops)
 */
@Composable
fun BrowserScreen(
    onOpenTabManager: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    browserViewModel: BrowserViewModel,
    navController: NavController
) {
    val webViewState by browserViewModel.webViewState.collectAsState()
    val isBookmarked by browserViewModel.isBookmarked.collectAsState()
    val tabs by browserViewModel.tabs.collectAsState()
    val showPhishingWarning by browserViewModel.showPhishingWarning.collectAsState()
    val barsVisible by browserViewModel.barsVisible.collectAsState()
    val isDesktopMode by browserViewModel.isDesktopMode.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(barsVisible) {
        if (barsVisible) {
            delay(3000)
            browserViewModel.hideBars()
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ FIXED: No navigation loops
    // ═══════════════════════════════════════════════════════════════
    // BrowserScreen.kt

    BackHandler(enabled = true) {
        when {
            showMenu -> {
                Timber.d("🔙 Back: Closing menu")
                showMenu = false
            }

            webViewState.canGoBack -> {
                Timber.d("🔙 Back: WebView go back")
                browserViewModel.goBack()
            }

            else -> {
                browserViewModel.navigateToHomeScreen()
                navController.popBackStack()
            }
        }
    }

    LaunchedEffect(Unit) {
        browserViewModel.navigationEvent.collect { event ->
            when (event) {
                is BrowserViewModel.NavigationEvent.ShowError ->
                    snackbarHostState.showSnackbar(event.message)
                is BrowserViewModel.NavigationEvent.ShowMessage ->
                    snackbarHostState.showSnackbar(event.message)
                else -> {}
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // WebView
        WebViewContainer(
            browserViewModel = browserViewModel,
            modifier = Modifier.fillMaxSize(),
            onScrollUp = { browserViewModel.showBars() },
            onScrollDown = { browserViewModel.hideBars() }
        )

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        )

        // Address Bar
        AnimatedVisibility(
            visible = barsVisible,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(300)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            BrowserAddressBar(
                webViewState = webViewState,
                isBookmarked = isBookmarked,
                isDesktopMode = isDesktopMode,
                onNavigate = { browserViewModel.navigateToUrl(it) },
                onReload = { browserViewModel.reload() },
                onStop = { browserViewModel.stopLoading() },
                onBookmarkToggle = { browserViewModel.toggleBookmark() },
                onMenuClick = { showMenu = true },
                onDesktopModeToggle = { browserViewModel.toggleDesktopMode() },
                onHomeClick = {
                    // ✅ FIX: Pop instead of navigate
                    navController.popBackStack()
                },
                modifier = Modifier.padding(
                    start = BrowserDimens.BrowserPaddingScreenEdge,
                    end = BrowserDimens.BrowserPaddingScreenEdge,
                    top = 8.dp
                )
            )
        }

        // Navigation Controls
        AnimatedVisibility(
            visible = barsVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(300)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            NavigationControls(
                webViewState = webViewState,
                tabCount = tabs.size,
                onBack = { browserViewModel.goBack() },
                onForward = { browserViewModel.goForward() },
                onHome = {
                    // ✅ FIX: Pop instead of navigate
                    navController.popBackStack()
                },
                onTabManager = onOpenTabManager,
                onMenuClick = { showMenu = true },
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(
                        bottom = BrowserDimens.BrowserPaddingBarBottom,
                        start = BrowserDimens.BrowserPaddingScreenEdge,
                        end = BrowserDimens.BrowserPaddingScreenEdge
                    )
            )
        }
    }

    if (showMenu) {
        BrowserMenu(
            onDismiss = { showMenu = false },
            onBookmarks = { showMenu = false; onOpenBookmarks() },
            onHistory = { showMenu = false; onOpenHistory() },
            onSettings = { showMenu = false; onOpenSettings() },
            onNewTab = { showMenu = false; browserViewModel.createNewTab() },
            onShare = { showMenu = false },
            onRefresh = { showMenu = false; browserViewModel.reload() }
        )
    }

    showPhishingWarning?.let { warning ->
        AlertDialog(
            onDismissRequest = { browserViewModel.dismissPhishingWarning() },
            icon = { Icon(Icons.Rounded.Warning, null, tint = BrowserColors.BrowserColorWarning) },
            title = { Text("Security Warning") },
            text = { Text(warning) },
            confirmButton = {
                Button(onClick = { browserViewModel.dismissPhishingWarning() }) {
                    Text("Go Back")
                }
            },
            dismissButton = {
                TextButton(onClick = { browserViewModel.proceedDespitePhishingWarning() }) {
                    Text("Proceed Anyway")
                }
            }
        )
    }
}