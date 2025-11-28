package com.octane.browser.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun SecurityBadge(isSecure: Boolean) {
    Icon(
        imageVector = if (isSecure) Icons.Default.Lock else Icons.Default.Warning,
        contentDescription = if (isSecure) "Secure" else "Not Secure",
        tint = if (isSecure) Color.Green else Color.Red
    )
}