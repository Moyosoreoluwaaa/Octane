package com.octane.browser.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.octane.browser.design.*
import com.octane.browser.domain.models.Bookmark

@Composable
fun BookmarkItem(
    bookmark: Bookmark,
    onClick: () -> Unit,
    onDelete: () -> Unit,
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
                color = BrowserColors.BrowserColorAccent.copy(alpha = 0.12f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Language,
                        contentDescription = null,
                        modifier = Modifier.size(BrowserDimens.BrowserSizeIconLarge),
                        tint = BrowserColors.BrowserColorAccent
                    )
                }
            }

            Spacer(modifier = Modifier.width(BrowserDimens.BrowserSpacingMedium))

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bookmark.title,
                    style = BrowserTypography.BrowserFontBodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = BrowserColors.BrowserColorPrimaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = bookmark.url,
                    style = BrowserTypography.BrowserFontBodySmall,
                    color = BrowserColors.BrowserColorSecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Delete Button (Circular)
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        BrowserColors.BrowserColorError.copy(alpha = 0.1f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(BrowserDimens.BrowserSizeIconMedium),
                    tint = BrowserColors.BrowserColorError
                )
            }
        }
    }
}