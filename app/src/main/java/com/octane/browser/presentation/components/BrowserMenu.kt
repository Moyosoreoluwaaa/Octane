package com.octane.browser.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.octane.browser.design.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserMenu(
    onDismiss: () -> Unit,
    onBookmarks: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onNewTab: () -> Unit,
    onShare: () -> Unit,
    onRefresh: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BrowserColors.BrowserColorPrimarySurface,
        shape = RoundedCornerShape(
            topStart = BrowserDimens.BrowserShapeRoundedMedium,
            topEnd = BrowserDimens.BrowserShapeRoundedMedium
        ),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = BrowserDimens.BrowserSpacingMedium)
                    .width(32.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BrowserColors.BrowserColorTertiaryText)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = BrowserDimens.BrowserSpacingSmall,
                    vertical = BrowserDimens.BrowserSpacingUnit
                )
        ) {
            // Action Items
            BrowserMenuItem(
                icon = Icons.Rounded.Refresh,
                text = "Refresh Page",
                onClick = onRefresh
            )
            BrowserMenuItem(
                icon = Icons.Rounded.Add,
                text = "New Tab",
                onClick = onNewTab
            )
            BrowserMenuItem(
                icon = Icons.Rounded.Bookmark,
                text = "Bookmarks",
                onClick = onBookmarks
            )
            BrowserMenuItem(
                icon = Icons.Rounded.History,
                text = "History",
                onClick = onHistory
            )
            BrowserMenuItem(
                icon = Icons.Rounded.Share,
                text = "Share",
                onClick = onShare
            )

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(
                    vertical = BrowserDimens.BrowserSpacingUnit,
                    horizontal = BrowserDimens.BrowserSpacingMedium
                ),
                color = BrowserColors.BrowserColorTertiaryText.copy(alpha = 0.3f)
            )

            // Settings
            BrowserMenuItem(
                icon = Icons.Rounded.Settings,
                text = "Settings",
                onClick = onSettings
            )

            Spacer(modifier = Modifier.height(BrowserDimens.BrowserSpacingLarge))
        }
    }
}

@Composable
private fun BrowserMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = BrowserDimens.BrowserSpacingMedium, vertical = 4.dp),
        shape = RoundedCornerShape(BrowserDimens.BrowserShapeRoundedMedium),
        color = BrowserColors.BrowserColorPrimarySurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(
                    horizontal = BrowserDimens.BrowserSpacingMedium,
                    vertical = BrowserDimens.BrowserSpacingMedium
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(BrowserDimens.BrowserSpacingMedium)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(BrowserDimens.BrowserSizeIconLarge),
                tint = BrowserColors.BrowserColorPrimaryText
            )

            Text(
                text,
                style = BrowserTypography.BrowserFontBodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = BrowserColors.BrowserColorPrimaryText
            )
        }
    }
}