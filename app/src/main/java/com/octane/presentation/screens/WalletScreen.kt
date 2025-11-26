package com.octane.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.octane.core.util.LoadingState
import com.octane.domain.models.Wallet
import com.octane.presentation.components.*
import com.octane.presentation.theme.*
import com.octane.presentation.utils.metallicBorder
import com.octane.presentation.viewmodel.WalletEvent
import com.octane.presentation.viewmodel.WalletsViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * ✅ Separated Wallets screen for wallet management only.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletsScreen(
    viewModel: WalletsViewModel = koinViewModel(),
    navController: NavController,
    onBack: () -> Unit,
    onNavigateToSeedPhrase: (String, String, String) -> Unit,
    onNavigateToImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val walletsState by viewModel.walletsState.collectAsState()
    val activeWallet by viewModel.activeWallet.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val scrollState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle wallet events
    LaunchedEffect(Unit) {
        viewModel.walletEvents.collect { event ->
            when (event) {
                is WalletEvent.WalletCreated -> {
                    snackbarHostState.showSnackbar("Wallet created successfully!")
                }

                is WalletEvent.WalletCreatedWithSeed -> {
                    onNavigateToSeedPhrase(
                        event.seedPhrase,
                        event.wallet.toString(),
                        event.iconEmoji.toString()
                    )
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
        topBar = {
            TopAppBar(
                title = { Text("Wallets", style = AppTypography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            "Back",
                            tint = AppColors.TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showCreateWallet() }) {
                        Icon(
                            Icons.Rounded.Add,
                            "Create Wallet",
                            tint = AppColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Background
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                navController = navController,
                onBackToHome = onBack
            )
        }
    ) { innerPadding ->
        when (val state = walletsState) {
            is LoadingState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Success)
                }
            }

            is LoadingState.Success -> {
                if (state.data.isEmpty()) {
                    EmptyWalletsState(
                        onCreateWallet = { viewModel.showCreateWallet() },
                        onImportWallet = { onNavigateToImport() },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                } else {
                    LazyColumn(
                        state = scrollState,
                        modifier = modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentPadding = PaddingValues(Dimensions.Padding.standard),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
                    ) {
                        items(
                            items = state.data,
                            key = { it.id }
                        ) { wallet ->
                            WalletCard(
                                wallet = wallet,
                                isActive = wallet.id == activeWallet?.id,
                                onClick = { viewModel.switchWallet(wallet.id) },
                                onEdit = { viewModel.showEditWallet(wallet.id) },
                                onDelete = { viewModel.showDeleteConfirmation(wallet.id) },
                                getWalletColor = { viewModel.getWalletColor(wallet) },
                                getWalletDisplayName = { viewModel.getWalletDisplayName(wallet) }
                            )
                        }

                        // Add wallet buttons
                        item {
                            Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
                        }

                        item {
                            OutlinedCard(
                                onClick = { viewModel.showCreateWallet() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Dimensions.Padding.large),
                                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.Add,
                                        contentDescription = null,
                                        tint = AppColors.Success
                                    )
                                    Text(
                                        "Create New Wallet",
                                        style = AppTypography.titleMedium,
                                        color = AppColors.TextPrimary
                                    )
                                }
                            }
                        }

                        item {
                            OutlinedCard(
                                onClick = { onNavigateToImport() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Dimensions.Padding.large),
                                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.Download,
                                        contentDescription = null,
                                        tint = AppColors.Success
                                    )
                                    Text(
                                        "Import Existing Wallet",
                                        style = AppTypography.titleMedium,
                                        color = AppColors.TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            is LoadingState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
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

            else -> Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding))
        }
    }

    // Bottom Sheets & Dialogs
    if (uiState.showCreateSheet) {
        CreateWalletBottomSheet(
            onDismiss = { viewModel.hideCreateWallet() },
            onCreateWallet = { name, emoji, color ->
                viewModel.createWallet(name, emoji, color)
            }
        )
    }

    if (uiState.showImportSheet) {
        ImportWalletBottomSheet(
            onDismiss = { viewModel.hideImportWallet() },
            onImportWallet = { seedPhrase, name, emoji, color ->
                viewModel.importWallet(seedPhrase, name, emoji, color)
            }
        )
    }

    if (uiState.showEditSheet && uiState.editingWallet != null) {
        EditWalletBottomSheet(
            wallet = uiState.editingWallet!!,
            onDismiss = { viewModel.hideEditWallet() },
            onUpdateWallet = { name, emoji, color ->
                viewModel.updateWallet(
                    uiState.editingWallet!!.id,
                    name,
                    emoji,
                    color
                )
            }
        )
    }

    if (uiState.showDeleteConfirmation) {
        DeleteConfirmationDialog(
            onDismiss = { viewModel.hideDeleteConfirmation() },
            onConfirm = { viewModel.deleteWallet() }
        )
    }
}

/**
 * Individual wallet card component
 */
@Composable
private fun WalletCard(
    wallet: Wallet,
    isActive: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    getWalletColor: (Wallet) -> androidx.compose.ui.graphics.Color,
    getWalletDisplayName: (Wallet) -> String
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                AppColors.Success.copy(alpha = 0.1f)
            } else {
                AppColors.Surface
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isActive) {
                    Modifier.metallicBorder(
                        width = 2.dp,
                        shape = MaterialTheme.shapes.medium,
                        angleDeg = 135f
                    )
                } else {
                    Modifier
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.Padding.large),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Wallet icon
                Box(
                    modifier = Modifier
                        .size(Dimensions.Avatar.large)
                        .clip(CircleShape)
                        .background(getWalletColor(wallet).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        wallet.iconEmoji ?: wallet.name.take(1).uppercase(),
                        style = AppTypography.headlineSmall
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            getWalletDisplayName(wallet),
                            style = AppTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                        if (isActive) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = AppColors.Success.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "Active",
                                    style = AppTypography.labelSmall,
                                    color = AppColors.Success,
                                    modifier = Modifier.padding(
                                        horizontal = Dimensions.Padding.small,
                                        vertical = 2.dp
                                    )
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
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Rounded.Edit,
                        "Edit",
                        tint = AppColors.TextSecondary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Rounded.Delete,
                        "Delete",
                        tint = AppColors.Error
                    )
                }
            }
        }
    }
}