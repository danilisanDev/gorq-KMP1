package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.data.model.GameStateModel
import com.danilisan.kmp.domain.repository.GameStateRepository
import com.danilisan.kmp.domain.usecase.UseCaseNoParams

/**
 * Loads GameState saved in local storage
 */
class GetGameStateModelUseCase(
    private val repository: GameStateRepository
): UseCaseNoParams<GameStateModel?> {
    override suspend operator fun invoke(): GameStateModel?{
        return repository.getElement()
    }
}