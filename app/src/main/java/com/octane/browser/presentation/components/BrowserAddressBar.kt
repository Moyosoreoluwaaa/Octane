package com.octane.browser.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.octane.browser.design.BrowserDimens
import com.octane.browser.domain.models.WebViewState

/**
 * ✅ ENHANCED: Added desktop mode toggle and home button
 */
@Composable
fun BrowserAddressBar(
    webViewState: WebViewState,
    isBookmarked: Boolean,
    isDesktopMode: Boolean,
    onNavigate: (String) -> Unit,
    onReload: () -> Unit,
    onStop: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onMenuClick: () -> Unit,
    onDesktopModeToggle: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var urlInput by remember { mutableStateOf(webViewState.url) }
    var isEditing by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    LaunchedEffect(webViewState.url) {
        if (!isEditing) {
            urlInput = webViewState.url
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(BrowserDimens.BrowserSpacingUnit)
        ) {
            // LEFT: Home Button
            IconButton(
                onClick = onHomeClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Rounded.Home,
                    contentDescription = "Home",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // CENTER: Address Pill
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clickable { isEditing = true },
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = BrowserDimens.BrowserElevationLow
            ) {
                Box {
                    // Progress Indicator
                    if (webViewState.isLoading) {
                        LinearProgressIndicator(
                            progress = { webViewState.progress / 100f },
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(0.1f),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = Color.Companion.Transparent
                        )
                    }

                    // Content
                    if (isEditing) {
                        AddressBarEditMode(
                            urlInput = urlInput,
                            onUrlChange = { urlInput = it },
                            isSecure = webViewState.isSecure,
                            onNavigate = {
                                onNavigate(urlInput)
                                isEditing = false
                            },
                            onClear = { urlInput = "" }
                        )
                    } else {
                        AddressBarDisplayMode(
                            url = webViewState.url,
                            isSecure = webViewState.isSecure,
                            isLoading = webViewState.isLoading,
                            onReload = onReload,
                            onStop = onStop
                        )
                    }
                }
            }

            // RIGHT: Options Menu
            Box {
                IconButton(
                    onClick = { showOptionsMenu = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Rounded.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Options Dropdown
                DropdownMenu(
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false }
                ) {
                    // Desktop Mode Toggle
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Computer,
                                    contentDescription = null
                                )
                                Text(if (isDesktopMode) "Mobile Mode" else "Desktop Mode")
                            }
                        },
                        onClick = {
                            onDesktopModeToggle()
                            showOptionsMenu = false
                        }
                    )

                    Divider()

                    // Bookmark
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    if (isBookmarked) Icons.Rounded.Star
                                    else Icons.Rounded.StarBorder,
                                    contentDescription = null,
                                    tint = if (isBookmarked)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                Text(if (isBookmarked) "Remove Bookmark" else "Bookmark")
                            }
                        },
                        onClick = {
                            onBookmarkToggle()
                            showOptionsMenu = false
                        }
                    )

                    // Menu
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.Menu, contentDescription = null)
                                Text("Menu")
                            }
                        },
                        onClick = {
                            onMenuClick()
                            showOptionsMenu = false
                        }
                    )
                }
            }
        }
    }
}
