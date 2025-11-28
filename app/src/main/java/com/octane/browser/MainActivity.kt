package com.octane.browser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.octane.browser.presentation.navigation.BrowserApp
import com.octane.ui.theme.OctaneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OctaneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // ✅ TESTING: Show browser instead of wallet
                    BrowserApp()

                    // 🔄 PRODUCTION: Uncomment this after testing
                    // val navController = rememberNavController()
                    // AppNavHost(
                    //     navController = navController,
                    //     modifier = Modifier
                    // )
                }
            }
        }
    }
}