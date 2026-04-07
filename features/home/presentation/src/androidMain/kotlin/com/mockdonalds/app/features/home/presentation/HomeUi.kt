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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mockdonalds.app.features.home.api.domain.Craving
import com.mockdonalds.app.features.home.api.domain.ExploreItem
import com.mockdonalds.app.features.home.api.navigation.HomeScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(HomeScreen::class, AppScope::class)
@Inject
@Composable
fun HomeUi(state: HomeUiState, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(bottom = 128.dp)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(48.dp)
    ) {
        // Greeting Section
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "GOOD EVENING, GOURMET",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = state.userName,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Hero Promotional Banner
        state.heroPromotion?.let { hero ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
            ) {
                AsyncImage(
                    model = hero.imageUrl,
                    contentDescription = hero.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background.copy(alpha = 0.9f))
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(MaterialTheme.colorScheme.background.copy(alpha = 0.6f), Color.Transparent)
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(32.dp)
                        .fillMaxWidth(0.8f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondary, CircleShape)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = hero.tag,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF584200)
                        )
                    }

                    Text(
                        text = hero.title,
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = MaterialTheme.typography.displayMedium.fontSize * 0.9f
                    )

                    Text(
                        text = hero.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Button(
                        onClick = { state.eventSink(HomeEvent.HeroCtaClicked) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        modifier = Modifier.padding(top = 16.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, Color(0xFF930003)))
                                )
                                .padding(horizontal = 32.dp, vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = hero.ctaText,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFFFEBE8)
                            )
                        }
                    }
                }
            }
        }

        // Recent Cravings Section
        if (state.recentCravings.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Recent Cravings",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    items(state.recentCravings) { craving ->
                        CravingCard(
                            craving = craving,
                            onClick = { state.eventSink(HomeEvent.CravingClicked(craving.id)) },
                        )
                    }
                }
            }
        }

        // Quick Actions Bento Grid
        if (state.exploreItems.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Explore",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // First two items as side-by-side cards
                val gridItems = state.exploreItems.take(2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    gridItems.forEach { item ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .clickable { state.eventSink(HomeEvent.ExploreItemClicked(item.id)) }
                                .padding(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = item.icon, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
                                Column {
                                    Text(text = item.title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                    Text(text = item.subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }

                // Remaining items as full-width rows
                state.exploreItems.drop(2).forEach { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .clickable { state.eventSink(HomeEvent.ExploreItemClicked(item.id)) }
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = item.icon, color = MaterialTheme.colorScheme.secondary)
                                }
                                Column {
                                    Text(text = item.title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                    Text(text = item.subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }
                            Text(text = ">", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun CravingCard(craving: Craving, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(288.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = craving.imageUrl,
            contentDescription = craving.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = craving.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = craving.subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
