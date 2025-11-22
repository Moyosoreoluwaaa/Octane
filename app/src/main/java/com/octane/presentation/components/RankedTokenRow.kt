package com.octane.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.octane.presentation.theme.AppColors
import com.octane.presentation.theme.AppTypography
import com.octane.presentation.theme.Dimensions
import com.octane.presentation.utils.metallicBorder

/**
 * Token row with rank badge.
 * Used in trending/discovery lists.
 */
@Composable
fun RankedTokenRow(
    rank: Int,
    symbol: String,
    name: String,
    marketCap: String,
    price: String,
    changePercent: Double,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.CornerRadius.standard))
            .background(AppColors.Background)
            .metallicBorder(
                Dimensions.Border.standard,
                RoundedCornerShape(Dimensions.CornerRadius.standard),
                angleDeg = 90f
            )
            .clickable(onClick = onClick)
            .padding(Dimensions.Padding.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // LEFT: Rank Badge + Icon + Info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
        ) {
            Box(contentAlignment = Alignment.BottomStart) {
                // Token Icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(Dimensions.Avatar.medium)
                        .clip(CircleShape)
                        .background(iconColor)
                ) {
                    Text(
                        symbol.take(1),
                        style = AppTypography.labelLarge,
                        color = Color.White
                    )
                }
                
                // Rank Badge
                Box(
                    modifier = Modifier
                        .offset(x = (-4).dp, y = 4.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(AppColors.TextPrimary)
                        .border(2.dp, AppColors.Background, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rank.toString(),
                        style = AppTypography.labelSmall,
                        color = Color.Black
                    )
                }
            }
            
            Column {
                Text(
                    symbol,
                    style = AppTypography.titleSmall,
                    color = AppColors.TextPrimary
                )
                Text(
                    marketCap,
                    style = AppTypography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }
        }
        
        // RIGHT: Price Data
        Column(horizontalAlignment = Alignment.End) {
            Text(
                price,
                style = AppTypography.titleSmall,
                color = AppColors.TextPrimary
            )
            PriceChangeBadge(changePercent = changePercent)
        }
    }
}