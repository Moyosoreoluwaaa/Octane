package com.octane.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.octane.presentation.theme.AppColors
import com.octane.presentation.theme.AppTypography
import com.octane.presentation.theme.Dimensions

@Composable
internal fun EmptyActivityScreen() {
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