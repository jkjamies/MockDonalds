package com.mockdonalds.app.features.kiosk.attract.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mockdonalds.app.core.theme.MockDimens
import com.mockdonalds.app.features.kiosk.attract.api.domain.Ad
import com.mockdonalds.app.features.kiosk.attract.api.navigation.AttractScreen
import com.mockdonalds.app.features.kiosk.attract.api.ui.AttractTestTags
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@CircuitInject(AttractScreen::class, AppScope::class)
@Composable
fun AttractUi(state: AttractUiState, modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(
        initialPage = state.currentIndex.coerceAtMost(state.ads.size.coerceAtLeast(1) - 1).coerceAtLeast(0),
        pageCount = { state.ads.size.coerceAtLeast(1) },
    )

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != state.currentIndex) {
            state.eventSink(AttractEvent.IndexChanged(pagerState.currentPage))
        }
    }

    LaunchedEffect(state.ads.size, state.rotationSeconds) {
        if (state.ads.size <= 1 || state.rotationSeconds <= 0) return@LaunchedEffect
        while (true) {
            delay(state.rotationSeconds.seconds)
            val next = (pagerState.currentPage + 1) % state.ads.size
            pagerState.animateScrollToPage(next)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(AttractTestTags.SCREEN)
            .pointerInput(Unit) {
                detectTapGestures { state.eventSink(AttractEvent.TouchToOrder) }
            },
    ) {
        if (state.ads.isEmpty()) {
            BrandFallbackPanel()
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(AttractTestTags.PAGER),
            ) { page ->
                AttractAdPage(ad = state.ads[page])
            }
        }

        TouchToOrderOverlay(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = MockDimens.SpacingXxxl),
        )
    }
}

@Composable
private fun AttractAdPage(ad: Ad, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            model = ad.imageUrl,
            contentDescription = ad.headline,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .testTag("${AttractTestTags.AD_IMAGE}-${ad.id}"),
        )
        // Bottom darkening gradient so headline text stays legible over photography.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.55f),
                        ),
                        startY = 0f,
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = MockDimens.SpacingXxxl, start = MockDimens.SpacingXl, end = MockDimens.SpacingXl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = ad.headline,
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            ad.subheadline?.let { subheadline ->
                Text(
                    text = subheadline,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = MockDimens.SpacingMd),
                )
            }
        }
    }
}

@Composable
private fun TouchToOrderOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(horizontal = MockDimens.SpacingXl, vertical = MockDimens.SpacingMd)
            .testTag(AttractTestTags.TOUCH_TO_ORDER_OVERLAY),
    ) {
        Text(
            text = "Touch to Order",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun BrandFallbackPanel(modifier: Modifier = Modifier) {
    // Rendered only when the ad list is empty (e.g. remote-config returned []).
    // The static fallback in AttractRepositoryImpl normally prevents this state.
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "MockDonalds",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(MockDimens.SpacingXl),
        )
    }
}

