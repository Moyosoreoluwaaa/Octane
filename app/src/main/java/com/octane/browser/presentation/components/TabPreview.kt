package com.octane.browser.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.octane.browser.design.BrowserColors
import com.octane.browser.design.BrowserDimens
import com.octane.browser.design.BrowserTypography
import com.octane.browser.domain.models.BrowserTab

@Composable
fun TabPreview(
    tab: BrowserTab,
    isActive: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(BrowserDimens.BrowserShapeRoundedMedium),
        color = BrowserColors.BrowserColorPrimarySurface,
        shadowElevation = if (isActive)
            BrowserDimens.BrowserElevationMedium
        else
            BrowserDimens.BrowserElevationLow
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(BrowserDimens.BrowserSpacingMedium)
            ) {
                // Icon + Title Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(BrowserDimens.BrowserSpacingSmall)
                ) {
                    // Website Icon
                    Icon(
                        Icons.Rounded.Language,
                        contentDescription = null,
                        modifier = Modifier.size(BrowserDimens.BrowserSizeIconMedium),
                        tint = if (isActive)
                            BrowserColors.BrowserColorAccent
                        else
                            BrowserColors.BrowserColorSecondaryText
                    )

                    // Title
                    Text(
                        text = tab.title.ifEmpty { "New Tab" },
                        style = BrowserTypography.BrowserFontBodyMedium.copy(
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = BrowserColors.BrowserColorPrimaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(BrowserDimens.BrowserSpacingSmall))

                // URL
                Text(
                    text = tab.url.takeIf { it != "about:blank" } ?: "New Tab",
                    style = BrowserTypography.BrowserFontBodySmall,
                    color = BrowserColors.BrowserColorSecondaryText,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Close Button (Circular, Top-Right)
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(32.dp)
                    .background(
                        BrowserColors.BrowserColorSecondarySurface,
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Close Tab",
                    modifier = Modifier.size(16.dp),
                    tint = BrowserColors.BrowserColorPrimaryText
                )
            }

            // Active Indicator (Bottom Border - ONLY VISUAL INDICATOR)
            if (isActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(
                            RoundedCornerShape(
                                bottomStart = BrowserDimens.BrowserShapeRoundedMedium,
                                bottomEnd = BrowserDimens.BrowserShapeRoundedMedium
                            )
                        )
                        .background(BrowserColors.BrowserColorAccent)
                )
            }
        }
    }
}