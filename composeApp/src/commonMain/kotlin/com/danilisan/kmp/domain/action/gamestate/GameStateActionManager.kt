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
    private val saveGameStateUseCase: SaveGameStateUseCase,
    //Game actions
    private val loadGameStateFromModelAction: LoadGameStateFromModelAction,
    private val newGameAction: NewGameAction,
    private val pressReloadButtonAction: PressReloadButtonAction,
    private val selectBoxAction: SelectBoxAction,
    private val startLineAction: StartLineAction,
    private val dragLineAction: DragLineAction,
    private val endLineAction: EndLineAction,
    ) {
    //Delegate state holders
    private var gameMode: GameMode = GameMode.EasyAdd
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
    ) = newGameAction(getStateMethod, updateStateMethod, gameMode)

    /* ACCESO A PERSISTENCIA
        //Access to saved gameState by Settings
        getSavedGameStateUseCase()
            ?.let{ pair -> //<GameMode, GameStateUiState>
                val savedGameMode = pair.first
                updateGameMode(
                    newGameMode = savedGameMode,
                    updateViewModelGameMode
                )
                val savedGameState = pair.second
                loadGameStateFromModelAction(getStateMethod, updateStateMethod, gameMode,
                    params = savedGameState)
            }
            ?: run{ //If no game is saved -> new game
                newGameAction(getStateMethod, updateStateMethod, gameMode)
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

}


/*
0. LoadStateAction
    - Model to GameState
    - UpdateGameState
    - UpdateBoardStateAndLines
    - UpdateDisplayMessage

1. CreateCleanStateAction
    - new GameState()
    - Create empty board
    - SetTurns
    - UpdateGameState

2. CreateNewGameAction
    - GetFullPool
    - UpdateBoard (full)
    - UpdateQueue (full)
    - UpdateBoardStateAndLines
    - UpdateDisplayMessage

3. ReloadQueueAction
    - UpdateSelectedNumbers (empty)
    - UpdateDisplayMessage (empty)
    - GetCurrentPool (optional)
    - UpdateQueue (full)
    - UpdateTurns (consume)

5. ReloadBoardAction
    - UpdateSelectedNumbers (empty)
    - UpdateDisplayMessage (empty)
    - GetCurrentPool (optional)
    - UpdateBoard (full)
    - UpdateTurns (consume)
    - UpdateBoardStateAndLines
    - UpdateDisplayMessage

6. SelectionAction: Boolean
    - UpdateSelectedNumbers
    - UpdateDisplayMessage
    - Check win condition
        - UpdateScore
        - UpdateQueue
        - UpdateBoard
        - UpdateBoardStateAndLines
    - UpdateDisplayMessage

7. StartLineAction
    - UpdateSelectedNumbers (empty)
    - UpdateDisplayMessage (empty)

8. EndLineAction
    - UpdateTurns
    - UpdateScore
    - UpdateBoard
    - UpdateBoardStateAndLines

9. BingoAction
    - UpdateTurns
    - UpdateScore
    ( - CreateNewGameAction)

 */