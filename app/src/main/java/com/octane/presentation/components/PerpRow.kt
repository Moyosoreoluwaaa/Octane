package com.octane.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material3.Icon
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
 * Perpetual futures row component.
 * Shows leverage badge and volume data.
 */
@Composable
fun PerpRow(
    symbol: String,
    name: String,
    price: String,
    changePercent: Double,
    volume24h: String,
    leverageMax: Int,
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
        // LEFT: Badged Icon + Info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
        ) {
            // Icon with Infinity Badge
            Box(contentAlignment = Alignment.BottomEnd) {
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
                
                // Perp Badge
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = 4.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.AllInclusive,
                        contentDescription = "Perp",
                        tint = Color.Black,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            
            Column {
                Text(
                    name,
                    style = AppTypography.titleSmall,
                    color = AppColors.TextPrimary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.extraSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        volume24h,
                        style = AppTypography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                    Text(
                        "•",
                        style = AppTypography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                    Text(
                        "${leverageMax}x",
                        style = AppTypography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                }
            }
        }
        
        // RIGHT: Price + Change
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