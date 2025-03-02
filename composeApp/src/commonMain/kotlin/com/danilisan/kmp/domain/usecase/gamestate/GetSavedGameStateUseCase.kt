package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.mapper.toUiState
import com.danilisan.kmp.domain.repository.GameStateRepository
import com.danilisan.kmp.domain.usecase.UseCase
import com.danilisan.kmp.ui.state.GameStateUiState
import kotlinx.coroutines.withContext


/**
 * Recovers GameState saved in local storage (Settings)
 * and returns a Pair with GameMode and GameState in UI format
 */
class GetSavedGameStateUseCase(
    override val dispatcher: DispatcherProvider,
    private val repository: GameStateRepository
) : UseCase {
    suspend operator fun invoke(): Pair<GameMode, GameStateUiState>? = withContext(dispatcher.io) {
        repository.getElement()
    }?.let{ model ->
        withContext(dispatcher.default){
            model.toUiState()
        }
    }
}