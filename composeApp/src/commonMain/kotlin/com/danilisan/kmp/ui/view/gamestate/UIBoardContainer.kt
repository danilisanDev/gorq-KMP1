package com.danilisan.kmp.ui.view.gamestate

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import com.danilisan.kmp.ui.state.BoardState
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.ui.view.withAlpha
import com.danilisan.kmp.ui.view.toPx

//TODO Abstraer propiedades y medidas
const val FRAME_WIDTH = 0.1f
const val FRAME_PADDING = 1 - (FRAME_WIDTH)
const val INNER_SIZE = 1 - (FRAME_WIDTH * 2)

@Composable
fun UIBoardContainer(
    lineLength: Int,
    board: Map<BoardPosition, NumberBox>,
    selectedPositions: List<BoardPosition> = emptyList(),
    linedPositions: List<BoardPosition> = emptyList(),
    completedLines: List<Int> = emptyList(),
    availableLines: Set<Int> = emptySet(),
    boardState: BoardState = BoardState.READY,
    isEnabled: Boolean = false,
    selectAction: (BoardPosition) -> Unit = {},
    dragAction: (BoardPosition?, Int) -> Unit = {_, _ -> },
) {
    //Board container
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
    ) {
        //Board background (READY only)
        if(boardState == BoardState.READY){
            BoardBackground()
        }
        val maxWidthInPx = maxWidth.toPx()
        Box(
            modifier = Modifier //Action display box for dragging lines
                .fillMaxSize()
                .dragActions( //TODO Condicionar dragActions a BoardState.READY &&
                    maxWidthInPx = maxWidthInPx,
                    lineLength = lineLength,
                    dragAction = dragAction,
                )
//                .run{
//                    if(isEnabled || linedPositions.isNotEmpty()){
//                        this
//                    }else{
//                        this
//                    }
//    }
                ,
            contentAlignment = Alignment.Center,
        ) {
            //Board frame
            UIBoardFrame(
                containerWidthInPx = maxWidthInPx,
                boardState = boardState,
                lineLength = lineLength,
                completedLines = completedLines,
                availableLines = availableLines
            )

            //Board numbers
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(INNER_SIZE)
                    .aspectRatio(1f)
            ) {
                UIBoardNumbers(
                    lineLength = lineLength,
                    selectedPositions = selectedPositions,
                    linedPositions = linedPositions,
                    completedLinesSize = completedLines.size,
                    board = board,
                    state = boardState,
                    isEnabled = isEnabled,
                    selectAction = selectAction
                )
            }
        }
    }
}

@Composable
private fun BoardBackground() =
    Theme.colors.primary.withAlpha(0.1f).let{ bgColor ->
        Canvas(modifier = Modifier
            .fillMaxSize()
            .clip(shape = Theme.shapes.softBlockShape)
        ){
            drawRect(color = bgColor)
        }
    }

private fun Modifier.dragActions(
    maxWidthInPx: Float,
    lineLength: Int,
    dragAction: (BoardPosition?, Int) -> Unit,
): Modifier = this
    .pointerInput(Unit) { //TODO Abstraer a una funcion separada
        detectDragGestures(
            onDragStart = { offset ->
                val startingPosition =
                    positionFromOffset(
                        offset = offset,
                        containerWidthInPx = maxWidthInPx,
                        lineLength = lineLength,
                        onDragStart = true)
                dragAction(startingPosition, 0)
            },
            onDrag = { pointerChange, _ ->
                val newPosition = positionFromOffset(
                    offset = pointerChange.position,
                    containerWidthInPx = maxWidthInPx,
                    lineLength = lineLength,
                    onDragStart = false,
                )
                dragAction(newPosition, 1)
            },
            onDragEnd = { dragAction(null, 2) },
            onDragCancel = { dragAction(null, 2) },
        )
    }




//Offset calculations for drag action
/**
 * Returns a BoardPosition from the offset passed
 * by the drag event.
 * If the event is onDragStart, an offset outside Board bounds
 * will return the BoardPosition with first or last row / column.
 * If else, the BoardPosition will only be returned when the offset
 * is within the inner center of the box (80%);
 * if not, null is returned
 */
private fun positionFromOffset(
    offset: Offset,
    containerWidthInPx: Float,
    lineLength: Int,
    onDragStart: Boolean,
): BoardPosition? {
    val row = offset.y
        .takeIf { it in 0f..containerWidthInPx }
        ?.let{ yPos ->
            calculatePositionFromOffset(yPos, containerWidthInPx, lineLength, onDragStart)
        } ?: return null

    val column = offset.x
        .takeIf { it in 0f..containerWidthInPx }
        ?.let{ xPos ->
            calculatePositionFromOffset(xPos, containerWidthInPx, lineLength, onDragStart)
        } ?: return null

    return BoardPosition(row = row, column = column)
}

/**
 * Returns the row or column relative to the offset dimension passed (x or y).
 * If onDragStart is false, returns null when offset
 * is not within the inner center of the length of the box (80%)
 */
private fun calculatePositionFromOffset(
    relativeOffsetDimension: Float,
    totalLength: Float,
    lineLength: Int,
    onDragStart: Boolean
): Int? {
    val realOffset = relativeOffsetDimension - FRAME_WIDTH * totalLength
    val boardLength = totalLength * INNER_SIZE
    val realPosition = (realOffset / (boardLength / lineLength)).toInt()

    return if (onDragStart) {
        when {
            realOffset < 0 -> 0
            realOffset > boardLength -> lineLength - 1
            else -> realPosition
        }
    } else {
        val MAX_DEVIATION = 0.20f
        val centerDeviation = (boardLength / lineLength).toInt().let { boxLength ->
            realOffset % boxLength / boxLength
        }
        realPosition.takeUnless {
            realOffset < 0 || realOffset > boardLength ||
                    centerDeviation !in MAX_DEVIATION..(1 - MAX_DEVIATION)
        }
    }
}