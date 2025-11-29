package com.octane.browser.presentation.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.octane.browser.design.*
import com.octane.browser.domain.models.Theme
import com.octane.browser.presentation.components.ConfirmationBottomSheet
import com.octane.browser.presentation.components.ThemePickerBottomSheet
import com.octane.browser.presentation.viewmodels.ConnectionViewModel
import com.octane.browser.presentation.viewmodels.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenConnections: () -> Unit = {},
    settingsViewModel: SettingsViewModel = koinViewModel(),
    connectionViewModel: ConnectionViewModel = koinViewModel()
) {

    val settings by settingsViewModel.settings.collectAsState()
    val connections by connectionViewModel.allConnections.collectAsState()

    var showDisconnectAllSheet by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }

    // FLOATING DESIGN: Box with background + floating elements
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // ✅ Material theme
    ) {
        // Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // NEW: Appearance Section
            SettingsSection(title = "Appearance") {
                SettingItem(
                    title = "Theme",
                    description = when (settings.theme) {
                        Theme.LIGHT -> "Light"
                        Theme.DARK -> "Dark"
                        Theme.SYSTEM -> "Follow System"
                    },
                    onClick = { showThemePicker = true }
                )

                // Dynamic Colors (Android 12+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    SettingSwitchItem(
                        title = "Dynamic Colors",
                        description = "Use colors from wallpaper",
                        checked = settings.useDynamicColors,
                        onCheckedChange = {
                            settingsViewModel.updateSettings(
                                settings.copy(useDynamicColors = it)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(BrowserDimens.BrowserSpacingMedium))

            // Privacy Section
            SettingsSection(title = "Privacy") {
                SettingSwitchItem(
                    title = "Save History",
                    description = "Remember visited pages",
                    checked = settings.saveHistory,
                    onCheckedChange = {
                        settingsViewModel.updateSettings(
                            settings.copy(saveHistory = it)
                        )
                    }
                )

                SettingSwitchItem(
                    title = "Clear Data on Exit",
                    description = "Delete cookies, cache, and history when closing",
                    checked = settings.clearDataOnExit,
                    onCheckedChange = {
                        settingsViewModel.updateSettings(
                            settings.copy(clearDataOnExit = it)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(BrowserDimens.BrowserSpacingMedium))

            // Content Section
            SettingsSection(title = "Content") {
                SettingSwitchItem(
                    title = "JavaScript",
                    description = "Enable JavaScript (required for most sites)",
                    checked = settings.enableJavaScript,
                    onCheckedChange = {
                        settingsViewModel.toggleJavaScript(it)
                    }
                )

                SettingSwitchItem(
                    title = "Block Ads",
                    description = "Block ads and trackers",
                    checked = settings.blockAds,
                    onCheckedChange = {
                        settingsViewModel.toggleAdBlocking(it)
                    }
                )
            }

            Spacer(modifier = Modifier.height(BrowserDimens.BrowserSpacingMedium))

            // Security Section
            SettingsSection(title = "Security") {
                SettingSwitchItem(
                    title = "Phishing Protection",
                    description = "Warn about suspicious sites",
                    checked = settings.enablePhishingProtection,
                    onCheckedChange = {
                        settingsViewModel.togglePhishingProtection(it)
                    }
                )
            }

            Spacer(modifier = Modifier.height(BrowserDimens.BrowserSpacingMedium))

            // Web3 Section
            SettingsSection(title = "Web3") {
                SettingSwitchItem(
                    title = "Enable Web3",
                    description = "Connect to dApps and use crypto features",
                    checked = settings.enableWeb3,
                    onCheckedChange = {
                        settingsViewModel.toggleWeb3(it)
                    }
                )

                SettingItem(
                    title = "Connected Sites",
                    description = "${connections.size} sites connected",
                    onClick = onOpenConnections
                )

                if (connections.isNotEmpty()) {
                    SettingItem(
                        title = "Disconnect All Sites",
                        description = "Remove all Web3 connections",
                        onClick = { showDisconnectAllSheet = true },
                        isDestructive = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(BrowserDimens.BrowserSpacingMedium))

            // About Section
            SettingsSection(title = "About") {
                SettingItem(
                    title = "Version",
                    description = "Octane Browser 1.0.0",
                    onClick = { }
                )

                SettingItem(
                    title = "Privacy Policy",
                    description = "Learn how we protect your data",
                    onClick = { /* TODO: Open privacy policy */ }
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // FLOATING TOP BAR
        SettingsTopBar(
            onBack = onBack,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(
                    start = BrowserDimens.BrowserPaddingScreenEdge,
                    end = BrowserDimens.BrowserPaddingScreenEdge
                )
        )
    }

    // Theme Picker Bottom Sheet
    if (showThemePicker) {
        ThemePickerBottomSheet(
            currentTheme = settings.theme,
            onDismiss = { showThemePicker = false },
            onThemeSelected = { theme ->
                settingsViewModel.updateSettings(
                    settings.copy(theme = theme)
                )
                showThemePicker = false
            }
        )
    }

    // Disconnect All Bottom Sheet
    if (showDisconnectAllSheet) {
        ConfirmationBottomSheet(
            title = "Disconnect All Sites?",
            message = "This will disconnect ${connections.size} connected sites. " +
                    "You'll need to reconnect to use Web3 features on these sites.",
            confirmText = "Disconnect",
            isDestructive = true,
            onDismiss = { showDisconnectAllSheet = false },
            onConfirm = {
                connectionViewModel.disconnectAll()
            }
        )
    }
}

@Composable
private fun SettingsTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BrowserDimens.BrowserShapeRoundedMedium),
        color = MaterialTheme.colorScheme.surface.copy(
            alpha = BrowserOpacity.BrowserOpacitySurfaceHigh
        ),
        shadowElevation = BrowserDimens.BrowserElevationMedium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = BrowserDimens.BrowserSpacingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back Button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Title
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Spacer for balance
            Spacer(modifier = Modifier.size(40.dp))
        }
    }
}


@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = BrowserDimens.BrowserPaddingScreenEdge)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = BrowserDimens.BrowserSpacingMedium)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(BrowserDimens.BrowserShapeRoundedMedium),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = BrowserDimens.BrowserElevationLow
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingItem(
    title: String,
    description: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(BrowserDimens.BrowserSpacingMedium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = if (isDestructive)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(BrowserDimens.BrowserSizeIconMedium),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(BrowserDimens.BrowserSpacingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.surface,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}