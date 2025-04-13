package com.danilisan.kmp.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import kotlinproject.composeapp.generated.resources.Quantico_Bold
import kotlinproject.composeapp.generated.resources.Quantico_Regular
import kotlinproject.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.Font

private val LocalAppColors = staticCompositionLocalOf {
    LightAppColors
}

private val LocalAppShapes = staticCompositionLocalOf {
    DefaultAppShapes
}

private val LocalAppBorders = staticCompositionLocalOf {
    DefaultAppBorders
}

object Theme{
    val colors: AppColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current

    val shapes: AppShapes
    @Composable
    @ReadOnlyComposable
    get() = LocalAppShapes.current

    val borders: AppBorders
        @Composable
        @ReadOnlyComposable
        get() = LocalAppBorders.current
}

@Composable
fun ThemeProvider(content: @Composable () -> Unit) {
    val defaultTypography = Typography(
        defaultFontFamily = FontFamily(
            Font(Res.font.Quantico_Regular),
            Font(Res.font.Quantico_Bold, FontWeight.Bold)
        )
    )
    CompositionLocalProvider(
        LocalAppColors provides LightAppColors,
        LocalAppShapes provides DefaultAppShapes,
        LocalAppBorders provides DefaultAppBorders,
    )
    {
        MaterialTheme(
            typography = defaultTypography,
            content = content,
        )
    }
}


