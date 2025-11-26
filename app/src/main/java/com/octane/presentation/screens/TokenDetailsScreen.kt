package com.octane.presentation.screens

import android.R.attr.padding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 1. Logo/Icon (Always in a fixed size Box for consistent layout)
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .padding(end = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Safely retrieve the logo URL from the loaded state
                            val logoUrl = (tokenDetail as? LoadingState.Success)?.data?.logoUrl
                            AsyncImage(
                                model = logoUrl
                                    ?: "logo_url_for_$symbol", // Use actual URL or fallback placeholder
                                contentDescription = "$symbol Logo",
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // 2. Title content (Symbol + Name)
                        Column(horizontalAlignment = Alignment.Start) {
                            when (val state = tokenDetail) {
                                is LoadingState.Success -> {
                                    Text(
                                        state.data.symbol.uppercase(), // e.g., SOL
                                        style = AppTypography.headlineSmall,
                                        color = AppColors.TextPrimary
                                    )
                                    Text(
                                        state.data.name, // e.g., Solana (The full name)
                                        style = AppTypography.labelMedium,
                                        color = AppColors.TextSecondary
                                    )
                                }

                                else -> {
                                    // Fallback Text while loading
                                    Text(
                                        symbol.uppercase(),
                                        style = AppTypography.headlineSmall,
                                        color = AppColors.TextPrimary
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Placeholder for actions
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Background)
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                horizontal = Dimensions.Padding.standard,
                vertical = Dimensions.Spacing.large
            ),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
        ) {

            // Content
            when (val state = tokenDetail) {
                is LoadingState.Success -> {
                    val token = state.data

                    // Price Header with Chart
                    item {
                        DetailHeader(
                            price = UiFormatters.formatUsd(token.currentPrice), // <--- CHANGE: Used to be "$${token.currentPrice}"
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
                                InfoRow(
                                    "24h Volume",
                                    UiFormatters.formatCompactNumber(token.volume24h)
                                )
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