package com.octane.browser.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.octane.browser.design.BrowserColors
import com.octane.browser.design.BrowserDimens
import com.octane.browser.design.BrowserTypography
import com.octane.browser.domain.models.WebViewState

@Composable
fun NavigationControls(
    webViewState: WebViewState,
    tabCount: Int,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onHome: () -> Unit,
    onTabManager: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(BrowserDimens.BrowserShapeRoundedLarge),
        color = BrowserColors.BrowserColorPrimarySurface,
        shadowElevation = BrowserDimens.BrowserElevationHigh
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 1. BACK BUTTON (with disabled state)
            IconButton(
                onClick = onBack,
                enabled = webViewState.canGoBack
            ) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = if (webViewState.canGoBack)
                        BrowserColors.BrowserColorPrimaryText
                    else BrowserColors.BrowserColorSecondaryText.copy(alpha = 0.3f)
                )
            }

            // 2. FORWARD BUTTON (with disabled state)
            IconButton(
                onClick = onForward,
                enabled = webViewState.canGoForward
            ) {
                Icon(
                    Icons.Rounded.ArrowForward,
                    contentDescription = "Forward",
                    tint = if (webViewState.canGoForward)
                        BrowserColors.BrowserColorPrimaryText
                    else BrowserColors.BrowserColorSecondaryText.copy(alpha = 0.3f)
                )
            }

            // 3. CENTER HOME BUTTON (Prominent Circle)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(BrowserColors.BrowserColorAccent)
                    .clickable { onHome() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Home,
                    contentDescription = "Home",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 4. TAB COUNTER (Bordered Box)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(BrowserDimens.BrowserShapeRoundedSmall))
                    .border(
                        2.dp,
                        BrowserColors.BrowserColorPrimaryText,
                        RoundedCornerShape(BrowserDimens.BrowserShapeRoundedSmall)
                    )
                    .clickable { onTabManager() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$tabCount",
                    style = BrowserTypography.BrowserFontLabelNav.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = BrowserColors.BrowserColorPrimaryText
                )
            }

            // 5. MENU/OPTIONS BUTTON
            IconButton(
                onClick = { /* Menu action - handle in parent */ }
            ) {
                Icon(
                    Icons.Rounded.MoreVert,
                    contentDescription = "Menu",
                    tint = BrowserColors.BrowserColorPrimaryText
                )
            }
        }
    }
}