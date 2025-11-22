package com.octane.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.octane.core.util.LoadingState
import com.octane.presentation.components.*
import com.octane.presentation.theme.AppColors
import com.octane.presentation.theme.AppTypography
import com.octane.presentation.theme.Dimensions
import com.octane.presentation.utils.UiFormatters
import com.octane.presentation.viewmodel.TokenDetailViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Token detail screen with price chart and stats.
 * Connected to TokenDetailViewModel.
 */
@Composable
fun TokenDetailsScreen(
    assetId: String,
    symbol: String,
    viewModel: TokenDetailViewModel = koinViewModel(),
    onBack: () -> Unit,
    onNavigateToSend: (String) -> Unit,
    onNavigateToReceive: (String) -> Unit,
    onNavigateToSwap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val assetDetail by viewModel.assetDetail.collectAsState()
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()
    
    LaunchedEffect(assetId) {
        viewModel.loadAsset(assetId, symbol)
    }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
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
        when (val state = assetDetail) {
            is LoadingState.Success -> {
                val asset = state.data
                
                // Price Header
                item {
                    DetailHeader(
                        price = viewModel.formatPrice(asset.priceUsd ?: 0.0),
                        changeAmount = UiFormatters.formatUsd(
                            (asset.priceUsd ?: 0.0) * ((asset.priceChange24h ?: 0.0) / 100.0)
                        ),
                        changePercent = asset.priceChange24h ?: 0.0,
                        isPositive = (asset.priceChange24h ?: 0.0) >= 0,
                        selectedTimeframe = selectedTimeframe,
                        onTimeframeSelected = viewModel::onTimeframeSelected
                    )
                }
                
                // Action Grid
                item {
                    ChartActionGrid(
                        onReceive = { onNavigateToReceive(symbol) },
                        onCashBuy = { /* Navigate to on-ramp */ },
                        onShare = { /* Share functionality */ },
                        onMore = { /* More options */ }
                    )
                }
                
                // Position Card
                item {
                    Text(
                        "Your Position",
                        style = AppTypography.titleLarge,
                        color = AppColors.TextPrimary
                    )
                    
                    MetallicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)) {
                            InfoRow("Balance", "${asset.balance} ${asset.symbol}")
                            InfoRow("Value", UiFormatters.formatUsd(asset.valueUsd ?: 0.0))
                        }
                    }
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
                            InfoRow("Name", asset.name)
                            InfoRow("Symbol", asset.symbol)
                            if (asset.mintAddress != null) {
                                InfoRow(
                                    "Mint Address",
                                    UiFormatters.formatAddress(asset.mintAddress!!)
                                )
                            }
                        }
                    }
                }
            }
            
            is LoadingState.Loading -> {
                item { LoadingScreen() }
            }
            
            is LoadingState.Error -> {
                item {
                    ErrorScreen(
                        message = state.message ?: "Failed to load token details",
                        onRetry = { viewModel.loadAsset(assetId, symbol) }
                    )
                }
            }

            LoadingState.Idle -> TODO()
            LoadingState.Simulating -> TODO()
            is LoadingState.Stale<*> -> TODO()
        }
    }
}