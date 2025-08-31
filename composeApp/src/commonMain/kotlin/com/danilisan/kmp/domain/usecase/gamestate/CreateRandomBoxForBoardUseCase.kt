package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.NumberPool
import com.danilisan.kmp.domain.entity.BoardHelper.getAllBoardLinesFromPosition
import com.danilisan.kmp.domain.usecase.UseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext

/**
 * Use case that returns a BoardNumberBox (RegularBox)
 * for the given BoardPosition,
 * whose value is randomly picked from the Pool,
 * excluding numbers which would fulfill the winCondition
 * on the given (current) Board
 */
class CreateRandomBoxForBoardUseCase(
    override val dispatcher: DispatcherProvider
) : UseCase {
    suspend operator fun invoke(
        currentBoard: Map<BoardPosition, NumberBox>,
        targetPosition: BoardPosition,
        pool: NumberPool,
        parity: Boolean,
        getNeededNumbers: suspend (List<Int>) -> Set<Int>,
    ): NumberBox? = withContext(dispatcher.default) {
        //1. Check if target position has EmptyBox
        currentBoard[targetPosition]
            ?.let{ targetBox ->
                if(targetBox !is NumberBox.EmptyBox){
                    Napier.d(message = "There is no empty box in $targetPosition")
                    return@withContext null
                }
            }
            ?: run{ //If there is no box (get == null), add an empty box
                currentBoard
                    .toMutableMap()[targetPosition] = NumberBox.EmptyBox()
            }

        //2. Remove all possible winning numbers from pool
        val poolForBox = mutableSetOf<Int>().also { winningNumbers ->
            //Check all available lines for target position
            currentBoard
                .getAllBoardLinesFromPosition(targetPosition)   //Map<Int, List<NumberBox>
                .values.forEach { line -> //List<NumberBox>
                    //Add needed numbers for lines with 1 EmptyBox and no StarBoxes
                    line.takeIf { lineBoxes ->
                        lineBoxes.none { it is NumberBox.StarBox }
                                && lineBoxes.count { it is NumberBox.EmptyBox } == 1
                    }
                        ?.let { lineBoxes ->
                            winningNumbers.addAll(getNeededNumbers(lineBoxes.map { it.value }))
                        }
                }
        }.let { winningNumbers -> //Remove winningNumbers from pool
            pool.getPartialPool(parity, winningNumbers)
        }

        //3. Return random number from pool
        return@withContext NumberBox.RegularBox(poolForBox.random())
    }
}
