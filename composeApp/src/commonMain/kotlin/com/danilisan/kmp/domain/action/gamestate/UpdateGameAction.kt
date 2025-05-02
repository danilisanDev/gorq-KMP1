package com.danilisan.kmp.domain.action.gamestate

import androidx.compose.ui.text.font.FontWeight
import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.action.gamestate.GameStateActionManager.Companion.UPDATE_BOARD_TOTAL_DELAY
import com.danilisan.kmp.domain.action.gamestate.GameStateActionManager.Companion.TRAVEL_ACTION_DELAY
import com.danilisan.kmp.domain.entity.BoardHelper.getEmptyPositionsSortedDiagonally
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.DisplayMessage
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.Score
import com.danilisan.kmp.domain.usecase.gamestate.AddBoxOnBoardUseCase
import com.danilisan.kmp.domain.usecase.gamestate.AddBoxOnQueueUseCase
import com.danilisan.kmp.domain.usecase.gamestate.CreateEmptyBoardUseCase
import com.danilisan.kmp.domain.usecase.gamestate.GetPoolAndParityUseCase
import com.danilisan.kmp.domain.usecase.gamestate.SaveGameStateUseCase
import com.danilisan.kmp.domain.usecase.gamestate.UpdateBoardValuesUseCase
import com.danilisan.kmp.domain.usecase.gamestate.UpdateQueueToBoardValuesUseCase
import com.danilisan.kmp.domain.usecase.gamestate.UpdateQueueValuesUseCase
import com.danilisan.kmp.ui.state.BoardState
import com.danilisan.kmp.ui.state.GameStateUiState
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.displayMsgAddPoints
import kotlinproject.composeapp.generated.resources.displayMsgAddReloads
import kotlinproject.composeapp.generated.resources.refresh
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpdateGameAction(
    override val dispatcher: DispatcherProvider,
    private val getPoolAndParityUseCase: GetPoolAndParityUseCase,
    private val updateBoardValuesUseCase: UpdateBoardValuesUseCase,
    private val updateQueueValuesUseCase: UpdateQueueValuesUseCase,
    private val updateQueueToBoardValuesUseCase: UpdateQueueToBoardValuesUseCase,
    private val saveGameStateUseCase: SaveGameStateUseCase,
    private val createEmptyBoardUseCase: CreateEmptyBoardUseCase,
    private val addBoxOnBoardUseCase: AddBoxOnBoardUseCase,
    private val addBoxOnQueueUseCase: AddBoxOnQueueUseCase,
    private val checkBoardStateAction: CheckBoardStateAction,
) : GameStateAction {
    override suspend operator fun invoke(
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
        gameMode: GameMode,
        params: Any?,
    ): Boolean = withContext(dispatcher.default) {
        //Check expected params type (UpdateOptions)
        val option = if (params is UpdateOptions){
            params
        }else{
            return@withContext false
        }

        //Save state fields needed for updates
        var updatingBoard = getState().board
        var updatingQueue = getState().queue
        var scoreIncrement: Score? = null
        var reloadsIncrement: Int? = null

        val newPositions = when (option) {
            UpdateOptions.AFTER_SELECTION -> getState().selectedPositions
            UpdateOptions.AFTER_LINE -> {
                getState().linedPositions.filterNotNull()
                    .run{
                        val completedPositions = 1 + (getState().completedLines.size * (gameMode.lineLength - 1))
                        if(completedPositions > size){
                            emptyList() //Impossible case
                        }else{
                            subList(0, completedPositions).distinct()
                        }
                    }
            }
            else -> createEmptyBoardUseCase(gameMode.lineLength).getEmptyPositionsSortedDiagonally()
        }

        //Update values
        if (option == UpdateOptions.AFTER_SELECTION) {
            val selectedValues = newPositions.mapNotNull{
                updatingBoard[it]?.value
            }
            getPoolAndParityUseCase(
                numberSet = gameMode.blockNumbers,
                otherNumbers = selectedValues,
                newValuesLength = selectedValues.size,
            ).let { (pool, parity) ->
                //Update score
                scoreIncrement = Score(
                    points = gameMode.getWinConditionPoints(selectedValues),
                    turns = 1,
                )
                //Update board AND queue
                updateQueueToBoardValuesUseCase(
                    queuePool = pool,
                    parityOrderList = parity,
                    board = updatingBoard,
                    queue = updatingQueue,
                    score = getState().score + scoreIncrement,
                    gameMode = gameMode,
                    selectedPositions = newPositions
                ).let { (newBoard, newQueue) ->
                    updatingBoard = newBoard
                    updatingQueue = newQueue
                }

            }
        } else {
            getPoolAndParityUseCase(
                numberSet = gameMode.regularNumbers,
                repetitions = gameMode.numberRepetitions,
                otherNumbers = when (option) {
                    UpdateOptions.RELOAD_QUEUE -> updatingBoard.map { it.value.value }
                    UpdateOptions.RELOAD_BOARD -> updatingQueue.map { it.value }
                    UpdateOptions.AFTER_LINE -> (updatingQueue + updatingBoard.mapNotNull { (position, box) ->
                        if (position !in newPositions) box else null
                    }).map { box -> box.value }

                    else -> emptyList()
                },
                newValuesLength = when (option) {
                    UpdateOptions.RELOAD_QUEUE -> gameMode.queueSize
                    UpdateOptions.RELOAD_BOARD -> gameMode.getBoardSize()
                    UpdateOptions.AFTER_LINE -> newPositions.size
                    else -> gameMode.queueSize + gameMode.getBoardSize()
                }
            ).let { (pool, parity) ->
                //Update board
                if (option.boardIsUpdating) {
                    updateBoardValuesUseCase(
                        currentPool = pool,
                        parityOrderList = parity,
                        board = updatingBoard,
                        targetPositions = newPositions,
                        gameMode = gameMode,
                    ).let { updatingBoard = it }
                }

                //Update queue
                if (option.queueIsUpdating) {
                    updateQueueValuesUseCase(
                        currentPool = pool,
                        parityOrderList = parity,
                        queue = updatingQueue,
                        queueSize = gameMode.queueSize,
                        isWithBlockBox = false,
                    ).let { updatingQueue = it }
                }

                //Update score and reloads
                if (option == UpdateOptions.AFTER_LINE) {
                    getState().completedLines.size.let { lines ->
                        scoreIncrement = gameMode.getScoreForLines(
                            lines = lines
                        )
                        reloadsIncrement = gameMode.getReloadIncrementForLines(
                            lines = lines
                        )
                    }
                } else if (option == UpdateOptions.AFTER_BINGO) {
                    scoreIncrement = gameMode.getScoreForBingo()
                    reloadsIncrement = gameMode.getReloadIncrementForBingo()
                }
            }
        }

        //Clean current state
        if(option == UpdateOptions.NEW_GAME){
            //Create new clean gameState
            GameStateUiState(
                board = createEmptyBoardUseCase(gameMode.lineLength),
                reloadsLeft = gameMode.initialReloads
            ).let{ newGameState ->
                //Update UI state
                updateState(newGameState)
            }
        }else{
            //Clean non-model action-related state fields
            updateStateFields(getState, updateState,
                boardState = BoardState.READY,
                incompleteSelection = false,
                linedPositions = emptyList(),
                completedLines = emptyList(),
            )
        }

        //Update model
        saveGameStateUseCase(
            gameModeId = gameMode.getModeId(),
            board = updatingBoard,
            queue = updatingQueue,
            score = getState().score + scoreIncrement,
            reloadsLeft = getState().reloadsLeft + (reloadsIncrement?: 0),
        )

        //Update UI
        if(option.scoreIsUpdating){
            //TODO Revisar AFTER_BINGO (en Desktop a veces no suma la puntuacion)
            //TODO Revisar AFTER_SELECTION (en Desktop a veces no suma la puntuacion en HardMultiply)
            launch{
                val sizeDiff = scoreIncrement?.lines?.let{ lines ->
                    if(lines > 0 ) lines - 1 else lines
                }?: 0
                updateStateFields(
                    getState, updateState,
                    scoreDifference = scoreIncrement,
                    displayMessage = DisplayMessage(
                        res = Res.string.displayMsgAddPoints,
                        arg = scoreIncrement?.points.toString(),
                        weight = FontWeight.SemiBold,
                        sizeDiff = sizeDiff,
                    ),
                )
                if(reloadsIncrement != null) {
                    delay(UPDATE_BOARD_TOTAL_DELAY / 2)
                    updateStateFields(getState, updateState,
                        displayMessage = DisplayMessage(
                            res = Res.string.displayMsgAddReloads,
                            arg = reloadsIncrement.toString(),
                            weight = FontWeight.SemiBold,
                            sizeDiff = sizeDiff,
                            icon = Res.drawable.refresh,
                        ),
                        reloadsDifference = reloadsIncrement
                    )
                }
            }
        }
        if(option == UpdateOptions.AFTER_SELECTION){
            updateUIQueueToBoard(
                gameMode = gameMode,
                updatedBoard = updatingBoard,
                updatedQueue = updatingQueue,
                selectedPositions = newPositions,
                getState = getState,
                updateState = updateState,
            )
            //Check board state
            return@withContext checkBoardStateAction(getState, updateState, gameMode)
        }else{
            var updateBoardJob: Job? = null
            var updateQueueJob: Job? = null

            if(option.boardIsUpdating){
                updateBoardJob = launch {
                    updateUIBoard(
                        updatedBoard = updatingBoard,
                        targetPositions = newPositions,
                        getState = getState,
                        updateState = updateState,
                    )
                    //Check board state
                    checkBoardStateAction(getState, updateState, gameMode)
                }
            }
            if(option.queueIsUpdating){
                updateQueueJob = launch{
                    updateUIQueue(
                        updatedQueue = updatingQueue,
                        getState = getState,
                        updateState = updateState,
                    )
                }
            }

            updateBoardJob?.join()
            updateQueueJob?.join()

            return@withContext true
        }
    }

    enum class UpdateOptions(
        val boardIsUpdating: Boolean,
        val queueIsUpdating: Boolean,
        val scoreIsUpdating: Boolean,
    ) {
        NEW_GAME(true, true, false),
        RELOAD_QUEUE(false, true, false),
        RELOAD_BOARD(true, false, false),
        AFTER_SELECTION(true, true, true),
        AFTER_LINE(true, false, true),
        AFTER_BINGO(true, true, true),
    }

    private suspend fun updateUIBoard(
        updatedBoard: Map<BoardPosition, NumberBox>,
        targetPositions: List<BoardPosition>,
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
    ) {
        val intervalDelay = UPDATE_BOARD_TOTAL_DELAY / targetPositions.size * 2
        for (targetPosition in targetPositions) {
            //Replace a random regular box on target position
            getState().board.let { currentBoard ->
                updatedBoard[targetPosition]?.let { newBox ->
                    addBoxOnBoardUseCase(
                        newBox = newBox,
                        targetPosition = targetPosition,
                        board = currentBoard,
                    ).let { boardWithNewRegularBox ->
                        updateStateFields(
                            getState, updateState,
                            board = boardWithNewRegularBox
                        )
                    }
                }
            }
            delay(intervalDelay)
        }
        delay(UPDATE_BOARD_TOTAL_DELAY - intervalDelay)
    }

    private suspend fun updateUIQueue(
        updatedQueue: List<NumberBox>,
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
    ) {
        updateStateFields(getState, updateState,
            queue = updatedQueue,
        )
    }

    private suspend fun updateUIQueueToBoard(
        updatedBoard: Map<BoardPosition, NumberBox>,
        updatedQueue: List<NumberBox>,
        selectedPositions: List<BoardPosition>,
        gameMode: GameMode,
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
    ) {
        selectedPositions.forEachIndexed { index, targetPosition ->
            updatedBoard[targetPosition]?.let { newBox ->
                //Add box on board
                val newBoard = addBoxOnBoardUseCase(
                    board = getState().board,
                    targetPosition = targetPosition,
                    newBox = newBox,
                )
                //Add box on queue
                val updatedPosition = index + gameMode.queueSize - selectedPositions.size
                val newQueue = addBoxOnQueueUseCase(
                    currentQueue = getState().queue,
                    newBox = updatedQueue[updatedPosition],
                    queueSize = gameMode.queueSize
                )
                //Update UI (queue)
                updateStateFields(
                    getState, updateState,
                    queue = newQueue,
                    board = newBoard,
                    selectedPositions = selectedPositions.drop(index + 1),
                    travellingBox = newBox,
                    targetPositionFromQueue = targetPosition,
                )
                delay(TRAVEL_ACTION_DELAY)
            }
        }
        //Update travelling box (Empty)
        updateStateFields(
            getState, updateState,
            travellingBox = NumberBox.EmptyBox(),
        )
    }
}