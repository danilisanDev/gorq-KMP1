package com.danilisan.kmp.domain.entity

import com.danilisan.kmp.domain.entity.GameMode.EasyAdd
import com.danilisan.kmp.domain.entity.GameMode.HardMultiply
import com.danilisan.kmp.domain.entity.NumberBox.Companion.EMPTY_VALUE
import org.jetbrains.compose.resources.StringResource

object BoardHelper {
    val GAME_MODES: List<GameMode> = listOf(
        EasyAdd,
        HardMultiply,
    )

    fun getModeNames(): List<StringResource> = GAME_MODES.map { it.modeName }

    enum class LineDirection(private val code: Int) {
        LEFT_DIAGONAL(code = 0),    //TopLeft to BottomRight
        RIGHT_DIAGONAL(code = 1),   //TopRight to BottomLeft
        HORIZONTAL(code = 2),       //20 + BoardPosition.row
        VERTICAL(code = 3);         //30 + BoardPosition.column

        operator fun invoke() = code
    }

    private val AVAILABLE_DIRECTIONS: IntRange =
        LineDirection.LEFT_DIAGONAL()..LineDirection.VERTICAL()

    private fun Int.isDiagonal(): Boolean = (this < LineDirection.HORIZONTAL())
    suspend fun Int.isVertical(): Boolean = (this / 10 == LineDirection.VERTICAL())
    suspend fun Int.isHorizontal(): Boolean = (this / 10 == LineDirection.HORIZONTAL())

    /**
     * Returns a set of lineId that fulfill the condition.
     * Default lineLength (or no parameters) returns empty set.
     * Default condition returns full set.
     */
    suspend fun getLineIdsWithCondition(
        lineLength: Int = 0,
        condition: suspend (Int) -> Boolean = { _ -> true }
    ): Set<Int> = if (lineLength == 0) {
        emptySet()
    } else {
        mutableSetOf<Int>()
            .also { resultSet ->
                for (direction in AVAILABLE_DIRECTIONS) {
                    for (index in 0 until lineLength) {
                        lineIdBuilder(direction, index)
                            ?.let { lineId ->
                                if (condition(lineId)) {
                                    resultSet.add(lineId)
                                }
                            }
                    }
                }
            }
    }

    fun lineIdBuilder(direction: Int, position: Int = 0): Int? {
        val impossibleDirection = direction !in AVAILABLE_DIRECTIONS
        val impossibleLine = direction.isDiagonal() && position != 0
        return if (impossibleDirection || impossibleLine) {
            null
        } else {
            direction * 10 + position
        }
    }

    /**
     * From a list of BoardPositions
     * returns null when they cannot take part together in the same line,
     * or the first position couldn't be the first on the line;
     * otherwise, return the lineId.
     */
    suspend fun getLineIdFromPositions(
        positions: List<BoardPosition>,
        lineLength: Int
    ): Int? {
        val firstRow = positions.first().row
        val firstCol = positions.first().column
        val isCenterCol = firstCol in 1..lineLength-2
        val isCenterRow = firstRow in 1..lineLength-2
        return when{
            positions.all{ it.isOnLeftDiagonal() } && !isCenterCol
                -> lineIdBuilder(LineDirection.LEFT_DIAGONAL())
            positions.all{ it.isOnRightDiagonal(lineLength)} && !isCenterCol
                -> lineIdBuilder(LineDirection.RIGHT_DIAGONAL())
            positions.all { it.row == firstRow } && !isCenterCol
                -> lineIdBuilder(LineDirection.HORIZONTAL(), firstRow)
            positions.all{ it.column == firstCol } && !isCenterRow
                -> lineIdBuilder(LineDirection.VERTICAL(), firstCol)
            else -> null
        }
    }

    fun getDirectionAndIndexFromLineId(lineId: Int): Pair<Int,Int> =
        Pair(
            getDirectionFromLineId(lineId),
            getIndexFromLineId(lineId)
        )

    private fun getDirectionFromLineId(lineId: Int): Int = lineId / 10

    private fun getIndexFromLineId(lineId: Int): Int = lineId % 10

    suspend fun getBoardPositionFromLineIdAndIndex(lineId: Int, index: Int, lineLength: Int): BoardPosition =
        getDirectionFromLineId(lineId).let{direction ->
            when(direction){
                LineDirection.LEFT_DIAGONAL() -> BoardPosition (row = index, column = index)
                LineDirection.RIGHT_DIAGONAL() -> BoardPosition(row = (lineLength - index - 1), column = index)
                LineDirection.HORIZONTAL() -> BoardPosition(row = getIndexFromLineId(lineId), column = index)
                LineDirection.VERTICAL() -> BoardPosition(row = index, column = getIndexFromLineId(lineId))
                else -> BoardPosition(0,0)
            }
        }

    /**
     * A lambda boolean filter
     * that identifies BoardNumberBoxes (from its BoardPosition)
     * within a given line on the board
     */
    private suspend fun getFilterByLine(
        position: BoardPosition,
        lineId: Int,
        lineLength: Int
    ): Boolean =
        getDirectionFromLineId(lineId).let { direction ->
            when (direction) {
                LineDirection.LEFT_DIAGONAL() -> {
                    position.isOnLeftDiagonal()
                }

                LineDirection.RIGHT_DIAGONAL() -> {
                    position.isOnRightDiagonal(lineLength)
                }

                LineDirection.HORIZONTAL() -> {
                    position.row == getIndexFromLineId(lineId)
                }

                LineDirection.VERTICAL() -> {
                    position.column == getIndexFromLineId(lineId)
                }

                else -> false
            }
        }

    //region EXTENSION FUNCTIONS OF BOARD = Map<BoardPosition, BoardNumberBox>

    /**
     * Gets lineLength (= side length) from the board
     */
    fun Map<BoardPosition, NumberBox>.getLineLength(): Int =
        this.keys.count { it.row == 0 }

    /**
     * Gets the maximum number of lines
     * that can be made on board
     * Line length * 2 (HORIZONTAL & VERTICAL) + 2 (DIAGONALS)
     */
    fun Map<BoardPosition, NumberBox>.getMaxLines(): Int =
        this.getLineLength().let { it * 2 + 2 }

    /**
     * Returns a List of the BoardPositions where there is an EmptyBox,
     * sorted diagonally from upperLeft to bottomRight
     */
    fun Map<BoardPosition, NumberBox>.getEmptyPositionsSortedDiagonally(): List<BoardPosition> =
        this.filterValues { it is NumberBox.EmptyBox }.keys
            .sortedWith(compareBy { it.getRightDiagonalValue() })

    /**
     * Checks if the given line has as many values as the lineLength requires
     * AND if those values fulfill the win condition OR there is a StarBox without value
     */
    suspend fun Map<BoardPosition, NumberBox>.checkWinningLine(
        lineId: Int, isWinCondition: suspend (List<Int>) -> Boolean,
    ): Boolean = this.filterByLine(lineId).let { lineBoxes ->
        lineBoxes.size == this.getLineLength() &&
                (isWinCondition(lineBoxes.map { it.value }) ||
                        lineBoxes.any { it is NumberBox.StarBox && it.value == EMPTY_VALUE })
    }

    /**
     * Returns a list of the BoardLines
     * where the given targetPosition takes part in
     */
    suspend fun Map<BoardPosition, NumberBox>.getAllBoardLinesFromPosition(
        targetPosition: BoardPosition,
    ): Map<Int, List<NumberBox>> =
        mutableMapOf<Int, List<NumberBox>>().also { resultMap ->
            for (direction in AVAILABLE_DIRECTIONS) {
                this.getLineFromDirectionAndPosition(
                    direction = direction,
                    targetPosition = targetPosition,
                )?.let { boardLine ->
                    resultMap += boardLine
                }
            }
        }


    /**
     * Returns a Pair with the lineId and BoardNumberBoxes
     * corresponding the given direction
     * where the targetPosition takes part in.
     * If the targetPosition is not present in a diagonal,
     * returns null
     */
    private suspend fun Map<BoardPosition, NumberBox>.getLineFromDirectionAndPosition(
        direction: Int,
        targetPosition: BoardPosition,
    ): Pair<Int, List<NumberBox>>? {
        val index = when (direction) {
            LineDirection.HORIZONTAL() -> targetPosition.row
            LineDirection.VERTICAL() -> targetPosition.column
            else -> 0.takeIf {
                targetPosition.isOnGivenLine(
                    direction = direction,
                    lineLength = this.getLineLength()
                )
            }
        } ?: return null

        return lineIdBuilder(direction, index)?.let { lineId ->
            Pair(lineId, this.filterByLine(lineId))
        }
    }

    /**
     * Gets an ordered list of NumberBox for a given line (lineId = direction + index)
     */
    suspend fun Map<BoardPosition, NumberBox>.filterByLine(
        lineId: Int,
    ): List<NumberBox> {
        val linePositions = this.getLineLength().let { lineLength ->
            this.keys.filter { key -> getFilterByLine(key, lineId, lineLength) }.sorted()
        }

        return mutableListOf<NumberBox>()
            .also { resultList ->
                linePositions.forEach { position ->
                    this[position]?.let { boxValue ->
                        resultList.add(boxValue)
                    }
                }
            }
    }
//endregion
}



