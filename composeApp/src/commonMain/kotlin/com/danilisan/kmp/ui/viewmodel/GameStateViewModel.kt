package com.danilisan.kmp.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danilisan.kmp.core.log.DebugTest
import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.action.gamestate.GameStateActionManager
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.ui.state.GameModeState
import com.danilisan.kmp.ui.state.GameStateUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class GameStateViewModel(
    private val actionManager: GameStateActionManager,
    private val savedStateHandle: SavedStateHandle,
    private val dispatcher: DispatcherProvider,
) : ViewModel() {
    //region STATE HOLDERS
    /**
     * Variables that hold the state of the game
     * GameState StateFlow (from savedStateHandle)
     * GameMode StateFlow
     */
    //const val KEY_SAVED_STATE = "game_state_flow"
    //var gameState = savedStateHandle.getStateFlow(KEY_SAVED_STATE, GameStateUiState())

    //Concurrency control
    private val gameStateMutex = Mutex()
    private val dragActionMutex = Mutex()

    //Game state
    private val _gameState = MutableStateFlow(GameStateUiState())
    val gameState = _gameState.asStateFlow()

    private suspend fun getGameState(): GameStateUiState =
        gameStateMutex.withLock { _gameState.value }
    private suspend fun updateGameState(newState: GameStateUiState) =
        gameStateMutex.withLock{
            withContext(dispatcher.main) { _gameState.update { newState }  }
        }
    private suspend fun setIsLoadingState(isLoading: Boolean) =
        withContext(dispatcher.main){
            _gameState.value = _gameState.value.copy(isLoading = isLoading)
        }

    //Game mode
    private val _gameModeState = MutableStateFlow(GameMode.EasyAdd.getGameModeState())
    val gameModeState = _gameModeState.asStateFlow()

    private suspend fun updateGameModeState(newState: GameModeState) =
        withContext(dispatcher.main) { _gameModeState.update{ newState } }

    //endregion

    //region EVENT HANDLERS
    init {
        actionManager.bindStateHolder(
            getter = ::getGameState,
            updater = ::updateGameState,
        )
        viewModelScope.launch(dispatcher.main) {
            DebugTest.debugLog("initViewModel", true)
            actionManager.initialLoad(::updateGameModeState)
            setIsLoadingState(false)
            DebugTest.debugLog("initViewModel", false)
        }
    }

    fun newGameHandler(gameModeId: Int = 0){
        viewModelScope.launch(dispatcher.main){
            setIsLoadingState(true)
            actionManager.generateNewGame(
                gameModeId = gameModeId,
                updateViewModelGameMode = ::updateGameModeState
            )
            setIsLoadingState(false)
        }
    }

    fun reloadButtonHandler() =
        viewModelScope.launch(dispatcher.main) {
            setIsLoadingState(true)
            actionManager.pressReloadButton()
            setIsLoadingState(false)
        }

    fun selectBoxHandler(selected: BoardPosition) =
        viewModelScope.launch(dispatcher.main){
            setIsLoadingState(true)
            actionManager.selectBox(selected)
            setIsLoadingState(false)
        }

    fun dragLineHandler(position: BoardPosition? = null, step: Int = 2) =
        viewModelScope.launch(dispatcher.main){
            when(step){
                //StartLine
                0 -> {
                    if(actionManager.startLine(position)){
                        setIsLoadingState(true)
                    }
                }
                //OnDragLine
                1 -> {
                    dragActionMutex.withLock {
                        actionManager.dragLine(position)
                    }
                }
                //EndLine
                else -> {
                    if(actionManager.endLine()){
                        setIsLoadingState(false)
                    }
                }
            }
        }
    //endregion
}