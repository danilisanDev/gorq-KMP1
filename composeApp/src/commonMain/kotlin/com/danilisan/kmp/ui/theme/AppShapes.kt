package com.danilisan.kmp.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape

@Stable
data class AppShapes(
    val regularShape: Shape,
    val softBlockShape: Shape,
    val hardBlockShape: Shape,
    val roundShape: Shape,
)

private val regular: Shape = CutCornerShape(percent = 14)

private val softBlock: Shape = RoundedCornerShape(percent = 14)

private val hardBlock: Shape = RectangleShape

private val round: Shape = CircleShape

val DefaultAppShapes = AppShapes(
    regularShape = regular,
    softBlockShape = softBlock,
    hardBlockShape = hardBlock,
    roundShape = round,
)