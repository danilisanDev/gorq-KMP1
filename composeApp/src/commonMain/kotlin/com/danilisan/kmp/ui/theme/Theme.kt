package com.danilisan.kmp.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

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
    CompositionLocalProvider(
        LocalAppColors provides LightAppColors,
        LocalAppShapes provides DefaultAppShapes,
        LocalAppBorders provides DefaultAppBorders,
    )
    {
        content()
    }
}


