package com.danilisan.kmp.domain.action.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.BoardHelper.getEmptyPositionsSortedDiagonally
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.usecase.gamestate.AddBoxOnBoardUseCase
import com.danilisan.kmp.domain.usecase.gamestate.CreateEmptyBoardUseCase
import com.danilisan.kmp.ui.state.GameStateUiState
import kotlinx.coroutines.withContext

class LoadGameStateFromModelAction(
    override val dispatcher: DispatcherProvider,
    private val createEmptyBoardUseCase: CreateEmptyBoardUseCase,
    private val addBoxOnBoardUseCase: AddBoxOnBoardUseCase,
    private val checkBoardStateAction: CheckBoardStateAction,
) : GameStateAction {
    override suspend operator fun invoke(
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
        gameMode: GameMode,
        params: Any?,
    ) = withContext(dispatcher.default) {
        //Check expected param type (GameStateUiState)
        val savedGameState = if (params is GameStateUiState) {
            params
        } else {
            return@withContext
        }

        //Create an empty board
        val emptyBoard = createEmptyBoardUseCase(gameMode.lineLength)

        //Load empty board, and saved turnsLeft, queue and Score
        updateStateFields(
            getState, updateState,
            board = emptyBoard,
            queue = savedGameState.queue,
            reloadsDifference = savedGameState.reloadsLeft,
            scoreDifference = savedGameState.score
        )

        //Update board with animation
        emptyBoard.getEmptyPositionsSortedDiagonally().forEach { targetPosition ->
            savedGameState.board[targetPosition]?.let { savedBox ->
                getState().board.let { currentBoard ->
                    addBoxOnBoardUseCase(
                        board = currentBoard,
                        targetPosition = targetPosition,
                        newBox = savedBox,
                    )
                        .let { updatedBoard ->
                            updateStateFields(
                                getState, updateState,
                                board = updatedBoard
                            )
                        }
                }
            }
        }

        //Invoke check board state Action
        checkBoardStateAction(getState, updateState, gameMode)
    }
}