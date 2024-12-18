package com.danilisan.kmp.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danilisan.kmp.data.model.GameStateModel
import com.danilisan.kmp.domain.entity.BoardNumberBox
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.NumberPool
import com.danilisan.kmp.domain.mapper.toUiState
import com.danilisan.kmp.domain.usecase.GameStateUseCaseManager
import com.danilisan.kmp.domain.usecase.GenerationParams
import com.danilisan.kmp.ui.state.GameStateUiState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class GameStateViewModel(
    private val useCaseManager: GameStateUseCaseManager,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    //var gameState = savedStateHandle.getStateFlow(KEY_SAVED_STATE, GameStateUiState())

    //Game state
    private val _gameState = MutableStateFlow(GameStateUiState())
    var gameState = _gameState.asStateFlow()

    //Public viewmodel methods
    fun initData() {
        viewModelScope.launch {
            toggleLoading(true)
            //Get gameStateModel
            val gameStateModel: GameStateModel? =
                getGameStateModel()

            if (gameStateModel == null) {
                generateNewGameState()
            } else {
                generateGameStateFromModel(gameStateModel)
            }
            toggleLoading(false)
        }
    }

    fun reloadBoard() {
        viewModelScope.launch {
            toggleLoading(true)
            val gameMode = _gameState.value.gameMode
            //Get general pool
            val currentPool: NumberPool =
                getNumberPool(gameMode.getNumberPool)

            //Exclude queue numbers
            val queueNumbers= _gameState.value.queue.map{it.value}.toIntArray()
            currentPool.excludeNumbersFromPool(*queueNumbers)

            val newBoard: Set<BoardNumberBox>
            withContext(Dispatchers.Default) {
                //Generate newBoard
                newBoard = generateNewBoard(
                    currentPool,
                    gameMode.generateNewBoardRule,
                )
            }
            _gameState.update {
                _gameState.value.copy(
                    board = newBoard,
                )
            }
            toggleLoading(false)
        }
    }

    fun reloadQueue(){
        viewModelScope.launch {
            toggleLoading(true)
            val gameMode = _gameState.value.gameMode
            //Get general pool
            val currentPool: NumberPool =
                getNumberPool(gameMode.getNumberPool)

            //Exclude board numbers
            val boardNumbers = _gameState.value.board.map{it.number.value}.toIntArray()
            currentPool.excludeNumbersFromPool(*boardNumbers)

            val newQueue: List<NumberBox>
            withContext(Dispatchers.Default) {
                //Generate newBoard
                newQueue = generateNewQueue(
                    currentPool,
                    gameMode.generateNewQueueRule,
                )
            }
            _gameState.update {
                _gameState.value.copy(
                    queue = newQueue,
                )
            }
            toggleLoading(false)
        }
    }

    //Loading state
    private suspend fun toggleLoading(isLoading: Boolean){
        _gameState.update { _gameState.value.copy(isLoading = isLoading) }
        println("*** is loading = $isLoading")
    }

    //Private viewmodel methods
    private suspend fun getGameStateModel(): GameStateModel? {
        val gameStateModel: GameStateModel?
        withContext(Dispatchers.IO) {
            gameStateModel = useCaseManager.getGameStateModel()
        }
        return gameStateModel
    }

    private suspend fun generateGameStateFromModel(
        gameStateModel: GameStateModel
    ){
        val stateFromModel = gameStateModel.toUiState()
        _gameState.value = stateFromModel

        if (!stateFromModel.isBlocked) {
            val availableLines = stateFromModel.calculateAvailableLines()
            if (availableLines.isNotEmpty()) {
                _gameState.update {
                    _gameState.value.copy(
                        availableLines = availableLines
                    )
                }
            }
        }
    }

    private suspend fun generateNewGameState() {
        //Generate new game
        val gameMode = _gameState.value.gameMode
        val currentPool: NumberPool =
            getNumberPool(gameMode.getNumberPool)

        val newBoard: Set<BoardNumberBox>
        val newQueue: List<NumberBox>
        withContext(Dispatchers.Default) {
            //Generate newBoard
            newBoard = generateNewBoard(
                currentPool,
                gameMode.generateNewBoardRule,
            )

            //Update pool
            currentPool.excludeNumbersFromPool(
                *newBoard.map{it.number.value}.toIntArray()
            )

            //Generate new Queue
            newQueue = generateNewQueue(
                currentPool,
                gameMode.generateNewQueueRule,
            )
        }
        _gameState.update {
            _gameState.value.copy(
                board = newBoard,
                queue = newQueue,
            )
        }
    }

    private suspend fun getNumberPool(
        getNumberPoolList: () -> NumberPool,
    ): NumberPool{
        val numberPool: NumberPool
        withContext(Dispatchers.Default){
            numberPool = useCaseManager
                .getNumberPoolUseCase(getNumberPoolList)
        }
        return numberPool
    }

    private suspend fun NumberPool.excludeNumbersFromPool(
        vararg numbers: Int
    ){
        for(number in numbers){
            this.excludeFromPool(number)
        }
//        withContext(Dispatchers.Default){
//            val job = mutableListOf<Deferred<Unit>>()
//            for(number in numbers){
//                job.add(async{
//                    this@excludeNumbersFromPool.excludeFromPool(number)
//                })
//            }
//            job.awaitAll()
//        }
    }

    private suspend fun generateNewBoard(
        currentPool: NumberPool,
        generateNewBoardRule: suspend (NumberPool) -> Set<BoardNumberBox>
    ): Set<BoardNumberBox> {
        Napier.d("generate New board START")
        val startTime = Clock.System.now().toEpochMilliseconds()
        val result = useCaseManager.generateBoard(
            params = GenerationParams(
                pool = currentPool,
                rule = generateNewBoardRule
            )
        )
        val endTime = Clock.System.now().toEpochMilliseconds()
        Napier.d("generate new game END -> Time: ${endTime - startTime}ms")
        return result
    }

    private suspend fun generateNewQueue(
        currentPool: NumberPool,
        generateNewQueueRule: suspend (NumberPool) -> List<NumberBox>
    ): List<NumberBox>{
        Napier.d("generate New queue START")
        val startTime = Clock.System.now().toEpochMilliseconds()
        val result = useCaseManager.generateQueue(
            params = GenerationParams(
                pool = currentPool,
                rule = generateNewQueueRule
            )
        )
        val endTime = Clock.System.now().toEpochMilliseconds()
        Napier.d("generate new queue END -> Time: ${endTime - startTime}ms")
        return result
    }


    companion object {
        const val KEY_SAVED_STATE = "game_state_flow"
    }

    //Cambio gamestate
    //savedStateHandle[KEY_SAVED_STATE] = nuevo valor

}


