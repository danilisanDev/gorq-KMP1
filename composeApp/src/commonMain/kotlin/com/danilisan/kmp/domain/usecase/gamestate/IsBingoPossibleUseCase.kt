package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.BoardHelper.checkWinningLine
import com.danilisan.kmp.domain.entity.BoardHelper.getAllBoardLinesFromPosition
import com.danilisan.kmp.domain.entity.NumberBox.Companion.EMPTY_VALUE
import com.danilisan.kmp.domain.usecase.UseCase
import kotlinx.coroutines.withContext

class IsBingoPossibleUseCase(
    override val dispatcher: DispatcherProvider
) : UseCase {
    suspend operator fun invoke(
        board: Map<BoardPosition, NumberBox>,
        isWinCondition: suspend (List<Int>) -> Boolean,
        getNeededNumbers: suspend (List<Int>) -> Set<Int>,
    ): Boolean = withContext(dispatcher.default) {
        //For saving winning values for each silverStar (Map<BoardPosition, MutableList<Int>>)
        val silverPositionValuesMap = board.filterValues { box ->
            box is NumberBox.SilverStarBox && box.value == EMPTY_VALUE
        }.keys.associateWith { mutableSetOf<Int>() }
            .takeIf{ it.isNotEmpty() }
        ?: run {
            return@withContext true
        }

        //For saving lines with more than 1 silver star
        val manyStarsLinesSet = mutableSetOf<Int>()

        //Check all lines with stars
        silverPositionValuesMap.keys.forEach { starPosition ->
            checkLinesWithOneSilverStar(
                starPosition,
                board,
                silverPositionValuesMap,
                manyStarsLinesSet,
                getNeededNumbers
            )
        }

        //Check if each star has only a single possible/needed value
        when {
            anySilverStarHasMultipleValues(silverPositionValuesMap.values.toList())
                -> false    //Any silverStar needs different values to win all its lines
            manyStarsLinesSet.isEmpty()
                -> true     //All silverStars need only one value AND there are no lines with two stars
            else
                -> checkLinesWithTwoSilverStars(
                    board,
                    silverPositionValuesMap,
                    manyStarsLinesSet,
                    isWinCondition
                )
        }
    }

    /**
     * Checks every line where a given SilverStarBox is present:
     * If there is more than one SilverStar, that lineId is saved in a Set;
     * else the needed number is saved in a list mapped to that position.
     * EXCEPTION: If there are more than 1 needed number.
     */
    private suspend fun checkLinesWithOneSilverStar(
        starPosition: BoardPosition,
        board: Map<BoardPosition, NumberBox>,
        silverPositionValuesMap: Map<BoardPosition, MutableSet<Int>>,
        manyStarsLinesSet: MutableSet<Int>,
        getNeededNumber: suspend (List<Int>) -> Set<Int>,
    ) {
        board.getAllBoardLinesFromPosition(starPosition).forEach { line ->
            line.value //List<NumberBox>
                .takeIf { lineBoxes ->
                    //If there is a GoldenStar, no need to look for winning numbers
                    lineBoxes.none { it is NumberBox.GoldenStarBox }
                }
                ?.let { lineBoxes ->
                    if (lineBoxes.count { it is NumberBox.SilverStarBox && it.value == EMPTY_VALUE } > 1) {
                        //If there is more than 1 silver star without value -> save line for later
                        manyStarsLinesSet.add(line.key)
                    } else {
                        //If there is only 1 silver star on the line -> look for winning number
                        val lineNumbers = lineBoxes.map { it.value }
                        val neededNumbers = getNeededNumber(lineNumbers)
                        check(neededNumbers.isNotEmpty()) {
                            "getNeededNumbers rule returned ${neededNumbers.size} elements. Function only supported for \"sum\" gamemodes."
                        }
                        silverPositionValuesMap[starPosition]?.add(neededNumbers.max())
                    }
                }
        }
    }

    /**
     * Recieves a list of sets with potential winning numbers for each silverStar
     * Checks if any set has more than one needed number
     */
    private suspend fun anySilverStarHasMultipleValues(
        winningNumbersByPosition: List<Set<Int>>
    ): Boolean =
        winningNumbersByPosition.any { winningNumbersSet ->
            winningNumbersSet.size != 1
        }

    /**
     * Checks win condition in those lines where there are more than 1 silver star
     * with values saved in silverPositionValuesMap.
     */
    private suspend fun checkLinesWithTwoSilverStars(
        board: Map<BoardPosition, NumberBox>,
        silverPositionValuesMap: Map<BoardPosition, MutableSet<Int>>,
        manyStarsLinesSet: MutableSet<Int>,
        isWinCondition: suspend (List<Int>) -> Boolean,
    ): Boolean {
        //Copy silver star value from Map to Board
        copySilverValuesOnBoard(silverPositionValuesMap, board)
        //Check win condition on board with all values on
        return checkLinesWithAllValues(board, manyStarsLinesSet, isWinCondition)
    }

    /**
     * For each silver star position saved in Map,
     * copy its value on Board (there is only one value
     */
    private suspend fun copySilverValuesOnBoard(
        silverPositionValuesMap: Map<BoardPosition, MutableSet<Int>>,
        board: Map<BoardPosition, NumberBox>,
    ) =
        board.toMutableMap().also { mutableBoard ->
            silverPositionValuesMap.forEach { (position, value) ->
                mutableBoard[position] = NumberBox.RegularBox(value.first())
            }
        }

    /**
     *
     */
    private suspend fun checkLinesWithAllValues(
        board: Map<BoardPosition, NumberBox>,
        manyStarsLinesSet: MutableSet<Int>,
        isWinCondition: suspend (List<Int>) -> Boolean,
    ): Boolean =
        manyStarsLinesSet.map { lineId ->
            board.checkWinningLine(lineId, isWinCondition)  //List<NumberBox>
        }.all { it }
}
