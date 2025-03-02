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

    //Game state
    private val _gameState = MutableStateFlow(GameStateUiState())
    val gameState = _gameState.asStateFlow()

    private suspend fun getGameState(): GameStateUiState = _gameState.value
    private suspend fun updateGameState(newState: GameStateUiState) =
        withContext(dispatcher.main) { _gameState.update { newState }  }
    private suspend fun setIsLoadingState(isLoading: Boolean) {
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
            println(gameModeState.value.modeId)
            setIsLoadingState(false)
            DebugTest.debugLog("initViewModel", false)
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
                    setIsLoadingState(true)
                    actionManager.startLine(position)
                }
                //OnDragLine
                1 -> {
                    Mutex().withLock {
                        actionManager.dragLine(position)
                    }
                }
                //EndLine
                else -> {
                    actionManager.endLine()
                    setIsLoadingState(false)
                }
            }



        }



    /*
    fun resumeGameHandler()
    fun newGameHandler()
    fun startLineHandler()
    fun finishLineHandler()
     */


    //endregion


    //DEPRECATED
/*
    //Functions for events -> pass as method references in UI (::method)
    fun selectAction(position: BoardPosition) {
        viewModelScope.launch(dispatcher.main) {
            DebugTest.debugLog("select", true)
            setIsLoadingState(true)

            //Add or remove selected position
            val selectedPositions = _gameState.value.selectedPositions.toMutableList()
            if (position in selectedPositions) {
                selectedPositions.remove(position)
            } else {
                selectedPositions.add(position)
            }

            //Get selected values
            val values = selectedPositions.mapNotNull { position ->
                _gameState.value.board[position]?.value
            }

            //Update message
            updateDisplayMessage(values)


            //Check win condition
            val selectionResult = useCaseManager.checkSelectedNumbers(
                minSelection = _gameMode.minSelection,
                maxSelection = _gameMode.maxSelection,
                selectedNumbers = values,
                getWinConditionResult = _gameMode::getWinConditionResult
            )

            //Update selected numbers
            updateState2(selectedPositions = selectedPositions)

            if (selectionResult > 0 || values.size >= _gameMode.maxSelection) {
                delay(500)
                updateDisplayMessage()
                updateState2(selectedPositions = emptyList())
            }
            if (selectionResult > 0) {
                executeWinningSelection(selectionResult, selectedPositions)
            }
            setIsLoadingState(false)
        }
    }


    private suspend fun executeWinningSelection(
        selectionResult: Int,
        selectedPositions: List<BoardPosition>,
    ) {
        //Increase score
        coroutineScope {
            launch {
                updateScore(
                    pointsIncrement = selectionResult,
                    turnsIncrement = 1
                )
            }
        }

        //GetParityOrderList

        //For loop
        selectedPositions.forEach { targetPosition ->
            //Box from Queue to Board
            val travellingBox = _gameState.value.queue[0]
            //Add box on Board
            val newBoard = useCaseManager.addBoxOnBoard(
                board = _gameState.value.board,
                targetPosition = targetPosition,
                newBox = travellingBox,
            )
            //New box for queue
            val newBoxOnQueue = useCaseManager.getRandomBoxForQueue(
                _gameMode.blockNumbers,
                _gameMode.generateBlocksOnQueue
            )
            //Add box on queue
            val newQueue = useCaseManager.addBoxOnQueue(
                currentQueue = _gameState.value.queue,
                newBox = newBoxOnQueue,
                queueSize = _gameMode.queueSize
            )

            //Update board and queue
            _gameState.value = _gameState.value.copy(
                board = newBoard,
                queue = newQueue,
            )
        }
        //actionManager.checkBoardState()
    }


    /*
    newGame:
        - generateNewGameState

    reloadButton():
        when(boardState)
        - BINGO:
            1. Sumar puntos + recargas
            2. getNumberPool(queue) -> generateNewBoard(gameMode)
            3. boardState -> Ready
            4. availableLines -> emptySet()
            5. auxDisplay -> Ready message
        - BLOCKED:
            1. Restar 2 recargas
            2. getNumberPool(queue) -> generateNewBoard(gameMode)
            3. boardState -> Ready
            4. availableLines -> emptySet()
            5. auxDisplay -> Ready message
        - READY:
            1. Restar 1 recarga
            2. getNumberPool(board) -> generateNewQueue(gameMode)

    selectBox(BoardPosition):
        if(estaba seleccionada)
            1. Deseleccionar casilla (isSelected = -1)
            2. Revisar otras casillas seleccionadas
            3. auxDisplay -> Actualizar mensaje (Ready / x + x)
        else(no estaba seleccionada)
            1. Seleccionar casilla (isSelected = max(isSelected) + 1)
            2. if (winCondition && isSelected.count >= (lineLength - 1))
                a. auxDisplay -> Actualizar mensaje (x + x + x)
                b. Sumar puntos (Turnos + 1?)
                c. Sustituir board.isSelected == queue.index
                d. refillQueue(isSelected.count)
                e. Reiniciar isSelected = -1
               else if(!winCondition && isSelected.count == lineLength)
                a. auxDisplay -> Actualizar mensaje (x + x + x)
                b. Reiniciar isSelected = -1
            3. auxDisplay -> Ready message

    lineAction(Set<BoardPosition>)
        1. Calcular número de líneas + Sumar puntos y lineas + (Turnos + 1?)
        2. Eliminar casillas alineadas?
        3. getNumberPool(queue, board) + refillBoard

     [Todas -> updateGameStateModel]
     */

    //Loading state


    //region PRIVATE METHODS
    //region GAMESTATE PERSISTENCE

    //Cambio gamestate
    //savedStateHandle[KEY_SAVED_STATE] = nuevo valor
*/
}


