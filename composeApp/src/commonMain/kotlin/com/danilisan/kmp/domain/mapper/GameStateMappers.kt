package com.danilisan.kmp.domain.mapper

import com.danilisan.kmp.data.model.GameStateModel
import com.danilisan.kmp.data.model.gameState.ScoreModel
import com.danilisan.kmp.domain.entity.BoardNumberBox
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.entity.Score
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.extension.getAllAvailableLinesOnBoard
import com.danilisan.kmp.ui.state.GameStateUiState

private const val GAME_MODE_EASY_ADD = 0 //Default
private const val GAME_MODE_HARD_MULTIPLY = 1

private const val NUMBER_TYPE_REGULAR = 0 //Default
private const val NUMBER_TYPE_BLOCK = 1
private const val NUMBER_TYPE_STAR = 2
private const val INTERVAL_TYPE = 10



//Extensions for mapping from Model to Domain/UI
//Aplicar corrutinas
suspend fun GameStateModel.toUiState(): GameStateUiState{
    val gameMode = this.gameMode.toUiGameMode()
    val board = this.board.toUiBoard(gameMode.lineLength)
    val queue = this.queue.toUiQueue()
    val score = this.score.toUiScore()
    val availableLines = board.getAllAvailableLinesOnBoard(gameMode.isWinCondition)
    return GameStateUiState(
        gameMode = gameMode,
        board = board,
        queue = queue,
        score = score,
        availableLines = availableLines,
        reloadsLeft = this.reloadsLeft,
        isBlocked = this.isBlocked,
    )
}

private fun Int.toUiGameMode(): GameMode {
    return when(this){
        GAME_MODE_EASY_ADD -> GameMode.EasyAddGameMode
        GAME_MODE_HARD_MULTIPLY -> GameMode.HardMultiplyGameMode
        else -> GameMode.EasyAddGameMode
    }
}

private fun List<Int>.toUiBoard(boardSize: Int): Set<BoardNumberBox> {
    return this.mapIndexed {index, value ->
        BoardNumberBox(
            number = value.toUiNumberBox(),
            position = BoardPosition(
                column = index / boardSize,
                row = index % boardSize,
            )
        )
    }.toSet()
}

private fun List<Int>.toUiQueue(): List<NumberBox> {
    return this.map { value ->
        value.toUiNumberBox()
    }.toMutableList()
}

private fun Int.toUiNumberBox(): NumberBox{
    val type = this / INTERVAL_TYPE
    return when (type){
        NUMBER_TYPE_REGULAR -> NumberBox.RegularBox(
            value = this
        )
        NUMBER_TYPE_BLOCK -> NumberBox.BlockBox(
            value = this - (NUMBER_TYPE_BLOCK * INTERVAL_TYPE)
        )
        NUMBER_TYPE_STAR -> NumberBox.StarBox()
        else -> NumberBox.RegularBox(
            value = this % INTERVAL_TYPE
        )
    }
}

private fun ScoreModel.toUiScore(): Score {
    return Score(
        points = this.points,
        lines = this.lines
    )
}











//Extensions for mapping from Domain/UI to Model
fun GameStateUiState.toModel(): GameStateModel {
    val gameModeInt = when (this.gameMode) {
        is GameMode.EasyAddGameMode -> GAME_MODE_EASY_ADD
        is GameMode.HardMultiplyGameMode -> GAME_MODE_HARD_MULTIPLY
    }
    return GameStateModel(
        gameMode = gameModeInt,
        //board
        //queue
        //score
        reloadsLeft = this.reloadsLeft
    )
}

fun NumberBox.toModel(): Int{
    return when(this){
        is NumberBox.RegularBox -> this.value
        is NumberBox.BlockBox -> NUMBER_TYPE_BLOCK * INTERVAL_TYPE + this.value
        is NumberBox.StarBox -> NUMBER_TYPE_STAR * INTERVAL_TYPE
    }
}



