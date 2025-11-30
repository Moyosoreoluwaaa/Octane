package com.octane.browser.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.octane.browser.design.BrowserDimens
import com.octane.browser.domain.models.QuickAccessLink

/**
 * ✅ Dialog for Adding/Editing Quick Access Links
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuickAccessDialog(
    existingLink: QuickAccessLink? = null,
    onDismiss: () -> Unit,
    onConfirm: (url: String, title: String) -> Unit
) {
    var url by remember { mutableStateOf(existingLink?.url ?: "") }
    var title by remember { mutableStateOf(existingLink?.title ?: "") }
    var urlError by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(BrowserDimens.BrowserShapeRoundedLarge),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = BrowserDimens.BrowserElevationHigh
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (existingLink == null) Icons.Rounded.Add else Icons.Rounded.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (existingLink == null) "Add Quick Access" else "Edit Quick Access",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = false
                    },
                    label = { Text("Title") },
                    placeholder = { Text("e.g., Google") },
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("Title is required") }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // URL Field
                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        urlError = false
                    },
                    label = { Text("URL") },
                    placeholder = { Text("e.g., google.com or https://google.com") },
                    isError = urlError,
                    supportingText = if (urlError) {
                        { Text("URL is required") }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (url.isNotBlank() && title.isNotBlank()) {
                                onConfirm(url, title)
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            when {
                                title.isBlank() -> titleError = true
                                url.isBlank() -> urlError = true
                                else -> onConfirm(url, title)
                            }
                        }
                    ) {
                        Icon(
                            if (existingLink == null) Icons.Rounded.Add else Icons.Rounded.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (existingLink == null) "Add" else "Update")
                    }
                }
            }
        }
    }
}

/**
 * ✅ Confirmation Dialog for Deletion
 */
@Composable
fun DeleteQuickAccessDialog(
    linkTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove Quick Access?") },
        text = { Text("Are you sure you want to remove \"$linkTitle\" from quick access?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Remove")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}