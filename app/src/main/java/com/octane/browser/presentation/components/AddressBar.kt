package com.octane.browser.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.octane.browser.domain.models.WebViewState

@OptIn(ExperimentalMaterial3Api::class)
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
    var urlInput by remember { mutableStateOf(webViewState.url) }
    var isEditing by remember { mutableStateOf(false) }
    
    // Sync with WebView state when not editing
    LaunchedEffect(webViewState.url) {
        if (!isEditing) {
            urlInput = webViewState.url
        }
    }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // URL TextField
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search or enter URL") },
                    leadingIcon = {
                        SecurityBadge(isSecure = webViewState.isSecure)
                    },
                    trailingIcon = {
                        if (urlInput.isNotEmpty()) {
                            IconButton(onClick = { urlInput = "" }) {
                                Icon(Icons.Default.Clear, "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            onNavigate(urlInput)
                            isEditing = false
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                
                // Reload/Stop Button
                IconButton(onClick = {
                    if (webViewState.isLoading) onStop() else onReload()
                }) {
                    Icon(
                        imageVector = if (webViewState.isLoading) 
                            Icons.Default.Close else Icons.Default.Refresh,
                        contentDescription = if (webViewState.isLoading) "Stop" else "Reload"
                    )
                }
                
                // Bookmark Button
                IconButton(onClick = onBookmarkToggle) {
                    Icon(
                        imageVector = if (isBookmarked) 
                            Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) 
                            MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }
                
                // Menu Button
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.MoreVert, "Menu")
                }
            }
            
            // Progress Indicator
            if (webViewState.isLoading) {
                LinearProgressIndicator(
                    progress = webViewState.progress / 100f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

