package com.danilisan.kmp.domain.extension

import com.danilisan.kmp.domain.entity.BoardNumberBox
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.Line


/**
 * TODO mover a companion object de class Line
 */

////Extensions for List<BoardNumberBox>
///**
// * Gets a list of numbers in line on the GameBoard
// */
//suspend fun List<BoardNumberBox>.getLine(
//    direction: BoardDirection,
//    originPos: Int
//): List<Int> {
//    val origin = if (originPos < 0) 3 else originPos
//    val lineFilter: (BoardNumberBox) -> Boolean = { box ->
//        when (direction) {
//            BoardDirection.HORIZONTAL -> box.row == origin
//            BoardDirection.VERTICAL -> box.column == origin
//            BoardDirection.DIAGONAL -> checkDiagonal(box, origin)
//        }
//    }
//    return this.filter{ lineFilter(it) }.map{ it.number.value }
//}
//
///**
// * Gets one list of numbers for every possible line
// * in which the given box can be
// */
//suspend fun List<BoardNumberBox>.getAllLines(
//    box: BoardNumberBox
//): List<List<Int>> {
//    val allLines: MutableList<List<Int>> = mutableListOf()
//    //Get HORIZONTAL LINE
//    allLines += this.getLine(
//        direction = BoardDirection.HORIZONTAL,
//        originPos = box.row
//    )
//    //Get VERTICAL LINE
//    allLines += this.getLine(
//        direction = BoardDirection.VERTICAL,
//        originPos = box.column
//    )
//    //Get DIAGONAL LINES
//    for(direction in 0..1){
//        if (checkDiagonal(box, direction)) {
//            allLines += this.getLine(
//                direction = BoardDirection.DIAGONAL,
//                originPos = direction
//            )
//        }
//    }
//    return allLines
//}
//
suspend fun Set<BoardNumberBox>.getAllAvailableLinesOnBoard(
    winCondition: suspend (List<Int>) -> Boolean
): List<Line>{
    return listOf()
}
//
//suspend fun List<BoardNumberBox>.getAvailableLinesByBox(
//    winCondition: (List<Int>) -> Boolean,
//    position: BoardPosition
//) {
//
//}
//
////Constants for Diagonals
//const val LEFT_TO_RIGHT = 0
//const val RIGHT_TO_LEFT = 1
//private const val HAS_ANY_DIAGONAL = -1
//private const val HAS_BOTH_DIAGONALS = -2
//
////Lambda function to check for diagonals for a given BoardNumberBox
//private val checkDiagonal: (BoardNumberBox, Int) -> Boolean =
//    { lambdaBox, origin ->
//        when (origin) {
//            LEFT_TO_RIGHT -> lambdaBox.row == lambdaBox.column
//            RIGHT_TO_LEFT -> lambdaBox.row + lambdaBox.column == 2
//            HAS_ANY_DIAGONAL -> (lambdaBox.row + lambdaBox.column) % 2 == 0
//            HAS_BOTH_DIAGONALS -> lambdaBox.row == 1 && lambdaBox.column == 1
//            else -> false
//        }
//    }

