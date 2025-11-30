package com.octane.browser.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.octane.browser.design.BrowserDimens
import com.octane.browser.domain.models.QuickAccessLink
import com.octane.browser.presentation.components.HomeAddressBar
import com.octane.browser.presentation.viewmodels.BrowserViewModel

@Composable
fun BrowserHomeScreen(
    onOpenUrl: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onNewTabAndGoHome: () -> Unit,
    onNavigateToTabs: () -> Unit,
    browserViewModel: BrowserViewModel
) {
    val quickAccessLinks = remember {
        mutableStateListOf(
            QuickAccessLink(1, "https://google.com", "Google", null),
            QuickAccessLink(2, "https://github.com", "GitHub", null),
            QuickAccessLink(3, "https://youtube.com", "YouTube", null),
            QuickAccessLink(4, "https://twitter.com", "Twitter", null)
        )
    }
    val webViewState by browserViewModel.webViewState.collectAsState()
    val tabs by browserViewModel.tabs.collectAsState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(BrowserDimens.BrowserPaddingScreenEdge),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar (replaces menu)
                Surface(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Address Bar
                HomeAddressBar(
                    webViewState = webViewState,
                    onNavigate = onOpenUrl,
                    onNewTab = onNewTabAndGoHome,
                    modifier = Modifier.weight(1f)
                )

                // Tab Counter
                TabCounterButton(
                    tabCount = tabs.size,
                    onClick = onNavigateToTabs
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Quick Actions
            QuickActionsRow(
                onBookmarks = onOpenBookmarks,
                onHistory = onOpenHistory,
                onTabs = onNavigateToTabs,
                modifier = Modifier.padding(horizontal = BrowserDimens.BrowserPaddingScreenEdge)
            )

            Spacer(Modifier.height(24.dp))

            // Quick Access Title
            Text(
                text = "Quick Access",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = BrowserDimens.BrowserPaddingScreenEdge)
            )

            Spacer(Modifier.height(16.dp))

            // Quick Access Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(horizontal = BrowserDimens.BrowserPaddingScreenEdge),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(quickAccessLinks, key = { it.id }) { link ->
                    QuickAccessItem(link = link, onClick = onOpenUrl)
                }

                // Add button
                item {
                    QuickAccessAddButton(onAdd = {
                        quickAccessLinks.add(
                            QuickAccessLink(
                                id = quickAccessLinks.size + 1,
                                url = "https://example.com/new",
                                title = "New Link",
                                favicon = null
                            )
                        )
                    })
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onBookmarks: () -> Unit,
    onHistory: () -> Unit,
    onTabs: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            icon = Icons.Rounded.Star,
            label = "Bookmarks",
            onClick = onBookmarks,
            modifier = Modifier.weight(1f)
        )

        QuickActionCard(
            icon = Icons.Rounded.History,
            label = "History",
            onClick = onHistory,
            modifier = Modifier.weight(1f)
        )

        QuickActionCard(
            icon = Icons.Rounded.Tab,
            label = "Tabs",
            onClick = onTabs,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(BrowserDimens.BrowserShapeRoundedMedium),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = BrowserDimens.BrowserElevationLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TabCounterButton(
    tabCount: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "$tabCount",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun QuickAccessItem(link: QuickAccessLink, onClick: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick(link.url) }
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (link.favicon != null) {
                    Image(
                        bitmap = link.favicon.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        Icons.Rounded.Public,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = link.title,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun QuickAccessAddButton(onAdd: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .size(64.dp)
                .clickable(onClick = onAdd),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "Add Quick Access",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Add",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}