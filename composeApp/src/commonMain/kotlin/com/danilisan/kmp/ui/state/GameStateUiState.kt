package com.danilisan.kmp.ui.state

import com.danilisan.kmp.domain.entity.BoardNumberBox
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.entity.Line
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.Score
import com.danilisan.kmp.domain.extension.getAllAvailableLinesOnBoard

data class GameStateUiState (
    val gameMode: GameMode = DEFAULT_MODE,
    val board: Set<BoardNumberBox> = emptySet(),
    val queue: List<NumberBox> = emptyList(),
    val score: Score = Score(),
    val availableLines: List<Line> = emptyList(),
    val reloadsLeft: Int = gameMode.initialReloads,
    val isLoading: Boolean = true,
    val isBlocked: Boolean = true
){
    suspend fun calculateAvailableLines(): List<Line>{
        return this.board.getAllAvailableLinesOnBoard(this.gameMode.isWinCondition)
    }
    companion object{
        val DEFAULT_MODE = GameMode.EasyAddGameMode
    }
}

