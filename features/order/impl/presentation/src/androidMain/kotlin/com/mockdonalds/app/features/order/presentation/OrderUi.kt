package com.mockdonalds.app.features.order.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mockdonalds.app.core.theme.MockDimens
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.core.theme.adaptiveBottomBarPadding
import com.mockdonalds.app.core.theme.isCompactHeight
import com.mockdonalds.app.features.order.api.domain.FeaturedItem
import com.mockdonalds.app.features.order.api.navigation.OrderScreen
import com.mockdonalds.app.features.order.api.ui.OrderTestTags
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(OrderScreen::class, AppScope::class)
@Inject
@Composable
fun OrderUi(state: OrderUiState, modifier: Modifier = Modifier) {
    val landscape = isCompactHeight()

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = adaptiveBottomBarPadding())
                .statusBarsPadding(),
        ) {
            // Category Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = MockDimens.SpacingXl),
                horizontalArrangement = Arrangement.spacedBy(MockDimens.SpacingMd),
                modifier = Modifier.padding(bottom = MockDimens.SpacingXxl),
            ) {
                items(state.categories) { category ->
                    val isSelected = category.id == state.selectedCategoryId
                    Box(
                        modifier = Modifier
                            .testTag("${OrderTestTags.CATEGORY_CHIP}-${category.id}")
                            .background(
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                },
                                CircleShape,
                            )
                            .clickable { state.eventSink(OrderEvent.CategorySelected(category.id)) }
                            .padding(horizontal = MockDimens.SpacingXl, vertical = MockDimens.SpacingSm),
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) {
                                MockDonaldsTheme.extendedColors.onPrimaryButton
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                }
            }

            // Featured Items — 2-column grid in landscape, single column in portrait
            if (landscape) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = MockDimens.SpacingXl,
                    ).testTag(OrderTestTags.FEATURED_ITEMS_SECTION),
                    verticalArrangement = Arrangement.spacedBy(MockDimens.SpacingXxl),
                ) {
                    state.featuredItems.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(MockDimens.SpacingLg),
                        ) {
                            pair.forEach { item ->
                                FeaturedItemCard(
                                    item = item,
                                    onAddToOrder = { state.eventSink(OrderEvent.AddToOrder(item.id)) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("${OrderTestTags.FEATURED_ITEM_CARD}-${item.id}"),
                                )
                            }
                            if (pair.size == 1) {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.padding(
                        horizontal = MockDimens.SpacingXl,
                    ).testTag(OrderTestTags.FEATURED_ITEMS_SECTION),
                    verticalArrangement = Arrangement.spacedBy(MockDimens.SpacingXxxl),
                ) {
                    state.featuredItems.forEach { item ->
                        FeaturedItemCard(
                            item = item,
                            onAddToOrder = { state.eventSink(OrderEvent.AddToOrder(item.id)) },
                            modifier = Modifier.testTag("${OrderTestTags.FEATURED_ITEM_CARD}-${item.id}"),
                        )
                    }
                }
            }
        }

        // Floating Cart Bar
        state.cartSummary?.let { cart ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(MockDimens.SpacingXl)
                    .padding(bottom = 64.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(MockDimens.RadiusMd))
                    .background(MaterialTheme.colorScheme.primary)
                    .testTag(OrderTestTags.CART_BAR)
                    .clickable { state.eventSink(OrderEvent.CartClicked) }
                    .padding(horizontal = MockDimens.SpacingXl, vertical = MockDimens.SpacingLg),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MockDimens.SpacingMd),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(MockDimens.SpacingXxl)
                                .background(
                                    MockDonaldsTheme.extendedColors.primaryDarker.copy(alpha = 0.2f),
                                    CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${cart.itemCount}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MockDonaldsTheme.extendedColors.onPrimaryButton,
                            )
                        }
                        Text(
                            text = "${cart.itemCount} ITEMS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MockDonaldsTheme.extendedColors.onPrimaryButton,
                            letterSpacing = 2.sp,
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MockDimens.SpacingSm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = cart.total,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = MockDonaldsTheme.extendedColors.onPrimaryButton,
                        )
                        Text(
                            text = "->",
                            color = MockDonaldsTheme.extendedColors.onPrimaryButton,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeaturedItemCard(item: FeaturedItem, onAddToOrder: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(MockDimens.SpacingLg)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(MockDimens.RadiusMd))
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            Box(
                modifier = Modifier
                    .padding(MockDimens.SpacingLg)
                    .align(if (item.isPrimary) Alignment.TopEnd else Alignment.TopStart)
                    .background(
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        CircleShape,
                    )
                    .padding(horizontal = MockDimens.SpacingMd, vertical = MockDimens.SpacingXs),
            ) {
                Text(
                    text = item.tag,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(MockDimens.SpacingSm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black, fontSize = 32.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 36.sp,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = item.price,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(start = MockDimens.SpacingLg),
                )
            }

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = MockDimens.SpacingLg),
            )

            Button(
                onClick = onAddToOrder,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                modifier = Modifier.fillMaxWidth().testTag("${OrderTestTags.ADD_TO_ORDER_BUTTON}-${item.id}"),
                shape = RoundedCornerShape(MockDimens.RadiusSm),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (item.isPrimary) {
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MockDonaldsTheme.extendedColors.primaryDark,
                                    ),
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surfaceContainerHighest,
                                        MaterialTheme.colorScheme.surfaceContainerHighest,
                                    ),
                                )
                            },
                        )
                        .padding(vertical = MockDimens.SpacingLg),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MockDimens.SpacingSm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "+",
                            color = if (item.isPrimary) {
                                MockDonaldsTheme.extendedColors.onPrimaryButton
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "ADD TO ORDER",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (item.isPrimary) {
                                MockDonaldsTheme.extendedColors.onPrimaryButton
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                        )
                    }
                }
            }
        }
    }
}
