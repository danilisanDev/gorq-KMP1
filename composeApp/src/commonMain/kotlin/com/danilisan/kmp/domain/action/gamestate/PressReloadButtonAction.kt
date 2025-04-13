package com.danilisan.kmp.domain.action.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.usecase.gamestate.GetDisplayMessageUseCase
import com.danilisan.kmp.ui.state.BoardState
import com.danilisan.kmp.ui.state.GameStateUiState
import com.danilisan.kmp.domain.action.gamestate.UpdateGameAction.UpdateOptions
import kotlinx.coroutines.withContext

class PressReloadButtonAction(
    override val dispatcher: DispatcherProvider,
    private val getDisplayMessageUseCase: GetDisplayMessageUseCase,
    private val updateGameAction: UpdateGameAction,
) : GameStateAction {
    override suspend operator fun invoke(
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
        gameMode: GameMode,
        params: Any?,
    ) = withContext(dispatcher.default) {
        //Does not expect any params
        if (params != null) return@withContext

        //Current board state
        val boardState = getState().boardState

        //Turns consumed
        val turnsConsumed = when (boardState) {
            BoardState.READY -> gameMode.reloadQueueCost
            BoardState.BLOCKED -> gameMode.reloadBoardCost
            else -> null
        }

        //Show default message
        val newMessage = getDisplayMessageUseCase(
            boardState = boardState,
            selectedNumbers = emptyList(),
            gameMode = gameMode,
        )

        //Update state
        updateStateFields(
            getState, updateState,
            reloadsDifference = turnsConsumed,
            selectedPositions = emptyList(),
            displayMessage = newMessage,
        )

        //Invoke Action depending on BoardState
        when(boardState){
            BoardState.READY -> updateGameAction(getState, updateState, gameMode,
                params = UpdateOptions.RELOAD_QUEUE)
            BoardState.BLOCKED -> updateGameAction(getState, updateState, gameMode,
                params = UpdateOptions.RELOAD_BOARD)
            BoardState.BINGO -> updateGameAction(getState, updateState, gameMode,
                params = UpdateOptions.AFTER_BINGO)
            BoardState.GAMEOVER -> updateGameAction(getState, updateState, gameMode,
                params = UpdateOptions.NEW_GAME)
        }
    }
}
