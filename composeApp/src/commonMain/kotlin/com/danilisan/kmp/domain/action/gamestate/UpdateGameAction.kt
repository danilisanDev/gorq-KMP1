package com.danilisan.kmp.domain.action.gamestate

import androidx.compose.ui.text.font.FontWeight
import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.action.gamestate.GameStateActionManager.Companion.UPDATE_BOARD_TOTAL_DELAY
import com.danilisan.kmp.domain.action.gamestate.GameStateActionManager.Companion.TRAVEL_ACTION_DELAY
import com.danilisan.kmp.domain.entity.BoardHelper.getEmptyPositionsSortedDiagonally
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.DisplayMessage
import com.danilisan.kmp.domain.entity.DisplayMessage.Companion.DISPLAY_TEXT_SECONDARY
import com.danilisan.kmp.domain.entity.DisplayMessage.Companion.DISPLAY_TEXT_SELECTED
import com.danilisan.kmp.domain.entity.DisplayMessage.Companion.DISPLAY_TEXT_SUCCESS
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.Score
import com.danilisan.kmp.domain.usecase.gamestate.CreateEmptyBoardUseCase
import com.danilisan.kmp.domain.usecase.gamestate.GetLocalMaxScoreUseCase
import com.danilisan.kmp.domain.usecase.gamestate.GetPoolAndParityUseCase
import com.danilisan.kmp.domain.usecase.gamestate.SaveGameStateUseCase
import com.danilisan.kmp.domain.usecase.gamestate.SaveLocalMaxScoreUseCase
import com.danilisan.kmp.domain.usecase.gamestate.UpdateBoardValuesUseCase
import com.danilisan.kmp.domain.usecase.gamestate.UpdateQueueToBoardValuesUseCase
import com.danilisan.kmp.domain.usecase.gamestate.UpdateQueueValuesUseCase
import com.danilisan.kmp.ui.state.GameStateUiState
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.displayMsgAddPoints
import kotlinproject.composeapp.generated.resources.displayMsgAddReloads
import kotlinproject.composeapp.generated.resources.refresh
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Action responsible of updating board and/or queue depending of the source action (UpdateOption);
 * the updated data is persisted on data layer and exposed to UI-state.
 * @param params (expected UpdateOptions)
 * 1. NEW GAME: From menu or Reload Button while BoardState.GAMEOVER
 * 2. RELOAD_QUEUE: Reload Button while BoardState.READY
 * 3. RELOAD_BOARD: Reload Button while BoardState.BLOCKED.
 * 4. AFTER_SELECTION: SelectAction when win condition is fulfilled.
 * 5. AFTER_LINE: LineEndAction when completedLines.size > 0.
 * 6. AFTER_BINGO: Reload Button while BoardState.BINGO.
 */
class UpdateGameAction(
    override val dispatcher: DispatcherProvider,
    private val saveGameStateUseCase: SaveGameStateUseCase,
    private val getLocalMaxScoreUseCase: GetLocalMaxScoreUseCase,
    private val saveLocalMaxScoreUseCase: SaveLocalMaxScoreUseCase,
    private val getPoolAndParityUseCase: GetPoolAndParityUseCase,
    private val updateBoardValuesUseCase: UpdateBoardValuesUseCase,
    private val updateQueueValuesUseCase: UpdateQueueValuesUseCase,
    private val updateQueueToBoardValuesUseCase: UpdateQueueToBoardValuesUseCase,
    private val createEmptyBoardUseCase: CreateEmptyBoardUseCase,
    private val checkBoardStateAction: CheckBoardStateAction,
) : GameStateAction {

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

    override suspend operator fun invoke(
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
        gameMode: GameMode,
        params: Any?,
    ): Boolean = withContext(dispatcher.default) {
        //Check expected params type (UpdateOptions)
        val option = if (params is UpdateOptions) {
            params
        } else {
            return@withContext false
        }

        //Target state values.
        var updatingBoard = getState().board
        var updatingQueue = getState().queue
        var scoreIncrement = Score()
        var reloadsIncrement = 0
        val newPositions = getNewPositions(
            option = option,
            lineLength = gameMode.lineLength,
            getState = getState,
        )

        //Update values
        updateValues(
            option = option,
            gameMode = gameMode,
            getState = getState,
            updateBoard = { newBoard -> updatingBoard = newBoard },
            updateQueue = { newQueue -> updatingQueue = newQueue },
            updateScoreIncrement = { newIncrement -> scoreIncrement = newIncrement },
            updateReloadsIncrement = { newIncrement -> reloadsIncrement = newIncrement },
            newPositions = newPositions,
        )

        //Update score
        val newScore = updateLocalMaxScore(
            gameModeId = gameMode.getModeId(),
            formerScore = getState().score,
            scoreIncrement = scoreIncrement
        )

        //Update model
        saveGameStateUseCase(
            gameModeId = gameMode.getModeId(),
            board = updatingBoard,
            queue = updatingQueue,
            score = newScore,
            reloadsLeft = getState().reloadsLeft + (reloadsIncrement),
        )

        //Update UI
        if (option == UpdateOptions.NEW_GAME) {
            GameStateUiState(
                board = createEmptyBoardUseCase(gameMode.lineLength),
                updatingPositions = newPositions,
                score = Score(),
                reloadsLeft = gameMode.initialReloads
            ).let { newGameState ->
                updateState(newGameState)
            }
        }

        val sizeDiff = scoreIncrement.lines.let { lines ->
            if (lines > 0) lines - 1 else lines
        }

        //TODO Los takeIf pueden no ser necesarios
        updateStateFields(
            getState, updateState,
            board = updatingBoard.takeIf { option.boardIsUpdating },
            queue = updatingQueue.takeIf { option.queueIsUpdating },
            updatingPositions = (newPositions
                .takeUnless { option == UpdateOptions.AFTER_SELECTION }
                ?: (newPositions + null))
                .takeIf { option.boardIsUpdating },
            incompleteSelection = false,
            selectedPositions = emptyList(),
            linedPositions = emptyList(),
            completedLines = emptyList(),
            score = newScore,
            displayMessage = DisplayMessage(
                res = Res.string.displayMsgAddPoints,
                arg = scoreIncrement.points.toString(),
                weight = FontWeight.SemiBold,
                highlightColor = if (option == UpdateOptions.AFTER_SELECTION) {
                    DISPLAY_TEXT_SELECTED
                } else if (option == UpdateOptions.AFTER_LINE) {
                    DISPLAY_TEXT_SUCCESS
                } else {
                    DISPLAY_TEXT_SECONDARY
                },
                sizeDiff = sizeDiff,
            ).takeIf { option.scoreIsUpdating },
        )
        if (reloadsIncrement != 0 && option.scoreIsUpdating) {
            delay(UPDATE_BOARD_TOTAL_DELAY / 2)
            updateStateFields(
                getState, updateState,
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
        delay(
            if (option == UpdateOptions.AFTER_SELECTION) {
                TRAVEL_ACTION_DELAY * (newPositions.size + 1)
            } else {
                UPDATE_BOARD_TOTAL_DELAY
            }
        )
        return@withContext checkBoardStateAction(getState, updateState, gameMode)
    }

    /**
     * Depending on UpdateOption,
     * returns target value of updatingPositions field on GameState.
     */
    private suspend fun getNewPositions(
        option: UpdateOptions,
        lineLength: Int,
        getState: suspend () -> GameStateUiState
    ): List<BoardPosition> = when (option) {
        UpdateOptions.AFTER_SELECTION -> getState().selectedPositions
        UpdateOptions.AFTER_LINE -> {
            getState().linedPositions.filterNotNull()
                .run {
                    val completedPositions =
                        1 + (getState().completedLines.size * (lineLength - 1))
                    if (completedPositions > size) {
                        emptyList() //Impossible case
                    } else {
                        subList(0, completedPositions).distinct()
                    }
                }
        }

        else -> createEmptyBoardUseCase(lineLength).getEmptyPositionsSortedDiagonally()
    }

    /**
     * Calculates target values for queue, board, scoreIncrement and reloadIncrement,
     * depending on UpdateOption.
     */
    private suspend fun updateValues(
        option: UpdateOptions,
        gameMode: GameMode,
        getState: suspend () -> GameStateUiState,
        updateBoard: (Map<BoardPosition, NumberBox>) -> Unit,
        updateQueue: (List<NumberBox>) -> Unit,
        updateScoreIncrement: (Score) -> Unit,
        updateReloadsIncrement: (Int) -> Unit,
        newPositions: List<BoardPosition>,
    ) = if (option == UpdateOptions.AFTER_SELECTION) {
        updateValuesAfterSelection(
            gameMode = gameMode,
            getState = getState,
            updateBoard = updateBoard,
            updateQueue = updateQueue,
            updateScoreIncrement = updateScoreIncrement,
            newPositions = newPositions,
        )
    } else {
        updateValuesDefault(
            option = option,
            gameMode = gameMode,
            getState = getState,
            updateBoard = updateBoard,
            updateQueue = updateQueue,
            updateScoreIncrement = updateScoreIncrement,
            updateReloadsIncrement = updateReloadsIncrement,
            newPositions = newPositions,
        )
    }

    /**
     * From a pool of numbers, excluding remaining values on Board/Queue,
     * generate new values for Board and/or Queue;
     * and Score and Reload increments, depending on UpdateOption.
     */
    private suspend fun updateValuesDefault(
        option: UpdateOptions,
        gameMode: GameMode,
        getState: suspend () -> GameStateUiState,
        updateBoard: (Map<BoardPosition, NumberBox>) -> Unit,
        updateQueue: (List<NumberBox>) -> Unit,
        updateScoreIncrement: (Score) -> Unit,
        updateReloadsIncrement: (Int) -> Unit,
        newPositions: List<BoardPosition>
    ) =
        getPoolAndParityUseCase(
            numberSet = gameMode.regularNumbers,
            repetitions = gameMode.numberRepetitions,
            otherNumbers = when (option) {
                UpdateOptions.RELOAD_QUEUE -> getState().board.map { it.value.value }
                UpdateOptions.RELOAD_BOARD -> getState().queue.map { it.value }
                UpdateOptions.AFTER_LINE -> (getState().queue + getState().board.mapNotNull { (position, box) ->
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
                    board = getState().board,
                    targetPositions = newPositions,
                    gameMode = gameMode,
                ).let { newBoard -> updateBoard(newBoard) }
            }

            //Update queue
            if (option.queueIsUpdating) {
                updateQueueValuesUseCase(
                    currentPool = pool,
                    parityOrderList = parity,
                    queue = getState().queue,
                    queueSize = gameMode.queueSize,
                    isWithBlockBox = false,
                ).let { newQueue -> updateQueue(newQueue) }
            }

            //Update score and reloads
            when (option) {
                UpdateOptions.AFTER_LINE -> {
                    getState().completedLines.size.let { lines ->
                        updateScoreIncrement(
                            gameMode.getScoreForLines(
                                lines = lines
                            )
                        )
                        updateReloadsIncrement(
                            gameMode.getReloadIncrementForLines(
                                lines = lines
                            )
                        )
                    }
                }
                UpdateOptions.AFTER_BINGO -> {
                    updateScoreIncrement(
                        gameMode.getScoreForBingo()
                    )
                    updateReloadsIncrement(gameMode.getReloadIncrementForBingo())
                }
                UpdateOptions.NEW_GAME -> {
                    updateScoreIncrement(Score(points = -1L))
                }
                else -> { Unit }
            }
        }

    /**
     * From a pool of numbers, excluding values on Board and Queue,
     * Queue boxes are moved to the Board, and new values are generated for the Queue;
     * and calculate Score increment.
     */
    private suspend fun updateValuesAfterSelection(
        gameMode: GameMode,
        getState: suspend () -> GameStateUiState,
        updateBoard: (Map<BoardPosition, NumberBox>) -> Unit,
        updateQueue: (List<NumberBox>) -> Unit,
        updateScoreIncrement: (Score) -> Unit,
        newPositions: List<BoardPosition>,
    ) {
        val selectedValues = getState().board.let { board ->
            newPositions.mapNotNull { board[it]?.value }
        }
        getPoolAndParityUseCase(
            numberSet = gameMode.blockNumbers,
            otherNumbers = selectedValues,
            newValuesLength = selectedValues.size,
        ).let { (pool, parity) ->
            //Calculate score increment
            val scoreIncrement = Score(
                points = gameMode.getWinConditionPoints(selectedValues),
                turns = 1,
            )
            //Update board AND queue
            updateQueueToBoardValuesUseCase(
                queuePool = pool,
                parityOrderList = parity,
                board = getState().board,
                queue = getState().queue,
                score = getState().score + scoreIncrement,
                gameMode = gameMode,
                selectedPositions = newPositions
            ).let { (newBoard, newQueue) ->
                updateBoard(newBoard)
                updateQueue(newQueue)
                updateScoreIncrement(scoreIncrement)
            }
        }
    }

    /**
     * New score is calculated from former score plus score increment.
     * If new score is higher than the one saved on data layer,
     * the new high score is persisted and included in domain Score item.
     */
    private suspend fun updateLocalMaxScore(
        gameModeId: Int,
        formerScore: Score,
        scoreIncrement: Score
    ): Score =
        (formerScore + scoreIncrement).apply{
            if (points == 0L) {
                maxPoints = getLocalMaxScoreUseCase(
                    gameModeId = gameModeId
                ).points
            } else if (points > formerScore.maxPoints) {
                saveLocalMaxScoreUseCase(
                    gameModeId = gameModeId,
                    score = this
                )
                maxPoints = points
            }
        }

}