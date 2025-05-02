package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.NumberPool
import com.danilisan.kmp.domain.entity.Score
import com.danilisan.kmp.domain.usecase.UseCase
import kotlinx.coroutines.withContext

class UpdateQueueToBoardValuesUseCase(
    override val dispatcher: DispatcherProvider,
    private val createRandomBoxForQueueUseCase: CreateRandomBoxForQueueUseCase,
    private val addBoxOnBoardUseCase: AddBoxOnBoardUseCase,
    private val addBoxOnQueueUseCase: AddBoxOnQueueUseCase,
    private val countStarsOnBoardUseCase: CountStarsOnBoardUseCase,
) : UseCase {
    suspend operator fun invoke(
        queuePool: NumberPool,
        parityOrderList: List<Boolean>,
        board: Map<BoardPosition, NumberBox>,
        queue: List<NumberBox>,
        score: Score,
        gameMode: GameMode,
        selectedPositions: List<BoardPosition>
    ): Pair<Map<BoardPosition, NumberBox>, List<NumberBox>> = withContext(dispatcher.default) {
        //Copies of board and queue
        var boardCopy = board
        var queueCopy = queue

        selectedPositions.forEachIndexed { index, targetPosition ->
            //Box from Queue to Board
            val travellingBox = queueCopy[0]
            /**
             * Condicionar al tamaño de la queue
             * travellingBox = if(index > queueSize){
             *      getRandomBox
             * }else{
             *      queue[0]
             * }
             */
            //Add box on board
            addBoxOnBoardUseCase(
                board = boardCopy,
                targetPosition = targetPosition,
                newBox = travellingBox,
            ).let { boardCopy = it }

            val newBoxOnQueue = createRandomBoxForQueueUseCase(
                queuePool.getPartialPool(parityOrderList[index]),
                gameMode.generateBlocksOnQueue
            )
            addBoxOnQueueUseCase(
                currentQueue = queueCopy,
                newBox = newBoxOnQueue,
                queueSize = gameMode.queueSize
            ).let { queueCopy = it }
        }
        //Create star
        countStarsOnBoardUseCase(boardCopy).takeIf { it < gameMode.maxStars }
            ?.let {
                gameMode.isGoldenStar(score)?.let { isGoldenStar ->
                    addBoxOnQueueUseCase(
                        currentQueue = queueCopy,
                        newBox = if(isGoldenStar){
                            NumberBox.GoldenStarBox()
                        }else{
                            NumberBox.SilverStarBox()
                        },
                        queueSize = gameMode.queueSize,
                        replaceLast = true,
                    ).let{ queueCopy = it }
                }
            }
        return@withContext Pair(boardCopy, queueCopy)
    }
}