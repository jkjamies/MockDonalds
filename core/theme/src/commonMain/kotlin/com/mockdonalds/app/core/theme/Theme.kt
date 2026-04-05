package com.mockdonalds.app.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NocturnalGourmetColorScheme = darkColorScheme(
    primary = MockRed,
    onPrimary = OnSurfacePrimary,
    secondary = MockYellow,
    onSecondary = DeepObsidian,
    background = DeepObsidian,
    onBackground = OnSurfacePrimary,
    surface = SurfaceBase,
    onSurface = OnSurfacePrimary,
    surfaceVariant = SurfaceContainer,
    onSurfaceVariant = OnSurfaceSecondary,
    surfaceContainerLowest = SurfaceDim,
    surfaceContainerLow = SurfaceBase,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    error = StatusError,
    onError = OnSurfacePrimary,
    outline = SurfaceContainerHighest,
    outlineVariant = SurfaceContainerHigh,
)

@Composable
fun MockDonaldsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NocturnalGourmetColorScheme,
        typography = MockDonaldsTypography(),
        content = content,
    )
}
