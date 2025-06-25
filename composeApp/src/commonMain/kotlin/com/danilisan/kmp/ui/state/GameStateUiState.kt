package com.danilisan.kmp.ui.state

import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.DisplayMessage
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.Score

data class GameStateUiState (
    //Model fields
    val reloadsLeft: Int = 0,
    val queue: List<NumberBox> = emptyList(),
    val board: Map<BoardPosition, NumberBox> = emptyMap(),
    val score: Score = Score(),
    //UI exclusive fields
    val boardState: BoardState = BoardState.READY,
    val updatingPositions: List<BoardPosition?> = emptyList(),
    val availableLines: Set<Int> = emptySet(),
    val displayMessage: DisplayMessage = DisplayMessage(),
    //Box selection
    val selectedPositions: List<BoardPosition> = emptyList(),
    val incompleteSelection: Boolean = false,
    //Line drag
    val linedPositions: List<BoardPosition?> = emptyList(),
    val completedLines: List<Int> = emptyList(),

    //Loading flag
    val isLoading: Boolean = true,
)

