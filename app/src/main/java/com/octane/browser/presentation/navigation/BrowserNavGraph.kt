package com.octane.browser.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.octane.browser.presentation.screens.*
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * ✅ ENHANCED: Proper tab isolation and new tab creation
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

        // HOME SCREEN
        composable<HomeRoute> {
            BrowserHomeScreen(
                onOpenUrl = { query ->
                    navController.navigate(
                        BrowserRoute(url = query, forceNewTab = false)
                    )
                },
                onOpenSettings = {
                    navController.navigate(SettingsRoute)
                },
                onNewTabAndGoHome = {
                    browserViewModel.createNewTab()
                },
                onOpenBookmarks = {
                    navController.navigate(BookmarksRoute)
                },
                onOpenHistory = {
                    navController.navigate(HistoryRoute)
                },
                browserViewModel = browserViewModel,
                onNavigateToTabs = {
                    navController.navigate(TabManagerRoute)
                }
            )
        }

        // BROWSER SCREEN
        composable<BrowserRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<BrowserRoute>()

            BrowserScreen(
                url = route.url,
                tabId = route.tabId,
                forceNewTab = route.forceNewTab,
                onOpenTabManager = { navController.navigate(TabManagerRoute) },
                onOpenBookmarks = { navController.navigate(BookmarksRoute) },
                onOpenHistory = { navController.navigate(HistoryRoute) },
                onOpenSettings = { navController.navigate(SettingsRoute) },
                browserViewModel = browserViewModel,
                navController = navController
            )
        }

        // TAB MANAGER
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
                    // ✅ Navigate to BrowserRoute with specific tabId
                    navController.navigate(
                        BrowserRoute(tabId = tabId, forceNewTab = false)
                    ) {
                        popUpTo<TabManagerRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                browserViewModel = browserViewModel
            )
        }

        // BOOKMARKS
        composable<BookmarksRoute> {
            BookmarksScreen(navController)
        }

        // HISTORY
        composable<HistoryRoute> {
            HistoryScreen(navController)
        }

        // SETTINGS
        composable<SettingsRoute> {
            SettingsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // CONNECTIONS
        composable<ConnectionsRoute> {
            ConnectionsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}