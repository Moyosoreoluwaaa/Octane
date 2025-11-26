package com.octane.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.octane.presentation.screens.ActivityScreen
import com.octane.presentation.screens.DAppBrowserScreen
import com.octane.presentation.screens.DiscoverScreen
import com.octane.presentation.screens.HomeScreen
import com.octane.presentation.screens.ImportWalletScreen
import com.octane.presentation.screens.ManageTokensScreen
import com.octane.presentation.screens.PerpDetailScreen
import com.octane.presentation.screens.ReceiveScreen
import com.octane.presentation.screens.SeedPhraseDisplayScreen
import com.octane.presentation.screens.SendScreen
import com.octane.presentation.screens.SettingsScreen
import com.octane.presentation.screens.SwapScreen
import com.octane.presentation.screens.TokenDetailsScreen
import com.octane.presentation.screens.TransactionDetailsScreen
import com.octane.presentation.screens.WalletsScreen
import org.koin.androidx.compose.koinViewModel

/**
 * ✅ Fully wired navigation with separated Wallet and Activity screens.
 * Each screen has clear, type-safe navigation paths.
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
        // ========== MAIN TABS ==========

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
                    navController.navigate(AppRoute.Wallets)
                },
                onNavigateToActivity = {
                    navController.navigate(AppRoute.Activity)
                },
                onNavigateToSettings = {
                    navController.navigate(AppRoute.Settings)
                }
            )
        }

        // ✅ Separated: Wallet Management Screen
        composable<AppRoute.Wallets> {
            WalletsScreen(
                viewModel = koinViewModel(),
                navController = navController,
                onBack = { navController.popBackStack() },
                onNavigateToSeedPhrase = { walletId, walletName, seedPhrase ->
                    navController.navigate(
                        AppRoute.SeedPhraseDisplay(walletId, walletName, seedPhrase)
                    )
                },
                onNavigateToImport = {
                    navController.navigate(AppRoute.ImportWallet)
                }
            )
        }

        // ✅ Separated: Activity/Transaction History Screen
        composable<AppRoute.Activity> {
            ActivityScreen(
                viewModel = koinViewModel(),
                navController = navController,
                onBack = { navController.popBackStack() },
                onNavigateToDetails = { txHash ->
                    navController.navigate(AppRoute.TransactionDetails(txHash))
                }
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
                },
                onTokenArrow = {
                    // TODO:
                },
                onPerpArrow = {
                    // TODO:
                },
                onDAppArrow = {
                    // TODO:
                }
            )
        }

        // ========== DETAIL SCREENS ==========

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
                onNavigateToTrade = { perpSymbol ->
                    // Navigate to trading screen when implemented
                }
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

        composable<AppRoute.TransactionDetails> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.TransactionDetails>()
            TransactionDetailsScreen(
                txHash = route.txHash,
                onBack = { navController.navigateUp() }
            )
        }

        // ========== TRANSACTION SCREENS ==========

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

        // ========== WALLET MANAGEMENT ==========

        composable<AppRoute.SeedPhraseDisplay> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.SeedPhraseDisplay>()
            SeedPhraseDisplayScreen(
                seedPhrase = route.seedPhrase,
                walletName = route.walletName,
                onConfirm = {
                    // Navigate back to wallets after confirming backup
                    navController.navigate(AppRoute.Wallets) {
                        popUpTo(AppRoute.Wallets) { inclusive = true }
                    }
                },
                onBack = { navController.navigateUp() }
            )
        }

        composable<AppRoute.ImportWallet> {
            ImportWalletScreen(
                viewModel = koinViewModel(),
                onSuccess = {
                    navController.navigate(AppRoute.Wallets) {
                        popUpTo(AppRoute.Wallets) { inclusive = true }
                    }
                },
                onBack = { navController.navigateUp() }
            )
        }

        // ========== SETTINGS ==========

        composable<AppRoute.ManageTokens> {
            ManageTokensScreen(
                viewModel = koinViewModel(),
                onBack = { navController.popBackStack() }
            )
        }

        composable<AppRoute.Settings> {
            SettingsScreen(
                viewModel = koinViewModel(),
                onBack = { navController.popBackStack() },
//                onNavigateToSecurity = {
//                    navController.navigate(AppRoute.Security)
//                }
            )
        }

//        composable<AppRoute.Security> {
//            SecurityScreen(
//                viewModel = koinViewModel(),
//                onBack = { navController.popBackStack() }
//            )
//        }
    }
}