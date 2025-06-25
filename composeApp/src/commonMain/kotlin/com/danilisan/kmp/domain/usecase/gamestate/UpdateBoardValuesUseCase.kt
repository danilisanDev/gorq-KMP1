package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.NumberPool
import com.danilisan.kmp.domain.usecase.UseCase
import kotlinx.coroutines.withContext

class UpdateBoardValuesUseCase(
    override val dispatcher: DispatcherProvider,
    private val addBoxOnBoardUseCase: AddBoxOnBoardUseCase,
    private val createRandomBoxForBoardUseCase: CreateRandomBoxForBoardUseCase,
) : UseCase {
    suspend operator fun invoke(
        currentPool: NumberPool,
        parityOrderList: List<Boolean>,
        board: Map<BoardPosition, NumberBox>,
        targetPositions: List<BoardPosition>,
        gameMode: GameMode,
    ): Map<BoardPosition, NumberBox> = withContext(dispatcher.default){
        var resultBoard = board
        //Replace targetPositions for EmptyBox
        targetPositions.forEach { targetPosition ->
            addBoxOnBoardUseCase(
                newBox = NumberBox.EmptyBox(),
                targetPosition = targetPosition,
                board = resultBoard,
            ).let {
                resultBoard = it
            }
        }
        //Replace emptyBox for random RegularBox
        targetPositions.forEachIndexed { index, targetPosition ->
            //Create new box for target position
            createRandomBoxForBoardUseCase(
                currentBoard = resultBoard,
                targetPosition = targetPosition,
                pool = currentPool,
                parity = parityOrderList[index],
                getNeededNumbers = gameMode::getWinConditionNumbers,
            )?.let { newRegularBox ->
                currentPool.removeFromPool(newRegularBox.value)
                addBoxOnBoardUseCase(
                    newBox = newRegularBox,
                    targetPosition = targetPosition,
                    board = resultBoard,
                ).let {
                    resultBoard = it
                }
            }?: println("Error generando casilla")
        }
        return@withContext resultBoard
        //return@withContext Mocks.bingoNoStarsBoard
    }
}