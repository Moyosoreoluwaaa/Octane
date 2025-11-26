// presentation/navigation/AppNavHost.kt

package com.octane.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.octane.presentation.navigation.AppRoute.DAppWebViewRoute
import com.octane.presentation.screens.DAppBrowserScreen
import com.octane.presentation.screens.DAppWebViewScreen
import com.octane.presentation.screens.DiscoverScreen
import com.octane.presentation.screens.HomeScreen
import com.octane.presentation.screens.ManageTokensScreen
import com.octane.presentation.screens.PerpDetailScreen
import com.octane.presentation.screens.ReceiveScreen
import com.octane.presentation.screens.SendScreen
import com.octane.presentation.screens.SettingsScreen
import com.octane.presentation.screens.SwapScreen
import com.octane.presentation.screens.TokenDetailsScreen
import com.octane.presentation.screens.WalletScreen
import org.koin.androidx.compose.koinViewModel

/**
 * Main navigation host for Octane Wallet.
 * Handles all screen routing with type-safe navigation.
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: AppRoute = AppRoute.Home
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        // ============ Main Tabs ============

        composable<AppRoute.Home> {
            HomeScreen(
                viewModel = koinViewModel(),
                navController = navController, // ✅ Pass navController
                onNavigateToDetails = { assetId, symbol ->
                    navController.navigate(AppRoute.TokenDetails(assetId, symbol))
                },
                onNavigateToSend = { symbol ->
                    navController.navigate(AppRoute.Send(tokenSymbol = symbol))
                },
                onNavigateToReceive = {
                    navController.navigate(AppRoute.Receive())
                },
                onNavigateToSwap = {
                    navController.navigate(AppRoute.Swap)
                },
                onNavigateToManage = {
                    navController.navigate(AppRoute.ManageTokens)
                },
                onNavigateToWallets = {
                    navController.navigate(AppRoute.Wallet)
                },
                onNavigateToActivity = {
                    navController.navigate(AppRoute.Wallet)
                }
            )
        }

        composable<AppRoute.Wallet> {
            WalletScreen(
                activityViewModel = koinViewModel(),
                walletsViewModel = koinViewModel(),
                navController = navController, // ✅ Pass navController
                onNavigateToDetails = { txHash ->
                    navController.navigate(AppRoute.TransactionDetails(txHash))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<AppRoute.Swap> {
            SwapScreen(
                viewModel = koinViewModel(),
                navController = navController, // ✅ Pass navController
                onBack = { navController.popBackStack() },
                onSuccess = { txHash ->
                    navController.navigate(AppRoute.TransactionDetails(txHash))
                }
            )
        }

        composable<AppRoute.Search> {
            DiscoverScreen(
                viewModel = koinViewModel(),
                navController = navController, // ✅ Pass navController
                onNavigateToTokenDetails = { assetId, symbol ->
                    navController.navigate(AppRoute.TokenDetails(assetId, symbol))
                },
                // 2. Add New Perp Navigation
                onNavigateToPerpDetails = { perpSymbol ->
                    navController.navigate(AppRoute.PerpDetailRoute(perpSymbol))
                },
                // 3. Add New DApp Navigation
                onNavigateToDAppDetails = { url ->
                    navController.navigate(AppRoute.DAppWebViewRoute(url, "DApp"))
                }
            )
        }

        // ============ Detail Screens (No Bottom Nav) ============

        // Token Detail Screen
        composable<AppRoute.TokenDetails> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.TokenDetails>()
            TokenDetailsScreen(
                tokenId = route.tokenId,
                symbol = route.symbol,
                onBack = { navController.navigateUp() },
                onNavigateToSend = { symbol ->
                    // Navigate to send screen
                },
                onNavigateToReceive = { symbol ->
                    // Navigate to receive screen
                },
                onNavigateToSwap = { symbol ->
                    // Navigate to swap screen
                }
            )
        }

        // Perp Detail Screen
        composable<AppRoute.PerpDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.PerpDetailRoute>()
            PerpDetailScreen(
                perpSymbol = route.perpSymbol,
                onBack = { navController.navigateUp() },
                onNavigateToTrade = { perpSymbol ->
                    // Navigate to perp trading screen
                }
            )
        }

        // ============ Other Screens ============

        composable<AppRoute.DAppWebViewRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.DAppWebViewRoute>()
            DAppBrowserScreen(
                initialUrl = route.url,
                initialTitle = route.title,
                onBack = { navController.navigateUp() }
            )
        }

        composable<AppRoute.Send> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.Send>()
            SendScreen(
                viewModel = koinViewModel(),
                prefilledSymbol = route.tokenSymbol,
                prefilledAddress = route.prefilledAddress,
                onBack = { navController.popBackStack() },
                onSuccess = { txHash ->
                    navController.navigate(AppRoute.TransactionDetails(txHash))
                }
            )
        }

        composable<AppRoute.Receive> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.Receive>()
            ReceiveScreen(
                viewModel = koinViewModel(),
                selectedSymbol = route.tokenSymbol,
                onBack = { navController.popBackStack() }
            )
        }

        composable<AppRoute.ManageTokens> {
            ManageTokensScreen(
                viewModel = koinViewModel(),
                onBack = { navController.popBackStack() }
            )
        }

        composable<AppRoute.Settings> {
            SettingsScreen(
                viewModel = koinViewModel(),
                onBack = { navController.popBackStack() }
            )
        }

//        composable<AppRoute.Activity> {
//            ActivityScreen(
//                viewModel = koinViewModel(),
//                onNavigateToDetails = { txHash ->
//                    navController.navigate(AppRoute.TransactionDetails(txHash))
//                },
//                onBack = { navController.popBackStack() }
//            )
//        }
//
//        composable<AppRoute.TransactionDetails> { backStackEntry ->
//            val route = backStackEntry.toRoute<AppRoute.TransactionDetails>()
//            TransactionDetailsScreen(
//                viewModel = koinViewModel(),
//                txHash = route.txHash,
//                onBack = { navController.popBackStack() }
//            )
//        }
    }
}