package com.octane.browser.design

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.octane.browser.domain.managers.ThemeManager
import com.octane.browser.domain.models.Theme
import org.koin.compose.koinInject

/**
 * Main theme wrapper for Octane Browser.
 *
 * Features:
 * - Light/Dark/System theme
 * - Material You dynamic colors (Android 12+)
 * - Custom purple brand colors (fallback)
 * - Reactive (updates instantly when settings change)
 *
 * Usage:
 * ```
 * @Composable
 * fun App() {
 *     BrowserTheme {
 *         // Your app content
 *     }
 * }
 * ```
 */
@Composable
fun BrowserTheme(
    content: @Composable () -> Unit
) {
    // Inject ThemeManager (observes DataStore)
    val themeManager: ThemeManager = koinInject()

    // Observe theme state reactively
    val userTheme by themeManager.currentTheme.collectAsState()
    val useDynamicColors by themeManager.useDynamicColors.collectAsState()

    // Check system dark mode
    val systemInDarkTheme = isSystemInDarkTheme()

    // Determine if dark mode should be active
    val darkTheme = when (userTheme) {
        Theme.LIGHT -> false
        Theme.DARK -> true
        Theme.SYSTEM -> systemInDarkTheme
    }

    // Select color scheme
    val colorScheme = when {
        // Material You Dynamic Colors (Android 12+)
        useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        // Custom Purple Theme (Dark)
        darkTheme -> darkColorScheme(
            // Primary (Accent Purple)
            primary = Color(0xFF9D6BFF),           // Light purple for dark mode
            onPrimary = Color(0xFF1E1E1E),         // Dark text on purple
            primaryContainer = Color(0xFF6200EE),  // Container for purple
            onPrimaryContainer = Color(0xFFE0E0E0), // Light text on purple container

            // Secondary (Variant Purple)
            secondary = Color(0xFF7A4FCC),
            onSecondary = Color(0xFF1E1E1E),
            secondaryContainer = Color(0xFF3700B3),
            onSecondaryContainer = Color(0xFFE0E0E0),

            // Tertiary (Complementary - Teal for contrast)
            tertiary = Color(0xFF03DAC6),
            onTertiary = Color(0xFF1E1E1E),
            tertiaryContainer = Color(0xFF018786),
            onTertiaryContainer = Color(0xFFE0E0E0),

            // Background (Pure dark)
            background = Color(0xFF121212),
            onBackground = Color(0xFFE0E0E0),

            // Surface (Elevated from background)
            surface = Color(0xFF1E1E1E),
            onSurface = Color(0xFFE0E0E0),
            surfaceVariant = Color(0xFF2C2C2C),
            onSurfaceVariant = Color(0xFFB0B0B0),

            // Surface Tints (for elevation)
            surfaceTint = Color(0xFF9D6BFF),

            // Inverse (for high contrast elements)
            inverseSurface = Color(0xFFE0E0E0),
            inverseOnSurface = Color(0xFF121212),
            inversePrimary = Color(0xFF6200EE),

            // Outline (borders, dividers)
            outline = Color(0xFF808080),
            outlineVariant = Color(0xFF4A4A4A),

            // Scrim (overlays)
            scrim = Color.Black,

            // Error
            error = Color(0xFFF44336),
            onError = Color.White,
            errorContainer = Color(0xFFD32F2F),
            onErrorContainer = Color(0xFFFFCDD2)
        )

        // Custom Purple Theme (Light)
        else -> lightColorScheme(
            // Primary (Accent Purple)
            primary = Color(0xFF6200EE),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFBB86FC),
            onPrimaryContainer = Color(0xFF3700B3),

            // Secondary
            secondary = Color(0xFF03DAC6),
            onSecondary = Color.Black,
            secondaryContainer = Color(0xFF018786),
            onSecondaryContainer = Color.White,

            // Tertiary
            tertiary = Color(0xFF3700B3),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFBB86FC),
            onTertiaryContainer = Color(0xFF6200EE),

            // Background
            background = Color(0xFFF5F5F5),
            onBackground = Color.Black,

            // Surface
            surface = Color.White,
            onSurface = Color.Black,
            surfaceVariant = Color(0xFFFAFAFA),
            onSurfaceVariant = Color(0xFF757575),

            // Surface Tints
            surfaceTint = Color(0xFF6200EE),

            // Inverse
            inverseSurface = Color(0xFF1E1E1E),
            inverseOnSurface = Color.White,
            inversePrimary = Color(0xFF9D6BFF),

            // Outline
            outline = Color(0xFFBDBDBD),
            outlineVariant = Color(0xFFE0E0E0),

            // Scrim
            scrim = Color.Black,

            // Error
            error = Color(0xFFF44336),
            onError = Color.White,
            errorContainer = Color(0xFFFFCDD2),
            onErrorContainer = Color(0xFFD32F2F)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = createBrowserTypography(),
        shapes = createBrowserShapes(),
        content = content
    )
}

/**
 * Convert custom typography to Material Typography.
 */
private fun createBrowserTypography(): Typography {
    return Typography(
        // Body styles
        bodyLarge = BrowserTypography.BrowserFontBodyLarge,
        bodyMedium = BrowserTypography.BrowserFontBodyMedium,
        bodySmall = BrowserTypography.BrowserFontBodySmall,

        // Label styles
        labelLarge = BrowserTypography.BrowserFontLabelLarge,
        labelMedium = BrowserTypography.BrowserFontLabelMedium,
        labelSmall = BrowserTypography.BrowserFontLabelNav,

        // Headline styles
        headlineSmall = BrowserTypography.BrowserFontHeadlineSmall,
        headlineMedium = BrowserTypography.BrowserFontHeadlineMedium
    )
}

/**
 * Material Shapes matching browser design.
 */
private fun createBrowserShapes(): Shapes {
    return Shapes(
        extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(
            BrowserDimens.BrowserShapeRoundedSmall
        ),
        small = androidx.compose.foundation.shape.RoundedCornerShape(
            BrowserDimens.BrowserShapeRoundedSmall
        ),
        medium = androidx.compose.foundation.shape.RoundedCornerShape(
            BrowserDimens.BrowserShapeRoundedMedium
        ),
        large = androidx.compose.foundation.shape.RoundedCornerShape(
            BrowserDimens.BrowserShapeRoundedLarge
        ),
        extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(
            BrowserDimens.BrowserShapeRoundedLarge
        )
    )
}