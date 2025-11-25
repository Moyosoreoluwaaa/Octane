package com.octane.presentation.screens

import timber.log.Timber
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.octane.core.util.LoadingState
import com.octane.domain.models.DApp
import com.octane.domain.models.Perp
import com.octane.domain.models.Token
import com.octane.presentation.components.*
import com.octane.presentation.theme.AppColors
import com.octane.presentation.theme.AppTypography
import com.octane.presentation.theme.Dimensions
import com.octane.presentation.viewmodel.DiscoverMode
import com.octane.presentation.viewmodel.DiscoverViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
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

    // ✅ Pager state for swipeable tabs
    val pagerState = rememberPagerState(
        initialPage = when (selectedMode) {
            DiscoverMode.TOKENS -> 0
            DiscoverMode.PERPS -> 1
            DiscoverMode.LISTS -> 2
        },
        pageCount = { 3 }
    )

    // ✅ Separate scroll states for each tab
    val tokensScrollState = rememberLazyListState()
    val perpsScrollState = rememberLazyListState()
    val listsScrollState = rememberLazyListState()

    val scope = rememberCoroutineScope()

    // ✅ FIXED: Get current scroll state based on PAGER page, not selectedMode
    val currentScrollState = remember(pagerState.currentPage) {
        when (pagerState.currentPage) {
            0 -> tokensScrollState
            1 -> perpsScrollState
            2 -> listsScrollState
            else -> tokensScrollState
        }
    }

    // ✅ Calculate search bar visibility based on CURRENT tab's scroll
    val searchBarAlpha by remember {
        derivedStateOf {
            val firstVisibleIndex = currentScrollState.firstVisibleItemIndex
            val firstVisibleOffset = currentScrollState.firstVisibleItemScrollOffset

            when {
                firstVisibleIndex == 0 && firstVisibleOffset < 100 -> 1f // Fully visible
                firstVisibleIndex == 0 -> (1f - (firstVisibleOffset / 300f)).coerceIn(0f, 1f) // Fading
                else -> 0f // Hidden
            }
        }
    }

    // ✅ Animate alpha changes smoothly
    val animatedAlpha by animateFloatAsState(
        targetValue = searchBarAlpha,
        animationSpec = tween(durationMillis = 300), // Increased from 200ms for smoother feel
        label = "searchBarAlpha"
    )

    // Sync pager state with selected mode
    LaunchedEffect(pagerState.currentPage) {
        val newMode = when (pagerState.currentPage) {
            0 -> DiscoverMode.TOKENS
            1 -> DiscoverMode.PERPS
            2 -> DiscoverMode.LISTS
            else -> DiscoverMode.TOKENS
        }
        if (newMode != selectedMode) {
            Timber.d("🎨 Pager changed to page ${pagerState.currentPage}, updating mode to $newMode")
            viewModel.onModeSelected(newMode)
        }
    }

    // ✅ Check if ALL tabs are loading for full-screen shimmer
    val isFullScreenLoading = remember(trendingTokens, perps, dapps) {
        trendingTokens is LoadingState.Loading &&
                perps is LoadingState.Loading &&
                dapps is LoadingState.Loading
    }

    // ==================== UI ====================

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ✅ Show full-screen shimmer if ALL data is loading
            if (isFullScreenLoading) {
                ShimmerLoadingScreen()
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // ==================== COLLAPSING HEADER ====================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimensions.Padding.standard)
                            .padding(top = Dimensions.Spacing.large)
                    ) {
                        // ✅ Search Input - Fades out on scroll
                        if (animatedAlpha > 0f) {
                            SearchInput(
                                query = searchQuery,
                                onQueryChange = { newQuery ->
                                    Timber.d("🎨 Search input changed: '$newQuery'")
                                    viewModel.onSearchQueryChanged(newQuery)
                                },
                                placeholder = "Sites, tokens, URL",
                                modifier = Modifier
                                    .alpha(animatedAlpha)
                                    .graphicsLayer {
                                        translationY = (1f - animatedAlpha) * -20f
                                    }
                            )

                            if (animatedAlpha > 0.5f) {
                                Spacer(modifier = Modifier.height(Dimensions.Spacing.standard))
                            }
                        }

                        // ✅ Mode Tabs - Always visible
                        ModeSelectorTabs(
                            modes = listOf("Tokens", "Perps", "Lists"),
                            selectedMode = when (selectedMode) {
                                DiscoverMode.TOKENS -> "Tokens"
                                DiscoverMode.PERPS -> "Perps"
                                DiscoverMode.LISTS -> "Lists"
                            },
                            onModeSelected = { mode ->
                                Timber.d("🎨 Tab clicked: $mode")
                                val newMode = when (mode) {
                                    "Tokens" -> DiscoverMode.TOKENS
                                    "Perps" -> DiscoverMode.PERPS
                                    "Lists" -> DiscoverMode.LISTS
                                    else -> DiscoverMode.TOKENS
                                }
                                viewModel.onModeSelected(newMode)

                                scope.launch {
                                    val targetPage = when (newMode) {
                                        DiscoverMode.TOKENS -> 0
                                        DiscoverMode.PERPS -> 1
                                        DiscoverMode.LISTS -> 2
                                    }
                                    pagerState.animateScrollToPage(targetPage)
                                }
                            },
                            modifier = Modifier.padding(bottom = Dimensions.Spacing.standard)
                        )
                    }

                    // ==================== SWIPEABLE CONTENT ====================
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) { page ->
                        when (page) {
                            0 -> {
                                LazyColumn(
                                    state = tokensScrollState,
                                    contentPadding = PaddingValues(
                                        horizontal = Dimensions.Padding.standard,
                                        vertical = Dimensions.Spacing.standard
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
                                ) {
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
                            }
                            1 -> {
                                LazyColumn(
                                    state = perpsScrollState,
                                    contentPadding = PaddingValues(
                                        horizontal = Dimensions.Padding.standard,
                                        vertical = Dimensions.Spacing.standard
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
                                ) {
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
                            }
                            2 -> {
                                LazyColumn(
                                    state = listsScrollState,
                                    contentPadding = PaddingValues(
                                        horizontal = Dimensions.Padding.standard,
                                        vertical = Dimensions.Spacing.standard
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
                                ) {
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

    item {
        Text(
            if (searchQuery.isNotBlank()) "Search Results" else "Trending Tokens >",
            style = AppTypography.titleLarge,
            color = AppColors.TextPrimary
        )
    }

    when (displayState) {
        is LoadingState.Loading -> {
            // Individual tab loading (not full screen)
            items(5) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .shimmerEffect()
                )
            }
        }

        is LoadingState.Success -> {
            val tokens = displayState.data

            if (tokens.isEmpty()) {
                item {
                    EmptyState(
                        message = if (searchQuery.isNotBlank())
                            "No tokens found"
                        else
                            "No trending tokens available"
                    )
                }
            } else {
                items(tokens.take(20)) { token ->
                    RankedTokenRow(
                        rank = tokens.indexOf(token) + 1,
                        symbol = token.symbol,
                        name = token.name,
                        marketCap = token.formattedMarketCap,
                        price = token.formattedPrice,
                        changePercent = token.priceChange24h,
                        logoUrl = token.logoUrl,
                        fallbackIconColor = getTokenColor(token.symbol),
                        onClick = { onTokenClick(token) }
                    )
                }
            }
        }

        is LoadingState.Error -> {
            item {
                ErrorScreen(
                    message = displayState.message,
                    onRetry = { }
                )
            }
        }

        else -> {}
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

    item {
        Text(
            if (searchQuery.isNotBlank()) "Search Results" else "Perpetual Futures >",
            style = AppTypography.titleLarge,
            color = AppColors.TextPrimary
        )
    }

    when (displayState) {
        is LoadingState.Loading -> {
            items(5) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .shimmerEffect()
                )
            }
        }

        is LoadingState.Success -> {
            val perpList = displayState.data

            if (perpList.isEmpty()) {
                item {
                    EmptyState(message = "Perpetual futures coming soon")
                }
            } else {
                items(perpList.take(20)) { perp ->
                    PerpRow(
                        symbol = perp.symbol,
                        name = perp.name,
                        price = "$${perp.indexPrice}",
                        changePercent = perp.priceChange24h,
                        volume24h = perp.formattedOpenInterest,
                        leverageMax = perp.leverage.replace("x", "").toIntOrNull() ?: 20,
                        logoUrl = perp.logoUrl,
                        fallbackIconColor = getTokenColor(perp.symbol.split("-").first()),
                        onClick = { onPerpClick(perp) }
                    )
                }

                if (perpList.size > 20) {
                    item {
                        Text(
                            "Showing 20 of ${perpList.size} perps",
                            style = AppTypography.bodyMedium,
                            color = AppColors.TextSecondary,
                            modifier = Modifier.padding(vertical = Dimensions.Spacing.medium)
                        )
                    }
                }
            }
        }

        is LoadingState.Error -> {
            item {
                ErrorScreen(message = displayState.message, onRetry = { })
            }
        }

        else -> {}
    }
}

// ==================== LISTS TAB ====================

private fun LazyListScope.renderListsTab(
    searchQuery: String,
    dapps: LoadingState<List<DApp>>,
    searchResults: LoadingState<List<DApp>>,
    onDAppClick: (DApp) -> Unit
) {
    val displayState = if (searchQuery.isNotBlank()) searchResults else dapps

    item {
        Text(
            if (searchQuery.isNotBlank()) "Search Results" else "Trending Sites >",
            style = AppTypography.titleLarge,
            color = AppColors.TextPrimary
        )
    }

    when (displayState) {
        is LoadingState.Loading -> {
            items(5) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .shimmerEffect()
                )
            }
        }

        is LoadingState.Success -> {
            val dappList = displayState.data

            if (dappList.isEmpty()) {
                item {
                    EmptyState(message = "No dApps found")
                }
            } else {
                items(dappList.take(20)) { dapp ->
                    SiteRow(
                        rank = dappList.indexOf(dapp) + 1,
                        name = dapp.name,
                        category = dapp.category.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        logoUrl = dapp.logoUrl,
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
            item {
                ErrorScreen(message = displayState.message, onRetry = { })
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

@Composable
private fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
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