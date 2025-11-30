package com.octane.browser.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.octane.browser.design.BrowserDimens
import com.octane.browser.design.BrowserOpacity
import com.octane.browser.domain.models.BrowserTab
import com.octane.browser.presentation.components.TabPreview
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import com.octane.browser.presentation.viewmodels.TabManagerViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TabManagerScreen(
    onBack: () -> Unit,
    onNewTab: () -> Unit,
    onSelectTab: (String) -> Unit,
    browserViewModel: BrowserViewModel
) {
    val tabs by browserViewModel.tabs.collectAsState()
    val tabManagerViewModel: TabManagerViewModel = koinViewModel()
    val isGridLayout by tabManagerViewModel.isGridLayout.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Content Area
        if (tabs.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 72.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.Language,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No tabs open",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onNewTab) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Open New Tab")
                    }
                }
            }
        } else {
            if (isGridLayout) {
                // Grid Layout
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 72.dp, bottom = 16.dp),
                    contentPadding = PaddingValues(horizontal = BrowserDimens.BrowserPaddingScreenEdge),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tabs, key = { it.id }) { tab ->
                        TabPreviewCard(
                            tab = tab,
                            isActive = tab.isActive,
                            onClick = { onSelectTab(tab.id) },
                            onClose = { browserViewModel.closeTab(tab.id) }
                        )
                    }
                }
            } else {
                // List Layout
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 72.dp, bottom = 16.dp),
                    contentPadding = PaddingValues(horizontal = BrowserDimens.BrowserPaddingScreenEdge),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tabs, key = { it.id }) { tab ->
                        TabPreview(
                            tab = tab,
                            isActive = tab.isActive,
                            onClick = { onSelectTab(tab.id) },
                            onClose = { browserViewModel.closeTab(tab.id) }
                        )
                    }
                }
            }
        }

        // Floating Top Bar
        TabManagerTopBar(
            tabCount = tabs.size,
            isGridLayout = isGridLayout,
            onBack = onBack,
            onToggleLayout = { tabManagerViewModel.toggleLayout() },
            onNewTab = onNewTab,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = BrowserDimens.BrowserPaddingScreenEdge)
        )
    }
}

@Composable
private fun TabManagerTopBar(
    tabCount: Int,
    isGridLayout: Boolean,
    onBack: () -> Unit,
    onToggleLayout: () -> Unit,
    onNewTab: () -> Unit,
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
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
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
                text = "Tabs ($tabCount)",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Layout Toggle
                IconButton(
                    onClick = onToggleLayout,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                ) {
                    Icon(
                        if (isGridLayout) Icons.Rounded.ViewList else Icons.Rounded.GridView,
                        contentDescription = "Toggle Layout",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // New Tab
                IconButton(
                    onClick = onNewTab,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = "New Tab",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun TabPreviewCard(
    tab: BrowserTab,
    isActive: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .aspectRatio(0.7f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(BrowserDimens.BrowserShapeRoundedMedium),
        color = MaterialTheme.colorScheme.surface.copy(
            alpha = BrowserOpacity.BrowserOpacitySurfaceMedium
        ),
        shadowElevation = if (isActive)
            BrowserDimens.BrowserElevationMedium
        else
            BrowserDimens.BrowserElevationLow,
        border = if (isActive)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Screenshot Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (tab.screenshot != null) {
                        Image(
                            bitmap = tab.screenshot.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Rounded.Language,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.Center),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    }
                }

                // Title Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.Language,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isActive)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = tab.title.ifEmpty { "New Tab" },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        color = if (isActive)
                            MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Close Button
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Close",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}