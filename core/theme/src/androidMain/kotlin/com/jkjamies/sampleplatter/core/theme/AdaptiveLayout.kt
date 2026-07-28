package com.jkjamies.sampleplatter.core.theme

import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalWindowSizeClass = compositionLocalOf<WindowSizeClass> {
    error("WindowSizeClass not provided — ensure CompositionLocalProvider is set in App.kt")
}

@Composable
fun isCompactHeight(): Boolean {
    return LocalWindowSizeClass.current.heightSizeClass == WindowHeightSizeClass.Compact
}

@Composable
fun isExpandedWidth(): Boolean {
    return LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Expanded
}

@Composable
fun adaptiveHeroHeight(): Dp = if (isCompactHeight()) 240.dp else PlatterDimens.HeroHeight

@Composable
fun adaptiveQrCodeSize(): Dp = if (isCompactHeight()) 180.dp else 256.dp

@Composable
fun adaptiveBottomBarPadding(): Dp = if (isCompactHeight()) 72.dp else PlatterDimens.BottomBarPadding

@Composable
fun adaptiveBottomNavHeight(): Dp = if (isCompactHeight()) 56.dp else PlatterDimens.BottomNavHeight
