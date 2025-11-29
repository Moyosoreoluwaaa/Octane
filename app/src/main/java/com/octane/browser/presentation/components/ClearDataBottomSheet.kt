package com.octane.browser.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.octane.browser.design.BrowserDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClearDataBottomSheet(
    onDismiss: () -> Unit,
    onClear: (clearHistory: Boolean, clearCookies: Boolean, clearCache: Boolean) -> Unit
) {
    var clearHistory by remember { mutableStateOf(true) }
    var clearCookies by remember { mutableStateOf(true) }
    var clearCache by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface, // ✅ CHANGED
        shape = RoundedCornerShape(
            topStart = BrowserDimens.BrowserShapeRoundedMedium,
            topEnd = BrowserDimens.BrowserShapeRoundedMedium
        ),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = BrowserDimens.BrowserSpacingMedium)
                    .width(32.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outline) // ✅ CHANGED
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BrowserDimens.BrowserSpacingXLarge)
        ) {
            // Header
            Text(
                text = "Clear Browsing Data",
                style = MaterialTheme.typography.headlineMedium.copy( // ✅ CHANGED
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface // ✅ CHANGED
            )

            Spacer(modifier = Modifier.height(BrowserDimens.BrowserSpacingSmall))

            Text(
                text = "Select what you want to clear",
                style = MaterialTheme.typography.bodyMedium, // ✅ CHANGED
                color = MaterialTheme.colorScheme.onSurfaceVariant // ✅ CHANGED
            )

            Spacer(modifier = Modifier.height(BrowserDimens.BrowserSpacingXLarge))

            // Checkboxes
            ClearDataOption(
                checked = clearHistory,
                onCheckedChange = { clearHistory = it },
                title = "Browsing History",
                description = "Clear visited pages"
            )

            Spacer(modifier = Modifier.height(BrowserDimens.BrowserSpacingMedium))

            ClearDataOption(
                checked = clearCookies,
                onCheckedChange = { clearCookies = it },
                title = "Cookies & Site Data",
                description = "Clear login sessions"
            )

            Spacer(modifier = Modifier.height(BrowserDimens.BrowserSpacingMedium))

            ClearDataOption(
                checked = clearCache,
                onCheckedChange = { clearCache = it },
                title = "Cached Images & Files",
                description = "Clear downloaded resources"
            )

            Spacer(modifier = Modifier.height(BrowserDimens.BrowserSpacingXLarge))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(BrowserDimens.BrowserSpacingMedium)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = MaterialTheme.shapes.medium, // ✅ CHANGED
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface // ✅ CHANGED
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.5.dp
                    )
                ) {
                    Text(
                        "Cancel",
                        style = MaterialTheme.typography.labelLarge.copy( // ✅ CHANGED
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Button(
                    onClick = {
                        onClear(clearHistory, clearCookies, clearCache)
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = MaterialTheme.shapes.medium, // ✅ CHANGED
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error, // ✅ CHANGED
                        contentColor = MaterialTheme.colorScheme.onError // ✅ CHANGED (was BrowserColorPrimarySurface)
                    )
                ) {
                    Text(
                        "Clear Data",
                        style = MaterialTheme.typography.labelLarge.copy( // ✅ CHANGED
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(BrowserDimens.BrowserSpacingMedium))
        }
    }
}



