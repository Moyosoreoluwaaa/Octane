package com.octane.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.octane.domain.models.Transaction
import com.octane.domain.models.TransactionType
import com.octane.presentation.components.*
import com.octane.presentation.theme.AppColors
import com.octane.presentation.theme.AppTypography
import com.octane.presentation.theme.Dimensions
import com.octane.presentation.utils.UiFormatters

@Composable
fun TransactionPreviewRow(
    transaction: Transaction,
    onClick: () -> Unit
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
                    when (transaction.type) {
                        TransactionType.SEND -> "↗"
                        TransactionType.RECEIVE -> "↙"
                        TransactionType.SWAP -> "🔄"
                        else -> "•"
                    },
                    style = AppTypography.bodyMedium
                )
            }

            Column {
                Text(
                    when (transaction.type) {
                        TransactionType.SEND -> "Sent"
                        TransactionType.RECEIVE -> "Received"
                        TransactionType.SWAP -> "Swapped"
                        else -> "Transaction"
                    },
                    style = AppTypography.titleSmall,
                    color = AppColors.TextPrimary
                )
                Text(
                    UiFormatters.formatRelativeTime(transaction.timestamp),
                    style = AppTypography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${transaction.amount} ${transaction.tokenSymbol}",
                style = AppTypography.titleSmall,
                color = AppColors.TextPrimary
            )
            StatusBadge(status = transaction.status)
        }
    }
}