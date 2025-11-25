package com.octane.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Combined Wallet/Activity screen with swipeable tabs.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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

    // ✅ Pager state for swipeable tabs
    val pagerState = rememberPagerState(pageCount = { 2 })

    // ✅ Separate scroll states for each tab
    val activityScrollState = rememberLazyListState()
    val walletsScrollState = rememberLazyListState()

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
        bottomBar = {
            BottomNavBar(
                navController = navController,
                onBackToHome = onBack // ✅ Pass back callback for home navigation
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header with tab selector
            WalletScreenHeader(
                currentPage = pagerState.currentPage,
                pendingCount = pendingCount,
                onBack = onBack,
                onTabClick = { page ->
                    scope.launch { pagerState.animateScrollToPage(page) }
                },
                onFilterClick = { /* Show filter sheet */ },
                onExportClick = { activityViewModel.exportTransactions() }
            )

            // ✅ Swipeable content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> {
                        // Activity Tab
                        when (val state = filteredTransactions) {
                            is LoadingState.Loading -> LoadingScreen()
                            is LoadingState.Success -> {
                                if (state.data.isEmpty()) {
                                    EmptyActivityScreen()
                                } else {
                                    TransactionList(
                                        transactions = state.data,
                                        scrollState = activityScrollState,
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
                    1 -> {
                        // Wallets Tab
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
                                        scrollState = walletsScrollState,
                                        onWalletClick = { walletsViewModel.switchWallet(it.id) },
                                        onEditClick = { walletsViewModel.showEditWallet(it.id) },
                                        onDeleteClick = { walletsViewModel.showDeleteConfirmation(it.id) },
                                        onCreateClick = { walletsViewModel.showCreateWallet() },
                                        onImportClick = { walletsViewModel.showImportWallet() },
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


