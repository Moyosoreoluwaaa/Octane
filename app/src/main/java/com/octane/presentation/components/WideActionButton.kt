package com.octane.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.octane.presentation.theme.AppColors
import com.octane.presentation.theme.AppTypography
import com.octane.presentation.theme.Dimensions
import com.octane.presentation.utils.metallicBorder

/**
 * Wide action button (primary/secondary variants).
 * Used for main CTAs.
 */
@Composable
fun WideActionButton(
    text: String,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val backgroundColor = when {
        !enabled -> AppColors.SurfaceHighlight
        isPrimary -> AppColors.Primary
        else -> AppColors.Surface
    }

    val textColor = when {
        !enabled -> AppColors.TextDisabled
        isPrimary -> Color.Black
        else -> AppColors.TextPrimary
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimensions.Button.heightLarge)
            .clip(RoundedCornerShape(Dimensions.CornerRadius.standard))
            .background(backgroundColor)
            .then(
                if (!isPrimary) Modifier.metallicBorder(
                    Dimensions.Border.standard,
                    RoundedCornerShape(Dimensions.CornerRadius.standard),
                    170f
                ) else Modifier
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = AppTypography.labelLarge,
            color = textColor
        )
    }
}