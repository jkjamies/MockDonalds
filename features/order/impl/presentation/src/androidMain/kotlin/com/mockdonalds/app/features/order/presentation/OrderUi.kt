package com.mockdonalds.app.features.order.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mockdonalds.app.core.theme.MockDimens
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.core.theme.adaptiveBottomBarPadding
import com.mockdonalds.app.features.order.api.domain.CartSummary
import com.mockdonalds.app.features.order.api.domain.CategoryPreview
import com.mockdonalds.app.features.order.api.navigation.OrderScreen
import com.mockdonalds.app.features.order.api.ui.OrderTestTags
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(OrderScreen::class, AppScope::class)
@Inject
@Composable
fun OrderUi(state: OrderUiState, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                start = MockDimens.SpacingXl,
                end = MockDimens.SpacingXl,
                top = MockDimens.SpacingXl,
                bottom = adaptiveBottomBarPadding() + MockDimens.CartBarOffset,
            ),
            verticalArrangement = Arrangement.spacedBy(MockDimens.SpacingLg),
        ) {
            items(state.categoryPreviews, key = { it.id }) { preview ->
                CategoryPreviewCard(
                    preview = preview,
                    onTap = { state.eventSink(OrderEvent.CategoryTapped(preview.id)) },
                    modifier = Modifier.testTag("${OrderTestTags.CATEGORY_PREVIEW_CARD}-${preview.id}"),
                )
            }
        }

        state.cartSummary?.let { cart ->
            OrderCartBar(
                cart = cart,
                onClick = { state.eventSink(OrderEvent.CartClicked) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .testTag(OrderTestTags.CART_BAR),
            )
        }
    }
}

@Composable
private fun CategoryPreviewCard(
    preview: CategoryPreview,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2f)
            .clip(RoundedCornerShape(MockDimens.RadiusMd))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable { onTap() },
    ) {
        preview.firstItemImageUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = preview.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f),
                        ),
                    ),
                ),
        )

        Text(
            text = preview.name,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(MockDimens.SpacingXl),
        )
    }
}

@Composable
internal fun OrderCartBar(
    cart: CartSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(MockDimens.SpacingXl)
            .padding(bottom = MockDimens.CartBarOffset)
            .fillMaxWidth()
            .clip(RoundedCornerShape(MockDimens.RadiusMd))
            .background(MaterialTheme.colorScheme.primary)
            .clickable { onClick() }
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
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MockDonaldsTheme.extendedColors.onPrimaryButton,
                )
            }
        }
    }
}
