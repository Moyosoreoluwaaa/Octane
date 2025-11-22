package com.octane.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.octane.presentation.theme.AppColors
import com.octane.presentation.theme.AppTypography
import com.octane.presentation.theme.Dimensions
import com.octane.presentation.utils.metallicBorder

/**
 * Metallic icon button with label.
 * Used in quick action grids.
 */
@Composable
fun MetallicButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(Dimensions.CornerRadius.large))
                .background(AppColors.Surface)
                .metallicBorder(
                    Dimensions.Border.standard,
                    RoundedCornerShape(Dimensions.CornerRadius.large),
                    angleDeg = 45f // Button angle
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = AppColors.TextPrimary,
                modifier = Modifier.size(Dimensions.IconSize.extraLarge)
            )
        }
        Text(
            text = text,
            style = AppTypography.labelMedium,
            color = AppColors.TextSecondary
        )
    }
}

/**
 * Smaller action button for detail screens.
 */
@Composable
fun ChartActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(Dimensions.CornerRadius.standard))
                .background(AppColors.Surface)
                .metallicBorder(
                    Dimensions.Border.standard,
                    RoundedCornerShape(Dimensions.CornerRadius.standard),
                    angleDeg = 135f
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = AppColors.TextPrimary,
                modifier = Modifier.size(Dimensions.IconSize.large)
            )
        }
        Text(
            text = text,
            style = AppTypography.labelSmall,
            color = AppColors.TextSecondary
        )
    }
}