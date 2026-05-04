package com.mockdonalds.app.features.kiosk.order.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mockdonalds.app.core.theme.MockDimens
import com.mockdonalds.app.features.kiosk.order.api.navigation.KioskOrderScreen
import com.mockdonalds.app.features.kiosk.order.api.ui.KioskOrderTestTags
import com.mockdonalds.app.features.order.api.domain.CartSummary
import com.mockdonalds.app.features.order.api.domain.MenuCategory
import com.mockdonalds.app.features.order.api.domain.MenuItem
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope

@CircuitInject(KioskOrderScreen::class, AppScope::class)
@Composable
fun KioskOrderUi(state: KioskOrderUiState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(KioskOrderTestTags.SCREEN),
    ) {
        KioskCategoryRail(
            categories = state.categories,
            selectedCategoryId = state.selectedCategoryId,
            onSelect = { id -> state.eventSink(KioskOrderEvent.CategorySelected(id)) },
            onHomePressed = { state.eventSink(KioskOrderEvent.BackPressed) },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            KioskOrderTopBar(
                categoryName = state.selectedCategoryName,
                onBack = { state.eventSink(KioskOrderEvent.BackPressed) },
                onScanOffer = { state.eventSink(KioskOrderEvent.ScanOfferPressed) },
            )

            KioskMenuGrid(
                items = state.itemsForSelectedCategory,
                onItemTap = { id -> state.eventSink(KioskOrderEvent.ItemTapped(id)) },
                modifier = Modifier.weight(1f),
            )

            KioskCartBar(
                cartSummary = state.cartSummary,
                onCartTap = { state.eventSink(KioskOrderEvent.CartPressed) },
                onCancel = { state.eventSink(KioskOrderEvent.CancelOrderPressed) },
                onPay = { state.eventSink(KioskOrderEvent.PayPressed) },
            )
        }
    }
}

@Composable
private fun KioskCategoryRail(
    categories: List<MenuCategory>,
    selectedCategoryId: String?,
    onSelect: (String) -> Unit,
    onHomePressed: () -> Unit,
) {
    NavigationRail(
        modifier = Modifier
            .width(NavRailWidthDp.dp)
            .fillMaxHeight()
            .testTag(KioskOrderTestTags.NAV_RAIL),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        // HOME tile — branded yellow square at the top of the rail (matches Image 3 layout).
        NavigationRailItem(
            selected = false,
            onClick = onHomePressed,
            icon = {
                Surface(
                    modifier = Modifier
                        .size(NavRailHomeIconDp.dp)
                        .clip(RoundedCornerShape(MockDimens.RadiusSm)),
                    color = MaterialTheme.colorScheme.secondary,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "M",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }
            },
            label = {
                Text(
                    text = "HOME",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            },
            modifier = Modifier
                .height(NavRailItemHeightDp.dp)
                .testTag(KioskOrderTestTags.NAV_RAIL_HOME),
        )

        categories.forEach { category ->
            NavigationRailItem(
                selected = category.id == selectedCategoryId,
                onClick = { onSelect(category.id) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                    selectedTextColor = MaterialTheme.colorScheme.onSecondary,
                    indicatorColor = MaterialTheme.colorScheme.secondary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
                icon = {
                    if (category.iconUrl != null) {
                        AsyncImage(
                            model = category.iconUrl,
                            contentDescription = category.name,
                            modifier = Modifier.size(NavRailIconDp.dp),
                            contentScale = ContentScale.Fit,
                        )
                    } else {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(MockDimens.RadiusSm),
                            modifier = Modifier.size(NavRailIconDp.dp),
                        ) {}
                    }
                },
                label = {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                },
                modifier = Modifier
                    .height(NavRailItemHeightDp.dp)
                    .testTag("${KioskOrderTestTags.NAV_RAIL_CATEGORY_PREFIX}${category.id}"),
            )
        }
    }
}

@Composable
private fun KioskOrderTopBar(
    categoryName: String?,
    onBack: () -> Unit,
    onScanOffer: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(TopBarHeightDp.dp)
            .padding(horizontal = MockDimens.SpacingXl, vertical = MockDimens.SpacingMd)
            .testTag(KioskOrderTestTags.TOP_BAR),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = categoryName.orEmpty(),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(MockDimens.SpacingSm),
            horizontalAlignment = Alignment.End,
        ) {
            TopBarPill(
                text = "Back",
                onClick = onBack,
                modifier = Modifier.testTag(KioskOrderTestTags.BACK_BUTTON),
            )
            TopBarPill(
                text = "Scan Offer",
                onClick = onScanOffer,
                modifier = Modifier.testTag(KioskOrderTestTags.SCAN_OFFER_BUTTON),
            )
        }
    }
}

@Composable
private fun TopBarPill(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(TopBarPillHeightDp.dp)
            .width(TopBarPillWidthDp.dp),
        shape = RoundedCornerShape(MockDimens.RadiusMd),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun KioskMenuGrid(
    items: List<MenuItem>,
    onItemTap: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier
            .fillMaxSize()
            .testTag(KioskOrderTestTags.ITEM_GRID),
        contentPadding = PaddingValues(MockDimens.SpacingLg),
        horizontalArrangement = Arrangement.spacedBy(MockDimens.SpacingLg),
        verticalArrangement = Arrangement.spacedBy(MockDimens.SpacingLg),
    ) {
        items(items, key = { it.id }) { item ->
            KioskMenuItemCard(item = item, onClick = { onItemTap(item.id) })
        }
    }
}

@Composable
private fun KioskMenuItemCard(item: MenuItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(MenuCardHeightDp.dp)
            .testTag("${KioskOrderTestTags.ITEM_CARD_PREFIX}${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(MockDimens.RadiusLg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(MockDimens.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(MockDimens.SpacingSm),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(MockDimens.RadiusMd))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
                if (item.tags.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(MockDimens.RadiusSm),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(MockDimens.SpacingSm),
                    ) {
                        Text(
                            text = item.tags.first(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = MockDimens.SpacingSm, vertical = 4.dp),
                        )
                    }
                }
            }
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (item.calories != null) {
                    Text(
                        text = "${item.calories} Cal",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(text = "")
                }
                Text(
                    text = item.priceFormatted,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun KioskCartBar(
    cartSummary: CartSummary?,
    onCartTap: () -> Unit,
    onCancel: () -> Unit,
    onPay: () -> Unit,
) {
    val itemCount = cartSummary?.itemCount ?: 0
    val total = cartSummary?.total ?: "$0.00"

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .height(CartBarHeightDp.dp)
            .testTag(KioskOrderTestTags.CART_BAR),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(MockDimens.SpacingLg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MockDimens.SpacingLg),
        ) {
            // Loyalty / scan-now tile (matches Image 4 bottom-left).
            Surface(
                onClick = onCartTap,
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(MockDimens.RadiusMd),
                modifier = Modifier
                    .height(CartLoyaltyTileHeightDp.dp)
                    .width(CartLoyaltyTileWidthDp.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(MockDimens.SpacingMd),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Scan now",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = "Earn points",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f),
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "$itemCount items",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = total,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.testTag(KioskOrderTestTags.CART_TOTAL),
                )
            }

            CartActionButton(
                text = "Cancel",
                background = MaterialTheme.colorScheme.errorContainer,
                foreground = MaterialTheme.colorScheme.onErrorContainer,
                onClick = onCancel,
                modifier = Modifier.testTag(KioskOrderTestTags.CANCEL_BUTTON),
            )
            CartActionButton(
                text = "Pay",
                background = MaterialTheme.colorScheme.secondary,
                foreground = MaterialTheme.colorScheme.onSecondary,
                onClick = onPay,
                enabled = itemCount > 0,
                modifier = Modifier.testTag(KioskOrderTestTags.PAY_BUTTON),
            )
        }
    }
}

@Composable
private fun CartActionButton(
    text: String,
    background: Color,
    foreground: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = if (enabled) background else background.copy(alpha = 0.4f),
        shape = RoundedCornerShape(MockDimens.RadiusMd),
        modifier = modifier
            .height(CartButtonHeightDp.dp)
            .width(CartButtonWidthDp.dp)
            .border(
                width = 0.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(MockDimens.RadiusMd),
            ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall,
                color = if (enabled) foreground else foreground.copy(alpha = 0.5f),
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

// Kiosk dimensions — sized for vertical 32" hardware at standing distance.
private const val NavRailWidthDp = 200
private const val NavRailItemHeightDp = 120
private const val NavRailIconDp = 56
private const val NavRailHomeIconDp = 100
private const val TopBarHeightDp = 160
private const val TopBarPillHeightDp = 64
private const val TopBarPillWidthDp = 200
private const val MenuCardHeightDp = 360
private const val CartBarHeightDp = 140
private const val CartLoyaltyTileHeightDp = 100
private const val CartLoyaltyTileWidthDp = 160
private const val CartButtonHeightDp = 96
private const val CartButtonWidthDp = 200
