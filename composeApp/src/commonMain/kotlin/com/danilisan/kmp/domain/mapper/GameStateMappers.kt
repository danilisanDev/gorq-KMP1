package com.danilisan.kmp.domain.mapper

import com.danilisan.kmp.data.model.GameStateModel
import com.danilisan.kmp.data.model.gameState.ScoreModel
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.entity.GameMode.Companion.getListOfGameModes
import com.danilisan.kmp.domain.entity.Score
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.ui.state.GameStateUiState

/**
 * Functions for mapping GameState fields
 * from Model to Domain/UI and viceversa.
 */

private const val NUMBER_TYPE_REGULAR = 0 //Default
private const val NUMBER_TYPE_BLOCK = 1
private const val NUMBER_TYPE_GOLDEN_STAR = 2
private const val NUMBER_TYPE_SILVER_STAR = 3
private const val INTERVAL_TYPE = 10

//Extensions for mapping from Model to Domain/UI
fun GameStateModel.toUiState(): Pair<GameMode, GameStateUiState>{
    val gameMode = this.gameMode.toUiGameMode()
    return Pair(
        gameMode,
        GameStateUiState(
            reloadsLeft = this.reloadsLeft,
            queue = this.queue.toUiQueue(),
            board = this.board.toUiBoard(gameMode.lineLength),
            score = this.score.toUiScore(),
        )
    )
}

private fun Int.toUiGameMode(): GameMode = try{
        getListOfGameModes()[this]
    }catch (e: IndexOutOfBoundsException){ //Impossible case
        getListOfGameModes().first()?: GameMode.EasyAdd
    }

private fun List<Int>.toUiBoard(boardSize: Int): Map<BoardPosition, NumberBox> =
    this.mapIndexed {index, value ->
        BoardPosition(
            row = index % boardSize,
            column = index / boardSize,
        ).let{ positionKey ->
            positionKey to value.toUiNumberBox()
        }
    }.toMap()


private fun List<Int>.toUiQueue(): List<NumberBox> =
    this.map { value ->
        value.toUiNumberBox()
    }.toMutableList()

private fun Int.toUiNumberBox(): NumberBox =
    (this / INTERVAL_TYPE).let{ type ->
        when (type){
            NUMBER_TYPE_REGULAR -> NumberBox.RegularBox(
                value = this
            )
            NUMBER_TYPE_BLOCK -> NumberBox.BlockBox(
                value = this - (NUMBER_TYPE_BLOCK * INTERVAL_TYPE)
            )
            NUMBER_TYPE_GOLDEN_STAR -> NumberBox.GoldenStarBox()
            NUMBER_TYPE_SILVER_STAR -> NumberBox.SilverStarBox()
            else -> NumberBox.RegularBox(
                value = this % INTERVAL_TYPE
            )
        }
    }

fun ScoreModel.toUiScore(): Score = Score(
        points = this.points,
        lines = this.lines,
        turns = this.turns,
        maxPoints = this.maxPoints
    )


//Extensions for mapping from Domain/UI to Model
fun createGameStateModelFromUIFields(
    gameModeId: Int,
    board: Map<BoardPosition, NumberBox>,
    queue: List<NumberBox>,
    score: Score,
    reloadsLeft: Int,
): GameStateModel = GameStateModel(
    gameMode = gameModeId,
    board = board.toModelBoard(),
    queue = queue.toModelQueue(),
    score = score.toModelScore(),
    reloadsLeft = reloadsLeft,
)

private fun Map<BoardPosition, NumberBox>.toModelBoard(): List<Int> =
    this.keys.sorted().let{ sortedPositions ->
        mutableListOf<Int?>().also{ resultList ->
            sortedPositions.forEach{ position ->
                resultList.add(this[position]?.toModelBox())
            }
        }.filterNotNull()
    }

private fun List<NumberBox>.toModelQueue(): List<Int> = this.map{ it.toModelBox() }

fun Score.toModelScore(): ScoreModel = ScoreModel(
    points = this.points,
    turns = this.turns,
    lines = this.lines,
    maxPoints = this.maxPoints
)

private fun NumberBox.toModelBox(): Int{
    if(this is NumberBox.EmptyBox) return 0 //RegularBox value 0

    val boxValue = if(this is NumberBox.StarBox){
        0
    }else{
        this.value
    }
    return when(this){
        is NumberBox.BlockBox -> NUMBER_TYPE_BLOCK
        is NumberBox.GoldenStarBox -> NUMBER_TYPE_GOLDEN_STAR
        is NumberBox.SilverStarBox -> NUMBER_TYPE_SILVER_STAR
        else -> NUMBER_TYPE_REGULAR
    } * INTERVAL_TYPE + boxValue
}



