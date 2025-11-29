package com.octane.browser.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.octane.browser.design.BrowserDimens
import com.octane.browser.domain.models.BrowserTab
import com.octane.browser.presentation.components.EmptyState
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import com.octane.browser.presentation.viewmodels.TabManagerViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TabManagerScreen(
    onBack: () -> Unit,
    browserViewModel: BrowserViewModel = koinViewModel(),
    tabManagerViewModel: TabManagerViewModel = koinViewModel()
) {
    val tabs by browserViewModel.tabs.collectAsState()
    val isGridLayout by tabManagerViewModel.isGridLayout.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp) // Space for floating top bar
        ) {
            if (tabs.isEmpty()) {
                EmptyState(
                    icon = Icons.Rounded.Tab,
                    title = "No Tabs",
                    message = "Create a new tab to start browsing"
                )
            } else {
                if (isGridLayout) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(
                            horizontal = BrowserDimens.BrowserPaddingScreenEdge,
                            vertical = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(tabs, key = { it.id }) { tab ->
                            // ✅ Uses original aesthetic, adapted for Grid
                            TabCardCommon(
                                tab = tab,
                                isGrid = true,
                                onClick = { browserViewModel.switchTab(tab.id); onBack() },
                                onClose = { browserViewModel.closeTab(tab.id) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            horizontal = BrowserDimens.BrowserPaddingScreenEdge,
                            vertical = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(tabs, key = { it.id }) { tab ->
                            // ✅ Uses original aesthetic (List Height 160.dp)
                            TabCardCommon(
                                tab = tab,
                                isGrid = false,
                                onClick = { browserViewModel.switchTab(tab.id); onBack() },
                                onClose = { browserViewModel.closeTab(tab.id) }
                            )
                        }
                    }
                }
            }
        }

        // Top Bar (Restored Floating Design)
        TabManagerTopBar(
            tabCount = tabs.size,
            isGrid = isGridLayout,
            onBack = onBack,
            onToggleLayout = { tabManagerViewModel.toggleLayout() },
            onNewTab = { browserViewModel.createNewTab() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(
                    start = BrowserDimens.BrowserPaddingScreenEdge,
                    end = BrowserDimens.BrowserPaddingScreenEdge,
                    top = 8.dp
                )
        )
    }
}

/**
 * ✅ UNIFIED DESIGN SYSTEM
 * This component handles both List and Grid layouts using your original
 * "Background Image + Gradient Overlay + Text Inside" style.
 */
@Composable
private fun TabCardCommon(
    tab: BrowserTab,
    isGrid: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            // List gets fixed height (160.dp from original), Grid gets Aspect Ratio
            .then(if (isGrid) Modifier.aspectRatio(0.7f) else Modifier.height(160.dp)),
        shape = RoundedCornerShape(16.dp),
        color = if (tab.isActive)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface,
        shadowElevation = if (tab.isActive) 6.dp else 2.dp,
        tonalElevation = if (tab.isActive) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
        ) {
            // 1. Background Screenshot
            if (tab.screenshot != null) {
                Image(
                    bitmap = tab.screenshot.asImageBitmap(),
                    contentDescription = "Tab preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.8f // Slightly more opaque for better visibility
                )
            } else {
                // Placeholder Gradient if no screenshot
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                )
            }

            // 2. Gradient Overlay (Crucial for text readability on images)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), // Top (Darker for text)
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.2f), // Middle (Clear)
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)  // Bottom (Darker for URL)
                            )
                        )
                    )
            )

            // 3. Content (Text & Icons)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Top Row: Favicon, Title, Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Favicon & Title
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (tab.favicon != null) {
                            Image(
                                bitmap = tab.favicon.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                Icons.Rounded.Public,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = tab.title.ifEmpty { "New Tab" },
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.width(4.dp))

                    // Close Button (Small floating circle)
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // Bottom: URL (Only show domain for cleaner look)
                Text(
                    text = tab.url,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 4. Active Indicator
            if (tab.isActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun TabManagerTopBar(
    tabCount: Int,
    isGrid: Boolean,
    onBack: () -> Unit,
    onToggleLayout: () -> Unit,
    onNewTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = BrowserDimens.BrowserElevationMedium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
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

            Text(
                text = "$tabCount Tab${if (tabCount != 1) "s" else ""}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Layout Toggle
                IconButton(onClick = onToggleLayout) {
                    Icon(
                        imageVector = if (isGrid) Icons.Rounded.ViewList else Icons.Rounded.GridView,
                        contentDescription = "Toggle Layout",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(Modifier.width(4.dp))

                // New Tab
                IconButton(
                    onClick = onNewTab,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = "New Tab",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}