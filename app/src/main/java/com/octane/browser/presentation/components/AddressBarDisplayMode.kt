package com.octane.browser.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.net.URI

@Composable
internal fun AddressBarDisplayMode(
    url: String,
    isSecure: Boolean,
    isLoading: Boolean,
    onReload: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Lock Icon
        Icon(
            if (isSecure) Icons.Rounded.Lock
            else Icons.Rounded.LockOpen,
            contentDescription = "Security",
            modifier = Modifier.size(14.dp),
            tint = if (isSecure)
                MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.width(8.dp))

        // Domain Text
        Text(
            text = extractDomain(url),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Companion.Ellipsis,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Companion.Center
        )

        // Reload/Stop Button
        IconButton(
            onClick = { if (isLoading) onStop() else onReload() },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                if (isLoading) Icons.Rounded.Close
                else Icons.Rounded.Refresh,
                contentDescription = if (isLoading) "Stop" else "Reload",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun extractDomain(url: String): String {
    return try {
        URI(url).host ?: url
    } catch (e: Exception) {
        url
    }
}