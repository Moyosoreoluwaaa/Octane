package com.octane.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.octane.presentation.theme.*
import com.octane.presentation.utils.metallicBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWalletBottomSheet(
    onDismiss: () -> Unit,
    onCreateWallet: (name: String, emoji: String?, color: String?) -> Unit
) {
    var walletName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("🔥") }
    var selectedColor by remember { mutableStateOf("#4ECDC4") }
    var isCreating by remember { mutableStateOf(false) }
    
    val availableEmojis = remember {
        listOf("🔥", "⚡", "💎", "🚀", "🌟", "🎯", "💰", "🏆", "👑", "🎨", "🌈", "⭐")
    }
    
    val availableColors = remember {
        listOf(
            "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A",
            "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E2",
            "#E74C3C", "#3498DB", "#2ECC71", "#F39C12"
        )
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Background,
        contentColor = AppColors.TextPrimary,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.Padding.extraLarge)
                .padding(bottom = Dimensions.Padding.large),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.large)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Create New Wallet",
                    style = AppTypography.headlineSmall,
                    color = AppColors.TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = AppColors.TextSecondary
                    )
                }
            }
            
            // Preview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimensions.CornerRadius.large))
                    .background(AppColors.Surface)
                    .metallicBorder(
                        Dimensions.Border.standard,
                        RoundedCornerShape(Dimensions.CornerRadius.large),
                        angleDeg = 135f
                    )
                    .padding(Dimensions.Padding.large),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
                ) {
                    // Icon Preview
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(selectedColor))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            selectedEmoji,
                            style = AppTypography.titleLarge
                        )
                    }
                    
                    // Name Preview
                    Column {
                        Text(
                            if (walletName.isBlank()) "My Wallet" else walletName,
                            style = AppTypography.titleLarge,
                            color = if (walletName.isBlank()) 
                                AppColors.TextTertiary else AppColors.TextPrimary
                        )
                        Text(
                            "Solana Wallet",
                            style = AppTypography.bodyMedium,
                            color = AppColors.TextSecondary
                        )
                    }
                }
            }
            
            // Wallet Name Input
            Column(verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)) {
                Text(
                    "Wallet Name",
                    style = AppTypography.labelLarge,
                    color = AppColors.TextSecondary
                )
                OutlinedTextField(
                    value = walletName,
                    onValueChange = { walletName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter wallet name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Success,
                        unfocusedBorderColor = AppColors.BorderDefault,
                        focusedTextColor = AppColors.TextPrimary,
                        unfocusedTextColor = AppColors.TextPrimary
                    )
                )
            }
            
            // Emoji Selection
            Column(verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)) {
                Text(
                    "Choose Icon",
                    style = AppTypography.labelLarge,
                    color = AppColors.TextSecondary
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
                    modifier = Modifier.height(80.dp)
                ) {
                    items(availableEmojis) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (emoji == selectedEmoji) 
                                        AppColors.SurfaceHighlight 
                                    else AppColors.Surface
                                )
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                emoji,
                                style = AppTypography.titleMedium
                            )
                        }
                    }
                }
            }
            
            // Color Selection
            Column(verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)) {
                Text(
                    "Choose Color",
                    style = AppTypography.labelLarge,
                    color = AppColors.TextSecondary
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
                    modifier = Modifier.height(80.dp)
                ) {
                    items(availableColors) { color ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(color)))
                                .clickable { selectedColor = color },
                            contentAlignment = Alignment.Center
                        ) {
                            if (color == selectedColor) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(Dimensions.IconSize.large)
                                )
                            }
                        }
                    }
                }
            }
            
            // Create Button
            Button(
                onClick = {
                    if (walletName.isNotBlank()) {
                        isCreating = true
                        onCreateWallet(walletName, selectedEmoji, selectedColor)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.Button.heightLarge),
                enabled = walletName.isNotBlank() && !isCreating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Success,
                    disabledContainerColor = AppColors.SurfaceHighlight
                )
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.IconSize.medium),
                        color = Color.White
                    )
                } else {
                    Text(
                        "Create Wallet",
                        style = AppTypography.labelLarge
                    )
                }
            }
            
            // Info Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimensions.CornerRadius.standard))
                    .background(AppColors.InfoContainer)
                    .padding(Dimensions.Padding.standard)
            ) {
                Text(
                    "💡 You'll be shown your seed phrase after creation. Keep it safe!",
                    style = AppTypography.bodySmall,
                    color = AppColors.Info
                )
            }
        }
    }
}