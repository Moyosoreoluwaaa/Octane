package com.octane.browser.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.octane.browser.domain.models.WebViewState

@Composable
fun NavigationControls(
    webViewState: WebViewState,
    tabCount: Int,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onHome: () -> Unit,
    onTabManager: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(
                onClick = onBack,
                enabled = webViewState.canGoBack
            ) {
                Icon(Icons.Default.ArrowBack, "Back")
            }

            // Forward Button
            IconButton(
                onClick = onForward,
                enabled = webViewState.canGoForward
            ) {
                Icon(Icons.Default.ArrowForward, "Forward")
            }

            // Home Button
            IconButton(onClick = onHome) {
                Icon(Icons.Default.Home, "Home")
            }

            // Tab Manager Button
            BadgedBox(
                badge = {
                    if (tabCount > 1) {
                        Badge {
                            Text("$tabCount")
                        }
                    }
                }
            ) {
                IconButton(onClick = onTabManager) {
                    Icon(Icons.Default.Tab, "Tabs")
                }
            }
        }
    }
}