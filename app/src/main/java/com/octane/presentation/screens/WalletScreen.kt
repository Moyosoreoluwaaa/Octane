package com.octane.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.octane.core.util.LoadingState
import com.octane.domain.models.Transaction
import com.octane.domain.models.TransactionStatus
import com.octane.domain.models.TransactionType
import com.octane.domain.models.Wallet
import com.octane.presentation.components.*
import com.octane.presentation.theme.*
import com.octane.presentation.utils.UiFormatters
import com.octane.presentation.utils.metallicBorder
import com.octane.presentation.viewmodel.ActivityViewModel
import com.octane.presentation.viewmodel.WalletEvent
import com.octane.presentation.viewmodel.WalletsViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Combined Wallet/Activity screen.
 * Shows transaction history AND wallet management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    activityViewModel: ActivityViewModel = koinViewModel(),
    walletsViewModel: WalletsViewModel = koinViewModel(),
    navController: NavController,
    onNavigateToDetails: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Activity ViewModel State
    val filteredTransactions by activityViewModel.filteredTransactions.collectAsState()
    val activityUiState by activityViewModel.uiState.collectAsState()
    val pendingCount by activityViewModel.pendingCount.collectAsState()

    // Wallets ViewModel State
    val walletsState by walletsViewModel.walletsState.collectAsState()
    val activeWallet by walletsViewModel.activeWallet.collectAsState()
    val walletsUiState by walletsViewModel.uiState.collectAsState()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show which mode: "activity" or "wallets"
    var currentMode by remember { mutableStateOf("activity") }

    // Observe wallet events
    LaunchedEffect(Unit) {
        walletsViewModel.walletEvents.collect { event ->
            when (event) {
                is WalletEvent.WalletCreated -> {
                    snackbarHostState.showSnackbar("Wallet created successfully!")
                }
                is WalletEvent.WalletImported -> {
                    snackbarHostState.showSnackbar("Wallet imported successfully!")
                }
                is WalletEvent.WalletDeleted -> {
                    snackbarHostState.showSnackbar("Wallet deleted")
                }
                is WalletEvent.WalletSwitched -> {
                    snackbarHostState.showSnackbar("Wallet switched")
                }
                is WalletEvent.WalletUpdated -> {
                    snackbarHostState.showSnackbar("Wallet updated")
                }
                is WalletEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppColors.Background,
        bottomBar = { BottomNavBar(navController = navController) },
        floatingActionButton = {
            if (currentMode == "wallets") {
                // FABs for wallet management
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium),
                    horizontalAlignment = Alignment.End
                ) {
                    // Import Wallet
                    FloatingActionButton(
                        onClick = { walletsViewModel.showImportWallet() },
                        containerColor = AppColors.Surface,
                        contentColor = AppColors.TextPrimary,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Download,
                            contentDescription = "Import Wallet",
                            modifier = Modifier.size(Dimensions.IconSize.large)
                        )
                    }

                    // Create Wallet
                    FloatingActionButton(
                        onClick = { walletsViewModel.showCreateWallet() },
                        containerColor = AppColors.Success,
                        contentColor = Color.White,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = "Create Wallet",
                            modifier = Modifier.size(Dimensions.IconSize.extraLarge)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header with mode toggle
            WalletScreenHeader(
                currentMode = currentMode,
                pendingCount = pendingCount,
                onBack = onBack,
                onModeChange = { currentMode = it },
                onFilterClick = { /* Show filter sheet */ },
                onExportClick = { activityViewModel.exportTransactions() }
            )

            // Content based on mode
            when (currentMode) {
                "activity" -> {
                    // Transaction Activity View
                    when (val state = filteredTransactions) {
                        is LoadingState.Loading -> LoadingScreen()
                        is LoadingState.Success -> {
                            if (state.data.isEmpty()) {
                                EmptyActivityScreen()
                            } else {
                                TransactionList(
                                    transactions = state.data,
                                    onTransactionClick = { tx ->
                                        activityViewModel.showTransactionDetails(tx)
                                        onNavigateToDetails(tx.txHash)
                                    },
                                    formatTransactionType = activityViewModel::formatTransactionType,
                                    getTransactionIcon = activityViewModel::getTransactionIcon,
                                    getStatusColor = activityViewModel::getStatusColor
                                )
                            }
                        }
                        is LoadingState.Error -> ErrorScreen(
                            message = state.message,
                            onRetry = { /* Retry logic */ }
                        )
                        else -> Box(modifier = Modifier.fillMaxSize())
                    }
                }

                "wallets" -> {
                    // Wallets Management View
                    when (val state = walletsState) {
                        is LoadingState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = AppColors.Success)
                            }
                        }

                        is LoadingState.Success -> {
                            if (state.data.isEmpty()) {
                                EmptyWalletsState(
                                    onCreateWallet = { walletsViewModel.showCreateWallet() },
                                    onImportWallet = { walletsViewModel.showImportWallet() }
                                )
                            } else {
                                WalletsList(
                                    wallets = state.data,
                                    activeWallet = activeWallet,
                                    onWalletClick = { walletsViewModel.switchWallet(it.id) },
                                    onEditClick = { walletsViewModel.showEditWallet(it.id) },
                                    onDeleteClick = { walletsViewModel.showDeleteConfirmation(it.id) },
                                    getWalletColor = { walletsViewModel.getWalletColor(it) },
                                    getWalletDisplayName = { walletsViewModel.getWalletDisplayName(it) }
                                )
                            }
                        }

                        is LoadingState.Error -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(Dimensions.Padding.extraLarge),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Rounded.ErrorOutline,
                                    contentDescription = null,
                                    tint = AppColors.Error,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(Dimensions.Spacing.standard))
                                Text(
                                    "Failed to load wallets",
                                    style = AppTypography.titleMedium,
                                    color = AppColors.TextPrimary
                                )
                                Text(
                                    state.message,
                                    style = AppTypography.bodyMedium,
                                    color = AppColors.TextSecondary
                                )
                            }
                        }

                        else -> Box(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }

    // Bottom Sheets & Dialogs for Wallet Management
    if (walletsUiState.showCreateSheet) {
        CreateWalletBottomSheet(
            onDismiss = { walletsViewModel.hideCreateWallet() },
            onCreateWallet = { name, emoji, color ->
                walletsViewModel.createWallet(name, emoji, color)
            }
        )
    }

    if (walletsUiState.showImportSheet) {
        ImportWalletBottomSheet(
            onDismiss = { walletsViewModel.hideImportWallet() },
            onImportWallet = { seedPhrase, name, emoji, color ->
                walletsViewModel.importWallet(seedPhrase, name, emoji, color)
            }
        )
    }

    if (walletsUiState.showEditSheet && walletsUiState.editingWallet != null) {
        EditWalletBottomSheet(
            wallet = walletsUiState.editingWallet!!,
            onDismiss = { walletsViewModel.hideEditWallet() },
            onUpdateWallet = { name, emoji, color ->
                walletsViewModel.updateWallet(
                    walletsUiState.editingWallet!!.id,
                    name,
                    emoji,
                    color
                )
            }
        )
    }

    if (walletsUiState.showDeleteConfirmation) {
        DeleteConfirmationDialog(
            onDismiss = { walletsViewModel.hideDeleteConfirmation() },
            onConfirm = { walletsViewModel.deleteWallet() }
        )
    }
}

@Composable
private fun WalletScreenHeader(
    currentMode: String,
    pendingCount: Int,
    onBack: () -> Unit,
    onModeChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onExportClick: () -> Unit
) {
    Column {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.Padding.standard),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = AppColors.TextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (currentMode == "activity") "Activity" else "Wallets",
                    style = AppTypography.headlineSmall,
                    color = AppColors.TextPrimary
                )
                if (currentMode == "activity" && pendingCount > 0) {
                    Spacer(modifier = Modifier.width(Dimensions.Spacing.small))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AppColors.Warning)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "$pendingCount",
                            style = AppTypography.labelSmall,
                            color = Color.Black
                        )
                    }
                }
            }

            if (currentMode == "activity") {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)) {
                    IconButton(onClick = onFilterClick) {
                        Icon(
                            Icons.Rounded.FilterList,
                            contentDescription = "Filter",
                            tint = AppColors.TextPrimary
                        )
                    }
                    IconButton(onClick = onExportClick) {
                        Icon(
                            Icons.Rounded.FileDownload,
                            contentDescription = "Export",
                            tint = AppColors.TextPrimary
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp)) // Balance for back button
            }
        }

        // Mode Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.Padding.standard)
                .padding(bottom = Dimensions.Padding.standard),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
        ) {
            ModeToggleButton(
                text = "Activity",
                isSelected = currentMode == "activity",
                onClick = { onModeChange("activity") },
                modifier = Modifier.weight(1f)
            )
            ModeToggleButton(
                text = "Wallets",
                isSelected = currentMode == "wallets",
                onClick = { onModeChange("wallets") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ModeToggleButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(Dimensions.Button.heightMedium)
            .clip(RoundedCornerShape(Dimensions.CornerRadius.large))
            .background(
                if (isSelected) AppColors.SurfaceHighlight else AppColors.Surface
            )
            .metallicBorder(
                if (isSelected) Dimensions.Border.thick else Dimensions.Border.standard,
                RoundedCornerShape(Dimensions.CornerRadius.large),
                angleDeg = 135f
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = AppTypography.labelLarge,
            color = if (isSelected) AppColors.TextPrimary else AppColors.TextSecondary
        )
    }
}

@Composable
private fun WalletsList(
    wallets: List<Wallet>,
    activeWallet: Wallet?,
    onWalletClick: (Wallet) -> Unit,
    onEditClick: (Wallet) -> Unit,
    onDeleteClick: (Wallet) -> Unit,
    getWalletColor: (Wallet) -> Color,
    getWalletDisplayName: (Wallet) -> String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Dimensions.Padding.standard),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
    ) {
        items(wallets) { wallet ->
            WalletCard(
                wallet = wallet,
                isActive = wallet.id == activeWallet?.id,
                walletColor = getWalletColor(wallet),
                displayName = getWalletDisplayName(wallet),
                onClick = { onWalletClick(wallet) },
                onEditClick = { onEditClick(wallet) },
                onDeleteClick = { onDeleteClick(wallet) }
            )
        }
    }
}

@Composable
private fun WalletCard(
    wallet: Wallet,
    isActive: Boolean,
    walletColor: Color,
    displayName: String,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.CornerRadius.large))
            .background(if (isActive) AppColors.SurfaceHighlight else AppColors.Surface)
            .metallicBorder(
                Dimensions.Border.standard,
                RoundedCornerShape(Dimensions.CornerRadius.large),
                angleDeg = 135f
            )
            .clickable(onClick = onClick)
            .padding(Dimensions.Padding.standard),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
    ) {
        // Wallet Icon
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(walletColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                wallet.iconEmoji.toString(),
                style = AppTypography.titleLarge
            )
        }

        // Wallet Info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.extraSmall)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
            ) {
                Text(
                    displayName,
                    style = AppTypography.titleMedium,
                    color = AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Dimensions.CornerRadius.small))
                            .background(AppColors.Success)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "ACTIVE",
                            style = AppTypography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }

            Text(
                "${wallet.publicKey.take(4)}...${wallet.publicKey.takeLast(4)}",
                style = AppTypography.bodySmall,
                color = AppColors.TextSecondary
            )
        }

        // Menu Button
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    Icons.Rounded.MoreVert,
                    contentDescription = "Options",
                    tint = AppColors.TextSecondary
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        showMenu = false
                        onEditClick()
                    },
                    leadingIcon = {
                        Icon(Icons.Rounded.Edit, contentDescription = null)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = AppColors.Error) },
                    onClick = {
                        showMenu = false
                        onDeleteClick()
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = null,
                            tint = AppColors.Error
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyWalletsState(
    onCreateWallet: () -> Unit,
    onImportWallet: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimensions.Padding.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Rounded.AccountBalanceWallet,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = AppColors.TextTertiary
        )

        Spacer(modifier = Modifier.height(Dimensions.Spacing.large))

        Text(
            "No Wallets Yet",
            style = AppTypography.headlineMedium,
            color = AppColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(Dimensions.Spacing.small))

        Text(
            "Create your first wallet or import an existing one",
            style = AppTypography.bodyLarge,
            color = AppColors.TextSecondary
        )

        Spacer(modifier = Modifier.height(Dimensions.Spacing.extraLarge))

        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
        ) {
            Button(
                onClick = onCreateWallet,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Success
                ),
                modifier = Modifier.height(Dimensions.Button.heightLarge)
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.IconSize.medium)
                )
                Spacer(modifier = Modifier.width(Dimensions.Spacing.small))
                Text("Create Wallet", style = AppTypography.labelLarge)
            }

            OutlinedButton(
                onClick = onImportWallet,
                modifier = Modifier.height(Dimensions.Button.heightLarge)
            ) {
                Icon(
                    Icons.Rounded.Download,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.IconSize.medium)
                )
                Spacer(modifier = Modifier.width(Dimensions.Spacing.small))
                Text("Import", style = AppTypography.labelLarge)
            }
        }
    }
}

// Reuse existing components from original file
@Composable
private fun TransactionList(
    transactions: List<Transaction>,
    onTransactionClick: (Transaction) -> Unit,
    formatTransactionType: (TransactionType) -> String,
    getTransactionIcon: (TransactionType) -> String,
    getStatusColor: (TransactionStatus) -> Color
) {
    LazyColumn(
        contentPadding = PaddingValues(Dimensions.Padding.standard),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
    ) {
        items(transactions) { transaction ->
            TransactionRow(
                transaction = transaction,
                onClick = { onTransactionClick(transaction) },
                formatType = formatTransactionType,
                getIcon = getTransactionIcon,
                getStatusColor = getStatusColor
            )
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: Transaction,
    onClick: () -> Unit,
    formatType: (TransactionType) -> String,
    getIcon: (TransactionType) -> String,
    getStatusColor: (TransactionStatus) -> Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Dimensions.Padding.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimensions.Avatar.medium)
                    .clip(CircleShape)
                    .background(AppColors.SurfaceHighlight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    getIcon(transaction.type),
                    style = AppTypography.bodyMedium
                )
            }

            Column {
                Text(
                    formatType(transaction.type),
                    style = AppTypography.titleSmall,
                    color = AppColors.TextPrimary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.extraSmall)
                ) {
                    Text(
                        UiFormatters.formatRelativeTime(transaction.timestamp),
                        style = AppTypography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                    Text("•", style = AppTypography.bodySmall, color = AppColors.TextSecondary)
                    Text(
                        transaction.tokenSymbol,
                        style = AppTypography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${if (transaction.type == TransactionType.SEND) "-" else "+"}${transaction.amount}",
                style = AppTypography.titleSmall,
                color = AppColors.TextPrimary
            )
            StatusBadge(
                status = transaction.status,
                color = getStatusColor(transaction.status)
            )
        }
    }
}

@Composable
private fun StatusBadge(
    status: TransactionStatus,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Dimensions.CornerRadius.large))
            .background(color.copy(alpha = 0.15f))
            .padding(
                horizontal = Dimensions.Padding.small,
                vertical = Dimensions.Padding.tiny
            )
    ) {
        Text(
            text = when (status) {
                TransactionStatus.CONFIRMED -> "Confirmed"
                TransactionStatus.PENDING -> "Pending"
                TransactionStatus.FAILED -> "Failed"
            },
            style = AppTypography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun EmptyActivityScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimensions.Padding.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "No Activity Yet",
            style = AppTypography.headlineSmall,
            color = AppColors.TextPrimary
        )
        Spacer(modifier = Modifier.height(Dimensions.Spacing.small))
        Text(
            "Your transactions will appear here",
            style = AppTypography.bodyMedium,
            color = AppColors.TextSecondary
        )
    }
}
