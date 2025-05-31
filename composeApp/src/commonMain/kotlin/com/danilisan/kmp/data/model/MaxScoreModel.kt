package com.danilisan.kmp.data.model

import com.danilisan.kmp.data.model.gameState.ScoreModel
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class MaxScoreModel (
    val mode: Int = 0,
    val score: ScoreModel,
    val date: LocalDateTime? = null,
)