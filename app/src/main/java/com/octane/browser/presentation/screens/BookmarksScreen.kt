package com.octane.browser.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.octane.browser.design.*
import com.octane.browser.presentation.components.BookmarkItem
import com.octane.browser.presentation.components.EmptyState
import com.octane.browser.presentation.viewmodels.BookmarkViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun BookmarksScreen(
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
    bookmarkViewModel: BookmarkViewModel = koinViewModel()
) {
    val filteredBookmarks by bookmarkViewModel.filteredBookmarks.collectAsState()
    val searchQuery by bookmarkViewModel.searchQuery.collectAsState()

    var showSearchBar by remember { mutableStateOf(false) }

    // FLOATING DESIGN: Box with background + floating elements
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrowserColors.BrowserColorPrimaryBackground)
    ) {
        // Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 72.dp) // Space for floating top bar
        ) {
            if (filteredBookmarks.isEmpty()) {
                EmptyState(
                    icon = Icons.Rounded.StarBorder,
                    title = if (searchQuery.isEmpty()) "No Bookmarks" else "No Results",
                    message = if (searchQuery.isEmpty())
                        "Tap the star icon in the browser to bookmark pages"
                    else
                        "No bookmarks match your search"
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = BrowserDimens.BrowserPaddingScreenEdge
                    ),
                    verticalArrangement = Arrangement.spacedBy(BrowserDimens.BrowserSpacingUnit),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredBookmarks, key = { it.id }) { bookmark ->
                        BookmarkItem(
                            bookmark = bookmark,
                            onClick = {
                                onOpenUrl(bookmark.url)
                            },
                            onDelete = {
                                bookmarkViewModel.removeBookmark(bookmark.id)
                            }
                        )
                    }
                }
            }
        }

        // FLOATING TOP BAR (Rounded Pill)
        if (showSearchBar) {
            BookmarkSearchBar(
                query = searchQuery,
                onQueryChange = { bookmarkViewModel.updateSearchQuery(it) },
                onClose = {
                    bookmarkViewModel.updateSearchQuery("")
                    showSearchBar = false
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(
                        top = BrowserDimens.BrowserSpacingMedium,
                        start = BrowserDimens.BrowserPaddingScreenEdge,
                        end = BrowserDimens.BrowserPaddingScreenEdge
                    )
            )
        } else {
            BookmarkTopBar(
                onBack = onBack,
                onSearch = { showSearchBar = true },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(
                        top = BrowserDimens.BrowserSpacingMedium,
                        start = BrowserDimens.BrowserPaddingScreenEdge,
                        end = BrowserDimens.BrowserPaddingScreenEdge
                    )
            )
        }
    }
}

@Composable
private fun BookmarkTopBar(
    onBack: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BrowserDimens.BrowserShapeRoundedMedium),
        color = BrowserColors.BrowserColorPrimarySurface.copy(alpha = BrowserOpacity.BrowserOpacitySurfaceHigh),
        shadowElevation = BrowserDimens.BrowserElevationMedium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = BrowserDimens.BrowserSpacingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back Button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        BrowserColors.BrowserColorPrimarySurface.copy(alpha = 0.9f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = BrowserColors.BrowserColorPrimaryText
                )
            }

            // Title
            Text(
                text = "Bookmarks",
                style = BrowserTypography.BrowserFontHeadlineSmall,
                color = BrowserColors.BrowserColorPrimaryText
            )

            // Search Button
            IconButton(
                onClick = onSearch,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        BrowserColors.BrowserColorPrimarySurface.copy(alpha = 0.9f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = BrowserColors.BrowserColorPrimaryText
                )
            }
        }
    }
}

@Composable
private fun BookmarkSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BrowserDimens.BrowserShapeRoundedMedium),
        color = BrowserColors.BrowserColorPrimarySurface.copy(alpha = BrowserOpacity.BrowserOpacitySurfaceHigh),
        shadowElevation = BrowserDimens.BrowserElevationMedium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = BrowserDimens.BrowserSpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = "Close Search",
                    tint = BrowserColors.BrowserColorPrimaryText
                )
            }

            Spacer(modifier = Modifier.width(BrowserDimens.BrowserSpacingSmall))

            // Search TextField
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = BrowserTypography.BrowserFontBodyMedium.copy(
                    color = BrowserColors.BrowserColorPrimaryText
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            "Search bookmarks...",
                            style = BrowserTypography.BrowserFontBodyMedium,
                            color = BrowserColors.BrowserColorSecondaryText
                        )
                    }
                    innerTextField()
                }
            )

            // Clear Button
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Clear",
                        modifier = Modifier.size(BrowserDimens.BrowserSizeIconMedium),
                        tint = BrowserColors.BrowserColorSecondaryText
                    )
                }
            }
        }
    }
}