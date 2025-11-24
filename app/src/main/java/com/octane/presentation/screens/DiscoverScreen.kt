package com.octane.presentation.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.octane.core.util.LoadingState
import com.octane.presentation.components.BottomNavBar
import com.octane.presentation.components.LearnCard
import com.octane.presentation.components.ModeSelectorTabs
import com.octane.presentation.components.PerpRow
import com.octane.presentation.components.RankedTokenRow
import com.octane.presentation.components.SearchInput
import com.octane.presentation.components.SiteRow
import com.octane.presentation.theme.AppColors
import com.octane.presentation.theme.AppTypography
import com.octane.presentation.theme.Dimensions
import com.octane.presentation.viewmodel.DiscoverMode
import com.octane.presentation.viewmodel.DiscoverViewModel
import org.koin.androidx.compose.koinViewModel

private const val TAG = "DiscoverScreen"

@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel = koinViewModel(),
    navController: NavController,
    modifier: Modifier = Modifier,
    onNavigateToTokenDetails: (String, String) -> Unit
) {
    Log.d(TAG, "🎨 DiscoverScreen composing...")

    // ==================== State ====================

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedMode by viewModel.selectedMode.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val trendingTokens by viewModel.trendingTokens.collectAsState()
    val tokenSearchResults by viewModel.tokenSearchResults.collectAsState()

    val perps by viewModel.perps.collectAsState()
    val perpSearchResults by viewModel.perpSearchResults.collectAsState()

    val dapps by viewModel.dapps.collectAsState()
    val dappSearchResults by viewModel.dappSearchResults.collectAsState()

    // Log state changes
    LaunchedEffect(trendingTokens) {
        when (trendingTokens) {
            is LoadingState.Loading -> Log.d(TAG, "🎨 UI State: Trending tokens - Loading")
            is LoadingState.Success -> {
                val tokens = (trendingTokens as LoadingState.Success).data
                Log.i(TAG, "🎨 UI State: Trending tokens - ${tokens.size} tokens ready to display")
            }
            is LoadingState.Error -> {
                val error = (trendingTokens as LoadingState.Error).message
                Log.e(TAG, "🎨 UI State: Trending tokens - Error: $error")
            }
            else -> Log.d(TAG, "🎨 UI State: Trending tokens - Unknown state")
        }
    }

    LaunchedEffect(selectedMode) {
        Log.d(TAG, "🎨 UI: Selected mode changed to $selectedMode")
    }

    LaunchedEffect(searchQuery) {
        Log.d(TAG, "🎨 UI: Search query changed to '$searchQuery'")
    }

    LaunchedEffect(isRefreshing) {
        Log.d(TAG, "🎨 UI: Refreshing state = $isRefreshing")
    }

    // ==================== UI ====================

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                horizontal = Dimensions.Padding.standard,
                vertical = Dimensions.Spacing.large
            ),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
        ) {
            // ==================== SEARCH INPUT ====================
            item {
                Log.d(TAG, "🎨 Rendering search input with query: '$searchQuery'")
                SearchInput(
                    query = searchQuery,
                    onQueryChange = { newQuery ->
                        Log.d(TAG, "🎨 Search input changed: '$newQuery'")
                        viewModel.onSearchQueryChanged(newQuery)
                    },
                    placeholder = "Sites, tokens, URL"
                )
            }

            // ==================== MODE TABS ====================
            item {
                Log.d(TAG, "🎨 Rendering mode tabs, selected: $selectedMode")
                ModeSelectorTabs(
                    modes = listOf("Tokens", "Perps", "Lists"),
                    selectedMode = when (selectedMode) {
                        DiscoverMode.TOKENS -> "Tokens"
                        DiscoverMode.PERPS -> "Perps"
                        DiscoverMode.LISTS -> "Lists"
                    },
                    onModeSelected = { mode ->
                        Log.d(TAG, "🎨 Tab clicked: $mode")
                        viewModel.onModeSelected(
                            when (mode) {
                                "Tokens" -> DiscoverMode.TOKENS
                                "Perps" -> DiscoverMode.PERPS
                                "Lists" -> DiscoverMode.LISTS
                                else -> DiscoverMode.TOKENS
                            }
                        )
                    }
                )
            }

            // ==================== CONTENT BY MODE ====================
            when (selectedMode) {
                DiscoverMode.TOKENS -> {
                    Log.d(TAG, "🎨 Rendering TOKENS tab content")
                    renderTokensTab(
                        searchQuery = searchQuery,
                        trendingTokens = trendingTokens,
                        searchResults = tokenSearchResults,
                        onTokenClick = { token ->
                            Log.d(TAG, "🎨 Token row clicked: ${token.symbol}")
                            onNavigateToTokenDetails(token.id, token.symbol)
                        }
                    )
                }

                DiscoverMode.PERPS -> {
                    Log.d(TAG, "🎨 Rendering PERPS tab content")
                    renderPerpsTab(
                        searchQuery = searchQuery,
                        perps = perps,
                        searchResults = perpSearchResults,
                        onPerpClick = { perp ->
                            Log.d(TAG, "🎨 Perp row clicked: ${perp.symbol}")
                            viewModel.onPerpClicked(perp)
                        }
                    )
                }

                DiscoverMode.LISTS -> {
                    Log.d(TAG, "🎨 Rendering LISTS tab content")
                    renderListsTab(
                        searchQuery = searchQuery,
                        dapps = dapps,
                        searchResults = dappSearchResults,
                        onDAppClick = { dapp ->
                            Log.d(TAG, "🎨 DApp row clicked: ${dapp.name}")
                            viewModel.onDAppClicked(dapp)
                        }
                    )
                }
            }
        }
    }

    DisposableEffect(Unit) {
        Log.d(TAG, "🎨 DiscoverScreen entered")
        onDispose {
            Log.d(TAG, "🎨 DiscoverScreen disposed")
        }
    }
}

// ==================== TOKENS TAB ====================

private fun LazyListScope.renderTokensTab(
    searchQuery: String,
    trendingTokens: LoadingState<List<com.octane.domain.models.Token>>,
    searchResults: LoadingState<List<com.octane.domain.models.Token>>,
    onTokenClick: (com.octane.domain.models.Token) -> Unit
) {
    val displayState = if (searchQuery.isNotBlank()) searchResults else trendingTokens

    Log.d(TAG, "🎨 renderTokensTab: searchQuery='$searchQuery', displayState=${displayState.javaClass.simpleName}")

    item {
        Text(
            if (searchQuery.isNotBlank()) "Search Results" else "Trending Tokens >",
            style = AppTypography.titleLarge,
            color = AppColors.TextPrimary
        )
    }

    when (displayState) {
        is LoadingState.Loading -> {
            Log.d(TAG, "🎨 Displaying Loading state for tokens")
            item { LoadingScreen() }
        }

        is LoadingState.Success -> {
            val tokens = displayState.data
            Log.i(TAG, "🎨 Displaying ${tokens.size} tokens")

            if (tokens.isEmpty()) {
                Log.w(TAG, "🎨 Token list is empty, showing EmptyState")
                item {
                    EmptyState(
                        message = if (searchQuery.isNotBlank())
                            "No tokens found"
                        else
                            "No trending tokens available"
                    )
                }
            } else {
                Log.d(TAG, "🎨 Rendering ${tokens.take(20).size} token rows")
                items(tokens.take(20)) { token ->
                    RankedTokenRow(
                        rank = tokens.indexOf(token) + 1,
                        symbol = token.symbol,
                        name = token.name,
                        marketCap = token.formattedMarketCap,
                        price = token.formattedPrice,
                        changePercent = token.priceChange24h,
                        iconColor = getTokenColor(token.symbol),
                        onClick = {
                            Log.d(TAG, "🎨 Token clicked in row: ${token.symbol}")
                            onTokenClick(token)
                        }
                    )
                }
            }
        }

        is LoadingState.Error -> {
            Log.e(TAG, "🎨 Displaying Error state: ${displayState.message}")
            item {
                ErrorState(
                    message = displayState.message,
                    onRetry = {
                        Log.d(TAG, "🎨 Retry button clicked")
                        // Trigger refresh via ViewModel
                    }
                )
            }
        }

        else -> {
            Log.w(TAG, "🎨 Unknown LoadingState type: ${displayState.javaClass.simpleName}")
        }
    }
}

// ==================== PERPS TAB ====================

private fun LazyListScope.renderPerpsTab(
    searchQuery: String,
    perps: LoadingState<List<com.octane.domain.models.Perp>>,
    searchResults: LoadingState<List<com.octane.domain.models.Perp>>,
    onPerpClick: (com.octane.domain.models.Perp) -> Unit
) {
    val displayState = if (searchQuery.isNotBlank()) searchResults else perps

    Log.d(TAG, "🎨 renderPerpsTab: displayState=${displayState.javaClass.simpleName}")

    item {
        Text(
            if (searchQuery.isNotBlank()) "Search Results" else "Perpetual Futures >",
            style = AppTypography.titleLarge,
            color = AppColors.TextPrimary
        )
    }

    when (displayState) {
        is LoadingState.Loading -> {
            Log.d(TAG, "🎨 Displaying Loading state for perps")
            item { LoadingScreen() }
        }

        is LoadingState.Success -> {
            val perpList = displayState.data
            Log.i(TAG, "🎨 Displaying ${perpList.size} perps")

            if (perpList.isEmpty()) {
                Log.d(TAG, "🎨 Perp list is empty")
                item {
                    EmptyState(
                        message = "Perpetual futures coming soon"
                    )
                }
            } else {
                items(perpList) { perp ->
                    PerpRow(
                        symbol = perp.symbol,
                        name = perp.name,
                        price = "$${perp.indexPrice}",
                        changePercent = perp.priceChange24h,
                        volume24h = perp.formattedOpenInterest,
                        leverageMax = perp.leverage.replace("x", "").toIntOrNull() ?: 20,
                        iconColor = getTokenColor(perp.symbol.split("-").first()),
                        onClick = { onPerpClick(perp) }
                    )
                }
            }
        }

        is LoadingState.Error -> {
            Log.e(TAG, "🎨 Displaying Error state for perps: ${displayState.message}")
            item {
                ErrorState(
                    message = displayState.message,
                    onRetry = { }
                )
            }
        }

        else -> {}
    }
}

// ==================== LISTS TAB (dApps) ====================

private fun LazyListScope.renderListsTab(
    searchQuery: String,
    dapps: LoadingState<List<com.octane.domain.models.DApp>>,
    searchResults: LoadingState<List<com.octane.domain.models.DApp>>,
    onDAppClick: (com.octane.domain.models.DApp) -> Unit
) {
    val displayState = if (searchQuery.isNotBlank()) searchResults else dapps

    Log.d(TAG, "🎨 renderListsTab: displayState=${displayState.javaClass.simpleName}")

    item {
        Text(
            if (searchQuery.isNotBlank()) "Search Results" else "Trending Sites >",
            style = AppTypography.titleLarge,
            color = AppColors.TextPrimary
        )
    }

    when (displayState) {
        is LoadingState.Loading -> {
            Log.d(TAG, "🎨 Displaying Loading state for dApps")
            item { LoadingScreen() }
        }

        is LoadingState.Success -> {
            val dappList = displayState.data
            Log.i(TAG, "🎨 Displaying ${dappList.size} dApps")

            if (dappList.isEmpty()) {
                Log.d(TAG, "🎨 DApp list is empty")
                item {
                    EmptyState(
                        message = "No dApps found"
                    )
                }
            } else {
                items(dappList.take(20)) { dapp ->
                    SiteRow(
                        rank = dappList.indexOf(dapp) + 1,
                        name = dapp.name,
                        category = dapp.category.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        onClick = { onDAppClick(dapp) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(Dimensions.Spacing.large))
                    Text(
                        "Learn More >",
                        style = AppTypography.titleLarge,
                        color = AppColors.TextPrimary
                    )
                }

                item {
                    LearnCard(
                        title = "Getting Started with DeFi",
                        subtitle = "Learn the basics of decentralized finance",
                        iconColor = AppColors.Solana,
                        onClick = { }
                    )
                }
            }
        }

        is LoadingState.Error -> {
            Log.e(TAG, "🎨 Displaying Error state for dApps: ${displayState.message}")
            item {
                ErrorState(
                    message = displayState.message,
                    onRetry = { }
                )
            }
        }

        else -> {}
    }
}

// ==================== HELPERS ====================

private fun getTokenColor(symbol: String): androidx.compose.ui.graphics.Color {
    return when (symbol.uppercase()) {
        "SOL" -> AppColors.Solana
        "BTC" -> AppColors.Bitcoin
        "ETH" -> AppColors.Ethereum
        "USDC" -> AppColors.USDC
        "USDT" -> AppColors.USDT
        else -> AppColors.TextSecondary
    }
}

// ==================== EMPTY & ERROR STATES ====================

@Composable
private fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Log.d(TAG, "🎨 Rendering EmptyState: $message")
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimensions.Padding.large),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            message,
            style = AppTypography.bodyMedium,
            color = AppColors.TextSecondary
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d(TAG, "🎨 Rendering ErrorState: $message")
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimensions.Padding.large),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
    ) {
        Text(
            message,
            style = AppTypography.bodyMedium,
            color = AppColors.Error
        )

        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}