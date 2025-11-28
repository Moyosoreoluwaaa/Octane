package com.octane.browser.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.octane.browser.presentation.components.ConfirmationBottomSheet
import com.octane.browser.presentation.viewmodels.ConnectionViewModel
import com.octane.browser.presentation.viewmodels.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Privacy Section
            SettingsSection(title = "Privacy") {
                SwitchSettingItem(
                    title = "Save History",
                    description = "Remember visited pages",
                    checked = settings.saveHistory,
                    onCheckedChange = {
                        settingsViewModel.updateSettings(
                            settings.copy(saveHistory = it)
                        )
                    }
                )

                SwitchSettingItem(
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

            HorizontalDivider()

            // Content Section
            SettingsSection(title = "Content") {
                SwitchSettingItem(
                    title = "JavaScript",
                    description = "Enable JavaScript (required for most sites)",
                    checked = settings.enableJavaScript,
                    onCheckedChange = {
                        settingsViewModel.toggleJavaScript(it)
                    }
                )

                SwitchSettingItem(
                    title = "Block Ads",
                    description = "Block ads and trackers",
                    checked = settings.blockAds,
                    onCheckedChange = {
                        settingsViewModel.toggleAdBlocking(it)
                    }
                )
            }

            HorizontalDivider()

            // Security Section
            SettingsSection(title = "Security") {
                SwitchSettingItem(
                    title = "Phishing Protection",
                    description = "Warn about suspicious sites",
                    checked = settings.enablePhishingProtection,
                    onCheckedChange = {
                        settingsViewModel.togglePhishingProtection(it)
                    }
                )
            }

            HorizontalDivider()

            // Web3 Section
            SettingsSection(title = "Web3") {
                SwitchSettingItem(
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
                        onClick = { showDisconnectAllSheet = true }
                    )
                }
            }

            HorizontalDivider()

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
        }
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
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )
        content()
    }
}

@Composable
private fun SwitchSettingItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
