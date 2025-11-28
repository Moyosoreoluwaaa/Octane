package com.octane.browser.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserContextMenu(
    onOpenInNewTab: () -> Unit,
    onCopyLink: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            ListItem(
                headlineContent = { Text("Open in new tab") },
                leadingContent = { Icon(Icons.Default.OpenInNew, null) },
                modifier = Modifier.clickable {
                    onOpenInNewTab()
                    onDismiss()
                }
            )

            ListItem(
                headlineContent = { Text("Copy link") },
                leadingContent = { Icon(Icons.Default.ContentCopy, null) },
                modifier = Modifier.clickable {
                    onCopyLink()
                    onDismiss()
                }
            )

            ListItem(
                headlineContent = { Text("Share") },
                leadingContent = { Icon(Icons.Default.Share, null) },
                modifier = Modifier.clickable {
                    onShare()
                    onDismiss()
                }
            )
        }
    }
}
