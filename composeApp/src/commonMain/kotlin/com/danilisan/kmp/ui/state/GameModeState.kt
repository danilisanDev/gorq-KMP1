package com.danilisan.kmp.ui.state

import com.danilisan.kmp.domain.entity.Score
import org.jetbrains.compose.resources.DrawableResource

data class GameModeState (
    val modeId: Int,
    val icon: DrawableResource,
    val queueSize: Int,
    val lineLength: Int,
    val reloadBoardCost: Int,
    val reloadQueueCost: Int,
    val isGoldenStar: (Score) -> Boolean?,
)