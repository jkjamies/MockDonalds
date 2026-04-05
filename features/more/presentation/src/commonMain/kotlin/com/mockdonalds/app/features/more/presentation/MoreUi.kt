package com.mockdonalds.app.features.more.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mockdonalds.app.features.more.api.navigation.MoreScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope

@CircuitInject(MoreScreen::class, AppScope::class)
@Composable
fun MoreUi(state: MoreUiState, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
            .padding(bottom = 128.dp)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {

        // User Profile Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .clickable { }
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val primary = MaterialTheme.colorScheme.primary
                val secondary = MaterialTheme.colorScheme.secondary
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .drawWithContent {
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    0f to primary,
                                    0.45f to secondary,
                                    0.65f to secondary,
                                    1f to primary,
                                ),
                                style = Stroke(width = 3.dp.toPx()),
                            )
                            drawContent()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuBFVaei4-VUjNq0l_ZGz6rArkAkp-dfveXXlPEl_pcZxRm8PiBdB1Ou7OAx2lEYYD9sPqyC0Rw34pGxWZ_0NWGUvTRio8O5wqu0oobfn5TWDTglgEvOWyPDn6rtcYPzMO5PK6I5IvCQfhWru2-mYEE8s45hGeUZIAQd7mvcCjvhIV9c4vrSetu5K1hBrZmIUQu2TND-knQQzGfp7U_8aQ0JPl_FgLwTXM47MsuJKCkkhDodQ_0vCMZbkh6SRx_s4Qmwlk14O8d1sgQ",
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape),
                    )
                }
                Column {
                    Text(
                        text = "Alex Grayson",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Gold Member • 1,240 pts",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
            Text(text = ">", color = MaterialTheme.colorScheme.secondary) // Placeholder Icon
        }

        // Menu List
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            MenuItem(icon = "🕒", title = "Recents", isOdd = true)
            MenuItem(icon = "📍", title = "Locations", isOdd = false)
            MenuItem(icon = "🥗", title = "Nutrition", isOdd = true)
            MenuItem(icon = "❓", title = "Help", isOdd = false)
            MenuItem(icon = "💼", title = "Careers", isOdd = true)
        }

        // Join the Team Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            AsyncImage(
                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDtoKS4itUpfiQzJW9FGblMq9_3wzFqLR5CaS2eM929pYK-KWYvYqQiXcGfWz8ZVUlPcU1hmo0qseeHENBB_sP17bYCskdZ9VPfrIdYy7P63B5tGH6kgBQmn_i0RAanG3-y3r2F2U9G7IdqC5pgPPtd0CVRV-7jjEKtk7VGHqiwH40htvVQRSEZSqoJZ0hnlFw0FvqVNCM5k7pn_eI5N9zunkr86XGaaEl2qddd7Zld_sJOFnulnp_tJ8eqVDNAqvGdId-JcKf1t2s",
                contentDescription = "Kitchen Staff",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF000000), // neutral-950 roughly
                                Color(0xFF000000).copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Join the Team",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFFF5F5F5), // neutral-100
                    lineHeight = 36.sp
                )
                Text(
                    text = "Craft the future of late-night dining with us. We're looking for culinary masters.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFD4D4D4) // neutral-300
                )
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    modifier = Modifier.padding(top = 8.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, Color(0xFF930003)))
                            )
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "View Openings",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItem(icon: String, title: String, isOdd: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isOdd) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable { }
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, color = MaterialTheme.colorScheme.onSurfaceVariant) // Placeholder Icon
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(text = ">", color = MaterialTheme.colorScheme.onSurfaceVariant) // Placeholder Icon
    }
}
