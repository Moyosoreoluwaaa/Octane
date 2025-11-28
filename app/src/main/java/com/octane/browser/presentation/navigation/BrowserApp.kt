package com.octane.browser.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.octane.browser.presentation.navigation.BrowserNavGraph

/**
 * Main entry point for the browser feature
 * Call this from MainActivity to test the browser
 */
@Composable
fun BrowserApp(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    BrowserNavGraph(
        navController = navController,
        modifier = modifier.fillMaxSize()
    )
}