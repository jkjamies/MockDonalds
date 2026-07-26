package com.mockdonalds.app.core.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import mockdonalds.core.theme.generated.resources.Res
import mockdonalds.core.theme.generated.resources.epilogue
import mockdonalds.core.theme.generated.resources.manrope
import org.jetbrains.compose.resources.Font

val EpilogueFamily: FontFamily
    @Composable get() = FontFamily(
        Font(Res.font.epilogue, weight = FontWeight.Normal),
        Font(Res.font.epilogue, weight = FontWeight.Medium),
        Font(Res.font.epilogue, weight = FontWeight.SemiBold),
        Font(Res.font.epilogue, weight = FontWeight.Bold),
    )

val ManropeFamily: FontFamily
    @Composable get() = FontFamily(
        Font(Res.font.manrope, weight = FontWeight.Normal),
        Font(Res.font.manrope, weight = FontWeight.Medium),
        Font(Res.font.manrope, weight = FontWeight.SemiBold),
        Font(Res.font.manrope, weight = FontWeight.Bold),
    )

@Composable
fun MockDonaldsTypography(): Typography {
    val epilogue = EpilogueFamily
    val manrope = ManropeFamily

    return Typography(
        // Headlines — Epilogue
        displayLarge = TextStyle(
            fontFamily = epilogue,
            fontWeight = FontWeight.Bold,
            fontSize = 57.sp,
            lineHeight = 64.sp,
        ),
        displayMedium = TextStyle(
            fontFamily = epilogue,
            fontWeight = FontWeight.Bold,
            fontSize = 45.sp,
            lineHeight = 52.sp,
        ),
        displaySmall = TextStyle(
            fontFamily = epilogue,
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp,
            lineHeight = 44.sp,
        ),
        headlineLarge = TextStyle(
            fontFamily = epilogue,
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = epilogue,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = epilogue,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
        ),
        // Titles — Epilogue
        titleLarge = TextStyle(
            fontFamily = epilogue,
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp,
            lineHeight = 28.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = epilogue,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        ),
        titleSmall = TextStyle(
            fontFamily = epilogue,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
        // Body — Manrope
        bodyLarge = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        ),
        // Labels — Manrope
        labelLarge = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
        ),
    )
}
