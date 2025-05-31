package com.danilisan.kmp.domain.action.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.action.gamestate.GameStateActionManager.Companion.BASE_ACTION_DELAY
import com.danilisan.kmp.domain.action.gamestate.GameStateActionManager.Companion.SELECTION_DELAY
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.usecase.gamestate.GetDisplayMessageUseCase
import com.danilisan.kmp.ui.state.GameStateUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class SelectBoxAction(
    override val dispatcher: DispatcherProvider,
    private val getDisplayMessageUseCase: GetDisplayMessageUseCase,
    private val updateGameAction: UpdateGameAction,
) : GameStateAction {
    override suspend operator fun invoke(
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
        gameMode: GameMode,
        params: Any?,
    ): Boolean = withContext(dispatcher.default) {
        //Check expected param type (BoardPosition)
        val selectedPosition = if (params is BoardPosition) {
            params
        } else {
            return@withContext false
        }

        //Toggle selected position from state list
        val selectedPositions = getState().selectedPositions
            .toMutableList().apply {
                if (contains(selectedPosition)) {
                    remove(selectedPosition)
                } else {
                    add(selectedPosition)
                }
            }.toList()

        //Get current selected values
        val selectedValues = selectedPositions.mapNotNull { position ->
            getState().board[position]?.value
        }

        //Update selected positions and display message
        val currentBoardState = getState().boardState
        updateStateFields(
            getState, updateState,
            selectedPositions = selectedPositions,
            incompleteSelection = false,
            displayMessage = getDisplayMessageUseCase(
                boardState = currentBoardState,
                gameMode = gameMode,
                selectedNumbers = selectedValues,
            )
        )

        //Check minimum selection size
        val selectionSize = selectedValues.size
        if (selectionSize < gameMode.minSelection) {
            return@withContext true
        }

        //Check win condition
        if(gameMode.isWinCondition(selectedValues)){
            if (selectionSize < gameMode.maxSelection) {
                updateStateFields(
                    getState, updateState,
                    incompleteSelection = true
                )
                delay(SELECTION_DELAY)
                if (!getState().incompleteSelection) {
                    return@withContext true
                } else {
                    updateStateFields(
                        getState, updateState,
                        incompleteSelection = false
                    )
                }
            } else {
                delay(SELECTION_DELAY)
            }
            updateGameAction(getState, updateState, gameMode,
                params = UpdateGameAction.UpdateOptions.AFTER_SELECTION
            )
        } else if (selectionSize >= gameMode.maxSelection) {
            //Negative visual feedback
            updateStateFields(
                getState,updateState,
                displayMessage = getDisplayMessageUseCase(
                    boardState = currentBoardState,
                    gameMode = gameMode,
                    selectedNumbers = selectedValues,
                    wrongSelection = true,
                )
            )
            delay(SELECTION_DELAY)
            //Empty selected numbers
            updateStateFields(
                getState, updateState,
                selectedPositions = emptyList(),
                displayMessage = getDisplayMessageUseCase(
                    boardState = currentBoardState,
                    gameMode = gameMode,
                )
            )
        }
        return@withContext true
    }
}