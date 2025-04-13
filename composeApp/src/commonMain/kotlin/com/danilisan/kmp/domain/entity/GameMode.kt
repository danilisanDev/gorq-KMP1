package com.danilisan.kmp.domain.entity

import com.danilisan.kmp.Mocks
import com.danilisan.kmp.domain.entity.BoardHelper.GAME_MODES
import com.danilisan.kmp.domain.entity.NumberBox.Companion.EMPTY_VALUE
import com.danilisan.kmp.ui.state.GameModeState
import io.github.aakira.napier.Napier
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.displayMsgAddNumbers
import kotlinproject.composeapp.generated.resources.displayMsgMultiplyNumbers
import kotlinproject.composeapp.generated.resources.easyAddMode
import kotlinproject.composeapp.generated.resources.icon_easyaddmode
import kotlinproject.composeapp.generated.resources.icon_multiplymode
import kotlinproject.composeapp.generated.resources.multiplyMode
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

sealed class GameMode {
    //Common values
    protected val boxNumbers: IntRange = (0..9)
    val turnsForStar: Int = 10
    val maxStars: Int = 3

    //Mode dependent values
    abstract val modeName: StringResource
    abstract val icon: DrawableResource

    //Default overriddable variables
    open val regularNumbers: Set<Int> = (0..7).toSet()
    open val numberRepetitions: Int = 2
    open val blockNumbers: Set<Int> = setOf(8, 9)
    open val lineLength: Int = 3
    open val queueSize: Int = 3
    open val minSelection: Int = 2
    open val maxSelection: Int = 3
    open val initialReloads: Int = 3
    open val reloadQueueCost: Int = -1
    open val reloadBoardCost: Int = -2
    open val generateBlocksOnQueue: Boolean = true
    open val readyDisplayMessage: StringResource = Res.string.displayMsgAddNumbers
    open val selectedNumbersSeparator = " + "

    fun getBoardSize(): Int = lineLength * lineLength
    fun getModeId(): Int = GAME_MODES.indexOf(this)
    fun getGameModeState(): GameModeState =
        GameModeState(
            modeId = getModeId(),
            icon = icon,
            queueSize = queueSize,
            lineLength = lineLength,
            reloadBoardCost = reloadBoardCost * -1,
            reloadQueueCost = reloadQueueCost * -1,
            ::isGoldenStar
        )

    //region WIN CONDITION RULE
    protected open val goalValues: Set<Int> = setOf(7, 17)
    fun getGoalValuesToString(): String = goalValues.joinToString(" | ")

    //Public common methods
    suspend fun isWinCondition(lineValues: List<Int>): Boolean =
        getWinConditionPoints(lineValues) > 0

    suspend fun getWinConditionPoints(lineValues: List<Int>): Long =
        calculatePointsByGameMode(lineValues)?.toLong() ?: 0L

    protected open suspend fun calculatePointsByGameMode(lineValues: List<Int>): Int? =
        lineValues.sum().takeIf { it in goalValues }

    suspend fun getWinConditionNumbers(lineValues: List<Int>): Set<Int> =
        lineValues
            .takeIf { list ->
                list.size == lineLength && list.count { it == EMPTY_VALUE } == 1
            }
            ?.let { list ->
                getNeededNumbers(list)
            }
            ?: run {
                Napier.d(
                    message = """Cannot get win condition numbers because 
                    line length doesn't match board size AND/OR 
                    line does not have one and only one empty value. 
                    Returning empty set."""
                )
                emptySet()
            }


    //Game dependent inner method
    protected open suspend fun getNeededNumbers(lineValues: List<Int>): Set<Int> =
        lineValues.filterNot { it == EMPTY_VALUE }.sum()
            .let { sum ->
                goalValues
                    .map { goal -> goal - sum }
                    .filter { it in boxNumbers || it < 0 }
                    .toSet()
            }
    //endregion

    //Condition for a golden Star
    fun isGoldenStar(score: Score): Boolean? =
        takeIf { ((score.turns - 1) % turnsForStar) == 0 }
            ?.run {
                checkGoldenStarByGameMode(score.points)
            }

    protected open fun checkGoldenStarByGameMode(points: Long): Boolean =
        (points % 100).toInt() in goalValues


    //Action score and reloads
    open suspend fun getScoreForLines(lines: Int): Score = Score(
        points = getPointsForLines(lines),
        lines = lines,
    )

    private suspend fun getPointsForLines(lines: Int): Long =
        (1..lines).fold(0){acc, i -> acc + i * 100}.toLong()

    open suspend fun getReloadIncrementForLines(lines: Int): Int =
        (lines - 1).takeIf{ it > 0 }?: 0

    open suspend fun getScoreForBingo(): Score = Score(
        points = getPointsForLines(17),
        lines = lineLength * 2 + 2,
    )

    open suspend fun getReloadIncrementForBingo(): Int = 7

    //Get Mock board
    abstract suspend fun getMockBoard(): Map<BoardPosition, NumberBox>

    /** TODO Traducir
     * Modo de juego con tablero 3x3 y reserva de 3 numeros.
     * Las casillas regulares aleatorias tienen valores entre 0 y 7.
     * La cola se rellena con bloques de valor 8 o 9.
     * * La seleccion ha de ser de entre 2-3 numeros.
     * La condicion de victoria es sumar 7 o 17.
     */
    data object EasyAdd : GameMode(){
        override val modeName = Res.string.easyAddMode
        override val icon = Res.drawable.icon_easyaddmode
        override suspend fun getMockBoard(): Map<BoardPosition, NumberBox> =
            Mocks.easyAddBoard
    }
    //Default values and functions


    /** TODO Traducir
     * Modo de juego con tablero 3x3 y reserva de 3 numeros.
     * Las casillas regulares aleatorias pueden tener cualquier valor.
     * La cola se rellena con bloques de valor 9.
     * La seleccion ha de ser de entre 2-3 numeros.
     * La condicion de victoria es formar multiplos 7 o 17.
     */
    data object HardMultiply : GameMode() {
        override val modeName = Res.string.multiplyMode
        override val icon = Res.drawable.icon_multiplymode
        override val regularNumbers = boxNumbers.toSet()
        override val blockNumbers = setOf(9)
        override suspend fun getMockBoard(): Map<BoardPosition, NumberBox> =
            Mocks.hardMultiplyBoard

        //numberRepetitions = 2
        //lineLength = 3
        //queueSize = 3
        //minSelection = 2
        //initialReloads = 7
        //reloadQueueCost: Int = 1
        //reloadBoardCost: Int = 2
        //generateBlocksOnQueue: Boolean = true
        override val readyDisplayMessage = Res.string.displayMsgMultiplyNumbers
        override val selectedNumbersSeparator = ""

        override suspend fun calculatePointsByGameMode(lineValues: List<Int>): Int? =
            lineValues
                .joinToString("")
                .toIntOrNull()
                ?.takeIf{ it > 0 }
                ?.let { result ->
                    goalValues
                        .filter{ result % it == 0}
                        .maxOrNull()
                }

        override suspend fun getNeededNumbers(lineValues: List<Int>): Set<Int> =
            goalValues.flatMap { calculateEmptyValueForMultiple(it, lineValues) }.toSet()

        private suspend fun calculateEmptyValueForMultiple(
            goalValue: Int,
            lineValues: List<Int>,
        ): Set<Int> {
            val factor = ((3 * goalValue - 1) / 10) //7 -> 2 // 17 -> 5
            val hundreds = lineValues[0] % goalValue
            val tens = lineValues[1] % goalValue
            val units = lineValues[2] % goalValue

            return when (lineValues.indexOf(EMPTY_VALUE)) {
                0 -> calculateHundredsValueForMultiple(factor, tens, units)
                1 -> calculateTensValueForMultiple(factor, hundreds, units)
                2 -> calculateUnitsValueForMultiple(hundreds, tens)
                else -> null
            }
                ?.let { result ->
                    getPossibleNumbersSet(result, goalValue)
                }
                ?: run {
                    emptySet()
                }
        }

        private fun calculateHundredsValueForMultiple(
            factor: Int,
            tens: Int,
            units: Int,
        ): Int = factor * (tens - factor * units)

        private fun calculateTensValueForMultiple(
            factor: Int,
            hundreds: Int,
            units: Int,
        ): Int = units * factor - hundreds * 10

        private fun calculateUnitsValueForMultiple(
            hundreds: Int,
            tens: Int,
        ): Int = -(hundreds * 100 + tens * 10)

        private fun getPossibleNumbersSet(
            result: Int,
            goalValue: Int,
        ): Set<Int> = mutableSetOf<Int>().also { numberSet ->
            var nextNumber = result % goalValue
            while (nextNumber <= boxNumbers.max()) {
                if (nextNumber in boxNumbers) {
                    numberSet.add(nextNumber)
                }
                nextNumber += goalValue
            }
        }

        override fun checkGoldenStarByGameMode(points: Long): Boolean = true
    }
}

    /** TODO Traducir
     * Modo de juego con tablero 4x4 y reserva de 4 numeros.
     * Las casillas regulares aleatorias pueden tener cualquier valor.
     * La cola se rellena con bloques de cualquier valor.
     * La seleccion ha de ser de entre 2-4 numeros.
     * La condición de victoria es sumar 11 o 21
     *
    data object JumboAdd : GameMode(){
        override val regularNumbers: Set<Int> = boxNumbers.toSet()
        override val blockNumbers: Set<Int> = regularNumbers
        override val goalValues: Set<Int> = setOf(11,21)
        override val boardSize: Int = 4
        override val queueSize: Int = 4
        override val maxSelection: Int = 4
        override val numberRepetitions: Int = 3

        //minSelection = 2
        //initialReloads = 7
        //reloadQueueCost: Int = 1
        //reloadBoardCost: Int = 2
        //generateBlocksOnQueue: Boolean = true

        //isWinCondition = DEFAULT
        //getWinConditionNumbers = DEFAULT
        //isGoldenStar = DEFAULT

    }
}

/** TODO Traducir
 * Modo de juego con tablero 3x3 y reserva de 1 numero.
 * Las casillas regulares aleatorias pueden tener cualquier valor.
 * La cola se rellena con casillas regulares de cualquier valor.
 * La seleccion ha de ser de 3 numeros. (¿2 o 3?)
 * **El exceso de la cola se cubre con casillas regulares aleatorias.**
 * **No hay recargas** ???
 * La condición de victoria es sumar 7 o 17
 *
data object BlindMode : GameMode(){
    override val regularNumbers: Set<Int> = boxNumbers.toSet()
    override val blockNumbers: Set<Int> = regularNumbers
    override val queueSize: Int = 1
    override val minSelection: Int = 3
}

/** TODO Traducir
 * Modo de juego con tablero 5x5 y reserva de 1 numero.
 * Las casillas regulares aleatorias pueden tener cualquier valor.
 * La cola se rellena con casillas regulares de cualquier valor.
 * La seleccion ha de ser de entre 1-3 numeros. (¿?)
 * **El exceso de la cola se cubre con casillas regulares de valor 0.**
 * **No hay recargas**
 * La condición de victoria es sumar 7 o 17
 *
data object VoidMode : GameMode(){
    override val regularNumbers: Set<Int> = boxNumbers.toSet()
    override val blockNumbers: Set<Int> = setOf(0)
    override val queueSize: Int = 1
    override val minSelection: Int = 1
    override val maxSelection: Int = 3
}
*/