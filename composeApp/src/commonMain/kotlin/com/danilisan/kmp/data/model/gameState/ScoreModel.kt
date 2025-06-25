package com.danilisan.kmp.data.model.gameState

import kotlinx.serialization.Serializable

@Serializable
data class ScoreModel (
    val points: Long = 0L,
    val lines: Int = 0,
    val turns: Int = 0,
    val maxPoints: Long = 0L,
)

