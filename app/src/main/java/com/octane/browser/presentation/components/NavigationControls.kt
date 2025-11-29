package com.octane.browser.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.octane.browser.design.BrowserDimens
import com.octane.browser.domain.models.WebViewState

/**
 * ✅ ENHANCED: Menu moved here from address bar
 */
@Composable
fun NavigationControls(
    webViewState: WebViewState,
    tabCount: Int,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onHome: () -> Unit,
    onTabManager: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = BrowserDimens.BrowserElevationHigh
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Back Button
            IconButton(
                onClick = onBack,
                enabled = webViewState.canGoBack
            ) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = if (webViewState.canGoBack)
                        MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            }

            // Forward Button
            IconButton(
                onClick = onForward,
                enabled = webViewState.canGoForward
            ) {
                Icon(
                    Icons.Rounded.ArrowForward,
                    contentDescription = "Forward",
                    tint = if (webViewState.canGoForward)
                        MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            }

            // Center Home Button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onHome() }
                    .background(
                        if (webViewState.url == "about:home") // Highlight if already on home
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        else
                            Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Home,
                    contentDescription = "Home",
                    tint = if (webViewState.url == "about:home")
                        MaterialTheme.colorScheme.onPrimary // Highlighted color
                    else
                        MaterialTheme.colorScheme.onSurface, // Default color
                    modifier = Modifier.size(24.dp)
                )
            }

            // Tab Counter
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(BrowserDimens.BrowserShapeRoundedSmall))
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.onSurface,
                        RoundedCornerShape(BrowserDimens.BrowserShapeRoundedSmall)
                    )
                    .clickable { onTabManager() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$tabCount",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // ✅ Menu Button (moved from address bar)
            IconButton(onClick = onMenuClick) {
                Icon(
                    Icons.Rounded.Menu,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}