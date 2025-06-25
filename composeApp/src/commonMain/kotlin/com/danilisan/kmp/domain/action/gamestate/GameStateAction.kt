package com.danilisan.kmp.domain.action.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.DisplayMessage
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.Score
import com.danilisan.kmp.ui.state.BoardState
import com.danilisan.kmp.ui.state.GameStateUiState

/**
 * Contract for any action class,
 * whose main responsability is update UI-GameState
 * via lambdas delegated from view-model.
 */
interface GameStateAction {
    val dispatcher: DispatcherProvider
    suspend operator fun invoke(
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
        gameMode: GameMode,
        params: Any? = null,
    ): Boolean

    /**
     * Simplified version of updateState,
     * that allows to update only certain fields of GameStateUiState,
     * without passing values for every field.
     */
    suspend fun updateStateFields(
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
        reloadsDifference: Int? = null,
        queue: List<NumberBox>? = null,
        board: Map<BoardPosition, NumberBox>? = null,
        score: Score? = null,
        boardState: BoardState? = null,
        availableLines: Set<Int>? = null,
        displayMessage: DisplayMessage? = null,
        selectedPositions: List<BoardPosition>? = null,
        incompleteSelection: Boolean? = null,
        updatingPositions: List<BoardPosition?>? = null,
        linedPositions: List<BoardPosition?>? = null,
        completedLines: List<Int>? = null,
    ) {
        getState().let { currentState ->
            updateState(
                GameStateUiState(
                    reloadsLeft = currentState.reloadsLeft + (reloadsDifference ?: 0),
                    queue = queue ?: currentState.queue,
                    board = board ?: currentState.board,
                    score = score ?: currentState.score,
                    boardState = boardState ?: currentState.boardState,
                    availableLines = availableLines ?: currentState.availableLines,
                    displayMessage = displayMessage ?: currentState.displayMessage,
                    selectedPositions = selectedPositions ?: currentState.selectedPositions,
                    incompleteSelection = incompleteSelection ?: currentState.incompleteSelection,
                    updatingPositions = updatingPositions ?: currentState.updatingPositions,
                    linedPositions = linedPositions ?: currentState.linedPositions,
                    completedLines = completedLines ?: currentState.completedLines,
                )
            )
        }
    }
}