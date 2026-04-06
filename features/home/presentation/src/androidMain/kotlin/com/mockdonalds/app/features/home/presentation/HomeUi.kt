package com.mockdonalds.app.features.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import com.mockdonalds.app.features.home.api.navigation.HomeScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope

@CircuitInject(HomeScreen::class, AppScope::class)
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
                text = "Alex Mercer",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Hero Promotional Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
        ) {
            AsyncImage(
                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuD3XLmAyDLCbsMni75FhYeTO5jDtRg2NYPJKTxNbg4aleYFXC5xEXR3Y5hbq8-q9a5FKcQX6SBsY9mW5l3LFaBdXetovbtu6rFyBGMpAtYcCGVhtAhSsLhMOA9SzEQq6UUAVNhJMkSlDOtmR_L3LrJaSc-HN2aOIu7iEpAwKNrEWGP6Dk9E0tzIWrUEUQZCkrXKLjo2RtdB-aI_oRSLdnTwbnQPdXM5KV-VPld0gAdzjzoJrlCTM-h3Q5dEoLNSC587K_0ossB5VKQ",
                contentDescription = "Midnight Truffle Burger",
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
                        text = "LIMITED EDITION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF584200) // on-secondary-container
                    )
                }

                Text(
                    text = "Midnight Truffle Burger",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.displayMedium.fontSize * 0.9f
                )

                Text(
                    text = "Double-aged Wagyu beef, shaved Perigord truffles, and gold-dusted brioche.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Button(
                    onClick = { },
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Experience Now",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFFFEBE8) // on-primary-container
                            )
                        }
                    }
                }
            }
        }

        // Recent Cravings Section
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
                val cravings = listOf(
                    Triple("Smoky Bleu Royale", "$18.50 • 2 days ago", "https://lh3.googleusercontent.com/aida-public/AB6AXuAEUcSbBQQ1D_rTaoTt2M29Lrvc1k31ru-Yy2v9jETAX6Imb1Z3xkCnOGzwODlYEAmG-R6fG5GntSwCtMAyPKuKob4H0uGzqPSVCOkfE7akZl2l1MV_VPmX-9ixfI5RwMKqW6N2IX27gFWM7CErxP79myBoFx-sbk1M8I7I1lQ0aDnB0ung7sBer2nkJH2VaCbA4dlpnq0vXETYCe1I0rCRBosLC-VKu0aQONfSgfkE_LcYwSpNIIExgurOMxr6uB4R8nQlTeS622s"),
                    Triple("Roseway Fries (L)", "$6.20 • 5 days ago", "https://lh3.googleusercontent.com/aida-public/AB6AXuAek0b7xlNHx9hvtl19bOmndTLwRB59O51tbuaU6ddCqFCFHI6dWkI_9_1NS1MSfhg9-VPJGH9Ete-6g-o1qPhb8Q1HKjd_LA2w_Si34P5v2NyiJwlP3SH-zp3fXQwvSZQHuv_VWKgBEqo1pHgqATbotVPqPv876wMCIDmrlT8U6vNb3MgskQ1Jg2mBjPu2mecMMi697HR-20rVDQQ4aFpxkIUTJwQg1WFVa9moh-LRTpsH-jKrtb9PiyIGEXeTtCLGJkrY8WQN5sM"),
                    Triple("Velvet Cola Fizz", "$4.50 • 1 week ago", "https://lh3.googleusercontent.com/aida-public/AB6AXuDIID3SWN4ATvLcZ6sUpRRaN-NTdxu7MHXohCtFpcxQLSR5DjiSX14VgesUoV5lcNFJ-HGoLoEUkQDJNu7_WH9ftDn_N7OR63_ryAaOtRfIqegQ2VSeWkPQNfJuQtsFdElJH3gLhP-m-fGpl_mkNAzbEO-gfRSGjUxEWVGbNyOmn1OkstqlIRZinzg0cBA_jBBo7O7nGbos-OFvb1m6yeAJwLr53Yw32vXo62Tnkzxe8jg5wVEeBzEWFIxwZqt5MDwlm7HEG8_4Ln4")
                )
                items(cravings) { item ->
                    CravingCard(item.first, item.second, item.third)
                }
            }
        }

        // Quick Actions Bento Grid
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Explore",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "O", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary) // Placeholder icon
                        Column {
                            Text(text = "Find", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "A Lounge Near You", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "🎁", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary) // Placeholder icon
                        Column {
                            Text(text = "Gift Cards", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "Share the Flavor", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
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
                            Text(text = "🎧", color = MaterialTheme.colorScheme.secondary) // Placeholder icon
                        }
                        Column {
                            Text(text = "Concierge Support", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "Direct line to our kitchen masters", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                    Text(text = ">", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun CravingCard(title: String, subtitle: String, imageUrl: String) {
    Column(
        modifier = Modifier
            .width(288.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = title,
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
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
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
                Text(text = "+", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold) // Placeholder reorder icon
            }
        }
    }
}
