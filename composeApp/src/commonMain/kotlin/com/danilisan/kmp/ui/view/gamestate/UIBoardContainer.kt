package com.danilisan.kmp.ui.view.gamestate

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
    showMenuBar: MutableState<Boolean>,
    getLineLength: () -> Int,
    getBoard: () -> Map<BoardPosition, NumberBox>,
    getBoardState: () -> BoardState = { BoardState.BLOCKED },
    getUpdatingPositions: () -> List<BoardPosition?> = { emptyList() },
    getLinedPositions: () -> List<BoardPosition> = { emptyList() },
    getCompletedLines: () -> List<Int> = { emptyList() },
    getAvailableLines: () -> Set<Int> = { emptySet() },
    getSelectedPositions: () -> List<BoardPosition> = { emptyList() },
    applyStarAnimation: () -> Brush?,
    isEnabled: (Boolean) -> Boolean = { _ -> false },
    selectAction: (BoardPosition) -> Unit = {},
    dragAction: (BoardPosition?, Int) -> Unit = { _, _ -> },
) {
    //Board container
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        //Board container background
        BoardBackground(
            getBoardState = getBoardState
        )

        val maxWidthInPx = maxWidth.toPx()

        //Board frame
        UIBoardFrame(
            containerWidthInPx = maxWidthInPx,
            getBoardState = getBoardState,
            getLineLength = getLineLength,
            getCompletedLines = getCompletedLines,
            getAvailableLines = getAvailableLines,
            getPositionsNotInLine = { lineLength, completedLinesSize ->
                getLinedPositions()
                    .takeIf { it.isNotEmpty() }
                    ?.drop((lineLength - 1) * completedLinesSize)
                    ?: emptyList()
            },
            showMenuBar = showMenuBar,
        )

        //Board display
        UIBoardDragDisplay(
            containerWidthInPx = maxWidthInPx,
            getLineLength = getLineLength,
            isDragStartEnabled = { isEnabled(false) },
            isDragStarted = { getLinedPositions().isNotEmpty() },
            dragAction = dragAction
        ) {
            //Board numbers
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(INNER_SIZE)
                    .aspectRatio(1f)
            ) {
                UIBoardNumbers(
                    showMenuBar = showMenuBar,
                    getLineLength = getLineLength,
                    getBoard = getBoard,
                    getUpdatingPositions = getUpdatingPositions,
                    getSelectedPositions = getSelectedPositions,
                    getLinedPositions = getLinedPositions,
                    getCompletedLinesSize = { getCompletedLines().size },
                    getBoardState = getBoardState,
                    applyStarAnimation = applyStarAnimation,
                    isSelectionEnabled = { isEnabled(true) },
                    selectAction = selectAction
                )
            }
        }
        ClosingMenuDisplay(showMenuBar)
    }
}

@Composable
private fun ClosingMenuDisplay(showMenuBar: MutableState<Boolean>){
    if(showMenuBar.value){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable{
                    showMenuBar.value = false
                }
        )
    }
}

@Composable
private fun BoardBackground(
    getBoardState: () -> BoardState,
) = getBoardState().let { boardState ->
    when (boardState) {
        BoardState.READY -> Theme.colors.secondary.withAlpha(0.2f)
        BoardState.BINGO -> Theme.colors.golden.withAlpha(0.2f)
        else -> null
    }?.let { bgColor ->
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape = Theme.shapes.softBlockShape)
        ) {
            drawRect(color = bgColor)
        }
    }
}