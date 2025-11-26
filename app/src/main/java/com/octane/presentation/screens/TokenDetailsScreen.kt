package com.octane.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.octane.core.util.LoadingState
import com.octane.presentation.components.*
import com.octane.presentation.theme.*
import com.octane.presentation.utils.UiFormatters
import com.octane.presentation.viewmodel.TokenDetailViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenDetailsScreen(
    tokenId: String,
    symbol: String,
    viewModel: TokenDetailViewModel = koinViewModel(),
    onBack: () -> Unit,
    onNavigateToSend: (String) -> Unit,
    onNavigateToReceive: (String) -> Unit,
    onNavigateToSwap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokenDetail by viewModel.tokenDetail.collectAsState()
    val chartData by viewModel.chartData.collectAsState()
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()

    LaunchedEffect(tokenId) {
        viewModel.loadToken(tokenId, symbol)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
                        ) {
                            // ✅ Token Logo
                            if (tokenDetail is LoadingState.Success) {
                                val token = (tokenDetail as LoadingState.Success).data
                                AsyncImage(
                                    model = token.logoUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Text(
                                symbol,
                                style = AppTypography.titleMedium
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Rounded.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = AppColors.Surface
                    )
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier.fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                horizontal = Dimensions.Padding.standard,
                vertical = Dimensions.Spacing.large
            ),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = AppColors.TextPrimary
                        )
                    }
                    Text(
                        symbol,
                        style = AppTypography.headlineSmall,
                        color = AppColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Content
            when (val state = tokenDetail) {
                is LoadingState.Success -> {
                    val token = state.data

                    // Price Header with Chart
                    item {
                        DetailHeader(
                            price = viewModel.formatPrice(token.currentPrice),
                            changeAmount = UiFormatters.formatUsd(
                                token.currentPrice * (token.priceChange24h / 100.0)
                            ),
                            changePercent = token.priceChange24h,
                            isPositive = token.priceChange24h >= 0,
                            selectedTimeframe = selectedTimeframe,
                            onTimeframeSelected = viewModel::onTimeframeSelected,
                            chartState = chartData // ✅ Pass chart state
                        )
                    }

                    // Action Grid
                    item {
                        ChartActionGrid(
                            onReceive = { /* Navigate to receive */ },
                            onCashBuy = { /* Navigate to on-ramp */ },
                            onShare = { /* Share */ },
                            onMore = { /* More options */ }
                        )
                    }

                    // Info Section
                    item {
                        Text(
                            "Info",
                            style = AppTypography.titleLarge,
                            color = AppColors.TextPrimary
                        )

                        MetallicCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                InfoRow("Name", token.name)
                                InfoRow("Symbol", token.symbol)
                                InfoRow("Market Cap", token.formattedMarketCap)
                                InfoRow("24h Volume", UiFormatters.formatCompactNumber(token.volume24h))
                                if (token.mintAddress != null) {
                                    InfoRow(
                                        "Mint Address",
                                        UiFormatters.formatAddress(token.mintAddress)
                                    )
                                }
                            }
                        }
                    }
                }

                is LoadingState.Loading -> {
                    item { ShimmerLoadingScreen() }
                }

                is LoadingState.Error -> {
                    item {
                        ErrorScreen(
                            message = state.message,
                            onRetry = { viewModel.loadToken(tokenId, symbol) }
                        )
                    }
                }

                else -> {}
            }
        }
    }
}