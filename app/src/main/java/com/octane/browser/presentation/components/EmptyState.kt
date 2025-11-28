package com.octane.browser.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.octane.browser.design.*

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large Icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = BrowserColors.BrowserColorTertiaryText
        )

        Spacer(modifier = Modifier.height(BrowserDimens.BrowserSpacingLarge))

        // Title
        Text(
            text = title,
            style = BrowserTypography.BrowserFontHeadlineMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = BrowserColors.BrowserColorPrimaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(BrowserDimens.BrowserSpacingUnit))

        // Message
        Text(
            text = message,
            style = BrowserTypography.BrowserFontBodyMedium,
            color = BrowserColors.BrowserColorSecondaryText,
            textAlign = TextAlign.Center
        )
    }
}