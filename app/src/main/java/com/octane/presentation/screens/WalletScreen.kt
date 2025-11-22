package com.octane.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.navigation.NavController
import com.octane.core.util.LoadingState
import com.octane.domain.models.Transaction
import com.octane.domain.models.TransactionStatus
import com.octane.domain.models.TransactionType
import com.octane.presentation.components.*
import com.octane.presentation.theme.AppColors
import com.octane.presentation.theme.AppTypography
import com.octane.presentation.theme.Dimensions
import com.octane.presentation.utils.UiFormatters
import com.octane.presentation.viewmodel.ActivityViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Wallet/Activity screen showing transaction history.
 * Connected to ActivityViewModel.
 */
@Composable
fun WalletScreen(
    viewModel: ActivityViewModel = koinViewModel(),
    navController: NavController, // ✅ Pass navController
    onNavigateToDetails: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            ActivityHeader(
                pendingCount = pendingCount,
                onBack = onBack,
                onFilterClick = { /* Show filter sheet */ },
                onExportClick = { viewModel.exportTransactions() }
            )

            // Content
            when (val state = filteredTransactions) {
                is LoadingState.Loading -> LoadingScreen()
                is LoadingState.Success -> {
                    if (state.data.isEmpty()) {
                        EmptyActivityScreen()
                    } else {
                        TransactionList(
                            transactions = state.data,
                            onTransactionClick = { tx ->
                                viewModel.showTransactionDetails(tx)
                                onNavigateToDetails(tx.txHash)
                            },
                            formatTransactionType = viewModel::formatTransactionType,
                            getTransactionIcon = viewModel::getTransactionIcon,
                            getStatusColor = viewModel::getStatusColor
                        )
                    }
                }

                is LoadingState.Error -> ErrorScreen(
                    message = state.message ?: "Failed to load transactions",
                    onRetry = { /* Retry logic */ }
                )

                LoadingState.Idle -> TODO()
                LoadingState.Simulating -> TODO()
                is LoadingState.Stale<*> -> TODO()
            }
        }
    }
}

@Composable
private fun ActivityHeader(
    pendingCount: Int,
    onBack: () -> Unit,
    onFilterClick: () -> Unit,
    onExportClick: () -> Unit
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

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Activity",
                style = AppTypography.headlineSmall,
                color = AppColors.TextPrimary
            )
            if (pendingCount > 0) {
                Spacer(modifier = Modifier.width(Dimensions.Spacing.small))
                PriceChangeBadge(changePercent = pendingCount.toDouble())
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)) {
            IconButton(onClick = onFilterClick) {
                Icon(
                    Icons.Rounded.FilterList,
                    contentDescription = "Filter",
                    tint = AppColors.TextPrimary
                )
            }
            IconButton(onClick = onExportClick) {
                Icon(
                    Icons.Rounded.FileDownload,
                    contentDescription = "Export",
                    tint = AppColors.TextPrimary
                )
            }
        }
    }
}

@Composable
private fun TransactionList(
    transactions: List<Transaction>,
    onTransactionClick: (Transaction) -> Unit,
    formatTransactionType: (TransactionType) -> String,
    getTransactionIcon: (TransactionType) -> String,
    getStatusColor: (TransactionStatus) -> androidx.compose.ui.graphics.Color
) {
    LazyColumn(
        contentPadding = PaddingValues(Dimensions.Padding.standard),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
    ) {
        items(transactions) { transaction ->
            TransactionRow(
                transaction = transaction,
                onClick = { onTransactionClick(transaction) },
                formatType = formatTransactionType,
                getIcon = getTransactionIcon,
                getStatusColor = getStatusColor
            )
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: Transaction,
    onClick: () -> Unit,
    formatType: (TransactionType) -> String,
    getIcon: (TransactionType) -> String,
    getStatusColor: (TransactionStatus) -> androidx.compose.ui.graphics.Color
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
                    getIcon(transaction.type),
                    style = AppTypography.bodyMedium
                )
            }

            Column {
                Text(
                    formatType(transaction.type),
                    style = AppTypography.titleSmall,
                    color = AppColors.TextPrimary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.extraSmall)
                ) {
                    Text(
                        UiFormatters.formatRelativeTime(transaction.timestamp),
                        style = AppTypography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                    Text("•", style = AppTypography.bodySmall, color = AppColors.TextSecondary)
                    Text(
                        transaction.tokenSymbol,
                        style = AppTypography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${if (transaction.type == TransactionType.SEND) "-" else "+"}${transaction.amount}",
                style = AppTypography.titleSmall,
                color = AppColors.TextPrimary
            )
            StatusBadge(
                status = transaction.status,
                color = getStatusColor(transaction.status)
            )
        }
    }
}

@Composable
private fun StatusBadge(
    status: TransactionStatus,
    color: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(Dimensions.CornerRadius.large))
            .background(color.copy(alpha = 0.15f))
            .padding(
                horizontal = Dimensions.Padding.small,
                vertical = Dimensions.Padding.tiny
            )
    ) {
        Text(
            text = when (status) {
                TransactionStatus.CONFIRMED -> "Confirmed"
                TransactionStatus.PENDING -> "Pending"
                TransactionStatus.FAILED -> "Failed"
            },
            style = AppTypography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun EmptyActivityScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimensions.Padding.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "No Activity Yet",
            style = AppTypography.headlineSmall,
            color = AppColors.TextPrimary
        )
        Spacer(modifier = Modifier.height(Dimensions.Spacing.small))
        Text(
            "Your transactions will appear here",
            style = AppTypography.bodyMedium,
            color = AppColors.TextSecondary
        )
    }
}