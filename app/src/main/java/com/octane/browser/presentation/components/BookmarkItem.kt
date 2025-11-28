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
        shape = MaterialTheme.shapes.medium, // ✅ CHANGED
        color = MaterialTheme.colorScheme.surface.copy(alpha = BrowserOpacity.BrowserOpacitySurfaceHigh), // ✅ CHANGED
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
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) // ✅ CHANGED
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Language,
                        contentDescription = null,
                        modifier = Modifier.size(BrowserDimens.BrowserSizeIconLarge),
                        tint = MaterialTheme.colorScheme.primary // ✅ CHANGED
                    )
                }
            }

            Spacer(modifier = Modifier.width(BrowserDimens.BrowserSpacingMedium))

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bookmark.title,
                    style = MaterialTheme.typography.bodyMedium.copy( // ✅ CHANGED
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface, // ✅ CHANGED
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = bookmark.url,
                    style = MaterialTheme.typography.bodySmall, // ✅ CHANGED
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // ✅ CHANGED
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
                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f), // ✅ CHANGED
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(BrowserDimens.BrowserSizeIconMedium),
                    tint = MaterialTheme.colorScheme.error // ✅ CHANGED
                )
            }
        }
    }
}