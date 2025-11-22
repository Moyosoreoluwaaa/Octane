package com.octane.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.QrCode
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
 * Network row for receive screen.
 * Shows network name, address, and actions.
 */
@Composable
fun ReceiveNetworkRow(
    networkName: String,
    address: String,
    iconColor: Color,
    onCopy: () -> Unit,
    onShowQr: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.CornerRadius.standard))
            .background(AppColors.Surface)
            .metallicBorder(
                Dimensions.Border.standard,
                RoundedCornerShape(Dimensions.CornerRadius.standard),
                angleDeg = 90f
            )
            .padding(Dimensions.Padding.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Icon + Text
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(Dimensions.Avatar.medium)
                    .clip(RoundedCornerShape(Dimensions.CornerRadius.medium))
                    .background(AppColors.TextPrimary)
            ) {
                Box(
                    Modifier
                        .size(Dimensions.Avatar.small)
                        .clip(CircleShape)
                        .background(iconColor)
                )
            }
            
            Column {
                Text(
                    networkName,
                    style = AppTypography.titleSmall,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = UiFormatters.formatAddress(address),
                    style = AppTypography.bodySmall,
                    color = AppColors.TextSecondary,
                    maxLines = 1
                )
            }
        }
        
        // Right: Actions (QR + Copy)
        Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)) {
            listOf(
                Icons.Rounded.QrCode to onShowQr,
                Icons.Rounded.ContentCopy to onCopy
            ).forEach { (icon, action) ->
                Box(
                    modifier = Modifier
                        .size(Dimensions.Button.iconButtonSize)
                        .clip(CircleShape)
                        .background(AppColors.SurfaceHighlight)
                        .metallicBorder(
                            Dimensions.Border.standard,
                            CircleShape,
                            135f
                        )
                        .clickable(onClick = action),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = AppColors.TextPrimary,
                        modifier = Modifier.size(Dimensions.IconSize.medium)
                    )
                }
            }
        }
    }
}