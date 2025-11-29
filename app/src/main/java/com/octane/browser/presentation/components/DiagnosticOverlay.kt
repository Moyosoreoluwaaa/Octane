package com.octane.browser.presentation.components

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.octane.browser.utils.WebViewDiagnosticTool
import kotlinx.coroutines.delay

/**
 * ✅ NEW: Real-time diagnostic overlay for debugging WebView issues
 *
 * Shows:
 * - Page load status
 * - Error count
 * - Canvas/WebGL detection
 * - React framework detection
 * - Quick diagnostic actions
 */
@Composable
fun DiagnosticOverlay(
    webView: WebView?,
    isDebug: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isDebug || webView == null) return

    var diagnosticResult by remember { mutableStateOf<WebViewDiagnosticTool.DiagnosticResult?>(null) }
    var isScanning by remember { mutableStateOf(false) }

    // Auto-scan every 5 seconds
    LaunchedEffect(webView) {
        while (true) {
            delay(5000)
            if (!isScanning) {
                isScanning = true
                WebViewDiagnosticTool.runPageDiagnostics(webView) { result ->
                    diagnosticResult = result
                    isScanning = false
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "🔍 DEBUG OVERLAY",
                color = Color.White,
                fontSize = 10.sp,
                style = MaterialTheme.typography.labelSmall
            )

            diagnosticResult?.let { result ->
                DiagnosticStatus(
                    label = "Content",
                    status = if (result.isBlank) "❌ BLANK" else "✅ OK",
                    color = if (result.isBlank) Color.Red else Color.Green
                )

                DiagnosticStatus(
                    label = "Body Visible",
                    status = if (result.bodyVisible) "✅ YES" else "❌ NO",
                    color = if (result.bodyVisible) Color.Green else Color.Red
                )

                DiagnosticStatus(
                    label = "Canvas",
                    status = if (result.hasCanvas) "✅ YES" else "⚠️ NO",
                    color = if (result.hasCanvas) Color.Green else Color.Yellow
                )

                DiagnosticStatus(
                    label = "React",
                    status = if (result.hasReact) "✅ YES" else "⚠️ NO",
                    color = if (result.hasReact) Color.Green else Color.Yellow
                )

                DiagnosticStatus(
                    label = "Errors",
                    status = if (result.hasErrors) "❌ YES" else "✅ NONE",
                    color = if (result.hasErrors) Color.Red else Color.Green
                )
            } ?: Text(
                text = "⏳ Scanning...",
                color = Color.Gray,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Quick actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = {
                        isScanning = true
                        WebViewDiagnosticTool.runPageDiagnostics(webView) { result ->
                            diagnosticResult = result
                            isScanning = false
                        }
                    },
                    modifier = Modifier.height(24.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Scan", fontSize = 8.sp)
                }

                Button(
                    onClick = {
                        WebViewDiagnosticTool.forceRepaint(webView)
                    },
                    modifier = Modifier.height(24.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Repaint", fontSize = 8.sp)
                }
            }
        }
    }
}

@Composable
private fun DiagnosticStatus(
    label: String,
    status: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            color = Color.White,
            fontSize = 9.sp
        )
        Text(
            text = status,
            color = color,
            fontSize = 9.sp
        )
    }
}