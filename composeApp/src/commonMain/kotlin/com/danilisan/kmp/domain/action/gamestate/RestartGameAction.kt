package com.danilisan.kmp.domain.action.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.ui.state.GameStateUiState
import kotlinx.coroutines.withContext

//TODO Eliminar
class RestartGameAction(
    override val dispatcher: DispatcherProvider,
    private val reloadRandomBoardAction: ReloadRandomBoardAction,
    private val reloadQueueAction: ReloadQueueAction,
) : GameStateAction {
    override suspend operator fun invoke(
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
        gameMode: GameMode,
        params: Any?,
    ) = withContext(dispatcher.default) {
        reloadRandomBoardAction(getState, updateState, gameMode)
        reloadQueueAction(getState, updateState, gameMode)
    }
}