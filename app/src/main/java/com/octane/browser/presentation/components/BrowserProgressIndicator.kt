package com.octane.browser.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun BrowserProgressIndicator(
    progress: Int,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    if (isVisible && progress < 100) {
        LinearProgressIndicator(
        progress = { progress / 100f },
        modifier = modifier.fillMaxWidth(),
        color = Color.DarkGray,
        trackColor = Color.LightGray,
        )
    }
}