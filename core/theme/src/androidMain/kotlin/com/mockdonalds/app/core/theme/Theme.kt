package com.mockdonalds.app.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MockRed,
    onPrimary = DarkOnSurfacePrimary,
    secondary = MockYellow,
    onSecondary = DeepObsidian,
    background = DeepObsidian,
    onBackground = DarkOnSurfacePrimary,
    surface = DarkSurfaceBase,
    onSurface = DarkOnSurfacePrimary,
    surfaceVariant = DarkSurfaceContainer,
    onSurfaceVariant = DarkOnSurfaceSecondary,
    surfaceContainerLowest = DarkSurfaceDim,
    surfaceContainerLow = DarkSurfaceBase,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    error = StatusError,
    onError = DarkOnSurfacePrimary,
    outline = DarkSurfaceContainerHighest,
    outlineVariant = DarkSurfaceContainerHigh,
)

private val LightColorScheme = lightColorScheme(
    primary = MockRed,
    onPrimary = Color.White,
    secondary = MockYellow,
    onSecondary = OnSecondaryTag,
    background = LightSurfaceBase,
    onBackground = LightOnSurfacePrimary,
    surface = LightSurfaceBase,
    onSurface = LightOnSurfacePrimary,
    surfaceVariant = LightSurfaceContainer,
    onSurfaceVariant = LightOnSurfaceSecondary,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = LightSurfaceBase,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    error = StatusError,
    onError = Color.White,
    outline = LightOnSurfaceTertiary,
    outlineVariant = LightSurfaceContainerHighest,
)

@Immutable
data class MockDonaldsExtendedColors(
    val primaryDark: Color,
    val primaryDarker: Color,
    val onPrimaryButton: Color,
    val onSecondaryTag: Color,
    val onSecondaryContainer: Color,
    val secondaryLight: Color,
)

private val DarkExtendedColors = MockDonaldsExtendedColors(
    primaryDark = PrimaryDark,
    primaryDarker = PrimaryDarker,
    onPrimaryButton = OnPrimaryButton,
    onSecondaryTag = OnSecondaryTag,
    onSecondaryContainer = OnSecondaryContainer,
    secondaryLight = DarkSecondaryLight,
)

private val LightExtendedColors = MockDonaldsExtendedColors(
    primaryDark = PrimaryDark,
    primaryDarker = PrimaryDarker,
    onPrimaryButton = OnPrimaryButton,
    onSecondaryTag = OnSecondaryTag,
    onSecondaryContainer = OnSecondaryContainer,
    secondaryLight = LightSecondaryLight,
)

val LocalMockDonaldsExtendedColors = staticCompositionLocalOf { DarkExtendedColors }

@Composable
fun MockDonaldsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalMockDonaldsExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MockDonaldsTypography(),
            content = content,
        )
    }
}

object MockDonaldsTheme {
    val extendedColors: MockDonaldsExtendedColors
        @Composable get() = LocalMockDonaldsExtendedColors.current
}
