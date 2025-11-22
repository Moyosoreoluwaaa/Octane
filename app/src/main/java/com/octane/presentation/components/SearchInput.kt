package com.octane.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import com.octane.presentation.theme.AppColors
import com.octane.presentation.theme.AppTypography
import com.octane.presentation.theme.Dimensions
import com.octane.presentation.utils.metallicBorder

/**
 * Search input field with icon and clear button.
 */
@Composable
fun SearchInput(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = AppTypography.bodyMedium.copy(color = AppColors.TextPrimary),
        cursorBrush = SolidColor(AppColors.TextPrimary),
        decorationBox = { innerTextField ->
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .height(Dimensions.Input.heightCompact)
                    .clip(RoundedCornerShape(Dimensions.CornerRadius.pill))
                    .background(AppColors.SurfaceHighlight)
                    .metallicBorder(
                        Dimensions.Border.standard,
                        RoundedCornerShape(Dimensions.CornerRadius.pill),
                        angleDeg = 170f
                    )
                    .padding(horizontal = Dimensions.Padding.standard),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
            ) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = null,
                    tint = AppColors.TextSecondary,
                    modifier = Modifier.size(Dimensions.IconSize.medium)
                )
                
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            placeholder,
                            style = AppTypography.bodyMedium,
                            color = AppColors.TextTertiary
                        )
                    }
                    innerTextField()
                }
                
                if (query.isNotEmpty()) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Clear",
                        tint = AppColors.TextSecondary,
                        modifier = Modifier
                            .size(Dimensions.IconSize.medium)
                            .clickable { onQueryChange("") }
                    )
                }
            }
        }
    )
}