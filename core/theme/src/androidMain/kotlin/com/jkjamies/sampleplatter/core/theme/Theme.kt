package com.jkjamies.sampleplatter.core.theme

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
    primary = PlatterRed,
    onPrimary = DarkOnSurfacePrimary,
    secondary = PlatterYellow,
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
    primary = PlatterRed,
    onPrimary = Color.White,
    secondary = PlatterYellow,
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
data class SamplePlatterExtendedColors(
    val primaryDark: Color,
    val primaryDarker: Color,
    val onPrimaryButton: Color,
    val onSecondaryTag: Color,
    val onSecondaryContainer: Color,
    val secondaryLight: Color,
)

private val DarkExtendedColors = SamplePlatterExtendedColors(
    primaryDark = PrimaryDark,
    primaryDarker = PrimaryDarker,
    onPrimaryButton = OnPrimaryButton,
    onSecondaryTag = OnSecondaryTag,
    onSecondaryContainer = OnSecondaryContainer,
    secondaryLight = DarkSecondaryLight,
)

private val LightExtendedColors = SamplePlatterExtendedColors(
    primaryDark = PrimaryDark,
    primaryDarker = PrimaryDarker,
    onPrimaryButton = OnPrimaryButton,
    onSecondaryTag = OnSecondaryTag,
    onSecondaryContainer = OnSecondaryContainer,
    secondaryLight = LightSecondaryLight,
)

val LocalSamplePlatterExtendedColors = staticCompositionLocalOf { DarkExtendedColors }

@Composable
fun SamplePlatterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalSamplePlatterExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SamplePlatterTypography(),
            content = content,
        )
    }
}

object SamplePlatterTheme {
    val extendedColors: SamplePlatterExtendedColors
        @Composable get() = LocalSamplePlatterExtendedColors.current
}
