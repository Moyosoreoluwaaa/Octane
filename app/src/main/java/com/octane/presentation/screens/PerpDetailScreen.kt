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
import com.octane.core.util.LoadingState
import com.octane.presentation.components.*
import com.octane.presentation.theme.*
import com.octane.presentation.viewmodel.PerpDetailViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PerpDetailScreen(
    perpSymbol: String,
    viewModel: PerpDetailViewModel = koinViewModel(),
    onBack: () -> Unit,
    onNavigateToTrade: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val perpDetail by viewModel.perpDetail.collectAsState()
    val chartData by viewModel.chartData.collectAsState()
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()
    val selectedLeverage by viewModel.selectedLeverage.collectAsState()
    
    LaunchedEffect(perpSymbol) {
        viewModel.loadPerp(perpSymbol)
    }
    
    LaunchedEffect(Unit) {
        viewModel.onTimeframeSelected("1D")
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
                    perpSymbol,
                    style = AppTypography.headlineSmall,
                    color = AppColors.TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        when (val state = perpDetail) {
            is LoadingState.Success -> {
                val perp = state.data
                
                // Price Header with Chart
                item {
                    DetailHeader(
                        price = "$${perp.indexPrice}",
                        changeAmount = "$${(perp.indexPrice * (perp.priceChange24h / 100.0))}",
                        changePercent = perp.priceChange24h,
                        isPositive = perp.priceChange24h >= 0,
                        selectedTimeframe = selectedTimeframe,
                        onTimeframeSelected = viewModel::onTimeframeSelected,
                        chartState = chartData
                    )
                }
                
                // Leverage Selector
                item {
                    Text(
                        "Leverage",
                        style = AppTypography.titleMedium,
                        color = AppColors.TextPrimary
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
                    ) {
                        listOf(2, 5, 10, 20).forEach { leverage ->
                            FilterChip(
                                selected = leverage == selectedLeverage,
                                onClick = { viewModel.onLeverageSelected(leverage) },
                                label = { Text("${leverage}x") }
                            )
                        }
                    }
                }
                
                // Trade Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
                    ) {
                        Button(
                            onClick = { onNavigateToTrade(perpSymbol) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Success
                            )
                        ) {
                            Text("Long")
                        }
                        
                        Button(
                            onClick = { onNavigateToTrade(perpSymbol) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Error
                            )
                        ) {
                            Text("Short")
                        }
                    }
                }
                
                // Perp Info
                item {
                    Text(
                        "Info",
                        style = AppTypography.titleLarge,
                        color = AppColors.TextPrimary
                    )
                    
                    MetallicCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            InfoRow("Mark Price", "$${perp.markPrice}")
                            InfoRow("Index Price", "$${perp.indexPrice}")
                            InfoRow("Funding Rate", perp.formattedFundingRate)
                            InfoRow("Open Interest", perp.formattedOpenInterest)
                            InfoRow("24h Volume", "$${perp.volume24h}")
                            InfoRow("Exchange", perp.exchange)
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
                        onRetry = { viewModel.loadPerp(perpSymbol) }
                    )
                }
            }
            
            else -> {}
        }
    }
}