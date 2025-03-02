package com.danilisan.kmp.domain.action.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.BoardHelper
import com.danilisan.kmp.domain.entity.BoardHelper.filterByLine
import com.danilisan.kmp.domain.entity.BoardHelper.getBoardPositionFromLineIdAndIndex
import com.danilisan.kmp.domain.entity.BoardHelper.getLineLength
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.DisplayMessage
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.NumberBox.Companion.EMPTY_VALUE
import com.danilisan.kmp.domain.usecase.gamestate.GetDisplayMessageUseCase
import com.danilisan.kmp.domain.usecase.gamestate.GetWinningLinesUseCase
import com.danilisan.kmp.ui.state.GameStateUiState
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.displayMsgNewLines
import kotlinx.coroutines.withContext

class DragLineAction(
    override val dispatcher: DispatcherProvider,
    private val getWinningLinesUseCase: GetWinningLinesUseCase,
    private val getDisplayMessageUseCase: GetDisplayMessageUseCase,
) : GameStateAction {
    override suspend operator fun invoke(
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
        gameMode: GameMode,
        params: Any?,
    ) = withContext(dispatcher.default) {
        //Check expected type for params (BoardPosition)
        if (params !is BoardPosition) return@withContext

        //Stored positions in state
        val linedPositions = getState().linedPositions.toMutableList()
        if (linedPositions.isEmpty()) return@withContext //Impossible case

        //Return if there is no movement towards new position
        if (linedPositions.last() == params) return@withContext

        val lineLength = gameMode.lineLength
        val lastRealPosition = linedPositions.filterNotNull().last()
        //Stored lines in state
        val completedLines = getState().completedLines.toMutableList()
        val positionsInCurrentLine = linedPositions.run {
            try{
                subList(completedLines.size * (lineLength - 1), size).filterNotNull()
            }catch(e: Exception){
                return@withContext
            }
        }
        var board: Map<BoardPosition, NumberBox>? = null

        //Check movement backwards
        if (linedPositions.size > 1 && linedPositions[linedPositions.size - 2] == params) {
            linedPositions.run {
                removeAt(size - 1)
                    .takeIf { it != null }?.run {
                        if (positionsInCurrentLine.size == 1 && completedLines.isNotEmpty()) {
                            completedLines.run {
                                removeAt(size - 1)
                            }
                            //Update value from silverBoxes
                            board = getState().board.apply{
                                values.forEach{ box ->
                                    if(box is NumberBox.SilverStarBox){
                                        box.setValue(EMPTY_VALUE)
                                    }
                                }
                            }.let{newBoard ->
                                updateSilverStarValues(
                                    board = newBoard,
                                    completedLines = completedLines,
                                    getWinConditionNumbers = gameMode::getWinConditionNumbers
                                )
                            }
                        }
                    }
            }
            printLinesState(
                completedLines,
                linedPositions,
                lineLength
            )
        } else {
            if (!params.isAdjacentPosition(lastRealPosition) || params in positionsInCurrentLine) {
                linedPositions.addPositionOrNull()
            } else {//Check if last position + new position are in the same line
                val newLinedPositions = positionsInCurrentLine + params
                BoardHelper.getLineIdFromPositions(
                    positions = newLinedPositions,
                    lineLength = lineLength,
                )
                    ?.takeIf { lineId ->
                        lineId in getState().availableLines
                                && lineId !in completedLines
                    }
                    ?.let { newLineId ->
                        linedPositions.addPositionOrNull(params)
                        if (newLinedPositions.size == lineLength) {
                            completedLines.add(newLineId)
                            //Add value to SilverBox
                            board = updateSilverStarValues(
                                completedLines = completedLines,
                                board = getState().board,
                                getWinConditionNumbers = gameMode::getWinConditionNumbers
                            )
                        }
                        //Debug
                        printLinesState(
                            completedLines,
                            linedPositions,
                            lineLength
                        )
                    } ?: run {
                    linedPositions.addPositionOrNull()
                }
            }
        }

        //If silver stars changed value, check winning lines again
        val availableLines = board?.let {
            getWinningLinesUseCase(
                board = it,
                isWinCondition = gameMode::isWinCondition
            )
        }

        //Get new message
        val newMessage = if (completedLines.isEmpty()) {
            getDisplayMessageUseCase(getState().boardState, gameMode)
        } else {
            DisplayMessage(
                res = Res.plurals.displayMsgNewLines,
                arg = completedLines.size.toString()
            )
        }
        updateStateFields(
            getState, updateState,
            board = board,
            availableLines = availableLines,
            linedPositions = linedPositions,
            completedLines = completedLines,
            selectedPositions = emptyList(),
            displayMessage = newMessage,
        )
    }
}

//TODO Abstraer a UseCase
private suspend fun updateSilverStarValues(
    board: Map<BoardPosition, NumberBox>,
    completedLines: List<Int>,
    getWinConditionNumbers: suspend (List<Int>) -> Set<Int>,
): Map<BoardPosition, NumberBox> {
    completedLines.reversed().forEach{ lineId ->
        board.filterByLine(lineId).takeUnless { boxes ->
            boxes.count { it is NumberBox.SilverStarBox && it.value == EMPTY_VALUE } != 1
                    || boxes.any { it is NumberBox.GoldenStarBox }
        }?.let { boxes ->
            val silverPosition = getBoardPositionFromLineIdAndIndex(
                lineId = lineId,
                index = boxes.indexOfFirst { it is NumberBox.SilverStarBox && it.value == EMPTY_VALUE },
                lineLength = board.getLineLength(),
            )
            getWinConditionNumbers(boxes.map { it.value }).takeIf { it.isNotEmpty() }
                ?.let { winningNumbers ->
                    board[silverPosition]?.setValue(winningNumbers.max())
                }
        }
    }
    return board
}

private fun printLinesState(
    completedLines: List<Int>,
    linedPositions: List<BoardPosition?>,
    lineLength: Int,
) {
    val minPositions = completedLines.size * (lineLength - 1) + 1
    val maxPositions = minPositions + lineLength - 2
    val linesStatus = linedPositions.filterNotNull().size in minPositions..maxPositions
    println(
        "Lined positions: $linedPositions; " +
                "completed lines: $completedLines; " +
                "Line status = $linesStatus"
    )
}

private fun MutableList<BoardPosition?>.addPositionOrNull(
    newPosition: BoardPosition? = null
) = this.run {
    remove(null)
    add(newPosition)
}



