package com.octane.browser.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Color tokens for the Browser floating design system
 */
object BrowserColors {
    // Background Colors
    val BrowserColorPrimaryBackground = Color(0xFFF5F5F5) // Light gray background
    
    // Surface Colors
    val BrowserColorPrimarySurface = Color.White
    val BrowserColorSecondarySurface = Color(0xFFFAFAFA)
    
    // Text Colors
    val BrowserColorPrimaryText = Color.Black
    val BrowserColorSecondaryText = Color(0xFF757575) // Gray
    val BrowserColorTertiaryText = Color(0xFFBDBDBD) // Light gray
    
    // Accent & Action Colors
    val BrowserColorAccent = Color(0xFF6200EE) // Purple - primary action
    val BrowserColorAccentVariant = Color(0xFF3700B3) // Dark purple
    
    // Status Colors
    val BrowserColorSuccess = Color(0xFF4CAF50) // Green - connected/secure
    val BrowserColorWarning = Color(0xFFFF9800) // Orange - warning
    val BrowserColorError = Color(0xFFF44336) // Red - error/danger
    val BrowserColorInfo = Color(0xFF2196F3) // Blue - information
    
    // Overlay Colors
    val BrowserColorOverlay = Color.Black.copy(alpha = 0.5f)
    val BrowserColorProgressOverlay = Color.Black.copy(alpha = 0.1f)
}

/**
 * Dimension tokens for spacing, sizing, and shapes
 */
object BrowserDimens {
    // Spacing
    val BrowserSpacingUnit = 8.dp // Base unit (8dp grid)
    val BrowserSpacingSmall = 4.dp
    val BrowserSpacingMedium = 12.dp
    val BrowserSpacingLarge = 16.dp
    val BrowserSpacingXLarge = 24.dp
    
    // Screen Edge Padding
    val BrowserPaddingScreenEdge = 16.dp
    val BrowserPaddingBarBottom = 24.dp
    
    // Component Sizes
    val BrowserSizeIconButton = 40.dp
    val BrowserSizeIconSmall = 14.dp
    val BrowserSizeIconMedium = 16.dp
    val BrowserSizeIconLarge = 24.dp
    val BrowserSizeIconXLarge = 48.dp
    
    val BrowserSizeAddressBarHeight = 44.dp
    val BrowserSizeNavBarHeight = 64.dp
    val BrowserSizeTabCounter = 32.dp
    val BrowserSizeBadge = 8.dp
    
    // Border Widths
    val BrowserBorderThin = 1.dp
    val BrowserBorderMedium = 2.dp
    
    // Corner Radius (Shapes)
    val BrowserShapeRoundedSmall = 6.dp
    val BrowserShapeRoundedMedium = 24.dp
    val BrowserShapeRoundedLarge = 32.dp
    val BrowserShapeCircle = 50.dp // Effectively circular
    
    // Elevation
    val BrowserElevationLow = 2.dp
    val BrowserElevationMedium = 4.dp
    val BrowserElevationHigh = 8.dp
}

/**
 * Typography tokens for text styles
 */
object BrowserTypography {
    // Body Text
    val BrowserFontBodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp
    )
    
    val BrowserFontBodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp
    )
    
    val BrowserFontBodySmall = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp
    )
    
    // Labels
    val BrowserFontLabelLarge = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp
    )
    
    val BrowserFontLabelMedium = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp
    )
    
    val BrowserFontLabelNav = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp
    )
    
    // Headlines
    val BrowserFontHeadlineSmall = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 24.sp
    )
    
    val BrowserFontHeadlineMedium = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 28.sp
    )
}

/**
 * Animation duration tokens
 */
object BrowserAnimations {
    const val BrowserAnimationDurationFast = 150 // ms
    const val BrowserAnimationDurationMedium = 300 // ms
    const val BrowserAnimationDurationSlow = 500 // ms
}

/**
 * Opacity/Alpha tokens
 */
object BrowserOpacity {
    const val BrowserOpacitySurfaceHigh = 0.95f
    const val BrowserOpacitySurfaceMedium = 0.9f
    const val BrowserOpacityDisabled = 0.3f
    const val BrowserOpacityProgress = 0.1f
}