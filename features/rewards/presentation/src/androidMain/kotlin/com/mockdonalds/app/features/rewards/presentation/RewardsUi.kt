package com.mockdonalds.app.features.rewards.presentation

import androidx.compose.foundation.background
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
import coil3.compose.AsyncImage
import com.mockdonalds.app.features.rewards.api.navigation.RewardsScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope

@CircuitInject(RewardsScreen::class, AppScope::class)
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
        Box(modifier = Modifier.fillMaxWidth()) {
            // Gold Glow Background
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
                        text = "5,432",
                        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black, fontSize = 64.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 64.sp
                    )
                    Text(
                        text = "PTS",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFFFDF99) // secondary-fixed
                    )
                }

                // Tier Progress
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
                            text = "NEXT REWARD: GOLDEN BURGER",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "568 PTS TO GO",
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
                                .fillMaxWidth(0.88f)
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

        // The Vault Specials (Asymmetric/Bento)
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
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
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Large Feature
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(256.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuB5_cdcHUFE84dPqS6Myqe6DPjZLm7pZA-e1xL-BJHKW6FCi5icL1OaYz6O0QLr7dMgVSBGZTVSR3DW_x8R6vqU-1yGdcX4FitIvyYNz2CpwgdZY3RzxncTcPO2LXm58UMBTeT3MfGELg7SGehbrXvkUKdOhMUnPoHl4z5gxJMOzk8axC97CfHSaJWx-eSv0ZrGXjxJslTIoNQTYmcHWAYA7aknA-NTcH69D36Q3_7mthLoelcYqIPuYCloGsEcM3-a-aehWSYx23g",
                    contentDescription = "The Midnight Wagyu",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)) // overlay
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
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondary, CircleShape)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "EXCLUSIVE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = Color(0xFF3F2E00) // on-secondary
                        )
                    }
                    Text(
                        text = "The Midnight Wagyu",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                    Text(
                        text = "2,500 PTS",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Secondary Specials
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Truffle Penne
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
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
                            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAtHYBNce4Wzgh78FbEg0YNoKbsdq-hHJUw2wrcV-wEYo3SNwtvHYwoqnnnOIGdLp43aAMfr8hYCP8COxfQVNjzEr9KOR0efa8_4WR8xQE-5h0zGVsaG0tc0NPiahRIFU5FXttF6_u6UrOdHCnmJgOhjeyNsgmLOv0rclMNNkWmxsfgLjH2UpmjWyzuir5SJ4y5uGhKA0Ffw4iBaWJqDdEvJVixU4liT-OqUP7F6dSYbT7IYKonLHCnzcDKGo0AZCKSsovifdDdnlM",
                            contentDescription = "Truffle Penne",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column {
                        Text(
                            text = "Truffle Penne",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "1,200 PTS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Lava Soufflé
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
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
                            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCGwqtRYRBh2rJ9gv9F5Aj-NJIU0LV1aTQCuE-rAG-hc0Sp4HxZe68TmrfldrKtSWAyNhHps0VArNVduvzRYn7iju2ZUzmC0Ld1HgKNHCSgjcSPI6EiYYhlrRhTJiz9Lk5wmSFvZ9vjwTG6l6YLqr16HFuz9DHEoW5swuJDQYUGVMkxW-W8T_aJiKr8iM42PRBgnhBxVioMyJmqyIeZG0j4BgGaCLyK-v6mgNzlU5KAQmWDzhF4Vfr0JwBE4kOFH03cgiMyLgj8cLY",
                            contentDescription = "Lava Soufflé",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column {
                        Text(
                            text = "Lava Soufflé",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "850 PTS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // Earning History
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Earning History",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // History Items
            HistoryItem(
                title = "Late Night Diner Order",
                subtitle = "Oct 24 • Order #8821",
                points = "+125",
                isPositive = true,
                icon = "🍽️",
                containerColor = MaterialTheme.colorScheme.surface
            )
            HistoryItem(
                title = "The Midnight Wagyu",
                subtitle = "Oct 21 • Reward Redeemed",
                points = "-2,500",
                isPositive = false,
                icon = "🎁",
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
            HistoryItem(
                title = "Birthday Bonus",
                subtitle = "Oct 18 • Annual Gift",
                points = "+500",
                isPositive = true,
                icon = "🎉",
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
fun HistoryItem(
    title: String,
    subtitle: String,
    points: String,
    isPositive: Boolean,
    icon: String,
    containerColor: Color
) {
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
                Text(text = icon)
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = points,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
            color = if (isPositive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
        )
    }
}
