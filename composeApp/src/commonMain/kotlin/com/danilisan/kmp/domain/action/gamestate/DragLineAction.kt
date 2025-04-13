package com.danilisan.kmp.domain.action.gamestate

import androidx.compose.ui.text.font.FontWeight
import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.BoardHelper
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.DisplayMessage
import com.danilisan.kmp.domain.entity.DisplayMessage.Companion.DISPLAY_TEXT_SUCCESS
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.NumberBox.Companion.EMPTY_VALUE
import com.danilisan.kmp.domain.usecase.gamestate.GetDisplayMessageUseCase
import com.danilisan.kmp.domain.usecase.gamestate.GetWinningLinesUseCase
import com.danilisan.kmp.domain.usecase.gamestate.UpdateSilverStarValuesUseCase
import com.danilisan.kmp.ui.state.GameStateUiState
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.displayMsgNewLines
import kotlinx.coroutines.withContext

class DragLineAction(
    override val dispatcher: DispatcherProvider,
    private val updateSilverStarValuesUseCase: UpdateSilverStarValuesUseCase,
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

        //Update state with new position
        val lineLength = gameMode.lineLength
        val completedLines = getState().completedLines.toMutableList()
        val positionsInCurrentLine = linedPositions.run {
            try{
                subList(completedLines.size * (lineLength - 1), size).filterNotNull()
            }catch(e: Exception){
                return@withContext
            }
        }
        var board: Map<BoardPosition, NumberBox>? = null

        linedPositions
            .takeIf{ it.size > 1 && it[it.size - 2] == params }
            ?.run{ //Movement backwards
                removeAt(size - 1) //Remove last position
                    .takeIf { it != null }?.run {
                        //If removed is not null -> Check line undoing
                        if (positionsInCurrentLine.size == 1 && completedLines.isNotEmpty()) {
                            completedLines.run {
                                removeAt(size - 1)
                            }
                            //Update value from silverBoxes
                            board = getState().board.apply {
                                values.forEach { box ->
                                    if (box is NumberBox.SilverStarBox) {
                                        box.setValue(EMPTY_VALUE)
                                    }
                                }
                            }.let { newBoard ->
                                updateSilverStarValuesUseCase(
                                    board = newBoard,
                                    completedLines = completedLines,
                                    getWinConditionNumbers = gameMode::getWinConditionNumbers
                                )
                            }
                        }
                    }
        } ?: run{ //Movement towards new position
            val lastRealPosition = linedPositions.filterNotNull().last()
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
                            board = updateSilverStarValuesUseCase(
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
            completedLines.size.let { lines ->
                DisplayMessage(
                    res = Res.plurals.displayMsgNewLines,
                    arg = lines.toString(),
                    highlightColor = DISPLAY_TEXT_SUCCESS,
                    weight = when(lines){
                        0 -> FontWeight.Normal
                        5 -> FontWeight.ExtraBold
                        else -> FontWeight.Bold
                    },
                    sizeDiff = lines - 1,
                )
            }

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



