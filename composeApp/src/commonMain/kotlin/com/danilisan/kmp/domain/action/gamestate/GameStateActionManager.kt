package com.danilisan.kmp.domain.action.gamestate

import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.entity.GameMode.Companion.getListOfGameModes
import com.danilisan.kmp.domain.usecase.gamestate.GetSavedGameStateUseCase
import com.danilisan.kmp.ui.state.GameModeState
import com.danilisan.kmp.ui.state.GameStateUiState
import io.github.aakira.napier.Napier

/**
 * UI -> VIEW-MODEL -> **ACTION-MANAGER** -> ACTION -> USE-CASE
 * Intermediary between view-model ui-handlers and domain-actions.
 * RESPONSIBILITIES
 *  1. GameState repository access.
 *  2. Full control over UI-GameState (delegated by viewModel)
 *  3. Update UI-GameMode state (delegated by viewModel)
 *  4. Hold GameMode rules
 *  5. Choose the proper Action for viewModel Handler.
 */
class GameStateActionManager(
    //Persistence useCases
    private val getSavedGameStateUseCase: GetSavedGameStateUseCase,
    //Game actions
    private val loadGameStateFromModelAction: LoadGameStateFromModelAction,
    private val pressReloadButtonAction: PressReloadButtonAction,
    private val updateGameAction: UpdateGameAction,
    private val selectBoxAction: SelectBoxAction,
    private val lineStartAction: LineStartAction,
    private val lineDragAction: LineDragAction,
    private val lineEndAction: LineEndAction,
) {
    //Delegate state holders
    private var gameMode: GameMode = GameMode.EasyAdd //Default game mode
    private var getStateMethod: suspend () -> GameStateUiState = { GameStateUiState() }
    private var updateStateMethod: suspend (GameStateUiState) -> Unit = { }

    fun bindStateHolder(
        getter: suspend () -> GameStateUiState,
        updater: suspend (GameStateUiState) -> Unit,
    ) {
        this.getStateMethod = getter
        this.updateStateMethod = updater
    }

    private suspend fun updateGameMode(
        newGameMode: GameMode,
        updateViewModelGameMode: suspend (GameModeState) -> Unit,
    ) {
        this.gameMode = newGameMode
        updateViewModelGameMode(newGameMode.getGameModeState())
    }

    //Action invoking
    suspend fun generateNewGame(
        gameModeId: Int = 0,
        updateViewModelGameMode: suspend (GameModeState) -> Unit = {},
    ){
        //Update game mode
        try{
            updateGameMode(
                newGameMode = getListOfGameModes()[gameModeId],
                updateViewModelGameMode = updateViewModelGameMode
            )
        }catch(_: IndexOutOfBoundsException){
            Napier.e(message = "ERROR: Unknown game mode selected.")
        }

        //Update game state
        updateGameAction(
            getStateMethod, updateStateMethod, gameMode,
            params = UpdateGameAction.UpdateOptions.NEW_GAME
        )
    }

    suspend fun initialLoad(
        updateViewModelGameMode: suspend (GameModeState) -> Unit
    ){
        //Access to saved gameState by Settings
        getSavedGameStateUseCase()
            ?.let{ (savedGameMode, savedState) -> //<GameMode, GameStateUiState>
                updateGameMode(
                    newGameMode = savedGameMode,
                    updateViewModelGameMode
                )
                loadGameStateFromModelAction(getStateMethod, updateStateMethod, gameMode,
                    params = savedState)
            }
            ?: run{ //If no game is saved -> new game
                generateNewGame(
                    updateViewModelGameMode = updateViewModelGameMode
                )
            }
    }

    suspend fun pressReloadButton() =
        pressReloadButtonAction(getStateMethod, updateStateMethod, gameMode)

    suspend fun selectBox(selected: BoardPosition) =
        selectBoxAction(
            getStateMethod, updateStateMethod, gameMode,
            params = selected
        )

    suspend fun startLine(startingPosition: BoardPosition?) =
        lineStartAction(
            getStateMethod, updateStateMethod, gameMode,
            params = startingPosition,
        )

    suspend fun dragLine(newPosition: BoardPosition?) =
        lineDragAction(
            getStateMethod, updateStateMethod, gameMode,
            params = newPosition,
        )

    suspend fun endLine() =
        lineEndAction(getStateMethod, updateStateMethod, gameMode)

    companion object {
        //Animation delay constants
        private const val BASE_ACTION_DELAY = 300L
        const val TRAVEL_ACTION_DELAY = BASE_ACTION_DELAY * 3
        const val UPDATE_BOARD_TOTAL_DELAY = BASE_ACTION_DELAY * 2
        const val SELECTION_DELAY = BASE_ACTION_DELAY * 3
    }
}