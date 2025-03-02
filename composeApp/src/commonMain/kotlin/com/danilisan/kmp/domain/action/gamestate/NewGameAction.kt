package com.danilisan.kmp.domain.action.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.usecase.gamestate.CreateEmptyBoardUseCase
import com.danilisan.kmp.ui.state.GameStateUiState
import kotlinx.coroutines.withContext

class NewGameAction(
    override val dispatcher: DispatcherProvider,
    private val createEmptyBoardUseCase: CreateEmptyBoardUseCase,
    private val reloadRandomBoardAction: ReloadRandomBoardAction,
    private val reloadQueueAction: ReloadQueueAction,
): GameStateAction {
    override suspend operator fun invoke(
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
        gameMode: GameMode,
        params: Any?,
    ) = withContext(dispatcher.default){
        //Create clean gameState
        GameStateUiState(
            board = createEmptyBoardUseCase(gameMode.lineLength),
            reloadsLeft = gameMode.initialReloads
        ).let{ newGameState ->
            //Update UI state
            updateState(newGameState)
        }

        //Load new board and queue
        reloadRandomBoardAction(getState,updateState, gameMode)
        reloadQueueAction(getState, updateState, gameMode)
    }
}