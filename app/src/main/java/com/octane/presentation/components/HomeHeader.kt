package com.octane.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.octane.presentation.theme.AppColors
import com.octane.presentation.theme.AppTypography
import com.octane.presentation.theme.Dimensions
import com.octane.presentation.utils.UiFormatters
import com.octane.presentation.utils.metallicBorder

/**
 * Home screen header with balance and wallet info.
 */
@Composable
fun HomeHeader(
    totalBalance: String,
    changeAmount: String,
    changePercent: Double,
    walletName: String,
    onWalletClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.Spacing.large)
    ) {
        // Top Bar: Wallet Selector + Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Wallet Avatar + Name
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(Dimensions.CornerRadius.medium))
                    .background(AppColors.Surface)
                    .metallicBorder(
                        Dimensions.Border.standard,
                        RoundedCornerShape(Dimensions.CornerRadius.medium),
                        90f
                    )
                    .clickable(onClick = onWalletClick)
                    .padding(
                        horizontal = Dimensions.Padding.medium,
                        vertical = Dimensions.Padding.small
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimensions.Avatar.small)
                        .clip(CircleShape)
                        .background(AppColors.Background),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        walletName.take(1),
                        style = AppTypography.labelSmall,
                        color = Color.White
                    )
                }
                Text(
                    walletName,
                    style = AppTypography.labelLarge,
                    color = AppColors.TextPrimary
                )
            }
            
            // Action Icons
            Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)) {
                Icon(
                    Icons.Rounded.History,
                    contentDescription = "History",
                    tint = AppColors.TextPrimary,
                    modifier = Modifier
                        .size(Dimensions.IconSize.standard)
                        .clickable(onClick = onHistoryClick)
                )
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = AppColors.TextPrimary,
                    modifier = Modifier
                        .size(Dimensions.IconSize.standard)
                        .clickable(onClick = onSearchClick)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(Dimensions.Spacing.extraLarge))
        
        // Balance Display
        Text(
            text = totalBalance,
            style = AppTypography.balanceDisplay,
            color = AppColors.TextPrimary
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
        ) {
            Text(
                changeAmount,
                style = AppTypography.bodyMedium,
                color = AppColors.TextSecondary
            )
            PriceChangeBadge(changePercent = changePercent)
        }
    }
}