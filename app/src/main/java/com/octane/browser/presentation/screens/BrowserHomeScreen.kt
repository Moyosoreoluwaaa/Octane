package com.octane.browser.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.octane.browser.design.BrowserDimens
import com.octane.browser.domain.models.QuickAccessLink
import com.octane.browser.presentation.components.BrowserMenu
import com.octane.browser.presentation.components.HomeAddressBar
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun BrowserHomeScreen(
    onOpenUrl: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onNewTabAndGoHome: () -> Unit,
    browserViewModel: BrowserViewModel
) {
    // Dummy state for Quick Access links
    val quickAccessLinks = remember {
        mutableStateListOf(
            QuickAccessLink(1, "https://google.com", "Google", null),
            QuickAccessLink(2, "https://github.com", "GitHub", null),
            QuickAccessLink(3, "https://compose.com", "Compose Docs", null),
            QuickAccessLink(4, "https://octane.com", "Octane Home", null)
        )
    }
    val webViewState by browserViewModel.webViewState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // Reusing AddressBar's aesthetic for a search/navigation input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(BrowserDimens.BrowserPaddingScreenEdge),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dummy WebViewState for AddressBar input context
                val dummyState = com.octane.browser.domain.models.WebViewState(
                    url = "about:home",
                    title = "Home",
                    isLoading = false,
                    canGoBack = false,
                    canGoForward = false,
                    isSecure = true
                )

                HomeAddressBar(
                    webViewState = webViewState,
                    onNavigate = onOpenUrl,
                    onOpenMenu = { showMenu = true }, // Use settings icon as menu for simplicity here
                    onNewTab = onNewTabAndGoHome, // Wire up the new callback
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Icon(
                        Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "Quick Access",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(
                    horizontal = BrowserDimens.BrowserPaddingScreenEdge,
                    vertical = 16.dp
                )
            )

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
                        // In a real app, this would open a dialog to enter URL/Title
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

        if (showMenu) {
            BrowserMenu(
                onDismiss = { showMenu = false },
                onBookmarks = { showMenu = false; onOpenBookmarks() },
                onHistory = { showMenu = false; onOpenHistory() },
                onSettings = { showMenu = false; onOpenSettings() },
                onNewTab = { showMenu = false; browserViewModel.createNewTab() },
                onShare = { showMenu = false },
                onRefresh = { showMenu = false; browserViewModel.reload() }
            )
        }
    }
}

@Composable
fun QuickAccessItem(link: QuickAccessLink, onClick: (String) -> Unit) {
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
fun QuickAccessAddButton(onAdd: () -> Unit) {
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