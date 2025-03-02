package com.danilisan.kmp.domain.action.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.DisplayMessage
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.NumberPool
import com.danilisan.kmp.domain.entity.Score
import com.danilisan.kmp.domain.usecase.gamestate.AddBoxOnBoardUseCase
import com.danilisan.kmp.domain.usecase.gamestate.AddBoxOnQueueUseCase
import com.danilisan.kmp.domain.usecase.gamestate.CreateParityOrderListUseCase
import com.danilisan.kmp.domain.usecase.gamestate.CreateRandomBoxForQueueUseCase
import com.danilisan.kmp.domain.usecase.gamestate.SaveGameStateUseCase
import com.danilisan.kmp.domain.usecase.gamestate.UpdateStarBoxOnQueueUseCase
import com.danilisan.kmp.ui.state.GameStateUiState
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.displayMsgAddPoints
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class CompleteSelectionAction(
    override val dispatcher: DispatcherProvider,
    private val createParityOrderListUseCase: CreateParityOrderListUseCase,
    private val getRandomBoxForQueueUseCase: CreateRandomBoxForQueueUseCase,
    private val addBoxOnQueueUseCase: AddBoxOnQueueUseCase,
    private val addBoxOnBoardUseCase: AddBoxOnBoardUseCase,
    private val updateStarBoxOnQueueUseCase: UpdateStarBoxOnQueueUseCase,
    private val saveGameStateUseCase: SaveGameStateUseCase,
    private val checkBoardStateAction: CheckBoardStateAction,
) : GameStateAction {
    override suspend operator fun invoke(
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
        gameMode: GameMode,
        params: Any?,
    ) = withContext(dispatcher.default) {
        //Check expected params type (Int)
        if (params !is Int) return@withContext

        //Get selectedPositions
        val selectedPositions = getState().selectedPositions

        //Update UI with empty positions, display message and points
        updateStateFields(
            getState, updateState,
            selectedPositions = emptyList(),
            displayMessage = DisplayMessage(
                res = Res.string.displayMsgAddPoints,
                arg = params.toString()
            ),
            scoreDifference = Score(points = params.toLong(), turns = 1)
        )

        //Modify board and queue
        val queuePool = NumberPool(gameMode.blockNumbers.toMutableList())
        val parityOrderList = selectedPositions.mapNotNull { position ->
            getState().board[position]?.value
        }.run {
            count { it % 2 == 0 } > size
        }.let { parity ->
            createParityOrderListUseCase(parity, selectedPositions.size)
        }

        //Copies of board and queue
        var board = getState().board
        var queue = getState().queue

        selectedPositions.forEachIndexed { index, targetPosition ->
            //Box from Queue to Board
            val travellingBox = queue[0]
            /**
             * Condicionar al tamaño de la queue
             * travellingBox = if(index > queueSize){
             *      getRandomBox
             * }else{
             *      queue[0]
             * }
             */


            //Add box on board
            board = addBoxOnBoardUseCase(
                board = board,
                targetPosition = targetPosition,
                newBox = travellingBox,
            )
            val newBoxOnQueue = getRandomBoxForQueueUseCase(
                queuePool.getPartialPool(parityOrderList[index]),
                gameMode.generateBlocksOnQueue
            )
            queue = addBoxOnQueueUseCase(
                currentQueue = queue,
                newBox = newBoxOnQueue,
                queueSize = gameMode.queueSize
            )
        }
        //Create star
        createStar(getState().score, gameMode)?.let { starBox ->
            queue = updateStarBoxOnQueueUseCase(
                queue = queue,
                newBox = starBox
            )
        }

/*
        //Update model
        saveGameStateUseCase(
            gameModeId = gameMode.getModeId(),
            board = board,
            queue = queue,
            score = getState().score,
            reloadsLeft = getState().reloadsLeft)
        println("Settings updated!")
*/


        //Update UI
        selectedPositions.forEachIndexed { index, targetPosition ->
            board[targetPosition]?.let { newBox ->
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
                    newBox = queue[updatedPosition],
                    queueSize = gameMode.queueSize
                )
                //Update UI
                updateStateFields(
                    getState, updateState,
                    board = newBoard,
                    queue = newQueue,
                )
                delay(490)
            }
        }
        //Check board state
        checkBoardStateAction(getState, updateState, gameMode)
    }

    private suspend fun createStar(
        score: Score,
        gameMode: GameMode,
    ): NumberBox.StarBox? =
        if ((score.turns -1) % gameMode.turnsForStar == 0) {
            if (gameMode.isGoldenStar(score.points)) {
                NumberBox.GoldenStarBox()
            } else {
                NumberBox.SilverStarBox()
            }
        } else {
            null
        }
}
