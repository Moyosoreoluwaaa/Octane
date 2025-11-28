package com.octane.browser.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.octane.browser.design.BrowserColors
import com.octane.browser.design.BrowserDimens
import com.octane.browser.design.BrowserTypography
import com.octane.browser.domain.models.WebViewState

@Composable
fun AddressBar(
    webViewState: WebViewState,
    isBookmarked: Boolean,
    onNavigate: (String) -> Unit,
    onReload: () -> Unit,
    onStop: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // State for URL editing
    var urlInput by remember { mutableStateOf(webViewState.url) }
    var isEditing by remember { mutableStateOf(false) }

    // Sync with WebView state when not editing
    LaunchedEffect(webViewState.url) {
        if (!isEditing) {
            urlInput = webViewState.url
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BrowserDimens.BrowserSpacingUnit)
    ) {
        // LEFT: Menu Button (Circular)
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .size(40.dp)
                .background(
                    BrowserColors.BrowserColorPrimarySurface.copy(alpha = 0.9f),
                    CircleShape
                )
        ) {
            Icon(
                Icons.Rounded.Menu,
                contentDescription = "Menu",
                tint = BrowserColors.BrowserColorPrimaryText
            )
        }

        // CENTER: Address Pill (ALL FUNCTIONALITY INTEGRATED)
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clickable { isEditing = true },
            shape = RoundedCornerShape(BrowserDimens.BrowserShapeRoundedMedium),
            color = BrowserColors.BrowserColorPrimarySurface.copy(alpha = 0.95f),
            shadowElevation = BrowserDimens.BrowserElevationLow
        ) {
            Box {
                // BACKGROUND: Progress Indicator (integrated)
                if (webViewState.isLoading) {
                    LinearProgressIndicator(
                        progress = { webViewState.progress / 100f },
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.1f),
                        color = BrowserColors.BrowserColorAccent,
                        trackColor = Color.Transparent
                    )
                }

                // FOREGROUND: Content
                if (isEditing) {
                    // EDITING MODE: Show TextField
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
                    // DISPLAY MODE: Show Domain + Actions
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

        // RIGHT: Bookmark Button (Circular with state)
        IconButton(
            onClick = onBookmarkToggle,
            modifier = Modifier
                .size(40.dp)
                .background(
                    BrowserColors.BrowserColorPrimarySurface.copy(alpha = 0.9f),
                    CircleShape
                )
        ) {
            Icon(
                if (isBookmarked) Icons.Rounded.Star
                else Icons.Rounded.StarBorder,
                contentDescription = "Bookmark",
                tint = if (isBookmarked)
                    BrowserColors.BrowserColorAccent
                else BrowserColors.BrowserColorPrimaryText
            )
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
        textStyle = BrowserTypography.BrowserFontBodySmall.copy(
            fontWeight = FontWeight.SemiBold,
            color = BrowserColors.BrowserColorPrimaryText,
            textAlign = TextAlign.Start
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
        keyboardActions = KeyboardActions(
            onGo = { onNavigate() }
        ),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lock Icon (leading)
                Icon(
                    if (isSecure) Icons.Rounded.Lock
                    else Icons.Rounded.LockOpen,
                    contentDescription = "Security",
                    modifier = Modifier.size(14.dp),
                    tint = if (isSecure)
                        BrowserColors.BrowserColorPrimaryText
                    else BrowserColors.BrowserColorSecondaryText
                )

                Spacer(Modifier.width(8.dp))

                // Text Field
                Box(Modifier.weight(1f)) {
                    if (urlInput.isEmpty()) {
                        Text(
                            "Search or enter URL",
                            style = BrowserTypography.BrowserFontBodySmall,
                            color = BrowserColors.BrowserColorSecondaryText
                        )
                    }
                    innerTextField()
                }

                // Clear Button (trailing - when editing)
                if (urlInput.isNotEmpty()) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Clear",
                            modifier = Modifier.size(16.dp),
                            tint = BrowserColors.BrowserColorSecondaryText
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
                BrowserColors.BrowserColorPrimaryText
            else BrowserColors.BrowserColorSecondaryText
        )

        Spacer(Modifier.width(8.dp))

        // Domain Text (centered)
        Text(
            text = extractDomain(url),
            style = BrowserTypography.BrowserFontBodySmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = BrowserColors.BrowserColorPrimaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        // Reload/Stop Button (trailing - context-aware)
        IconButton(
            onClick = {
                if (isLoading) onStop()
                else onReload()
            },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                if (isLoading) Icons.Rounded.Close
                else Icons.Rounded.Refresh,
                contentDescription = if (isLoading) "Stop" else "Reload",
                modifier = Modifier.size(16.dp),
                tint = BrowserColors.BrowserColorPrimaryText
            )
        }
    }
}

// Helper function
private fun extractDomain(url: String): String {
    return try {
        java.net.URI(url).host ?: url
    } catch (e: Exception) {
        url
    }
}