package com.danilisan.kmp.ui.state

import com.danilisan.kmp.domain.entity.Score
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

data class GameModeState (
    val modeId: Int,
    val icon: DrawableResource,
    val name: StringResource,
    val queueSize: Int,
    val lineLength: Int,
    val reloadBoardCost: Int,
    val reloadQueueCost: Int,
    val isGoldenStar: (Score) -> Boolean?,
)