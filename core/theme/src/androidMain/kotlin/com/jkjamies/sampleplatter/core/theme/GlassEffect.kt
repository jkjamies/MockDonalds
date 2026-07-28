package com.jkjamies.sampleplatter.core.theme

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Glass morphism modifier — a translucent surface overlay.
 * Relies on tonal shift rather than borders (no-line policy).
 *
 * Overlay only: this does **not** blur what is behind it. Compose's `Modifier.blur` blurs a
 * composable's own content, not its backdrop, so a real backdrop blur needs platform-specific
 * work (a `RenderEffect` on Android 12+, `UIVisualEffectView`/`.ultraThinMaterial` on iOS)
 * that this modifier does not do. Add a `blurRadius` parameter when that lands — not before,
 * so no call site can tune a value that has no effect.
 */
fun Modifier.glassEffect(
    overlayColor: Color = DarkSurfaceContainer.copy(alpha = 0.6f),
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
val SamplePlatterBrandGradient = Brush.horizontalGradient(
    colors = listOf(PlatterRed, PlatterYellow),
)
