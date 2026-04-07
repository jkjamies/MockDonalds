package com.mockdonalds.app.features.rewards.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import coil3.compose.AsyncImage
import com.mockdonalds.app.features.rewards.api.domain.HistoryEntry
import com.mockdonalds.app.features.rewards.api.domain.VaultSpecial
import com.mockdonalds.app.features.rewards.api.navigation.RewardsScreen
import com.mockdonalds.app.features.rewards.api.ui.RewardsTestTags
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(RewardsScreen::class, AppScope::class)
@Inject
@Composable
fun RewardsUi(state: RewardsUiState, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
            .padding(bottom = 128.dp)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(48.dp)
    ) {

        // Points Hero Section
        state.progress?.let { progress ->
            Box(modifier = Modifier.fillMaxWidth().testTag(RewardsTestTags.POINTS_SECTION)) {
                Box(
                    modifier = Modifier
                        .size(256.dp)
                        .offset(x = (-48).dp, y = (-48).dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                    Color.Transparent
                                ),
                                radius = 256f
                            )
                        )
                )

                Column {
                    Text(
                        text = "CURRENT BALANCE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "%,d".format(progress.currentPoints),
                            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black, fontSize = 64.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 64.sp
                        )
                        Text(
                            text = "PTS",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFFDF99)
                        )
                    }

                    Column(
                        modifier = Modifier.padding(top = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "NEXT REWARD: ${progress.nextRewardName.uppercase()}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${progress.pointsToNextReward} PTS TO GO",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
                                .clip(CircleShape)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress.progressFraction)
                                    .fillMaxHeight()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }

        // The Vault Specials
        if (state.vaultSpecials.isNotEmpty()) {
            Column(modifier = Modifier.testTag(RewardsTestTags.VAULT_SPECIALS_SECTION), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "The Vault Specials",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "VIEW ALL",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.testTag(RewardsTestTags.VIEW_ALL).clickable { state.eventSink(RewardsEvent.ViewAllClicked) }
                    )
                }

                // Featured vault special
                state.vaultSpecials.firstOrNull { it.isFeatured }?.let { featured ->
                    FeaturedVaultCard(
                        special = featured,
                        onClick = { state.eventSink(RewardsEvent.VaultSpecialClicked(featured.id)) },
                        modifier = Modifier.testTag("${RewardsTestTags.FEATURED_VAULT_CARD}-${featured.id}"),
                    )
                }

                // Secondary specials
                val secondary = state.vaultSpecials.filter { !it.isFeatured }
                if (secondary.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        secondary.forEach { special ->
                            VaultSpecialCard(
                                special = special,
                                onClick = { state.eventSink(RewardsEvent.VaultSpecialClicked(special.id)) },
                                modifier = Modifier.weight(1f).testTag("${RewardsTestTags.VAULT_SPECIAL_CARD}-${special.id}"),
                            )
                        }
                    }
                }
            }
        }

        // Earning History
        if (state.history.isNotEmpty()) {
            Column(modifier = Modifier.testTag(RewardsTestTags.HISTORY_SECTION), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Earning History",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                state.history.forEachIndexed { index, entry ->
                    HistoryItem(
                        entry = entry,
                        containerColor = if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerLow,
                    )
                }
            }
        }
    }
}

@Composable
fun FeaturedVaultCard(special: VaultSpecial, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(256.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = special.imageUrl,
            contentDescription = special.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f))
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

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            special.tag?.let { tag ->
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = Color(0xFF3F2E00)
                    )
                }
            }
            Text(
                text = special.title,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )
            Text(
                text = special.pointsCost,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun VaultSpecialCard(special: VaultSpecial, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            AsyncImage(
                model = special.imageUrl,
                contentDescription = special.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Column {
            Text(
                text = special.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = special.pointsCost,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun HistoryItem(entry: HistoryEntry, containerColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .padding(20.dp),
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
                Text(text = entry.icon)
            }
            Column {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = entry.subtitle,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = entry.points,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
            color = if (entry.isPositive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
        )
    }
}
