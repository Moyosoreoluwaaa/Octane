package com.octane.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.octane.core.util.LoadingState
import com.octane.presentation.components.*
import com.octane.presentation.theme.AppColors
import com.octane.presentation.theme.AppTypography
import com.octane.presentation.theme.Dimensions
import com.octane.presentation.utils.UiFormatters
import com.octane.presentation.viewmodel.DiscoverViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Discover/Search screen.
 * Connected to DiscoverViewModel.
 */
@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel = koinViewModel(),
    navController: NavController, // ✅ Pass navController
    onNavigateToDetails: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedMode by viewModel.selectedMode.collectAsState()
    val trendingTokens by viewModel.trendingTokens.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) } // ✅ Fixed
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = Dimensions.Padding.standard,
                    vertical = Dimensions.Spacing.large
                ),
                verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
            ) {
                // Search Input
                item {
                    SearchInput(
                        query = searchQuery,
                        onQueryChange = viewModel::onSearchQueryChanged,
                        placeholder = "Sites, tokens, URL"
                    )
                }

                // Mode Tabs
                item {
                    ModeSelectorTabs(
                        modes = listOf("Tokens", "Perps", "Lists"),
                        selectedMode = selectedMode,
                        onModeSelected = viewModel::onModeSelected
                    )
                }

                // Content based on search or mode
                when (selectedMode) {
                    "Tokens" -> {
                        item {
                            Text(
                                "Trending Tokens >",
                                style = AppTypography.titleLarge,
                                color = AppColors.TextPrimary
                            )
                        }

                        when (val state = trendingTokens) {
                            is LoadingState.Success -> {
                                items(state.data.take(10)) { asset ->
                                    RankedTokenRow(
                                        rank = state.data.indexOf(asset) + 1,
                                        symbol = asset.symbol,
                                        name = asset.name,
                                        marketCap = asset.valueUsd?.let {
                                            UiFormatters.formatCompactNumber(it)
                                        } ?: "N/A",
                                        price = asset.priceUsd?.let {
                                            UiFormatters.formatUsd(it)
                                        } ?: "$0.00",
                                        changePercent = asset.priceChange24h ?: 0.0,
                                        iconColor = getAssetColor(asset.symbol),
                                        onClick = { onNavigateToDetails(asset.id, asset.symbol) }
                                    )
                                }
                            }

                            else -> {
                                item { LoadingScreen() }
                            }
                        }
                    }

                    "Perps" -> {
                        // Perps content (implement later)
                        item {
                            Text(
                                "Perps coming soon",
                                style = AppTypography.bodyMedium,
                                color = AppColors.TextSecondary
                            )
                        }
                    }

                    "Lists" -> {
                        // Lists content (implement later)
                        item {
                            Text(
                                "Trending Sites >",
                                style = AppTypography.titleLarge,
                                color = AppColors.TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getAssetColor(symbol: String): androidx.compose.ui.graphics.Color {
    return when (symbol.uppercase()) {
        "SOL" -> AppColors.Solana
        "BTC" -> AppColors.Bitcoin
        "ETH" -> AppColors.Ethereum
        "USDC" -> AppColors.USDC
        "USDT" -> AppColors.USDT
        else -> AppColors.TextSecondary
    }
}