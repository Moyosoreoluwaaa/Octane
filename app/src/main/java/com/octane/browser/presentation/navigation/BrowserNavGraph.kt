package com.octane.browser.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.octane.browser.presentation.screens.*
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * ✅ FINAL FIX: Prevent navigation loops
 *
 * Key Changes:
 * 1. Home button doesn't use navigate() - just saves state and pops
 * 2. Bookmarks/History properly restore BrowserRoute
 * 3. No circular navigation patterns
 */
@Composable
fun BrowserNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val browserViewModel: BrowserViewModel = koinViewModel()

    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier
    ) {
        // ═══════════════════════════════════════════════════════════════
        // HOME SCREEN
        // ═══════════════════════════════════════════════════════════════
        composable<HomeRoute> {
            BrowserHomeScreen(
                onOpenUrl = { url ->
                    browserViewModel.navigateToUrl(url)
                    navController.navigate(BrowserRoute) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onOpenSettings = {
                    navController.navigate(SettingsRoute)
                },
                onNewTabAndGoHome = {
                    browserViewModel.createNewTab()
                    // Stay on home
                },
                onOpenBookmarks = {
                    navController.navigate(BookmarksRoute)
                },
                onOpenHistory = {
                    navController.navigate(HistoryRoute)
                },
                browserViewModel = browserViewModel
            )
        }

        // ═══════════════════════════════════════════════════════════════
        // BROWSER SCREEN (Main Viewing)
        // ═══════════════════════════════════════════════════════════════
        composable<BrowserRoute> {
            BrowserScreen(
                onOpenTabManager = {
                    navController.navigate(TabManagerRoute)
                },
                onOpenBookmarks = {
                    navController.navigate(BookmarksRoute)
                },
                onOpenHistory = {
                    navController.navigate(HistoryRoute)
                },
                onOpenSettings = {
                    navController.navigate(SettingsRoute)
                },
                browserViewModel = browserViewModel,
                navController = navController
            )
        }

        // ═══════════════════════════════════════════════════════════════
        // TAB MANAGER
        // ═══════════════════════════════════════════════════════════════
        composable<TabManagerRoute> {
            TabManagerScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNewTab = {
                    browserViewModel.createNewTab()
                    navController.navigate(HomeRoute) {
                        popUpTo<TabManagerRoute> { inclusive = true }
                    }
                },
                onSelectTab = { tabId ->
                    browserViewModel.switchTab(tabId)
                    navController.navigate(BrowserRoute) {
                        popUpTo<TabManagerRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                browserViewModel = browserViewModel
            )
        }

        // ═══════════════════════════════════════════════════════════════
        // BOOKMARKS
        // ═══════════════════════════════════════════════════════════════
        composable<BookmarksRoute> {
            BookmarksScreen(
                onBack = {
                    navController.popBackStack()
                },
                onOpenUrl = { url ->
                    browserViewModel.navigateToUrl(url)
                    navController.navigate(BrowserRoute) {
                        popUpTo<BookmarksRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // ═══════════════════════════════════════════════════════════════
        // HISTORY
        // ═══════════════════════════════════════════════════════════════
        composable<HistoryRoute> {
            HistoryScreen(
                onBack = {
                    navController.popBackStack()
                },
                onOpenUrl = { url ->
                    browserViewModel.navigateToUrl(url)
                    navController.navigate(BrowserRoute) {
                        popUpTo<HistoryRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // ═══════════════════════════════════════════════════════════════
        // SETTINGS
        // ═══════════════════════════════════════════════════════════════
        composable<SettingsRoute> {
            SettingsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // ═══════════════════════════════════════════════════════════════
        // CONNECTIONS
        // ═══════════════════════════════════════════════════════════════
        composable<ConnectionsRoute> {
            ConnectionsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}