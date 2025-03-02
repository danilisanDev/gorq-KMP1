package com.danilisan.kmp.domain.action.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.BoardHelper.getMaxLines
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.usecase.gamestate.CalculateBoardStateUseCase
import com.danilisan.kmp.domain.usecase.gamestate.CountStarsOnBoardUseCase
import com.danilisan.kmp.domain.usecase.gamestate.GetDisplayMessageUseCase
import com.danilisan.kmp.domain.usecase.gamestate.GetWinningLinesUseCase
import com.danilisan.kmp.domain.usecase.gamestate.IsBingoPossibleUseCase
import com.danilisan.kmp.domain.usecase.gamestate.IsSelectionPossibleUseCase
import com.danilisan.kmp.ui.state.GameStateUiState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class CheckBoardStateAction(
    override val dispatcher: DispatcherProvider,
    val countStarsOnBoardUseCase: CountStarsOnBoardUseCase,
    val getWinningLinesUseCase: GetWinningLinesUseCase,
    val isSelectionPossibleUseCase: IsSelectionPossibleUseCase,
    val isBingoPossibleUseCase: IsBingoPossibleUseCase,
    val calculateBoardStateUseCase: CalculateBoardStateUseCase,
    val getDisplayMessageUseCase: GetDisplayMessageUseCase,
) : GameStateAction {
    override suspend operator fun invoke(
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
        gameMode: GameMode,
        params: Any?,
    ) = withContext(dispatcher.default) {
        //Current board
        val board = getState().board

        try{
            //Check board integrity
            check(board.values.count { it !is NumberBox.EmptyBox } == gameMode.getBoardSize()) {
                """Board is not complete: 
                        size = ${board.size}, 
                        emptyBoxes = ${board.values.count { it is NumberBox.EmptyBox }}
                     Generating new board."""
            }

            //Check number of stars on board
            async {
                countStarsOnBoardUseCase(
                    board = board,
                )
            }.await().let{ numberOfStars ->
                check(numberOfStars <= gameMode.maxStars) {
                    "There is $numberOfStars on board [LIMIT: ${gameMode.maxStars}. Generating new board"
                }
            }

            //Evaluating board state
            val isSelectionPossible = async {
                isSelectionPossibleUseCase(
                    board = board,
                    minSelection = gameMode.minSelection,
                    maxSelection = gameMode.maxSelection,
                    isWinCondition = gameMode::isWinCondition,
                )
            }
            val isBingoPossible = async {
                isBingoPossibleUseCase(
                    board = board,
                    isWinCondition = gameMode::isWinCondition,
                    getNeededNumbers = gameMode::getWinConditionNumbers,
                )
            }
            val availableLines = async {
                getWinningLinesUseCase(
                    board = board,
                    isWinCondition = gameMode::isWinCondition,
                )
            }.await()

            calculateBoardStateUseCase(
                availableLines = availableLines.size,
                maxLines = board.getMaxLines(),
                isSelectionPossible = isSelectionPossible.await(),
                isBingoPossible = isBingoPossible.await(),
                enoughReloadsLeft = getState().reloadsLeft >= gameMode.reloadBoardCost
            ).let{ currentBoardState ->
                val newMessage = getDisplayMessageUseCase(
                    boardState = currentBoardState,
                    gameMode = gameMode
                )
                updateStateFields(getState, updateState,
                    availableLines = availableLines,
                    boardState = currentBoardState,
                    displayMessage = newMessage,
                )
            }
        } catch (e: Exception) {
            Napier.d(message = "BoardState exception: ${e.message}")
            /**
             * More than 3 stars on board
             * Silver star on multiply game mode
             */
            //TODO Actualizar gameState con mocks
        }
    }
}