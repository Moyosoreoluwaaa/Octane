package com.octane.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.octane.core.util.LoadingState
import com.octane.presentation.components.ManageTokenRow
import com.octane.presentation.components.SearchInput
import com.octane.presentation.theme.*
import com.octane.presentation.viewmodel.ManageTokensViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTokensScreen(
    viewModel: ManageTokensViewModel = koinViewModel(),
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredAssets by viewModel.filteredAssets.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Tokens") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            SearchInput(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                placeholder = "Search tokens...",
                modifier = Modifier.padding(Dimensions.Padding.standard)
            )
            
            // Token List
            when (val state = filteredAssets) {
                is LoadingState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.Primary)
                    }
                }
                
                is LoadingState.Success -> {
                    if (state.data.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Text(
                                "No tokens found",
                                style = AppTypography.bodyMedium,
                                color = AppColors.TextSecondary
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(Dimensions.Padding.standard),
                            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
                        ) {
                            items(state.data) { asset ->
                                ManageTokenRow(
                                    symbol = asset.symbol,
                                    name = asset.name,
                                    balance = "%.4f".format(asset.balance),
                                    isEnabled = !asset.isHidden,
                                    iconColor = Color.DarkGray,
                                    onToggle = { enabled ->
                                        viewModel.onToggleVisibility(asset.id, !enabled)
                                    }
                                )
                            }
                        }
                    }
                }
                
                is LoadingState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
                        ) {
                            Text(
                                "Failed to load tokens",
                                style = AppTypography.bodyMedium,
                                color = AppColors.Error
                            )
                            Text(
                                state.message,
                                style = AppTypography.bodySmall,
                                color = AppColors.TextSecondary
                            )
                        }
                    }
                }
                
                else -> {}
            }
        }
    }
}