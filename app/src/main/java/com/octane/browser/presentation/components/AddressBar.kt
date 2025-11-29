package com.octane.browser.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.octane.browser.design.BrowserDimens
import com.octane.browser.domain.models.WebViewState

/**
 * ✅ ENHANCED: Added desktop mode toggle and home button
 */
@Composable
fun AddressBar(
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
                            trackColor = Color.Transparent
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

@Composable
private fun AddressBarEditMode(
    urlInput: String,
    onUrlChange: (String) -> Unit,
    isSecure: Boolean,
    onNavigate: () -> Unit,
    onClear: () -> Unit
) {
    BasicTextField(
        value = urlInput,
        onValueChange = onUrlChange,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
        keyboardActions = KeyboardActions(onGo = { onNavigate() }),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxSize(),
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

                // Text Field
                Box(Modifier.weight(1f)) {
                    if (urlInput.isEmpty()) {
                        Text(
                            "Search or enter URL",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }

                // Clear Button
                if (urlInput.isNotEmpty()) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Clear",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun AddressBarDisplayMode(
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
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
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
        java.net.URI(url).host ?: url
    } catch (e: Exception) {
        url
    }
}