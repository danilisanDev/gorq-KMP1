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

/**
 * LINE-START -> **LINE_DRAG** -> LINE_END
 * Action for the drag gesture performed on the Board (always after LineStartAction).
 * New BoardPosition is added into GameState (linedPositions);
 * if the drag gesture is going back (BoardPosition is second to last in linedPositions),
 * the 'new' BoardPosition is removed from GameState
 * When this action is performed, SelectBoxAction is excluded (until LineEndAction).
 * Display message is updated depending on numbers selected.
 * @param param (expected BoardPosition) BoardPosition where the pointer is dragged to
 *      If the BoardPosition remains the same, do nothing.
 *      If the BoardPosition is null (out of bounds of the board), do nothing.
 */
class LineDragAction(
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
    ): Boolean = withContext(dispatcher.default) {
        //Check expected type for params (BoardPosition)
        if (params !is BoardPosition) return@withContext false

        //Stored positions in state
        val linedPositions = getState().linedPositions.toMutableList()
        if (linedPositions.isEmpty()) return@withContext false//Impossible case

        //Return if there is no movement towards new position
        if (linedPositions.last() == params) return@withContext false

        //Add null to exclude select action
        if(linedPositions.size == 1 && linedPositions.first() != null){
            linedPositions.add(0, null)
        }

        //Update state with new position
        val lineLength = gameMode.lineLength
        val completedLines = getState().completedLines.toMutableList()
        val positionsInCurrentLine = linedPositions.filterNotNull().run {
            try{
                subList(completedLines.size * (lineLength - 1), size)
            }catch(e: Exception){
                //TODO: Napier.e(mensaje de error)
                return@withContext false
            }
        }
        var board: Map<BoardPosition, NumberBox>? = null

        linedPositions
            .takeIf{ it.size > 1 && it[it.size - 2] == params }
            ?.run{ //Movement backwards
                removeAt(size - 1) //Remove last position
                    .run {
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
            if (params.isAdjacentPosition(lastRealPosition) && params !in positionsInCurrentLine) {
                //Check if last position + new position are in the same line
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
                        linedPositions.add(params)
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

        return@withContext true
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