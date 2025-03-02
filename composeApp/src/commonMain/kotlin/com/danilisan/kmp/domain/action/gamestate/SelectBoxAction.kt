package com.danilisan.kmp.domain.action.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.usecase.gamestate.GetDisplayMessageUseCase
import com.danilisan.kmp.ui.state.GameStateUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class SelectBoxAction(
    override val dispatcher: DispatcherProvider,
    private val getDisplayMessageUseCase: GetDisplayMessageUseCase,

    private val completeSelectionAction: CompleteSelectionAction,
) : GameStateAction {
    override suspend operator fun invoke(
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
        gameMode: GameMode,
        params: Any?,
    ) = withContext(dispatcher.default) {
        //Check expected param type (BoardPosition)
        val selectedPosition = if (params is BoardPosition) {
            params
        } else {
            return@withContext
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
            return@withContext
        }

        //Check win condition
        gameMode.getWinConditionPoints(selectedValues).let { selectionPoints ->
            val isWinCondition = selectionPoints > 0
            if (isWinCondition) {
                if(selectionSize < gameMode.maxSelection){
                    updateStateFields(getState, updateState,
                        incompleteSelection = true)
                    delay(1000)
                    if(!getState().incompleteSelection){
                        return@withContext
                    }else{
                        updateStateFields(getState, updateState,
                            incompleteSelection = false)
                    }
                }else{
                    delay(1000)
                }
                completeSelectionAction(getState,updateState,gameMode,
                    params = selectionPoints)
            } else if (selectionSize >= gameMode.maxSelection) {
                delay(500)
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
        }


        /*
        selectionSize < minSelection || !winCondition && selectionSize < maxSelection -> wait (return)
        !winCondition && selectionSize >= maxSelection -> refresh selectedNumbers
        winCondition && selectionSize < maxSelection  -> update + delay + completeSelection
        winCondition && selectionSize == maxSelection -> complete selection




        if (selectionResult > 0 || values.size >= _gameMode.maxSelection) {
            delay(500)
            updateDisplayMessage()
            updateState2(selectedPositions = emptyList())
        }
        if (selectionResult > 0) {
            executeWinningSelection(selectionResult, selectedPositions)
        }

         */


    }
}