package com.octane.browser.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.octane.browser.design.*

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

@Composable
private fun ClearDataOption(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium, // ✅ CHANGED
        color = MaterialTheme.colorScheme.surfaceVariant // ✅ CHANGED
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BrowserDimens.BrowserSpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary, // ✅ CHANGED
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant // ✅ CHANGED
                )
            )

            Spacer(modifier = Modifier.width(BrowserDimens.BrowserSpacingMedium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy( // ✅ CHANGED
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface // ✅ CHANGED
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall, // ✅ CHANGED
                    color = MaterialTheme.colorScheme.onSurfaceVariant // ✅ CHANGED
                )
            }
        }
    }
}