package com.octane.browser.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.octane.browser.design.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationBottomSheet(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    isDestructive: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BrowserColors.BrowserColorPrimarySurface,
        shape = RoundedCornerShape(
            topStart = BrowserDimens.BrowserShapeRoundedMedium,
            topEnd = BrowserDimens.BrowserShapeRoundedMedium
        ),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = BrowserDimens.BrowserSpacingMedium)
                    .width(32.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BrowserColors.BrowserColorTertiaryText)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BrowserDimens.BrowserSpacingXLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Warning Icon (if destructive)
            if (isDestructive) {
                Icon(
                    Icons.Rounded.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(BrowserDimens.BrowserSizeIconXLarge),
                    tint = BrowserColors.BrowserColorError
                )

                Spacer(modifier = Modifier.height(BrowserDimens.BrowserSpacingLarge))
            }

            // Title
            Text(
                text = title,
                style = BrowserTypography.BrowserFontHeadlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = BrowserColors.BrowserColorPrimaryText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(BrowserDimens.BrowserSpacingMedium))

            // Message
            Text(
                text = message,
                style = BrowserTypography.BrowserFontBodyMedium,
                color = BrowserColors.BrowserColorSecondaryText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(BrowserDimens.BrowserSpacingXLarge))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(BrowserDimens.BrowserSpacingMedium)
            ) {
                // Cancel Button
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(BrowserDimens.BrowserShapeRoundedMedium),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = BrowserColors.BrowserColorPrimaryText
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.5.dp
                    )
                ) {
                    Text(
                        dismissText,
                        style = BrowserTypography.BrowserFontLabelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                // Confirm Button
                Button(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(BrowserDimens.BrowserShapeRoundedMedium),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDestructive)
                            BrowserColors.BrowserColorError
                        else
                            BrowserColors.BrowserColorAccent,
                        contentColor = BrowserColors.BrowserColorPrimarySurface
                    )
                ) {
                    Text(
                        confirmText,
                        style = BrowserTypography.BrowserFontLabelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(BrowserDimens.BrowserSpacingMedium))
        }
    }
}