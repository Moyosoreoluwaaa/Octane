package com.octane.browser.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
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
import com.octane.browser.presentation.components.AddQuickAccessDialog
import com.octane.browser.presentation.components.DeleteQuickAccessDialog
import com.octane.browser.presentation.components.HomeAddressBar
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import com.octane.browser.presentation.viewmodels.QuickAccessViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun BrowserHomeScreen(
    onOpenUrl: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onNewTabAndGoHome: () -> Unit,
    onNavigateToTabs: () -> Unit,
    browserViewModel: BrowserViewModel,
    quickAccessViewModel: QuickAccessViewModel = koinViewModel()
) {
    val webViewState by browserViewModel.webViewState.collectAsState()
    val tabs by browserViewModel.tabs.collectAsState()
    val quickAccessLinks by quickAccessViewModel.quickAccessLinks.collectAsState()
    val uiState by quickAccessViewModel.uiState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedLink by remember { mutableStateOf<QuickAccessLink?>(null) }
    var linkToDelete by remember { mutableStateOf<QuickAccessLink?>(null) }

    // Initialize default links on first launch
    LaunchedEffect(Unit) {
        quickAccessViewModel.initializeDefaultLinks()
    }

    // Show snackbar for success/error
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is QuickAccessViewModel.QuickAccessUiState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                quickAccessViewModel.resetUiState()
            }
            is QuickAccessViewModel.QuickAccessUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                quickAccessViewModel.resetUiState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(BrowserDimens.BrowserPaddingScreenEdge),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar
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

            // Quick Access Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BrowserDimens.BrowserPaddingScreenEdge),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Access",
                    style = MaterialTheme.typography.titleLarge
                )

                IconButton(
                    onClick = {
                        selectedLink = null
                        showAddDialog = true
                    }
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = "Add Quick Access",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Quick Access Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(horizontal = BrowserDimens.BrowserPaddingScreenEdge),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(quickAccessLinks, key = { it.id }) { link ->
                    QuickAccessItemWithActions(
                        link = link,
                        onClick = { onOpenUrl(link.url) },
                        onEdit = {
                            selectedLink = link
                            showAddDialog = true
                        },
                        onDelete = {
                            linkToDelete = link
                            showDeleteDialog = true
                        }
                    )
                }
            }

            // Empty State
            if (quickAccessLinks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.Star,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No quick access links yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { showAddDialog = true }) {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add Link")
                        }
                    }
                }
            }
        }
    }

    // Add/Edit Dialog
    if (showAddDialog) {
        AddQuickAccessDialog(
            existingLink = selectedLink,
            onDismiss = {
                showAddDialog = false
                selectedLink = null
            },
            onConfirm = { url, title ->
                if (selectedLink != null) {
                    // Update
                    quickAccessViewModel.updateQuickAccess(
                        selectedLink!!.copy(url = url, title = title)
                    )
                } else {
                    // Add
                    quickAccessViewModel.addQuickAccess(url, title)
                }
                showAddDialog = false
                selectedLink = null
            }
        )
    }

    // Delete Confirmation
    if (showDeleteDialog && linkToDelete != null) {
        DeleteQuickAccessDialog(
            linkTitle = linkToDelete!!.title,
            onDismiss = {
                showDeleteDialog = false
                linkToDelete = null
            },
            onConfirm = {
                quickAccessViewModel.deleteQuickAccess(linkToDelete!!.id)
                showDeleteDialog = false
                linkToDelete = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuickAccessItemWithActions(
    link: QuickAccessLink,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
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

        // Context Menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    showMenu = false
                    onEdit()
                },
                leadingIcon = { Icon(Icons.Rounded.Edit, null) }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    showMenu = false
                    onDelete()
                },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Delete,
                        null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
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