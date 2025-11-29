package com.octane.browser.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.octane.browser.presentation.screens.*
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import com.octane.wallet.presentation.screens.HomeScreen
import org.koin.androidx.compose.koinViewModel

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
        composable<HomeRoute> {
            BrowserHomeScreen(
                onOpenUrl = { url ->
                    browserViewModel.navigateToUrl(url)
                    navController.navigate(BrowserRoute) {
                        popUpTo<HomeRoute> { inclusive = true }
                    }
                },
                onOpenSettings = { navController.navigate(SettingsRoute) },
                onNewTabAndGoHome = { navController.navigate(HomeRoute)},
                onOpenBookmarks = { navController.navigate(BookmarksRoute) },
                onOpenHistory = { navController.navigate(HistoryRoute) },
                browserViewModel = browserViewModel

            )
        }

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
                navController
            )
        }

        composable<TabManagerRoute> {
            TabManagerScreen(
                onBack = {
                    navController.popBackStack()
                },
                // ✅ NEW: After creating a new tab, navigate to the Home Screen entry point
                onNewTab = {
                    navController.navigate(HomeRoute) {
                        popUpTo<TabManagerRoute> { inclusive = true } // Pop TabManager
                    }
                },
                browserViewModel = browserViewModel
            )
        }

        composable<BookmarksRoute> {
            BookmarksScreen(
                onBack = {
                    navController.popBackStack()
                },
                onOpenUrl = { url ->
                    navController.popBackStack()
                    browserViewModel.navigateToUrl(url)
                }
            )
        }

        composable<HistoryRoute> {
            HistoryScreen(
                onBack = {
                    navController.popBackStack()
                },
                onOpenUrl = { url ->
                    navController.popBackStack()
                    browserViewModel.navigateToUrl(url)
                }
            )
        }

        composable<SettingsRoute> {
            SettingsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<ConnectionsRoute> {
            ConnectionsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}