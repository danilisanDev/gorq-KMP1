package com.danilisan.kmp.ui.view.gamestate

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.danilisan.kmp.ui.state.BoardState
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.domain.entity.BoardHelper.LineDirection
import com.danilisan.kmp.domain.entity.BoardHelper.lineIdBuilder
import com.danilisan.kmp.domain.entity.BoardHelper.getLineLength
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.ui.theme.plus
import com.danilisan.kmp.ui.theme.withAlpha


const val FRAME_WIDTH = 0.1f
const val FRAME_PADDING = 1 - (FRAME_WIDTH / 4)
const val INNER_SIZE = 1 - (FRAME_WIDTH * 2)

@Composable
fun UIBoard(
    board: Map<BoardPosition, NumberBox>,
    selectedPositions: List<BoardPosition>,
    linedPositions: List<BoardPosition>,
    completedLines: List<Int>,
    availableLines: Set<Int>,
    boardState: BoardState,
    isEnabled: Boolean,
    selectAction: (BoardPosition) -> Unit,
    dragAction: (BoardPosition?, Int) -> Unit,
) {
    val lineLength = board.getLineLength()
    val primaryColor = when (boardState) {
        BoardState.READY -> Theme.colors.secondary.withAlpha(0.3f)
        BoardState.BLOCKED -> Theme.colors.error
        BoardState.BINGO -> Theme.colors.success
        BoardState.GAMEOVER -> Theme.colors.secondary.withAlpha(0.5f)
    }
    val shape = Theme.shapes.softBlockShape
//    if (boardState == BoardState.BINGO) {
//
//    } else {
//        Theme.shapes.hardBlockShape
//    }
    //Board container
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .background(
                color = primaryColor,
                shape = shape
            )
            .background(
                brush = Brush.radialGradient(
                    listOf(
                        Theme.colors.transparent,
                        Theme.colors.primary.withAlpha(0.2f)
                    )
                ),
                shape = shape
            ),
        contentAlignment = Alignment.Center,
    ) {
        val containerLength = maxWidth
        val lengthPx = with(LocalDensity.current) { containerLength.toPx() }
        Box(
            modifier = Modifier //Action display box for dragging lines
                .fillMaxSize()
                .pointerInput(Unit) {
                    if (isEnabled) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val startingPosition =
                                    positionFromOffset(offset, lengthPx, lineLength, true)
                                dragAction(startingPosition, 0)
                            },
                            onDrag = { pointerChange, _ ->
                                val newPosition = positionFromOffset(
                                    pointerChange.position,
                                    lengthPx,
                                    lineLength,
                                    false,
                                )
                                dragAction(newPosition, 1)
                            },
                            onDragEnd = { dragAction(null, 2) },
                            onDragCancel = { dragAction(null, 2) },
                        )
                    }
                },
        ) {
            //Board frame
            if (boardState == BoardState.READY) {
                LineIndicators(
                    lineLength,
                    completedLines,
                    availableLines,
                )
            } else if (boardState != BoardState.BINGO) {
                SteadyBorder(
                    containerLength,
                    primaryColor,
                )
            }

            //Board numbers
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(INNER_SIZE)
                    .aspectRatio(1f)
            ) {
                BoardNumbers(
                    lineLength = lineLength,
                    selectedPositions = selectedPositions,
                    linedPositions = linedPositions,
                    completedLinesSize = completedLines.size,
                    board = board,
                    state = boardState,
                    isLoading = isEnabled,
                    selectAction = selectAction
                )
            }
        }
    }
}

private fun positionFromOffset(
    offset: Offset,
    length: Float,
    lineLength: Int,
    starting: Boolean,
): BoardPosition? {
    val xPos = offset.x.takeIf { it in 0f..length } ?: return null
    val yPos = offset.y.takeIf { it in 0f..length } ?: return null

    val row = calculatePositionFromOffset(yPos, length, lineLength, starting)
    val column = calculatePositionFromOffset(xPos, length, lineLength, starting)

    return if (row != null && column != null) {
        BoardPosition(row = row, column = column)
    } else {
        null
    }

}

private fun calculatePositionFromOffset(
    relativeOffset: Float,
    totalLength: Float,
    lineLength: Int,
    starting: Boolean
): Int? {
    val realOffset = relativeOffset - FRAME_WIDTH * totalLength
    val boardLength = totalLength * INNER_SIZE
    val realPosition = (realOffset / (boardLength / lineLength)).toInt()

    return if (starting) {
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

@Composable
private fun SteadyBorder(
    containerSize: Dp,
    primaryColor: Color,
) {
    val borderShape = Theme.shapes.hardBlockShape
    Box(
        modifier = Modifier
            .fillMaxSize(FRAME_PADDING)
            .border(
                width = containerSize * (FRAME_WIDTH / 2),
                color = Theme.colors.secondary.withAlpha(0.2f),
                shape = borderShape
            )
            .border(
                width = containerSize * (FRAME_WIDTH / 2),
                color = primaryColor,
                shape = borderShape
            )
    )
}


@Composable
private fun LineIndicators(
    lineLength: Int,
    completedLines: List<Int>,
    availableLines: Set<Int>,
) {
    val relativeSize: Float = INNER_SIZE / lineLength
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val fullRowModifier = Modifier
            .fillMaxWidth()
            .weight(FRAME_WIDTH)
        val sideRowModifier = Modifier
            .fillMaxWidth()
            .weight(relativeSize)

        //Top row
        FullRowIndicators(
            fullRowModifier,
            availableLines,
            completedLines,
            lineLength,
            true,
        )

        //Middle rows
        for (index in 0 until lineLength) {
            SideRowIndicators(
                sideRowModifier,
                availableLines,
                completedLines,
                index
            )
        }

        //Bottom row
        FullRowIndicators(
            fullRowModifier,
            availableLines,
            completedLines,
            lineLength,
            false,
        )
    }
}

@Composable
private fun FullRowIndicators(
    modifier: Modifier,
    availableLines: Set<Int>,
    completedLines: List<Int>,
    lineLength: Int,
    isTopRow: Boolean,
) {
    val diagonalLines = getDiagonalLinesIdPair(isTopRow)
    var corner = if (isTopRow) 1 else 3
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        var alignment = getCornerAlignment(corner++)
        Box(
            modifier = Modifier
                .weight(FRAME_WIDTH)
                .aspectRatio(1f),
            contentAlignment = alignment
        ) {
            Indicator(
                diagonalLines?.first ?: -1,
                availableLines,
                completedLines,
            )
        }

        Row(
            modifier = Modifier
                .weight(INNER_SIZE)
                .aspectRatio(8f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            for (position in 0 until lineLength) {
                lineIdBuilder(LineDirection.VERTICAL(), position)?.let { lineId ->
                    Indicator(
                        lineId,
                        availableLines,
                        completedLines,
                    )
                }
            }
        }
        alignment = getCornerAlignment(corner++)
        Box(
            modifier = Modifier
                .weight(FRAME_WIDTH)
                .aspectRatio(1f),
            contentAlignment = alignment
        ) {
            Indicator(
                diagonalLines?.second ?: -1,
                availableLines,
                completedLines,
            )
        }
    }
}

private fun getDiagonalLinesIdPair(isTopRow: Boolean): Pair<Int, Int>? {
    val leftDiagonal = lineIdBuilder(direction = LineDirection.LEFT_DIAGONAL()) ?: return null
    val rightDiagonal = lineIdBuilder(direction = LineDirection.RIGHT_DIAGONAL()) ?: return null
    return if (isTopRow) {
        Pair(leftDiagonal, rightDiagonal)
    } else {
        Pair(rightDiagonal, leftDiagonal)
    }
}


@Composable
private fun SideRowIndicators(
    modifier: Modifier,
    availableLines: Set<Int>,
    completedLines: List<Int>,
    position: Int
) {
    lineIdBuilder(LineDirection.HORIZONTAL(), position)?.let { lineId ->
        Row(
            modifier = modifier,
        ) {
            Box(
                modifier = Modifier
                    .weight(FRAME_WIDTH)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Indicator(
                    lineId,
                    availableLines,
                    completedLines,
                )
            }
            Spacer(modifier = Modifier.weight(INNER_SIZE))
            Box(
                modifier = Modifier
                    .weight(FRAME_WIDTH)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Indicator(
                    lineId,
                    availableLines,
                    completedLines,
                )
            }
        }
    }


}

private val INDICATOR_SIZE = 10.dp

@Composable
private fun getCornerAlignment(position: Int): Alignment {
    return when (position) {
        1 -> Alignment.BottomEnd
        2 -> Alignment.BottomStart
        3 -> Alignment.TopEnd
        4 -> Alignment.TopStart
        else -> error("Impossible 5th corner")
    }
}

@Composable
private fun Indicator(
    lineId: Int,
    availableLines: Set<Int>,
    completedLines: List<Int>,
) {
    val color = when (lineId) {
        in completedLines -> Theme.colors.success
        in availableLines -> Theme.colors.primary
        else -> Color.Transparent
    }
    Box(
        modifier = Modifier
            .padding(INDICATOR_SIZE / 2)
            .size(INDICATOR_SIZE)
            .background(
                color = color,
                shape = CircleShape
            )
    )
}

@Composable
private fun BoardNumbers(
    lineLength: Int,
    board: Map<BoardPosition, NumberBox>,
    selectedPositions: List<BoardPosition>,
    linedPositions: List<BoardPosition>,
    completedLinesSize: Int,
    state: BoardState,
    isLoading: Boolean,
    selectAction: (BoardPosition) -> Unit
) {
    val relativeSize = 1f / lineLength

    Column(verticalArrangement = Arrangement.Center) {
        for (row in 0 until lineLength) {
            Row(horizontalArrangement = Arrangement.Center) {
                for (column in 0 until lineLength) {
                    val position = BoardPosition(row = row, column = column)
                    board[position]?.let { boardBox ->
                        Box(
                            modifier = Modifier
                                .weight(relativeSize)
                                .aspectRatio(1f)
                        ) {
                            getTintAndPosition(
                                position = position,
                                incompleteLength = lineLength - 1,
                                completedLinesSize = completedLinesSize,
                                selectedPositions = selectedPositions,
                                linedPositions = linedPositions,
                            ).let{ (tintColor, index) ->
                                UINumberBox(
                                    numberBox = boardBox,
                                    position = index,
                                    tintColor = tintColor,
                                    boardPosition = position,
                                    state = state,
                                    isEnabled = isLoading,
                                    selectAction = selectAction,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun getTintAndPosition(
    position: BoardPosition,
    incompleteLength: Int,
    completedLinesSize: Int,
    selectedPositions: List<BoardPosition>,
    linedPositions: List<BoardPosition>
): Pair<Color, Int> {
    when (position) {
        in selectedPositions -> {
            return Pair(
                Theme.colors.selected,
                selectedPositions.indexOf(position)
            )
        }
        in linedPositions -> {
            val limit = (1 + completedLinesSize * incompleteLength)
            if (completedLinesSize == 0 || limit > linedPositions.size) {
                return Pair(Color.Transparent, 1)
            } else {
                val greenNumbers = linedPositions.subList(0, limit)

                val color = greenNumbers.lastIndexOf(position).let{ index ->
                    if(index < 0){
                        Color.Transparent
                    }else{
                        val line = (index / incompleteLength) + 1
                        Theme.colors.success + if(line < completedLinesSize) {
                            Theme.colors.primary.withAlpha((completedLinesSize - line) * 0.5f)
                        }else{ null }
                    }
                }
                val pos = if(linedPositions.lastIndexOf(position) >= limit) 1 else 0
                return Pair(color, pos)
            }
        }
        else -> {
            return Pair(Color.Transparent, 0)
        }
    }
}

