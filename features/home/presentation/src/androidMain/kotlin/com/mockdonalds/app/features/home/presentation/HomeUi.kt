package com.mockdonalds.app.features.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import coil3.compose.AsyncImage
import com.mockdonalds.app.core.theme.MockDimens
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.core.theme.adaptiveBottomBarPadding
import com.mockdonalds.app.core.theme.adaptiveHeroHeight
import com.mockdonalds.app.core.theme.isCompactHeight
import com.mockdonalds.app.features.home.api.domain.Craving
import com.mockdonalds.app.features.home.api.navigation.HomeScreen
import com.mockdonalds.app.features.home.api.ui.HomeTestTags
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(HomeScreen::class, AppScope::class)
@Inject
@Composable
fun HomeUi(state: HomeUiState, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    val landscape = isCompactHeight()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(bottom = adaptiveBottomBarPadding())
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(MockDimens.SpacingXxxl),
    ) {
        // Greeting Section
        Column(modifier = Modifier.padding(horizontal = MockDimens.SpacingXl)) {
            Text(
                text = "GOOD EVENING, GOURMET",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = MockDimens.SpacingXs),
            )
            Text(
                text = state.userName,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag(HomeTestTags.USER_NAME),
            )
        }

        // Hero Promotional Banner
        state.heroPromotion?.let { hero ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(adaptiveHeroHeight())
                    .testTag(HomeTestTags.HERO_BANNER),
            ) {
                AsyncImage(
                    model = hero.imageUrl,
                    contentDescription = hero.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                                ),
                            ),
                        ),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(MockDimens.SpacingXxl)
                        .fillMaxWidth(0.8f),
                    verticalArrangement = Arrangement.spacedBy(MockDimens.SpacingLg),
                ) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondary, CircleShape)
                            .padding(horizontal = MockDimens.SpacingMd, vertical = MockDimens.SpacingXs),
                    ) {
                        Text(
                            text = hero.tag,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MockDonaldsTheme.extendedColors.onSecondaryTag,
                        )
                    }

                    Text(
                        text = hero.title,
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = MaterialTheme.typography.displayMedium.fontSize * 0.9f,
                    )

                    Text(
                        text = hero.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )

                    Button(
                        onClick = { state.eventSink(HomeEvent.HeroCtaClicked) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        modifier = Modifier.padding(top = MockDimens.SpacingLg).testTag(HomeTestTags.HERO_CTA_BUTTON),
                        shape = RoundedCornerShape(MockDimens.RadiusSm),
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MockDonaldsTheme.extendedColors.primaryDark,
                                        ),
                                    ),
                                )
                                .padding(horizontal = MockDimens.SpacingXxl, vertical = MockDimens.SpacingLg),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = hero.ctaText,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MockDonaldsTheme.extendedColors.onPrimaryButton,
                            )
                        }
                    }
                }
            }
        }

        // Recent Cravings Section
        if (state.recentCravings.isNotEmpty()) {
            Column(
                modifier = Modifier.testTag(HomeTestTags.RECENT_CRAVINGS_SECTION),
                verticalArrangement = Arrangement.spacedBy(MockDimens.SpacingXl),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MockDimens.SpacingXl),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = "Recent Cravings",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(MockDimens.SpacingXl),
                    contentPadding = PaddingValues(horizontal = MockDimens.SpacingXl),
                ) {
                    items(state.recentCravings) { craving ->
                        CravingCard(
                            craving = craving,
                            onClick = { state.eventSink(HomeEvent.CravingClicked(craving.id)) },
                            modifier = Modifier.testTag("${HomeTestTags.CRAVING_CARD}-${craving.id}"),
                        )
                    }
                }
            }
        }

        // Quick Actions Bento Grid
        if (state.exploreItems.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(horizontal = MockDimens.SpacingXl).testTag(HomeTestTags.EXPLORE_SECTION),
                verticalArrangement = Arrangement.spacedBy(MockDimens.SpacingXl),
            ) {
                Text(
                    text = "Explore",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Side-by-side cards — 3 in landscape, 2 in portrait
                val gridCount = if (landscape) 3 else 2
                val gridItems = state.exploreItems.take(gridCount)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MockDimens.SpacingLg),
                ) {
                    gridItems.forEach { item ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(MockDimens.ThumbnailHeight)
                                .clip(RoundedCornerShape(MockDimens.RadiusMd))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .testTag("${HomeTestTags.EXPLORE_ITEM}-${item.id}")
                                .clickable { state.eventSink(HomeEvent.ExploreItemClicked(item.id)) }
                                .padding(MockDimens.SpacingXl),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = item.icon,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                                Column {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = item.subtitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    )
                                }
                            }
                        }
                    }
                }

                // Remaining items as full-width rows
                state.exploreItems.drop(gridCount).forEach { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(MockDimens.RadiusMd))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .testTag("${HomeTestTags.EXPLORE_ITEM}-${item.id}")
                            .clickable { state.eventSink(HomeEvent.ExploreItemClicked(item.id)) }
                            .padding(MockDimens.SpacingXl),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(MockDimens.SpacingLg),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(MockDimens.IconLg)
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(text = item.icon, color = MaterialTheme.colorScheme.secondary)
                                }
                                Column {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = item.subtitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    )
                                }
                            }
                            Text(text = ">", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(MockDimens.SpacingXl))
    }
}

@Composable
fun CravingCard(craving: Craving, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(MockDimens.CardWidth)
            .clip(RoundedCornerShape(MockDimens.RadiusMd))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = craving.imageUrl,
            contentDescription = craving.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(MockDimens.CardHeight),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = craving.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = craving.subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
            Box(
                modifier = Modifier
                    .size(MockDimens.IconMd)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "+", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
