package com.danilisan.kmp.domain.action.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.BoardHelper.getEmptyPositionsSortedDiagonally
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.usecase.gamestate.CreateEmptyBoardUseCase
import com.danilisan.kmp.ui.state.GameStateUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Action performed right after initial load.
 * Persisted GameState is loaded after creating an empty board
 * with diagonal-sorted updating positions.
 * Finally, checks BoardState.
 * @param params (expected GameStateUiState) persisted GameState.
 */

class LoadGameStateFromModelAction(
    override val dispatcher: DispatcherProvider,
    private val createEmptyBoardUseCase: CreateEmptyBoardUseCase,
    private val checkBoardStateAction: CheckBoardStateAction,
) : GameStateAction {
    override suspend operator fun invoke(
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
        gameMode: GameMode,
        params: Any?,
    ): Boolean = withContext(dispatcher.default) {
        //Check expected param type (GameStateUiState)
        val savedGameState = if (params is GameStateUiState) {
            params
        } else {
            return@withContext false
        }

        //Create an empty board
        val emptyBoard = createEmptyBoardUseCase(gameMode.lineLength)

        //Load empty board with diagonal-sorted order
        updateStateFields(
            getState, updateState,
            board = emptyBoard,
            updatingPositions = emptyBoard.getEmptyPositionsSortedDiagonally(),
        )

        //Delay for the empty board to load
        delay(210)

        //Update board
        updateStateFields(
            getState, updateState,
            board = savedGameState.board,
            queue = savedGameState.queue,
            updatingPositions = emptyBoard.getEmptyPositionsSortedDiagonally(),
            reloadsDifference = savedGameState.reloadsLeft,
            score = savedGameState.score,
        )

        //Invoke check board state Action
        checkBoardStateAction(getState, updateState, gameMode)
    }
}