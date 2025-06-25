package com.danilisan.kmp.domain.entity
import com.danilisan.kmp.domain.entity.BoardHelper.LineDirection
import com.danilisan.kmp.domain.entity.BoardHelper.getDirectionAndIndexFromLineId

/**
 * Represents a two-dimension position on the Board
 */

class BoardPosition (
    val row: Int = -1,
    val column: Int = -1,
): Comparable<BoardPosition>{
    /**
     * Returns the sum of column + row
     */
    fun getRightDiagonalValue(): Int = row + column

    /**
     * Returns alternate values for a checked board
     */
    fun getRightDiagonalParity(): Boolean = getRightDiagonalValue() % 2 == 0
    /**
     * Checks if this position takes part in the LEFT diagonal
     */
    fun isOnLeftDiagonal(): Boolean =
        this.row == this.column

    /**
     * Checks if this position takes part in the RIGHT diagonal
     */
    fun isOnRightDiagonal (lineLength: Int = -1): Boolean =
         getRightDiagonalValue() == (lineLength - 1)

    /**
     * Checks if this position takes part in the given line (from direction and index)
     */
    fun isOnGivenLine (lineId: Int, lineLength: Int = -1): Boolean =
        getDirectionAndIndexFromLineId(lineId).let{ (direction, index) ->
            isOnGivenLine(direction,index,lineLength)
        }

    /**
     * Checks if this position takes part in the given line (from direction and index)
     */
    fun isOnGivenLine (direction: Int, index: Int = 0, lineLength: Int = -1): Boolean =
        when(direction){
            LineDirection.LEFT_DIAGONAL() -> isOnLeftDiagonal()
            LineDirection.RIGHT_DIAGONAL() -> isOnRightDiagonal(lineLength)
            LineDirection.HORIZONTAL() -> row == index
            LineDirection.VERTICAL() -> column == index
            else -> false
        }

    fun isValidPosition(lineLength: Int = 0): Boolean =
        (column in 0 until lineLength
                && row in 0 until lineLength)

    suspend fun isAdjacentPosition(other: BoardPosition): Boolean =
        this != other &&
            (this.row - other.row) in (-1..1) &&
                    (this.column - other.column) in (-1..1)

    override fun toString(): String = "[${this::class.simpleName}]: R$row-C$column"

    override fun equals(other: Any?): Boolean {
        if (this === other){
            return true
        }
        if (other !is BoardPosition)
            return false

        return this.row == other.row && this.column == other.column
    }

    override fun hashCode(): Int {
        return row.hashCode() + column.hashCode()
    }

    /**
     * BoardPositions are sorted first by column, then by row.
     * Ergo, diagonals are always read from left to right
     */
    override fun compareTo(other: BoardPosition): Int = when {
        this.column != other.column -> this.column compareTo other.column
        this.row != other.row -> this.row compareTo other.row
        else -> 0
    }
}