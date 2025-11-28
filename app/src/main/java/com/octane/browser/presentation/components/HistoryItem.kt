package com.octane.browser.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.octane.browser.design.BrowserColors
import com.octane.browser.design.BrowserDimens
import com.octane.browser.design.BrowserOpacity
import com.octane.browser.design.BrowserTypography
import com.octane.browser.domain.models.HistoryEntry


@Composable
fun HistoryItem(
    entry: HistoryEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(BrowserDimens.BrowserShapeRoundedMedium),
        color = BrowserColors.BrowserColorPrimarySurface.copy(alpha = BrowserOpacity.BrowserOpacitySurfaceHigh),
        shadowElevation = BrowserDimens.BrowserElevationLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BrowserDimens.BrowserSpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Website Icon (Circular Background)
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = BrowserColors.BrowserColorInfo.copy(alpha = 0.12f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Language,
                        contentDescription = null,
                        modifier = Modifier.size(BrowserDimens.BrowserSizeIconLarge),
                        tint = BrowserColors.BrowserColorInfo
                    )
                }
            }

            Spacer(modifier = Modifier.width(BrowserDimens.BrowserSpacingMedium))

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title.ifEmpty { "Untitled" },
                    style = BrowserTypography.BrowserFontBodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = BrowserColors.BrowserColorPrimaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = entry.url,
                    style = BrowserTypography.BrowserFontBodySmall,
                    color = BrowserColors.BrowserColorSecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Timestamp
                Text(
                    text = formatTimestamp(entry.visitedAt),
                    style = BrowserTypography.BrowserFontLabelMedium,
                    color = BrowserColors.BrowserColorTertiaryText
                )
            }
        }
    }
}

// Helper function to format timestamp (you can customize this)
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 604_800_000 -> "${diff / 86_400_000}d ago"
        else -> "${diff / 604_800_000}w ago"
    }
}