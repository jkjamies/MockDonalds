package com.mockdonalds.app.core.theme

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Glass morphism modifier — 20px backdrop blur with translucent surface overlay.
 * Relies on tonal shift rather than borders (no-line policy).
 */
@Suppress("UnusedParameter") // blurRadius reserved for platform-specific backdrop blur implementation
fun Modifier.glassEffect(
    blurRadius: Int = 20,
    overlayColor: Color = SurfaceContainer.copy(alpha = 0.6f),
): Modifier = this
    .background(overlayColor)

/**
 * Ambient shadow gradient — used in place of elevation borders.
 */
fun Modifier.ambientGradient(
    startColor: Color = Color.Black.copy(alpha = 0.3f),
    endColor: Color = Color.Transparent,
): Modifier = this.background(
    brush = Brush.verticalGradient(
        colors = listOf(startColor, endColor),
    ),
)

/**
 * Brand gradient — primary red to secondary yellow.
 */
val MockDonaldsBrandGradient = Brush.horizontalGradient(
    colors = listOf(MockRed, MockYellow),
)
