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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import coil3.compose.AsyncImage
import com.mockdonalds.app.core.theme.PlatterDimens
import com.mockdonalds.app.core.theme.SamplePlatterTheme
import com.mockdonalds.app.core.theme.adaptiveBottomBarPadding
import com.mockdonalds.app.features.order.api.domain.MenuItem
import com.mockdonalds.app.features.order.api.navigation.CategoryDetailScreen
import com.mockdonalds.app.features.order.api.ui.CategoryDetailTestTags
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(CategoryDetailScreen::class, AppScope::class)
@Inject
@Composable
fun CategoryDetailUi(state: CategoryDetailUiState, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            CategoryDetailTopBar(
                title = state.categoryName,
                onBackClick = { state.eventSink(CategoryDetailEvent.BackPressed) },
                modifier = Modifier.testTag(CategoryDetailTestTags.TOP_BAR),
            )

            LazyColumn(
                contentPadding = PaddingValues(
                    start = PlatterDimens.SpacingXl,
                    end = PlatterDimens.SpacingXl,
                    top = PlatterDimens.SpacingLg,
                    bottom = adaptiveBottomBarPadding() + PlatterDimens.CartBarOffset,
                ),
                verticalArrangement = Arrangement.spacedBy(PlatterDimens.SpacingXxl),
            ) {
                items(state.items, key = { it.id }) { item ->
                    MenuItemCard(
                        item = item,
                        onAddToOrder = { state.eventSink(CategoryDetailEvent.AddToOrder(item.id)) },
                        modifier = Modifier.testTag("${CategoryDetailTestTags.MENU_ITEM_CARD}-${item.id}"),
                    )
                }
            }
        }

        state.cartSummary?.let { cart ->
            OrderCartBar(
                cart = cart,
                onClick = { state.eventSink(CategoryDetailEvent.CartClicked) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .testTag(CategoryDetailTestTags.CART_BAR),
            )
        }
    }
}

@Composable
private fun CategoryDetailTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(PlatterDimens.SpacingXl),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(PlatterDimens.SpacingXxl)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .clickable { onBackClick() }
                .testTag(CategoryDetailTestTags.BACK_BUTTON),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = PlatterDimens.SpacingLg),
        )
    }
}

@Composable
private fun MenuItemCard(
    item: MenuItem,
    onAddToOrder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(PlatterDimens.SpacingMd)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(PlatterDimens.RadiusMd))
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Text(
            text = item.title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = item.restaurantChain,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        item.servingSize?.let { serving ->
            Text(
                text = serving,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Button(
            onClick = onAddToOrder,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("${CategoryDetailTestTags.ADD_TO_ORDER_BUTTON}-${item.id}"),
            shape = RoundedCornerShape(PlatterDimens.RadiusSm),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .padding(vertical = PlatterDimens.SpacingLg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+ ADD TO ORDER",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}
