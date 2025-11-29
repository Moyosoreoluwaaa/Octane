package com.octane.browser.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun AddressBarEditMode(
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
            textAlign = TextAlign.Companion.Start
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Companion.Go),
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