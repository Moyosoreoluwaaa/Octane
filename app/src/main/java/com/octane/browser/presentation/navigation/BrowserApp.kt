package com.octane.browser.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController

/**
 * Main entry point for the browser feature
 * Call this from MainActivity to test the browser
 */
@Composable
fun BrowserApp(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    Scaffold(
        modifier = modifier
    ) { paddingValues ->
        BrowserNavGraph(
            navController = navController,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        )
    }
}