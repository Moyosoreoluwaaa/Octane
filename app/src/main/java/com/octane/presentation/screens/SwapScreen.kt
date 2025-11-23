package com.octane.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.octane.presentation.components.*
import com.octane.presentation.navigation.AppRoute
import com.octane.presentation.theme.AppColors
import com.octane.presentation.theme.AppTypography
import com.octane.presentation.theme.Dimensions
import com.octane.presentation.utils.UiFormatters
import com.octane.presentation.viewmodel.SwapEvent
import com.octane.presentation.viewmodel.SwapViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Swap screen for token exchanges.
 * Connected to SwapViewModel.
 */
@Composable
fun SwapScreen(
    viewModel: SwapViewModel = koinViewModel(),
    navController: NavController, // ✅ Pass navController
    onBack: () -> Unit,
    onSuccess: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val swapState by viewModel.swapState.collectAsState()
    val portfolioState by viewModel.portfolioState.collectAsState()

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SwapEvent.Success -> onSuccess(event.txHash)
                is SwapEvent.Error -> {
                    // Show error toast
                }
                is SwapEvent.HighPriceImpactWarning -> {
                    // Show warning dialog
                }
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) } // ✅ Fixed
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            SwapHeader(
                onBack = onBack,
                onSettingsClick = { navController.navigate(AppRoute.Settings) }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.Padding.standard),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
            ) {
                // Swap Cards
                SwapCard(
                    payingToken = swapState.fromToken,
                    payingAmount = swapState.fromAmount,
                    receivingToken = swapState.toToken,
                    receivingAmount = swapState.toAmount,
                    onFlip = viewModel::reverseTokens
                )

                // Rate Info
                if (swapState.rate != null) {
                    InfoRow(
                        label = "Rate",
                        value = UiFormatters.formatExchangeRate(
                            fromAmount = 1.0,
                            fromSymbol = swapState.fromToken,
                            toAmount = swapState.rate ?: 0.0,
                            toSymbol = swapState.toToken
                        ),
                        showDivider = false
                    )
                }

                // Price Impact Warning
                if (swapState.priceImpact != null && swapState.priceImpact!! > 1.0) {
                    val (color, impact) = UiFormatters.formatPriceImpact(swapState.priceImpact!!)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Price Impact:",
                            style = AppTypography.bodyMedium,
                            color = AppColors.TextSecondary
                        )
                        Text(
                            impact,
                            style = AppTypography.bodyMedium,
                            color = color
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))

                // Swap Button
                WideActionButton(
                    text = if (swapState.isFetchingQuote) "Fetching Quote..." else "Review Swap",
                    isPrimary = true,
                    enabled = swapState.isValid && !swapState.isFetchingQuote,
                    onClick = { /* Navigate to confirmation */ }
                )
            }
        }
    }
}

@Composable
private fun SwapHeader(
    onBack: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimensions.Padding.standard),
        horizontalArrangement = Arrangement.SpaceBetween,
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
            "Swap",
            style = AppTypography.headlineSmall,
            color = AppColors.TextPrimary
        )

        IconButton(onClick = onSettingsClick) {
            Icon(
                Icons.Rounded.Settings,
                contentDescription = "Settings",
                tint = AppColors.TextPrimary
            )
        }
    }
}