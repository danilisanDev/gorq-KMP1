package com.danilisan.kmp.ui.view.gamestate

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import com.danilisan.kmp.domain.entity.BoardPosition

@Composable
fun UIBoardDragDisplay(
    containerWidthInPx: Float,
    getLineLength: () -> Int,
    isDragStartEnabled: () -> Boolean,
    isDragStarted: () -> Boolean,
    dragAction: (BoardPosition?, Int) -> Unit,
    content: @Composable () -> Unit,
) = Box(
    modifier = Modifier //Action display box for dragging lines
        .fillMaxSize()
        .onDragActions(
            containerWidthInPx,
            getLineLength,
            isDragStartEnabled,
            isDragStarted,
            dragAction
        )
) {
    content()
}

@Composable
private fun Modifier.onDragActions(
    containerWidthInPx: Float,
    getLineLength: () -> Int,
    isDragStartEnabled: () -> Boolean,
    isDragStarted: () -> Boolean,
    dragAction: (BoardPosition?, Int) -> Unit,
): Modifier {
    val dragStartEnabled by remember {
        derivedStateOf {
            isDragStartEnabled()
        }
    }
    val dragStarted by remember {
        derivedStateOf {
            isDragStarted()
        }
    }
    val lineLength = getLineLength()

    return this.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { offset ->
                    if(dragStartEnabled){
                        val startingPosition =
                            positionFromOffset(
                                offset = offset,
                                containerWidthInPx = containerWidthInPx,
                                lineLength = lineLength,
                                onDragStart = true
                            )
                        dragAction(startingPosition, 0)
                    }
                },
                onDrag = { pointerChange, _ ->
                    if (dragStarted) {
                        val newPosition = positionFromOffset(
                            offset = pointerChange.position,
                            containerWidthInPx = containerWidthInPx,
                            lineLength = lineLength,
                            onDragStart = false,
                        )
                        dragAction(newPosition, 1)
                    }
                },
                onDragEnd = { dragAction(null, 2) },
                onDragCancel = { dragAction(null, 2) },
            )
        }
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
        ?.let { yPos ->
            calculatePositionFromOffset(yPos, containerWidthInPx, lineLength, onDragStart)
        } ?: return null

    val column = offset.x
        .takeIf { it in 0f..containerWidthInPx }
        ?.let { xPos ->
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