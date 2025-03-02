package com.danilisan.kmp.ui.state

import org.jetbrains.compose.resources.DrawableResource

data class GameModeState (
    val modeId: Int,
    val icon: DrawableResource,
    val reloadBoardCost: Int,
    val reloadQueueCost: Int,
)