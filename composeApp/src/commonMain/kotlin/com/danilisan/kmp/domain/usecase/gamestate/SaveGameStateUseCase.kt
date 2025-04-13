package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.Score
import com.danilisan.kmp.domain.mapper.createGameStateModelFromUIFields
import com.danilisan.kmp.domain.repository.GameStateRepository
import com.danilisan.kmp.domain.usecase.UseCase
import kotlinx.coroutines.withContext

class SaveGameStateUseCase(
    override val dispatcher: DispatcherProvider,
    private val repository: GameStateRepository
) : UseCase {
    suspend operator fun invoke(
        gameModeId: Int,
        board: Map<BoardPosition, NumberBox>,
        queue: List<NumberBox>,
        score: Score,
        reloadsLeft: Int,
    ) = withContext(dispatcher.default) {
        createGameStateModelFromUIFields(
            gameModeId, board, queue, score, reloadsLeft
        ).let { gameStateModel ->
            withContext(dispatcher.io) {
                repository.updateElement(gameStateModel)
            }
        }
    }
}