package com.octane.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.octane.presentation.theme.AppColors
import com.octane.presentation.theme.AppTypography
import com.octane.presentation.theme.Dimensions

/**
 * Token detail screen header with price, change, and chart.
 */
@Composable
fun DetailHeader(
    price: String,
    changeAmount: String,
    changePercent: Double,
    isPositive: Boolean,
    selectedTimeframe: String,
    onTimeframeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Price Display
        Text(
            text = price,
            style = AppTypography.priceDisplay,
            color = AppColors.TextPrimary
        )
        
        // Change
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
        
        Spacer(modifier = Modifier.height(Dimensions.Spacing.large))
        
        // Chart Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(vertical = Dimensions.Spacing.standard)
        ) {
            val lineColor = if (isPositive) AppColors.Success else AppColors.Error
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val path = Path().apply {
                    moveTo(0f, height * 0.7f)
                    cubicTo(
                        width * 0.2f,
                        height * 0.8f,
                        width * 0.3f,
                        height * 0.4f,
                        width * 0.5f,
                        height * 0.5f
                    )
                    cubicTo(
                        width * 0.7f,
                        height * 0.6f,
                        width * 0.8f,
                        height * 0.2f,
                        width,
                        height * 0.1f
                    )
                }
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        
        // Timeframe Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("1H", "1D", "1W", "1M", "YTD", "ALL").forEach { timeframe ->
                TimeframeChip(
                    label = timeframe,
                    isSelected = timeframe == selectedTimeframe,
                    onClick = { onTimeframeSelected(timeframe) }
                )
            }
        }
    }
}

@Composable
private fun TimeframeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Dimensions.CornerRadius.small))
            .background(if (isSelected) AppColors.SurfaceHighlight else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .padding(
                horizontal = Dimensions.Padding.medium,
                vertical = Dimensions.Padding.small
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = AppTypography.labelMedium,
            color = if (isSelected) AppColors.TextPrimary else AppColors.TextSecondary
        )
    }
}