package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.Score
import com.danilisan.kmp.domain.usecase.UseCase
import kotlinx.coroutines.withContext

//TODO Eliminar
class IncrementScoreUseCase(
    override val dispatcher: DispatcherProvider
) : UseCase {
    suspend operator fun invoke(
        currentScore: Score,
        pointsIncrement: Int,
        turnsIncrement: Int,
        linesIncrement: Int
    ): Score = withContext(dispatcher.default) {
        Score(
            points = currentScore.points + pointsIncrement,
            turns = currentScore.turns + turnsIncrement,
            lines = currentScore.lines + linesIncrement,
        )
    }
}