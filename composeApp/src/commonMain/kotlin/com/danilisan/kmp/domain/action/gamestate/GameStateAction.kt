package com.danilisan.kmp.domain.action.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.DisplayMessage
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.Score
import com.danilisan.kmp.ui.state.BoardState
import com.danilisan.kmp.ui.state.GameStateUiState

interface GameStateAction {
    val dispatcher: DispatcherProvider
    suspend operator fun invoke(
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
        gameMode: GameMode,
        params: Any? = null,
    )

    //TODO Eliminar parametro isLoading -> Solo modificable desde viewmodel
    suspend fun updateStateFields(
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
        reloadsDifference: Int? = null,
        scoreDifference: Score? = null,
        queue: List<NumberBox>? = null,
        board: Map<BoardPosition, NumberBox>? = null,
        boardState: BoardState? = null,
        selectedPositions: List<BoardPosition>? = null,
        incompleteSelection: Boolean? = null,
        linedPositions: List<BoardPosition?>? = null,
        completedLines: List<Int>? = null,
        availableLines: Set<Int>? = null,
        displayMessage: DisplayMessage? = null,
        //isLoading: Boolean? = null,
    ) {
        getState().let{ currentState ->
            updateState(
                GameStateUiState(
                    reloadsLeft = currentState.reloadsLeft + (reloadsDifference ?: 0),
                    score = currentState.score + scoreDifference,
                    queue = queue ?: currentState.queue,
                    board = board ?: currentState.board,
                    boardState = boardState ?: currentState.boardState,
                    selectedPositions = selectedPositions ?: currentState.selectedPositions,
                    incompleteSelection = incompleteSelection ?: currentState.incompleteSelection,
                    linedPositions = linedPositions ?: currentState.linedPositions,
                    completedLines = completedLines ?: currentState.completedLines,
                    availableLines = availableLines ?: currentState.availableLines,
                    displayMessage = displayMessage ?: currentState.displayMessage,
                    //isLoading = isLoading ?: currentState.isLoading,
                )
            )
        }
    }
}