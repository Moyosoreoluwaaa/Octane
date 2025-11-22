package com.octane.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.navigation.NavController
import com.octane.core.util.LoadingState
import com.octane.domain.models.Asset
import com.octane.domain.models.Transaction
import com.octane.domain.models.TransactionStatus
import com.octane.domain.models.TransactionType
import com.octane.domain.models.Wallet
import com.octane.domain.usecases.asset.PortfolioState
import com.octane.presentation.components.*
import com.octane.presentation.theme.AppColors
import com.octane.presentation.theme.AppTypography
import com.octane.presentation.theme.Dimensions
import com.octane.presentation.utils.UiFormatters
import com.octane.presentation.viewmodel.HomeEvent
import com.octane.presentation.viewmodel.HomeViewModel
import com.octane.presentation.viewmodel.NetworkStatus
import org.koin.androidx.compose.koinViewModel
import kotlin.collections.isNotEmpty
import kotlin.collections.take

/**
 * Home screen with portfolio overview.
 * Connected to HomeViewModel for real data.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    navController: NavController, // ✅ Pass navController
    onNavigateToDetails: (String, String) -> Unit,
    onNavigateToSend: (String) -> Unit,
    onNavigateToReceive: () -> Unit,
    onNavigateToSwap: () -> Unit,
    onNavigateToManage: () -> Unit,
    onNavigateToWallets: () -> Unit,
    onNavigateToActivity: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Collect state
    val portfolioState by viewModel.portfolioState.collectAsState()
    val activeWallet by viewModel.activeWallet.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()

    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                HomeEvent.NavigateToWallets -> onNavigateToWallets()
                is HomeEvent.NavigateToSend -> onNavigateToSend(event.tokenSymbol)
                HomeEvent.NavigateToReceive -> onNavigateToReceive()
                HomeEvent.NavigateToSwap -> onNavigateToSwap()
                HomeEvent.NavigateToManage -> onNavigateToManage()
                is HomeEvent.NavigateToDetails -> onNavigateToDetails(event.assetId, event.symbol)
                is HomeEvent.NavigateToTransactionDetails -> {
                    // Navigate to transaction details (implement later)
                }
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) } // ✅ Fixed
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Box(modifier = modifier.fillMaxSize()) {
                when (val state = portfolioState) {
                    is LoadingState.Loading -> {
                        // Loading state
                        LoadingScreen()
                    }

                    is LoadingState.Success -> {
                        // Success state with data
                        HomeContent(
                            portfolioData = state.data,
                            activeWallet = activeWallet,
                            recentTransactions = recentTransactions,
                            pendingCount = pendingCount,
                            isRefreshing = isRefreshing,
                            networkStatus = networkStatus,
                            onRefresh = viewModel::onRefresh,
                            onWalletClick = viewModel::onWalletClick,
                            onAssetClick = viewModel::onAssetClick,
                            onQuickSend = viewModel::onQuickSend,
                            onQuickReceive = viewModel::onQuickReceive,
                            onQuickSwap = viewModel::onQuickSwap,
                            onManageTokens = viewModel::onManageTokens,
                            onActivityClick = onNavigateToActivity,
                            formatCurrency = viewModel::formatCurrency
                        )
                    }

                    is LoadingState.Error -> {
                        // Error state
                        ErrorScreen(
                            message = state.message ?: "Failed to load portfolio",
                            onRetry = viewModel::onRefresh
                        )
                    }

                    LoadingState.Idle -> TODO()
                    LoadingState.Simulating -> TODO()
                    is LoadingState.Stale<*> -> TODO()
                }

                // Offline banner
                if (!networkStatus.isConnected) {
                    OfflineBanner(
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    portfolioData: PortfolioState,
    activeWallet: Wallet?,
    recentTransactions: LoadingState<List<Transaction>>,
    pendingCount: Int,
    isRefreshing: Boolean,
    networkStatus: NetworkStatus,
    onRefresh: () -> Unit,
    onWalletClick: () -> Unit,
    onAssetClick: (Asset) -> Unit,
    onQuickSend: (String) -> Unit,
    onQuickReceive: () -> Unit,
    onQuickSwap: () -> Unit,
    onManageTokens: () -> Unit,
    onActivityClick: () -> Unit,
    formatCurrency: (Double) -> String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = Dimensions.Padding.standard,
            vertical = Dimensions.Spacing.large
        ),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
    ) {
        // Header
        item {
            HomeHeader(
                totalBalance = formatCurrency(portfolioData.totalValueUsd),
                changeAmount = formatCurrency(
                    portfolioData.totalValueUsd * (portfolioData.change24hPercent / 100.0)
                ),
                changePercent = portfolioData.change24hPercent,
                walletName = activeWallet?.name ?: "Account 1",
                onWalletClick = onWalletClick,
                onHistoryClick = onActivityClick,
                onSearchClick = { /* Navigate to search */ }
            )
        }

        // Quick Actions
        item {
            QuickActionGrid(
                onSend = { onQuickSend("SOL") }, // Default to SOL
                onReceive = onQuickReceive,
                onSwap = onQuickSwap,
                onBuy = { /* Navigate to on-ramp */ }
            )
        }

        // Assets Section
        if (portfolioData.assets.isEmpty()) {
            item {
                EmptyWalletHero(
                    onBuyCash = { /* Navigate to on-ramp */ },
                    onDepositCrypto = onQuickReceive
                )
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Your Assets",
                        style = AppTypography.titleLarge,
                        color = AppColors.TextPrimary
                    )
                    Text(
                        "Manage",
                        style = AppTypography.labelLarge,
                        color = AppColors.TextSecondary,
                        modifier = Modifier.clickable { onManageTokens() }
                    )
                }
            }

            items(portfolioData.assets) { asset ->
                TokenRow(
                    symbol = asset.symbol,
                    name = asset.name,
                    balance = UiFormatters.formatTokenAmount(
                        asset.balanceDouble,
                        asset.symbol,
                        maxDecimals = 4
                    ),
                    valueUsd = asset.valueUsd ?: 0.0,
                    changePercent = asset.priceChange24h ?: 0.0,
                    iconColor = getAssetColor(asset.symbol),
                    onClick = { onAssetClick(asset) }
                )
            }
        }

        // Recent Activity (if any)
        if (recentTransactions is LoadingState.Success && recentTransactions.data.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Activity",
                        style = AppTypography.titleLarge,
                        color = AppColors.TextPrimary
                    )
                    if (pendingCount > 0) {
                        PriceChangeBadge(changePercent = pendingCount.toDouble())
                    }
                }
            }

            items(recentTransactions.data.take(3)) { transaction ->
                TransactionPreviewRow(
                    transaction = transaction,
                    onClick = { /* Navigate to details */ }
                )
            }
        }
    }
}

@Composable
private fun TransactionPreviewRow(
    transaction: Transaction,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Dimensions.Padding.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(Dimensions.Avatar.medium)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(AppColors.SurfaceHighlight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    when (transaction.type) {
                        TransactionType.SEND -> "↗"
                        TransactionType.RECEIVE -> "↙"
                        TransactionType.SWAP -> "🔄"
                        else -> "•"
                    },
                    style = AppTypography.bodyMedium
                )
            }

            Column {
                Text(
                    when (transaction.type) {
                        TransactionType.SEND -> "Sent"
                        TransactionType.RECEIVE -> "Received"
                        TransactionType.SWAP -> "Swapped"
                        else -> "Transaction"
                    },
                    style = AppTypography.titleSmall,
                    color = AppColors.TextPrimary
                )
                Text(
                    UiFormatters.formatRelativeTime(transaction.timestamp),
                    style = AppTypography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${transaction.amount} ${transaction.tokenSymbol}",
                style = AppTypography.titleSmall,
                color = AppColors.TextPrimary
            )
            StatusBadge(status = transaction.status)
        }
    }
}

@Composable
private fun StatusBadge(status: TransactionStatus) {
    val (color, label) = when (status) {
        TransactionStatus.CONFIRMED -> AppColors.Success to "Confirmed"
        TransactionStatus.PENDING -> AppColors.Warning to "Pending"
        TransactionStatus.FAILED -> AppColors.Error to "Failed"
    }

    Box(
        modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(Dimensions.CornerRadius.large))
            .background(color.copy(alpha = 0.15f))
            .padding(
                horizontal = Dimensions.Padding.small,
                vertical = Dimensions.Padding.tiny
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = AppTypography.labelSmall,
            color = color
        )
    }
}

@Composable
internal fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = AppColors.TextPrimary)
    }
}

@Composable
internal fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimensions.Padding.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Error",
            style = AppTypography.headlineSmall,
            color = AppColors.Error
        )
        Spacer(modifier = Modifier.height(Dimensions.Spacing.small))
        Text(
            message,
            style = AppTypography.bodyMedium,
            color = AppColors.TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Dimensions.Spacing.large))
        WideActionButton(
            text = "Retry",
            isPrimary = true,
            onClick = onRetry
        )
    }
}

@Composable
private fun OfflineBanner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.Warning)
            .padding(Dimensions.Padding.small),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "You're offline - Showing cached data",
            style = AppTypography.labelMedium,
            color = androidx.compose.ui.graphics.Color.Black
        )
    }
}

// Helper function to get asset colors
private fun getAssetColor(symbol: String): androidx.compose.ui.graphics.Color {
    return when (symbol.uppercase()) {
        "SOL" -> AppColors.Solana
        "BTC" -> AppColors.Bitcoin
        "ETH" -> AppColors.Ethereum
        "USDC" -> AppColors.USDC
        "USDT" -> AppColors.USDT
        "MATIC" -> AppColors.Polygon
        else -> AppColors.TextSecondary
    }
}