package com.danilisan.kmp.ui.theme

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
data class AppBorders(
    val thinBorder: Dp,
    val mediumBorder: Dp,
    val regularBorder: (Dp) -> Dp,
    val blockBorder: (Dp) -> Dp,
)

private val thin = 1.dp
private val medium = 3.dp
private val regular: (Dp) -> Dp = {boxSize -> boxSize / 21}
private val block: (Dp) -> Dp = {boxSize -> boxSize / 14}

val DefaultAppBorders = AppBorders(
    thinBorder = thin,
    mediumBorder = medium,
    regularBorder = regular,
    blockBorder = block,
)