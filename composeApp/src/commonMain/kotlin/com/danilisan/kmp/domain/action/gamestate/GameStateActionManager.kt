package com.danilisan.kmp.domain.action.gamestate

import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.usecase.gamestate.GetSavedGameStateUseCase
import com.danilisan.kmp.domain.usecase.gamestate.SaveGameStateUseCase
import com.danilisan.kmp.ui.state.GameModeState
import com.danilisan.kmp.ui.state.GameStateUiState

/**
 * RESPONSABILITIES
 *  1. Model persistence (get & update gameStateModel)
 *  2. Full control over UI-GameState (delegated by viewModel)
 *  3. Update UI-GameMode state (delegated by viewModel)
 *  4. Hold GameMode rules
 *  5. Choose the proper Action for viewModel Handler
 */
class GameStateActionManager(
    //Persistence useCases
    private val getSavedGameStateUseCase: GetSavedGameStateUseCase,
    //Game actions
    private val loadGameStateFromModelAction: LoadGameStateFromModelAction,
    private val pressReloadButtonAction: PressReloadButtonAction,
    private val updateGameAction: UpdateGameAction,
    private val selectBoxAction: SelectBoxAction,
    private val startLineAction: StartLineAction,
    private val dragLineAction: DragLineAction,
    private val endLineAction: EndLineAction,
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
    suspend fun initialLoad(
        updateViewModelGameMode: suspend (GameModeState) -> Unit
    ) = updateGameAction(
        getStateMethod, updateStateMethod, gameMode,
        params = UpdateGameAction.UpdateOptions.NEW_GAME
    )

    /* ACCESO A PERSISTENCIA
        //Access to saved gameState by Settings
        getSavedGameStateUseCase()
            ?.let{ (savedGameMode, savedState) -> //<GameMode, GameStateUiState>
                updateGameMode(
                    newGameMode = gameMode,
                    updateViewModelGameMode
                )
                loadGameStateFromModelAction(getStateMethod, updateStateMethod, gameMode,
                    params = savedState)
            }
            ?: run{ //If no game is saved -> new game
                updateGameAction(getStateMethod, updateStateMethod, gameMode,
                    params = UpdateGameAction.UpdateOptions.NEW_GAME)
            }
    */

    suspend fun pressReloadButton() =
        pressReloadButtonAction(getStateMethod, updateStateMethod, gameMode)

    suspend fun selectBox(selected: BoardPosition) =
        selectBoxAction(
            getStateMethod, updateStateMethod, gameMode,
            params = selected
        )

    suspend fun startLine(startingPosition: BoardPosition?) =
        startLineAction(
            getStateMethod, updateStateMethod, gameMode,
            params = startingPosition,
        )

    suspend fun dragLine(newPosition: BoardPosition?) =
        dragLineAction(
            getStateMethod, updateStateMethod, gameMode,
            params = newPosition,
        )

    suspend fun endLine() =
        endLineAction(getStateMethod, updateStateMethod, gameMode)

    companion object {
        const val TOTAL_ACTION_DELAY = 1000L
    }
}