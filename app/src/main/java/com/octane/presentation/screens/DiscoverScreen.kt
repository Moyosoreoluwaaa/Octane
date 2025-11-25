package com.octane.presentation.screens

import timber.log.Timber // ✅ Replaced android.util.Log with Timber
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
import com.octane.domain.models.DApp
import com.octane.domain.models.Perp
import com.octane.domain.models.Token
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

// Removed: private const val TAG = "DiscoverScreen" - Timber auto-generates tags

@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel = koinViewModel(),
    navController: NavController,
    modifier: Modifier = Modifier,
    onNavigateToTokenDetails: (String, String) -> Unit
) {
    Timber.d("🎨 DiscoverScreen composing...")

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
            is LoadingState.Loading -> Timber.d("🎨 UI State: Trending tokens - Loading")
            is LoadingState.Success -> {
                val tokens = (trendingTokens as LoadingState.Success).data
                Timber.i("🎨 UI State: Trending tokens - ${tokens.size} tokens ready to display")
            }
            is LoadingState.Error -> {
                val error = (trendingTokens as LoadingState.Error).message
                Timber.e("🎨 UI State: Trending tokens - Error: $error")
            }
            else -> Timber.d("🎨 UI State: Trending tokens - Unknown state")
        }
    }

    LaunchedEffect(selectedMode) {
        Timber.d("🎨 UI: Selected mode changed to $selectedMode")
    }

    LaunchedEffect(searchQuery) {
        Timber.d("🎨 UI: Search query changed to '$searchQuery'")
    }

    LaunchedEffect(isRefreshing) {
        Timber.d("🎨 UI: Refreshing state = $isRefreshing")
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
                Timber.d("🎨 Rendering search input with query: '$searchQuery'")
                SearchInput(
                    query = searchQuery,
                    onQueryChange = { newQuery ->
                        Timber.d("🎨 Search input changed: '$newQuery'")
                        viewModel.onSearchQueryChanged(newQuery)
                    },
                    placeholder = "Sites, tokens, URL"
                )
            }

            // ==================== MODE TABS ====================
            item {
                Timber.d("🎨 Rendering mode tabs, selected: $selectedMode")
                ModeSelectorTabs(
                    modes = listOf("Tokens", "Perps", "Lists"),
                    selectedMode = when (selectedMode) {
                        DiscoverMode.TOKENS -> "Tokens"
                        DiscoverMode.PERPS -> "Perps"
                        DiscoverMode.LISTS -> "Lists"
                    },
                    onModeSelected = { mode ->
                        Timber.d("🎨 Tab clicked: $mode")
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
                    Timber.d("🎨 Rendering TOKENS tab content")
                    renderTokensTab(
                        searchQuery = searchQuery,
                        trendingTokens = trendingTokens,
                        searchResults = tokenSearchResults,
                        onTokenClick = { token ->
                            Timber.d("🎨 Token row clicked: ${token.symbol}")
                            onNavigateToTokenDetails(token.id, token.symbol)
                        }
                    )
                }

                DiscoverMode.PERPS -> {
                    Timber.d("🎨 Rendering PERPS tab content")
                    renderPerpsTab(
                        searchQuery = searchQuery,
                        perps = perps,
                        searchResults = perpSearchResults,
                        onPerpClick = { perp ->
                            Timber.d("🎨 Perp row clicked: ${perp.symbol}")
                            viewModel.onPerpClicked(perp)
                        }
                    )
                }

                DiscoverMode.LISTS -> {
                    Timber.d("🎨 Rendering LISTS tab content")
                    renderListsTab(
                        searchQuery = searchQuery,
                        dapps = dapps,
                        searchResults = dappSearchResults,
                        onDAppClick = { dapp ->
                            Timber.d("🎨 DApp row clicked: ${dapp.name}")
                            viewModel.onDAppClicked(dapp)
                        }
                    )
                }
            }
        }
    }

    DisposableEffect(Unit) {
        Timber.d("🎨 DiscoverScreen entered")
        onDispose {
            Timber.d("🎨 DiscoverScreen disposed")
        }
    }
}

// ==================== TOKENS TAB ====================

private fun LazyListScope.renderTokensTab(
    searchQuery: String,
    trendingTokens: LoadingState<List<Token>>,
    searchResults: LoadingState<List<Token>>,
    onTokenClick: (Token) -> Unit
) {
    val displayState = if (searchQuery.isNotBlank()) searchResults else trendingTokens

    Timber.d("🎨 renderTokensTab: searchQuery='$searchQuery', displayState=${displayState.javaClass.simpleName}")

    item {
        Text(
            if (searchQuery.isNotBlank()) "Search Results" else "Trending Tokens >",
            style = AppTypography.titleLarge,
            color = AppColors.TextPrimary
        )
    }

    when (displayState) {
        is LoadingState.Loading -> {
            Timber.d("🎨 Displaying Loading state for tokens")
            item { LoadingScreen() }
        }

        is LoadingState.Success -> {
            val tokens = displayState.data
            Timber.i("🎨 Displaying ${tokens.size} tokens")

            if (tokens.isEmpty()) {
                Timber.w("🎨 Token list is empty, showing EmptyState")
                item {
                    EmptyState(
                        message = if (searchQuery.isNotBlank())
                            "No tokens found"
                        else
                            "No trending tokens available"
                    )
                }
            } else {
                Timber.d("🎨 Rendering ${tokens.take(20).size} token rows")
                items(tokens.take(20)) { token ->
                    RankedTokenRow(
                        rank = tokens.indexOf(token) + 1,
                        symbol = token.symbol,
                        name = token.name,
                        marketCap = token.formattedMarketCap,
                        price = token.formattedPrice,
                        changePercent = token.priceChange24h,
                        logoUrl = token.logoUrl, // ✅ Passing the logo URL
                        fallbackIconColor = getTokenColor(token.symbol), // ✅ Passing fallback color
                        onClick = {
                            Timber.d("🎨 Token clicked in row: ${token.symbol}")
                            onTokenClick(token)
                        }
                    )
                }
            }
        }

        is LoadingState.Error -> {
            Timber.e("🎨 Displaying Error state: ${displayState.message}")
            item {
                ErrorState(
                    message = displayState.message,
                    onRetry = {
                        Timber.d("🎨 Retry button clicked")
                        // Trigger refresh via ViewModel
                    }
                )
            }
        }

        else -> {
            Timber.w("🎨 Unknown LoadingState type: ${displayState.javaClass.simpleName}")
        }
    }
}

// ==================== PERPS TAB ====================

private fun LazyListScope.renderPerpsTab(
    searchQuery: String,
    perps: LoadingState<List<Perp>>,
    searchResults: LoadingState<List<Perp>>,
    onPerpClick: (Perp) -> Unit
) {
    val displayState = if (searchQuery.isNotBlank()) searchResults else perps

    Timber.d("🎨 renderPerpsTab: displayState=${displayState.javaClass.simpleName}")

    item {
        Text(
            if (searchQuery.isNotBlank()) "Search Results" else "Perpetual Futures >",
            style = AppTypography.titleLarge,
            color = AppColors.TextPrimary
        )
    }

    when (displayState) {
        is LoadingState.Loading -> {
            Timber.d("🎨 Displaying Loading state for perps")
            item { LoadingScreen() }
        }

        is LoadingState.Success -> {
            val perpList = displayState.data
            Timber.i("🎨 Displaying ${perpList.size} perps")

            if (perpList.isEmpty()) {
                Timber.d("🎨 Perp list is empty")
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
                        logoUrl = perp.logoUrl, // ✅ Passing the logo URL
                        fallbackIconColor = getTokenColor(perp.symbol.split("-").first()), // ✅ Passing fallback color
                        onClick = { onPerpClick(perp) }
                    )
                }
            }
        }

        is LoadingState.Error -> {
            Timber.e("🎨 Displaying Error state for perps: ${displayState.message}")
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
    dapps: LoadingState<List<DApp>>,
    searchResults: LoadingState<List<DApp>>,
    onDAppClick: (DApp) -> Unit
) {
    val displayState = if (searchQuery.isNotBlank()) searchResults else dapps

    Timber.d("🎨 renderListsTab: displayState=${displayState.javaClass.simpleName}")

    item {
        Text(
            if (searchQuery.isNotBlank()) "Search Results" else "Trending Sites >",
            style = AppTypography.titleLarge,
            color = AppColors.TextPrimary
        )
    }

    when (displayState) {
        is LoadingState.Loading -> {
            Timber.d("🎨 Displaying Loading state for dApps")
            item { LoadingScreen() }
        }

        is LoadingState.Success -> {
            val dappList = displayState.data
            Timber.i("🎨 Displaying ${dappList.size} dApps")

            if (dappList.isEmpty()) {
                Timber.d("🎨 DApp list is empty")
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
                        logoUrl = dapp.logoUrl, // ✅ Passing the logo URL
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
            Timber.e("🎨 Displaying Error state for dApps: ${displayState.message}")
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
    Timber.d("🎨 Rendering EmptyState: $message")
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
    Timber.d("🎨 Rendering ErrorState: $message")
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