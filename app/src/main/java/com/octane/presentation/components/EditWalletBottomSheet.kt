package com.octane.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.octane.domain.models.Wallet
import com.octane.presentation.theme.*
import com.octane.presentation.utils.metallicBorder

/**
 * Edit Wallet Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWalletBottomSheet(
    wallet: Wallet,
    onDismiss: () -> Unit,
    onUpdateWallet: (name: String, emoji: String, color: String) -> Unit
) {
    var walletName by remember { mutableStateOf(wallet.name) }
    var selectedEmoji by remember { mutableStateOf(wallet.iconEmoji) }
    var selectedColor by remember { mutableStateOf(wallet.colorHex) }
    
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
                    "Edit Wallet",
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
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(selectedColor))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(selectedEmoji.toString(), style = AppTypography.titleLarge)
                    }
                    
                    Column {
                        Text(
                            walletName.ifBlank { "Wallet" },
                            style = AppTypography.titleLarge,
                            color = AppColors.TextPrimary
                        )
                        Text(
                            "${wallet.publicKey.take(4)}...${wallet.publicKey.takeLast(4)}",
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    availableEmojis.take(8).forEach { emoji ->
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
                            Text(emoji, style = AppTypography.titleMedium)
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    availableColors.take(8).forEach { color ->
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
                                    Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(Dimensions.IconSize.large)
                                )
                            }
                        }
                    }
                }
            }
            
            // Save Button
            Button(
                onClick = {
                    onUpdateWallet(
                        walletName.ifBlank { "Wallet" },
                        selectedEmoji.toString(),
                        selectedColor.toString()
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.Button.heightLarge),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Success
                )
            ) {
                Icon(
                    Icons.Rounded.Save,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.IconSize.medium)
                )
                Spacer(modifier = Modifier.width(Dimensions.Spacing.small))
                Text("Save Changes", style = AppTypography.labelLarge)
            }
        }
    }
}

/**
 * Delete Confirmation Dialog
 */
@Composable
fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Dimensions.CornerRadius.extraLarge))
                .background(AppColors.Surface)
                .metallicBorder(
                    Dimensions.Border.standard,
                    RoundedCornerShape(Dimensions.CornerRadius.extraLarge),
                    angleDeg = 135f
                )
                .padding(Dimensions.Padding.extraLarge)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.large),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Error Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(AppColors.ErrorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.DeleteForever,
                        contentDescription = null,
                        tint = AppColors.Error,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                // Title
                Text(
                    "Delete Wallet?",
                    style = AppTypography.headlineMedium,
                    color = AppColors.TextPrimary
                )
                
                // Description
                Text(
                    "This action cannot be undone. Make sure you have backed up your recovery phrase.",
                    style = AppTypography.bodyMedium,
                    color = AppColors.TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                // Warning Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimensions.CornerRadius.standard))
                        .background(AppColors.WarningContainer)
                        .padding(Dimensions.Padding.standard)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = AppColors.Warning,
                            modifier = Modifier.size(Dimensions.IconSize.medium)
                        )
                        Text(
                            "All wallet data will be permanently deleted",
                            style = AppTypography.bodySmall,
                            color = AppColors.Warning
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(Dimensions.Spacing.small))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimensions.Button.heightMedium)
                    ) {
                        Text("Cancel", style = AppTypography.labelLarge)
                    }
                    
                    // Delete Button
                    Button(
                        onClick = {
                            onConfirm()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimensions.Button.heightMedium),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Error
                        )
                    ) {
                        Text("Delete", style = AppTypography.labelLarge)
                    }
                }
            }
        }
    }
}