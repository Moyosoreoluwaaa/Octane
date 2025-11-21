package com.octane

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.octane.presentation.components.OctaneApp
import com.octane.ui.theme.OctaneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OctaneTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    OctaneApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
