package com.danilisan.kmp.data.model

import com.danilisan.kmp.data.model.gameState.ScoreModel
import kotlinx.serialization.Serializable

@Serializable
data class GameStateModel(
    val gameMode: Int = 0,
    val board: List<Int> = emptyList(),
    val queue: List<Int> = emptyList(),
    val score: ScoreModel = ScoreModel(),
    val reloadsLeft: Int = 0,
    val isBlocked: Boolean = true,
    )
