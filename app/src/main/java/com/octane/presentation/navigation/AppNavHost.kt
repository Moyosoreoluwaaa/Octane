package com.octane.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.octane.presentation.screens.*
import org.koin.androidx.compose.koinViewModel

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
        // Main Tabs
        composable<AppRoute.Home> {
            HomeScreen(
                viewModel = koinViewModel(),
                navController = navController,
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
                navController = navController,
                onNavigateToDetails = { txHash ->
                    navController.navigate(AppRoute.TransactionDetails(txHash))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<AppRoute.Swap> {
            SwapScreen(
                viewModel = koinViewModel(),
                navController = navController,
                onBack = { navController.popBackStack() },
                onSuccess = { txHash ->
                    navController.navigate(AppRoute.TransactionDetails(txHash))
                }
            )
        }

        composable<AppRoute.Search> {
            DiscoverScreen(
                viewModel = koinViewModel(),
                navController = navController,
                onNavigateToTokenDetails = { assetId, symbol ->
                    navController.navigate(AppRoute.TokenDetails(assetId, symbol))
                },
                onNavigateToPerpDetails = { perpSymbol ->
                    navController.navigate(AppRoute.PerpDetailRoute(perpSymbol))
                },
                onNavigateToDAppDetails = { url ->
                    navController.navigate(AppRoute.DAppWebViewRoute(url, "DApp"))
                }
            )
        }

        // Detail Screens
        composable<AppRoute.TokenDetails> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.TokenDetails>()
            TokenDetailsScreen(
                tokenId = route.tokenId,
                symbol = route.symbol,
                onBack = { navController.navigateUp() },
                onNavigateToSend = { symbol ->
                    navController.navigate(AppRoute.Send(tokenSymbol = symbol))
                },
                onNavigateToReceive = { symbol ->
                    navController.navigate(AppRoute.Receive(tokenSymbol = symbol))
                },
                onNavigateToSwap = { symbol ->
                    navController.navigate(AppRoute.Swap)
                }
            )
        }

        composable<AppRoute.PerpDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.PerpDetailRoute>()
            PerpDetailScreen(
                perpSymbol = route.perpSymbol,
                onBack = { navController.navigateUp() },
                onNavigateToTrade = { perpSymbol -> /* Navigate to trading */ }
            )
        }

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

        // NEW: Wallet Management Routes
        composable<AppRoute.SeedPhraseDisplay> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.SeedPhraseDisplay>()
            SeedPhraseDisplayScreen(
                seedPhrase = route.seedPhrase,
                walletName = route.walletName,
                onConfirm = {
                    // Navigate back to home after confirming backup
                    navController.navigate(AppRoute.Home) {
                        popUpTo(AppRoute.Home) { inclusive = true }
                    }
                },
                onBack = { navController.navigateUp() }
            )
        }

        composable<AppRoute.ImportWallet> {
            ImportWalletScreen(
                viewModel = koinViewModel(),
                onSuccess = {
                    navController.navigate(AppRoute.Home) {
                        popUpTo(AppRoute.Home) { inclusive = true }
                    }
                },
                onBack = { navController.navigateUp() }
            )
        }
    }
}