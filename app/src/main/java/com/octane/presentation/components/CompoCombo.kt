package com.octane.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ============================================================================
// 1. DESIGN SYSTEM CONSTANTS (Colors, Typography)
// ============================================================================

object AppColors {
    val Background = Color(0xFF000000)
    val Surface = Color(0xFF0A0A0A) // Slightly lighter for cards
    val SurfaceHighlight = Color(0xFF1C1C1E) // For inputs/pressed states

    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF8E8E93)
    val TextTertiary = Color(0xFF48484A)

    // Crypto Brand Colors
    val Bitcoin = Color(0xFFF7931A)
    val Ethereum = Color(0xFF627EEA)
    val Solana = Color(0xFF9945FF)
    val Polygon = Color(0xFF8247E5)

    // Status
    val Positive = Color(0xFFFFFFFF) // Design system uses White for neutral/positive gain
    val Negative = Color(0xFF8E8E93) // Grey for loss/neutral
    val Warning = Color(0xFFF7DC6F)
}

data class AssetUI(
    val id: String,
    val symbol: String,
    val name: String,
    val balance: String,
    val balanceUsd: String,
    val price: String,
    val priceChangePercent: Double,
    val tags: List<String> = emptyList(),
    val color: Color
)

val standardSpace = 8.dp
val heightSpace = 12.dp

data class TransactionUI(
    val id: String,
    val type: String, // "Sent", "Received"
    val amount: String,
    val symbol: String,
    val status: String, // "Confirmed", "Pending"
    val timestamp: String
)

// --------------------------- 1.2 Metallic Border Modifier ---------------------------

@Composable
fun DetailHeader(
    price: String,
    changeAmount: String,
    changePercent: String,
    isPositive: Boolean,
    selectedTimeframe: String,
    onTimeframeSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. Big Price
        Text(
            text = price,
            color = AppColors.TextPrimary,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1).sp
        )

        // 2. Change Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                changeAmount,
                color = if (isPositive) AppColors.TextPrimary else AppColors.Negative,
                fontSize = 16.sp
            )
            StatusChip(changePercent) // Reusing from DesignSystem.kt
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Graph Placeholder (Simulated Line Chart)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(vertical = 16.dp)
        ) {
            // Simple canvas to draw a "tech" looking line
            val lineColor = if (isPositive) AppColors.TextPrimary else AppColors.Negative
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val path = Path().apply {
                    moveTo(0f, height * 0.7f)
                    cubicTo(
                        width * 0.2f,
                        height * 0.8f,
                        width * 0.3f,
                        height * 0.4f,
                        width * 0.5f,
                        height * 0.5f
                    )
                    cubicTo(
                        width * 0.7f,
                        height * 0.6f,
                        width * 0.8f,
                        height * 0.2f,
                        width,
                        height * 0.1f
                    )
                }
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // 4. Timeframe Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("1H", "1D", "1W", "1M", "YTD", "ALL").forEach { tf ->
                val isSelected = tf == selectedTimeframe
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AppColors.SurfaceHighlight else Color.Transparent)
                        .clickable { onTimeframeSelected(tf) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tf,
                        color = if (isSelected) AppColors.TextPrimary else AppColors.TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}


/**
 * Applies the signature "Metallic" White-Black-White gradient border.
 * * @param angleDeg The angle of the light source/gradient.
 * - 170f: Steep Diagonal (Cards)
 * - 135f: Standard Diagonal (Buttons)
 * - 90f: Horizontal (List Rows)
 * - 180f: Vertical (Pills/Badges)
 */
fun Modifier.metallicBorder(
    width: Dp,
    shape: androidx.compose.ui.graphics.Shape,
    angleDeg: Float
): Modifier {
    return this.border(
        width = width,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White,
                Color.Black,
                Color.White,
                Color.Black
            ),
            // simple angle calculation for linear gradient start/end
            start = Offset.Zero,
            end = calculateGradientEnd(angleDeg)
        ),
        shape = shape
    )
}

// Helper to approximate gradient direction based on angle (0 is Left->Right)
private fun calculateGradientEnd(angleDeg: Float): Offset {
    val rad = Math.toRadians(angleDeg.toDouble())
    // This is a simplified infinite offset for the brush to span the content
    // In a real production app, usage of Modifier.drawWithCache is preferred to get exact size
    val scale = 1000f
    return Offset(
        x = (cos(rad) * scale).toFloat(),
        y = (sin(rad) * scale).toFloat()
    )
}

// ============================================================================
// PART 2: ATOMS & MOLECULES (Components from previous responses)
// ============================================================================

// --------------------------- 2.1 Shared Atoms/Molecules ---------------------------

@Composable
fun StatusChip(changePercent: String) {
    val isPositive = changePercent.contains("+")
    val color = if (isPositive) AppColors.Positive else AppColors.Negative

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = changePercent,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AboutSection(
    description: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "About",
            color = AppColors.TextSecondary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = description,
            color = AppColors.TextPrimary,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
fun SiteRow(
    rank: Int,
    name: String,
    category: String,
    iconUrl: String = "", // Placeholder
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp), // Less padding for dense lists
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank Badge (Medal Style)
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (rank <= 3) {
                // Medal Background (Silver/Grey)
                Icon(
                    Icons.Rounded.Stars, // Using Stars as medal placeholder
                    contentDescription = null,
                    tint = Color(0xFFD1D1D6), // Metallic Silver
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    rank.toString(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            } else {
                Text(rank.toString(), color = AppColors.TextSecondary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Site Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Text(name.take(1), color = Color.White)
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Name & Category
        Column(modifier = Modifier.weight(1f)) {
            Text(
                name,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(category, color = AppColors.TextSecondary, fontSize = 14.sp)
        }

        // Arrow
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.SurfaceHighlight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = AppColors.TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun UtilMetallicCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .defaultMinSize(180.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.Surface)
            .metallicBorder(0.5.dp, RoundedCornerShape(18.dp), angleDeg = 170f) // 170 deg for cards
            .padding(heightSpace),
        content = content
    )
}

@Composable
fun DetailUtilCard(
    header: String,
    subHeader: String,
) {
    Box(contentAlignment = Alignment.Center) {
        Column(verticalArrangement = Arrangement.spacedBy(standardSpace)) {
            UtilMetallicCard {
                Column {
                    Text(
                        text = if (header.isEmpty()) "0" else header,
                        color = if (header.isEmpty()) AppColors.TextTertiary else AppColors.TextPrimary,
                        fontSize = 18.sp,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (subHeader.isEmpty()) "0" else subHeader,
                        color = if (subHeader.isEmpty()) AppColors.TextTertiary else AppColors.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun MetallicCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(AppColors.Surface)
            .metallicBorder(1.dp, RoundedCornerShape(24.dp), angleDeg = 170f) // 170 deg for cards
            .padding(heightSpace),
        content = content
    )
}

@Composable
fun MetallicButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(standardSpace),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(AppColors.Surface)
                .metallicBorder(
                    1.dp,
                    RoundedCornerShape(20.dp),
                    angleDeg = 45f
                ) // Unique button angle
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = AppColors.TextPrimary,
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = text,
            color = AppColors.TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun HomeHeader(
    totalBalance: String,
    dayChange: String,
    percentChange: String,
    walletName: String,
    onWalletClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Balance & Change
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = totalBalance,
                color = AppColors.TextPrimary,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1.5).sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = dayChange,
                    color = AppColors.TextSecondary,
                    fontSize = 16.sp
                )
                StatusChip(percentChange)
            }
        }
        // Wallet Avatar
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.Surface)
                .metallicBorder(1.dp, RoundedCornerShape(12.dp), 90f)
                .clickable(onClick = onWalletClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icon Placeholder (e.g., first letter of wallet name or an emoji)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(AppColors.Background),
                contentAlignment = Alignment.Center
            ) {
                Text("A", color = Color.White, fontSize = 12.sp)
            }
            Text(walletName, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun TokenRow(
    symbol: String,
    name: String,
    balance: String,
    valueUsd: String,
    changePercent: Double,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Background) // Ensure list item uses BG color
            .clickable(onClick = onClick)
            .metallicBorder(1.dp, RoundedCornerShape(16.dp), angleDeg = 90f)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // LEFT: Icon + Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
            ) {
                Text(symbol.take(1), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Column {
                Text(
                    symbol,
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(balance, color = AppColors.TextSecondary, fontSize = 14.sp)
            }
        }

        // RIGHT: Value + Change
        Column(horizontalAlignment = Alignment.End) {
            Text(
                valueUsd,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            val changeStr = "${if (changePercent > 0) "+" else ""}$changePercent%"
            StatusChip(changeStr)
        }
    }
}

// --------------------------- 2.2 Perps/Discover/Manage (Extended) ---------------------------

@Composable
fun IndustrialSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .width(52.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(100))
            .background(if (checked) AppColors.TextPrimary else AppColors.SurfaceHighlight)
            .clickable { onCheckedChange(!checked) }
            .padding(4.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (checked) Color.Black else Color.Gray)
        )
    }
}


// ============================================================================
// 4. MOLECULES (Complex Components mapped to VM Data)
// ============================================================================

@Composable
fun TokenRow(
    asset: AssetUI,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Background)
            .metallicBorder(
                1.dp,
                RoundedCornerShape(16.dp),
                angleDeg = 90f
            ) // 90 deg (Horizontal) for rows
            .clickable(onClick = onClick)
            .padding(standardSpace),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Icon & Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Token Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(asset.color)
            ) {
                // In real app, load image here. Using text char for demo.
                Text(asset.symbol.take(1), color = Color.White, fontWeight = FontWeight.Bold)
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        asset.name,
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    asset.tags.forEach { tag -> StatusChip(tag) }
                }
                Text(asset.balance, color = AppColors.TextSecondary, fontSize = 14.sp)
            }
        }

        // Right: Value & Price
        Column(horizontalAlignment = Alignment.End) {
            Text(
                asset.balanceUsd,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            // Price Change logic
            val (color, prefix) = if (asset.priceChangePercent >= 0) AppColors.TextPrimary to "+" else AppColors.Negative to ""
            Text(
                text = "$prefix${asset.priceChangePercent}%",
                color = color,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun QuickActionGrid(
    onSend: () -> Unit,
    onReceive: () -> Unit,
    onSwap: () -> Unit,
    onBuy: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MetallicButton(text = "Receive", icon = Icons.Rounded.QrCode, onClick = onReceive)
        MetallicButton(text = "Send", icon = Icons.AutoMirrored.Rounded.Send, onClick = onSend)
        MetallicButton(text = "Swap", icon = Icons.Rounded.SwapHoriz, onClick = onSwap)
        MetallicButton(text = "Buy", icon = Icons.Rounded.AttachMoney, onClick = onBuy)
    }
}

@Composable
fun HomeHeader(
    totalBalance: String,
    changeAmount: String,
    changePercent: String,
    accountName: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                ) // Avatar
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("@MoyoKamaal", color = AppColors.TextSecondary, fontSize = 12.sp)
                    Text(accountName, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(
                    Icons.Rounded.History,
                    contentDescription = "History",
                    tint = AppColors.TextPrimary
                )
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = AppColors.TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Balance Display
        Text(
            text = totalBalance,
            color = AppColors.TextPrimary,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1).sp
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(changeAmount, color = AppColors.TextSecondary, fontSize = 16.sp)
            StatusChip(changePercent)
        }
    }
}

@Composable
fun SwapCard(
    payingToken: String,
    payingAmount: String,
    receivingToken: String,
    receivingAmount: String,
    onFlip: () -> Unit
) {
    Box(contentAlignment = Alignment.Center) {
        Column(verticalArrangement = Arrangement.spacedBy(standardSpace)) {
            // Pay Card
            MetallicCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("You Pay", color = AppColors.TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (payingAmount.isEmpty()) "0" else payingAmount,
                            color = if (payingAmount.isEmpty()) AppColors.TextTertiary else AppColors.TextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Medium
                        )
                        StatusChip(payingToken)
                    }
                }
            }

            // Receive Card
            MetallicCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("You Receive", color = AppColors.TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (receivingAmount.isEmpty()) "0" else receivingAmount,
                            color = if (receivingAmount.isEmpty()) AppColors.TextTertiary else AppColors.TextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Medium
                        )
                        StatusChip(receivingToken)
                    }
                }
            }
        }

        // Floating Switch Button
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(AppColors.SurfaceHighlight)
                .border(1.dp, Color.Black, CircleShape)
                .clickable(onClick = onFlip),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.SwapVert, contentDescription = "Swap", tint = AppColors.TextPrimary)
        }
    }
}

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
        textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 16.sp),
        cursorBrush = SolidColor(AppColors.TextPrimary),
        decorationBox = { innerTextField ->
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(100)) // Pill shape
                    .background(AppColors.SurfaceHighlight)
                    .metallicBorder(1.dp, RoundedCornerShape(100), angleDeg = 170f)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = null,
                    tint = AppColors.TextSecondary
                )
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(placeholder, color = AppColors.TextTertiary)
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Clear",
                        tint = AppColors.TextSecondary,
                        modifier = Modifier.clickable { onQueryChange("") }
                    )
                }
            }
        }
    )
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean = false,
    hasDropdown: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) AppColors.SurfaceHighlight else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) AppColors.TextPrimary else AppColors.TextSecondary,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
        if (hasDropdown) {
            Text(" â–¾", color = AppColors.TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
fun PerpRow(
    symbol: String,
    name: String,
    price: String,
    changePercent: Double,
    volume24h: String,
    leverageMax: Int,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Background)
            .metallicBorder(1.dp, RoundedCornerShape(16.dp), angleDeg = 90f)
            .clickable(onClick = onClick)
            .padding(standardSpace),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // LEFT: Icon with Infinity Badge + Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Badged Icon
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color)
                ) {
                    Text(symbol.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                }
                // The "Perp" Infinity Badge
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = 4.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, AppColors.Background, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.AllInclusive,
                        contentDescription = "Perp",
                        tint = Color.Black,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Column {
                Text(
                    name,
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(volume24h, color = AppColors.TextSecondary, fontSize = 12.sp)
                    Text("â€¢ ${leverageMax}x", color = AppColors.TextSecondary, fontSize = 12.sp)
                }
            }
        }

        // RIGHT: Price & Change
        Column(horizontalAlignment = Alignment.End) {
            Text(
                price,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            val changeStr = "${if (changePercent > 0) "+" else ""}$changePercent%"
            StatusChip(changeStr)
        }
    }
}

@Composable
fun RankedTokenRow(
    rank: Int,
    symbol: String,
    name: String,
    marketCap: String,
    price: String,
    changePercent: Double,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Background)
            .metallicBorder(1.dp, RoundedCornerShape(16.dp), angleDeg = 90f)
            .clickable(onClick = onClick)
            .padding(standardSpace),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // LEFT: Icon with Rank Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(contentAlignment = Alignment.BottomStart) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color)
                ) {
                    Text(symbol.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                }

                // Rank Badge (e.g., "1", "2")
                Box(
                    modifier = Modifier
                        .offset(x = (-4).dp, y = 4.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(AppColors.TextPrimary) // Use primary text color for silver/gold feel
                        .border(2.dp, AppColors.Background, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rank.toString(),
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Column {
                Text(
                    symbol,
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(marketCap, color = AppColors.TextSecondary, fontSize = 12.sp)
            }
        }

        // RIGHT: Price Data
        Column(horizontalAlignment = Alignment.End) {
            Text(
                price,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            val changeStr = "${if (changePercent > 0) "+" else ""}$changePercent%"
            StatusChip(changeStr)
        }
    }
}

@Composable
fun ManageTokenRow(
    symbol: String,
    name: String,
    balance: String,
    isEnabled: Boolean,
    color: Color,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Background)
            .metallicBorder(1.dp, RoundedCornerShape(16.dp), angleDeg = 90f)
            .padding(standardSpace),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
            ) {
                Text(symbol.take(1), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Column {
                Text(
                    name,
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(balance, color = AppColors.TextSecondary, fontSize = 14.sp)
            }
        }

        IndustrialSwitch(checked = isEnabled, onCheckedChange = onToggle)
    }
}

// --------------------------- 2.3 Empty/Navigation (Missing) ---------------------------

@Composable
fun WideActionButton(
    text: String,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isPrimary) AppColors.TextPrimary else AppColors.Surface
    val textColor = if (isPrimary) Color.Black else Color.White

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(heightSpace)
            .then(
                if (!isPrimary) Modifier.metallicBorder(
                    1.dp,
                    RoundedCornerShape(16.dp),
                    170f
                ) else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptyWalletHero(
    onBuyCash: () -> Unit,
    onDepositCrypto: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
    ) {
        // 1. Illustration Placeholder
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(AppColors.SurfaceHighlight)
                .metallicBorder(1.dp, CircleShape, 135f),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.AccountBalanceWallet,
                contentDescription = null,
                tint = AppColors.TextSecondary,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Text
        Text(
            "Your wallet is ready",
            color = AppColors.TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Fund your wallet with cash or crypto and\nyou'll be set to start trading!",
            color = AppColors.TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Buttons
        WideActionButton(text = "Buy SOL with Cash", isPrimary = true, onClick = onBuyCash)
        Spacer(modifier = Modifier.height(12.dp))
        WideActionButton(text = "Deposit Crypto", isPrimary = false, onClick = onDepositCrypto)
    }
}

@Composable
fun ModeSelectorTabs(
    modes: List<String>,
    selectedMode: String,
    onModeSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(heightSpace),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        modes.forEach { mode ->
            val isSelected = mode == selectedMode
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100))
                    .background(if (isSelected) AppColors.TextPrimary else AppColors.Surface)
                    .clickable { onModeSelected(mode) }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (mode == "Perps") {
                        Icon(
                            Icons.Rounded.AllInclusive,
                            contentDescription = null,
                            tint = if (isSelected) Color.Black else AppColors.TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = mode,
                        color = if (isSelected) Color.Black else AppColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(
    selectedRoute: String,
    onNavigate: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(AppColors.Background)
            .drawBehind {
                // Metallic top border
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Black, Color(0xFF666666), Color.Black)
                    ),
                    topLeft = Offset.Zero,
                    size = size.copy(height = 1.dp.toPx())
                )
            }
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavIcon("Home", Icons.Rounded.Home, selectedRoute == "Home") { onNavigate("Home") }
        NavIcon(
            "Wallet",
            Icons.Rounded.AccountBalanceWallet,
            selectedRoute == "Wallet"
        ) { onNavigate("Wallet") }
        NavIcon("Swap", Icons.Rounded.SwapHoriz, selectedRoute == "Swap") { onNavigate("Swap") }
        NavIcon("Search", Icons.Rounded.Search, selectedRoute == "Search") { onNavigate("Search") }
    }
}

@Composable
private fun NavIcon(
    route: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = route,
            tint = if (isSelected) AppColors.TextPrimary else AppColors.TextSecondary,
            modifier = Modifier.size(32.dp)
        )
    }
}

// --------------------------- 2.4 Details/Lists (Additional) ---------------------------

@Composable
fun ChartActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(standardSpace),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AppColors.Surface)
                .metallicBorder(1.dp, RoundedCornerShape(16.dp), angleDeg = 135f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = AppColors.TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = text,
            color = AppColors.TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ChartActionGrid(
    onReceive: () -> Unit,
    onCashBuy: () -> Unit,
    onShare: () -> Unit,
    onMore: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ChartActionButton("Receive", Icons.Rounded.QrCode, onReceive)
        ChartActionButton("Cash Buy", Icons.Rounded.AttachMoney, onCashBuy)
        ChartActionButton("Share", Icons.Rounded.IosShare, onShare)
        ChartActionButton("More", Icons.Rounded.MoreHoriz, onMore)
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = AppColors.TextSecondary, fontSize = 16.sp)
            Text(
                value,
                color = AppColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        // Metallic Separator Line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Black, Color(0xFF333333), Color.Black)
                    )
                )
        )
    }
}

@Composable
fun ReceiveNetworkRow(
    networkName: String,
    address: String,
    iconColor: Color,
    onCopy: () -> Unit,
    onShowQr: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Surface)
            .metallicBorder(1.dp, RoundedCornerShape(16.dp), angleDeg = 90f)
            .padding(standardSpace),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Icon + Text
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.TextPrimary)
            ) {
                Box(
                    Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(iconColor)
                )
            }
            Column {
                Text(
                    networkName,
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = address.take(10) + "..." + address.takeLast(4),
                    color = AppColors.TextSecondary,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }
        }

        // Right: Actions (QR + Copy)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                Icons.Rounded.QrCode to onShowQr,
                Icons.Rounded.ContentCopy to onCopy
            ).forEach { (icon, action) ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AppColors.SurfaceHighlight)
                        .metallicBorder(1.dp, CircleShape, 135f)
                        .clickable(onClick = action),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = AppColors.TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SiteRow(
    rank: Int,
    name: String,
    category: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .metallicBorder(1.dp, RoundedCornerShape(16.dp), angleDeg = 90f)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank Badge (Medal Style)
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (rank <= 3) {
                Icon(
                    Icons.Rounded.Stars,
                    contentDescription = null,
                    tint = when (rank) {
                        1 -> Color(0xFFC9B037) // Gold
                        2 -> Color(0xFFC0C0C0) // Silver
                        else -> Color(0xFFCD7F32) // Bronze
                    },
                    modifier = Modifier
                        .size(24.dp)
                        .shadow(2.dp, CircleShape)
                )
                Text(
                    rank.toString(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            } else {
                Text(rank.toString(), color = AppColors.TextSecondary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Site Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.SurfaceHighlight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                name.take(1),
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Name & Category
        Column(modifier = Modifier.weight(1f)) {
            Text(
                name,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(category, color = AppColors.TextSecondary, fontSize = 14.sp)
        }

        // Arrow
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(AppColors.SurfaceHighlight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = AppColors.TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun LearnCard(
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.Surface)
            .metallicBorder(1.dp, RoundedCornerShape(20.dp), angleDeg = 170f)
            .clickable(onClick = onClick)
            .padding(standardSpace),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Large Icon
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(iconColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.School, contentDescription = null, tint = Color.White)
        }

        Column {
            Text(
                title,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(subtitle, color = AppColors.TextSecondary, fontSize = 14.sp)
        }
    }
}

// ============================================================================
// PART 3: FULL SCREENS (Pages)
// ============================================================================

// --- Mock Data ---
data class MockToken(
    val symbol: String,
    val name: String,
    val balance: Double,
    val priceUsd: Double,
    val changePercent: Double,
    val color: Color,
    val marketCap: String? = null,
    val leverage: Int? = null,
    val rank: Int? = null,
    val isEnabled: Boolean = true
)

val mockTokens = listOf(
    MockToken("SOL", "Solana", 12.50, 165.23, 4.31, AppColors.Solana, marketCap = "$75.2B MC"),
    MockToken("BTC", "Bitcoin", 0.005, 92469.00, 0.11, AppColors.Bitcoin, leverage = 40),
    MockToken("ETH", "Ethereum", 0.12, 3018.90, -3.30, AppColors.Ethereum, leverage = 25),
    MockToken("USDC", "USD Coin", 500.00, 1.00, 0.00, Color(0xFF2775CA)),
    MockToken(
        "MEME",
        "MemeCoin",
        150000.0,
        0.0284,
        -30.58,
        Color.Gray,
        marketCap = "$28M MC",
        rank = 1
    ),
)

enum class AppRoute { Home, Wallet, Swap, Search, Details, Receive, Manage }

@Composable
fun OctaneApp(
    modifier: Modifier
) {
    var currentRoute by remember { mutableStateOf(AppRoute.Home) }

    // Simulating Different States
    val hasTokens = remember { mutableStateOf(true) }

    Scaffold(
        containerColor = AppColors.Background,
        bottomBar = {
            BottomNavBar(currentRoute.name) { routeName ->
                currentRoute = AppRoute.valueOf(routeName)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (currentRoute) {
                AppRoute.Home -> HomeScreen(
                    hasTokens = hasTokens.value,
                    onManageClick = { currentRoute = AppRoute.Manage }
                )

                AppRoute.Wallet -> WalletScreen()
                AppRoute.Swap -> SwapScreen()
                AppRoute.Search -> DiscoverScreen()
                AppRoute.Details -> TokenDetailsScreen()
                AppRoute.Receive -> ReceiveScreen()
                AppRoute.Manage -> ManageTokensScreen(onBack = { currentRoute = AppRoute.Home })
            }
        }
    }
}

// --------------------------- Home Screen (Loaded & Empty States) ---------------------------

@Composable
fun HomeScreen(
    hasTokens: Boolean,
    onManageClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(standardSpace)
    ) {
        // 1. Header
        item {
            HomeHeader(
                totalBalance = "$9,876.54",
                dayChange = "+$402.11",
                percentChange = "+4.26%",
                walletName = "Account 1"
            )
        }

        // 2. Quick Action Grid
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetallicButton("Send", Icons.Rounded.ArrowOutward, {})
                MetallicButton("Receive", Icons.Rounded.QrCode, {})
                MetallicButton("Swap", Icons.Rounded.SwapHoriz, {})
                MetallicButton("Manage", Icons.Rounded.Settings, onManageClick)
            }
        }

        // 3. Content (Empty vs. Loaded)
        if (hasTokens) {
            item {
                Text(
                    "Your Assets",
                    color = AppColors.TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            items(mockTokens.filter { it.symbol != "MEME" }) { token ->
                TokenRow(
                    symbol = token.symbol,
                    name = token.name,
                    balance = "${"%.2f".format(token.balance)} ${token.symbol}",
                    valueUsd = "$${"%,.2f".format(token.balance * token.priceUsd)}",
                    changePercent = token.changePercent,
                    color = token.color,
                    onClick = {}
                )
            }
        } else {
            item {
                // Empty State
                EmptyWalletHero(onBuyCash = {}, onDepositCrypto = {})

                Spacer(modifier = Modifier.height(32.dp))

                // Perps Promo Banner (from Screenshot 2)
                Text(
                    "Perps >",
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppColors.Surface)
                        .metallicBorder(1.dp, RoundedCornerShape(20.dp), angleDeg = 170f)
                        .padding(20.dp)
                        .clickable {}
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Rounded.AllInclusive,
                            contentDescription = null,
                            tint = AppColors.TextPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                "More Power with Perps",
                                color = AppColors.TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Trade with up to 40x leverage",
                                color = AppColors.TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// --------------------------- Wallet/Activity Screen ---------------------------

@Composable
fun WalletScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(standardSpace)
    ) {
        item {
            HomeHeader(
                totalBalance = "$9,876.54",
                dayChange = "-$402.11",
                percentChange = "-4.26%",
                walletName = "Account 1"
            )
        }
        item {
            Text(
                "Activity",
                color = AppColors.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        items(5) { index ->
            // Mock TransactionRow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AppColors.SurfaceHighlight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (index % 2 == 0) "S" else "R", color = AppColors.TextPrimary)
                    }
                    Column {
                        Text(
                            if (index % 2 == 0) "Send" else "Receive",
                            color = AppColors.TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (index % 2 == 0) "0.1 SOL" else "2 USDC",
                            color = AppColors.TextSecondary
                        )
                    }
                }
                Text(
                    if (index % 2 == 0) "-$16.52" else "+$2.00",
                    color = if (index % 2 == 0) AppColors.Negative else AppColors.Positive,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --------------------------- Swap Screen ---------------------------

@Composable
fun SwapScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(heightSpace)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Title
        Text(
            "Swap",
            color = AppColors.TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(heightSpace)
        )

        // 2. From Card
        SwapCard(
            title = "You Pay",
            tokenSymbol = "SOL",
            amount = "12.50",
            balance = "12.5 SOL",
            iconColor = AppColors.Solana,
            onTokenClick = {},
            onAmountChange = {}
        )

        // 3. Flip Button
        Box(
            modifier = Modifier
                .offset(y = (-8).dp) // Overlap cards slightly
                .shadow(8.dp, CircleShape)
                .size(48.dp)
                .clip(CircleShape)
                .background(AppColors.Surface)
                .metallicBorder(2.dp, CircleShape, 45f)
                .clickable(onClick = {}),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.SwapVert,
                contentDescription = "Flip",
                tint = AppColors.TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        // 4. To Card
        SwapCard(
            title = "You Receive",
            tokenSymbol = "USDC",
            amount = "1,987.50",
            balance = "500 USDC",
            iconColor = Color(0xFF2775CA),
            onTokenClick = {},
            onAmountChange = {},
            isInput = false
        )

        // 5. Rate Info
        InfoRow("Rate", "1 SOL â‰ˆ 159 USDC")

        // 6. Action Button
        WideActionButton(text = "Review Swap", isPrimary = true, onClick = {})
    }
}

@Composable
fun SwapCard(
    title: String,
    tokenSymbol: String,
    amount: String,
    balance: String,
    iconColor: Color,
    onTokenClick: () -> Unit,
    onAmountChange: (String) -> Unit,
    isInput: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AppColors.Surface)
            .metallicBorder(1.dp, RoundedCornerShape(24.dp), angleDeg = 170f)
            .padding(20.dp)
    ) {
        // Title & Max Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = AppColors.TextSecondary, fontSize = 16.sp)
            if (isInput) {
                Text(
                    "MAX",
                    color = AppColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {})
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Token Selector & Amount
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Token Selector
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.SurfaceHighlight)
                    .clickable(onClick = onTokenClick)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconColor)
                ) {
                    Text(
                        tokenSymbol.take(1),
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Text(tokenSymbol, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold)
                Icon(
                    Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = AppColors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Amount Input
            Box(modifier = Modifier.weight(1f)) {
                if (isInput) {
                    BasicTextField(
                        value = amount,
                        onValueChange = onAmountChange,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        textStyle = TextStyle(
                            color = AppColors.TextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End
                        ),
                        cursorBrush = SolidColor(AppColors.TextPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = amount,
                        color = AppColors.TextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Footer / Balance
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text("Balance: $balance", color = AppColors.TextSecondary, fontSize = 12.sp)
        }
    }
}

// --------------------------- Manage Tokens Screen ---------------------------

@Composable
fun ManageTokensScreen(onBack: () -> Unit) {
    val searchInput = remember { mutableStateOf("") }
    val mockTokensState = remember { mutableStateListOf(*mockTokens.toTypedArray()) }

    // Toggle logic
    val onToggle: (MockToken, Boolean) -> Unit = { token, isEnabled ->
        val index = mockTokensState.indexOf(token)
        if (index != -1) {
            mockTokensState[index] = token.copy(isEnabled = isEnabled)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = AppColors.TextPrimary
                    )
                }
                Text(
                    "Manage Tokens",
                    color = AppColors.TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Search
        item {
            SearchInput(searchInput.value, { searchInput.value = it })
        }

        // List
        items(mockTokensState.filter {
            it.name.contains(
                searchInput.value,
                ignoreCase = true
            )
        }) { token ->
            ManageTokenRow(
                symbol = token.symbol,
                name = token.name,
                balance = "${"%.2f".format(token.balance)} ${token.symbol}",
                isEnabled = token.isEnabled,
                color = token.color,
                onToggle = { onToggle(token, it) }
            )
        }
    }
}

// --------------------------- Discover Screen (Perps/Lists/Trending) ---------------------------

@Composable
fun DiscoverScreen() {
    val searchInput = remember { mutableStateOf("Sites, tokens, URL") }
    val selectedMode = remember { mutableStateOf("Tokens") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(standardSpace)
    ) {
        // Search Input (Simulating the active/placeholder state)
        item {
            SearchInput(
                searchInput.value,
                { searchInput.value = it },
                placeholder = "Sites, tokens, URL"
            )
        }

        // Mode Tabs
        item {
            ModeSelectorTabs(
                modes = listOf("Tokens", "Perps", "Lists"),
                selectedMode = selectedMode.value,
                onModeSelected = { selectedMode.value = it }
            )
        }

        when (selectedMode.value) {
            "Tokens" -> {
                // Trending Tokens List (Ranked)
                item {
                    Text(
                        "Trending Tokens >",
                        color = AppColors.TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(mockTokens.filter { it.rank != null }.sortedBy { it.rank }) { token ->
                    RankedTokenRow(
                        rank = token.rank!!,
                        symbol = token.symbol,
                        name = token.name,
                        marketCap = token.marketCap!!,
                        price = "$${"%.4f".format(token.priceUsd)}",
                        changePercent = token.changePercent,
                        color = token.color,
                        onClick = {}
                    )
                }
            }

            "Perps" -> {
                // Perps List (Volume/OI)
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip("Volume", isSelected = true, hasDropdown = true, onClick = {})
                        FilterChip("Solana", hasDropdown = true, onClick = {})
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(mockTokens.filter { it.leverage != null }) { token ->
                    PerpRow(
                        symbol = token.symbol,
                        name = "${token.symbol}-USD",
                        price = "$${"%,.2f".format(token.priceUsd)}",
                        changePercent = token.changePercent,
                        volume24h = "$${Random.nextInt(1, 5)}.0B Vol",
                        leverageMax = token.leverage!!,
                        color = token.color,
                        onClick = {}
                    )
                }
            }

            "Lists" -> {
                // Trending Sites & Learn
                item {
                    Text(
                        "Trending Sites >",
                        color = AppColors.TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(
                    listOf(
                        Triple(1, "Unclaimed SOL", "Tools"),
                        Triple(2, "Jupiter", "DeFi"),
                        Triple(3, "Drift Protocol", "DeFi"),
                        Triple(4, "pump.fun", "Memes")
                    )
                ) { (rank, name, category) ->
                    SiteRow(rank, name, category, onClick = {})
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Learn >",
                        color = AppColors.TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(
                    listOf(
                        Pair("Liquid Staking 101", Color(0xFF1E88E5)),
                        Pair("What is a LST?", Color(0xFF4ECDC4))
                    )
                ) { (title, color) ->
                    LearnCard(title, "Dive into the future of DeFi.", color, onClick = {})
                }
            }
        }
    }
}

// --------------------------- Token Details Screen ---------------------------

@Composable
fun TokenDetailsScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Button & Token Name
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = AppColors.TextPrimary
                    )
                }
                Text(
                    "Bitcoin",
                    color = AppColors.TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Detail Header (Price, Chart, Timeframes)
        item {
            val selectedTimeframe = remember { mutableStateOf("1D") }
            DetailHeader(
                price = "$92,213.75",
                changeAmount = "+$100.18",
                changePercent = "+0.11%",
                isPositive = true,
                selectedTimeframe = selectedTimeframe.value,
                onTimeframeSelected = { selectedTimeframe.value = it }
            )
        }

        // Action Grid (Receive, Cash Buy, Share, More)
        item {
            ChartActionGrid({}, {}, {}, {})
        }

        // Your Position Card (Re-using metallic styling)
        item {
            Text("Your Position", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppColors.Surface)
                    .metallicBorder(1.dp, RoundedCornerShape(20.dp), angleDeg = 170f)
                    .padding(20.dp)
            ) {
                // Mock Position Data
                Column {
                    InfoRow("Balance", "0.005 BTC")
                    InfoRow("Value", "$462.34")
                }
            }
        }

        // Info Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Info",
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            InfoRow("Name", "Bitcoin")
            InfoRow("Symbol", "BTC")
            InfoRow("Market Cap", "$1.84T")
            InfoRow("Circulating Supply", "19.68M BTC")

            // About Section (Simple text block)
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
                    .also { Modifier.padding(top = 16.dp) }) {
                Text(
                    "About",
                    color = AppColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bitcoin is the first successful internet money based on peer-to-peer technology. It is a decentralized digital currency, without a central bank or single administrator, that can be sent from user to user on the peer-to-peer bitcoin network without the need for intermediaries.",
                    color = AppColors.TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

// --------------------------- Receive Screen ---------------------------

@Composable
fun ReceiveScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(heightSpace)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            "Receive BTC",
            color = AppColors.TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // QR Code Box (Simulated)
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(heightSpace),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for the actual QR code image
            Text(
                "QR CODE",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        // Address
        Text(
            "bc1qxy2kgdygjrsqtzq2n0yrf2493p83krdqvfdc61",
            color = AppColors.TextPrimary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Network Selector Title
        Text(
            "Select Network",
            color = AppColors.TextSecondary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 8.dp)
        )

        // Network Rows
        ReceiveNetworkRow("Bitcoin (Native)", "bc1qxy...dc61", AppColors.Bitcoin, {}, {})
        ReceiveNetworkRow("Solana (Bridged)", "A1B2C3...XYZ9", AppColors.Solana, {}, {})
    }
}


// ============================================================================
// PART 4: FULL APP PREVIEW
// ============================================================================

@Preview(showSystemUi = true)
@Composable
fun PreviewOctaneApp() {
    OctaneApp(modifier = Modifier)
}


// ============================================================================
// 5. PREVIEWS
// ============================================================================

@Preview(name = "Token Details Screen", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewTokenDetails() {
    Column(modifier = Modifier.padding(heightSpace)) {
        DetailHeader(
            price = "$92,213.75",
            changeAmount = "+$100.18",
            changePercent = "+0.11%",
            isPositive = true,
            selectedTimeframe = "1D",
            onTimeframeSelected = {}
        )

        ChartActionGrid({}, {}, {}, {})

        Spacer(modifier = Modifier.height(24.dp))
        Text("Your Position", color = AppColors.TextSecondary, fontWeight = FontWeight.Bold)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailUtilCard("Balance", "0")
                DetailUtilCard("Value", "$0.00")
            }
            DetailUtilCard("ROI", "0")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Info",
            color = AppColors.TextSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        InfoRow("Name", "Bitcoin")
        InfoRow("Symbol", "BTC")
        InfoRow("Market Cap", "$1.84T")

        Spacer(modifier = Modifier.height(16.dp))
        AboutSection("Bitcoin is the first successful internet money based on peer-to-peer technology...")
    }
}

@Preview(name = "Receive Screen", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewReceiveScreen() {
    Column(modifier = Modifier.padding(heightSpace), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ReceiveNetworkRow("Solana", "DJyz...Etuh", Color(0xFF9945FF), {}, {})
        ReceiveNetworkRow("Ethereum", "0x5D08...0169", Color(0xFF627EEA), {}, {})
        ReceiveNetworkRow("Bitcoin", "bc1p...a23x", Color(0xFFF7931A), {}, {})
    }
}

@Preview(name = "Trending Sites", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewTrendingSites() {
    Column(modifier = Modifier.padding(heightSpace), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Trending Sites >",
            color = AppColors.TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        SiteRow(1, "Unclaimed SOL", "Tools", onClick = {})
        SiteRow(2, "Jupiter", "DeFi", onClick = {})
        SiteRow(4, "pump.fun", "DeFi", onClick = {})

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Learn >",
            color = AppColors.TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        LearnCard("Liquid Staking 101", "What is liquid staking?", Color.Gray, {})
    }
}


// ============================================================================
// 5. PREVIEWS
// ============================================================================

@Preview(name = "Perps Screen Components", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewPerpsComponents() {
    Column(modifier = Modifier.padding(standardSpace), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Header / Filters
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip("Volume", hasDropdown = true, onClick = {})
            FilterChip("Price", hasDropdown = true, onClick = {})
        }

        // Perp Rows
        PerpRow(
            symbol = "BTC", name = "BTC-USD", price = "$91,469.00",
            changePercent = -1.53, volume24h = "$4.4B Vol", leverageMax = 40,
            color = AppColors.Bitcoin, onClick = {}
        )
        PerpRow(
            symbol = "ETH", name = "ETH-USD", price = "$3,018.90",
            changePercent = -3.30, volume24h = "$2.8B Vol", leverageMax = 25,
            color = AppColors.Ethereum, onClick = {}
        )
    }
}

@Preview(name = "Discover Screen Components", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewDiscoverComponents() {
    Column(modifier = Modifier.padding(standardSpace), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Trending Tokens >",
            color = AppColors.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        RankedTokenRow(
            rank = 1, symbol = "MEME", name = "MemeCoin", marketCap = "$28M MC",
            price = "$0.0284", changePercent = -30.58, color = Color.Gray, onClick = {}
        )
        RankedTokenRow(
            rank = 2, symbol = "ACA", name = "Acala", marketCap = "$987K MC",
            price = "$0.0009", changePercent = -27.21, color = Color.DarkGray, onClick = {}
        )
    }
}

@Preview(name = "Manage Tokens Screen", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewManageTokens() {
    Column(modifier = Modifier.padding(standardSpace), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SearchInput(query = "", onQueryChange = {})

        ManageTokenRow(
            symbol = "BTC", name = "Bitcoin", balance = "0 BTC",
            isEnabled = true, color = AppColors.Bitcoin, onToggle = {}
        )
        ManageTokenRow(
            symbol = "SOL", name = "Solana", balance = "0 SOL",
            isEnabled = false, color = AppColors.Solana, onToggle = {}
        )
    }
}

// ============================================================================
// 3. COMPOSITE PREVIEWS (Putting it all together)
// ============================================================================

@Preview(name = "Empty State Screen", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        Column(modifier = Modifier.padding(heightSpace)) {
            HomeHeader("$0.00", "+$0.00", "+0.00%", "Account 1")
            EmptyWalletHero({}, {})
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Perps >",
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            // Banner for Perps
            MetallicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Rounded.AllInclusive,
                        contentDescription = null,
                        tint = AppColors.TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            "More Power with Perps",
                            color = AppColors.TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Trade with up to 40x leverage",
                            color = AppColors.TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            BottomNavBar("Home", {})
        }
    }
}

@Preview(name = "Discover/Search Screen", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewDiscoverScreenFull() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        Column(
            modifier = Modifier.padding(heightSpace),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Search
            SearchInput("Sites, tokens, URL", {}, placeholder = "Sites, tokens, URL")

            // Mode Tabs
            ModeSelectorTabs(
                modes = listOf("Tokens", "Perps", "Lists"),
                selectedMode = "Tokens",
                onModeSelected = {}
            )

            // Content
            Text(
                "Trending Tokens >",
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            // Reusing components from ExtendedComponents.kt
            RankedTokenRow(1, "MEME", "MemeCoin", "$28M MC", "$0.0284", -30.58, Color.Gray, {})
            RankedTokenRow(2, "ACA", "Acala", "$987K MC", "$0.0009", -27.21, Color.DarkGray, {})
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            BottomNavBar("Search", {})
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(heightSpace)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(standardSpace)) {

            // Header
            HomeHeader(
                totalBalance = "$12,450.00",
                changeAmount = "+$230.50",
                changePercent = "+1.2%",
                accountName = "Account 1"
            )

            // Quick Actions
            QuickActionGrid({}, {}, {}, {})

            // Tokens List
            Column(verticalArrangement = Arrangement.spacedBy(standardSpace)) {
                Text(
                    "Tokens >",
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                TokenRow(
                    asset = AssetUI(
                        "1",
                        "BTC",
                        "Bitcoin",
                        "0.45 BTC",
                        "$28,000.00",
                        "$62,000",
                        2.4,
                        listOf("Taproot"),
                        AppColors.Bitcoin
                    ),
                    onClick = {}
                )
                TokenRow(
                    asset = AssetUI(
                        "2",
                        "ETH",
                        "Ethereum",
                        "12.5 ETH",
                        "$24,000.00",
                        "$1,900",
                        -0.5,
                        emptyList(),
                        AppColors.Ethereum
                    ),
                    onClick = {}
                )
                TokenRow(
                    asset = AssetUI(
                        "3",
                        "SOL",
                        "Solana",
                        "450 SOL",
                        "$12,000.00",
                        "$135",
                        5.2,
                        emptyList(),
                        AppColors.Solana
                    ),
                    onClick = {}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSwapComponent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(heightSpace),
        contentAlignment = Alignment.Center
    ) {
        SwapCard(
            payingToken = "SOL",
            payingAmount = "12.5",
            receivingToken = "USDC",
            receivingAmount = "1,650.00",
            onFlip = {}
        )
    }
}