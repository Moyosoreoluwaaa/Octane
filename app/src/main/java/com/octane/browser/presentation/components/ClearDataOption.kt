package com.octane.browser.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.octane.browser.design.BrowserDimens

@Composable
fun ClearDataOption(
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
