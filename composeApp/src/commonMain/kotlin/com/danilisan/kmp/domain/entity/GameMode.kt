package com.danilisan.kmp.domain.entity

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

sealed class GameMode {
    //Pool generation rule (FINAL)
    val getNumberPool: () -> NumberPool = {
        val pool: MutableList<Int> = mutableListOf()
        for (i in 2..lineLength) {
            pool += regularNumbers
        }
        pool.shuffle()
        NumberPool(pool)
    }

    //Default values
    open val regularNumbers: Set<Int> = (0..7).toSet()
    open val blockNumbers: Set<Int> = setOf(8, 9)
    open val goalValues: Set<Int> = setOf(7, 17)
    open val lineLength: Int = 3
    open val initialReloads: Int = 7

    //Default lambda functions
    open val getNeededNumbers: suspend (List<Int>) -> List<Int> = { lineValues ->
        val sum = lineValues.sum()
        for (goal in goalValues.sorted()) {
            val result = goal - sum
            if (result >= 0) {
                listOf(result)
            }
        }
        listOf()
    }
    open val isWinCondition: suspend (List<Int>) -> Boolean = { lineValues ->
        goalValues.contains(lineValues.sum())
    }

    private suspend fun getPartialPool(pool: NumberPool, isEven: Boolean): List<Int> {
        val parityPool: Deferred<List<Int>>
        withContext(Dispatchers.Default){
            parityPool = async {
                pool.getPartialPool(
                    parityEven = isEven
                )
            }
        }
        return parityPool.await()
    }

    private sealed class boardDisposition{
        protected suspend fun generateDisposition(vararg positions: POSITION): List<BoardPosition>{
            val result = mutableListOf<Deferred<BoardPosition>>()
            withContext(Dispatchers.Default){
                for(position in positions){
                    result.add(async {
                        getBoardPosition(position)
                    })
                }
            }
            return result.awaitAll()
        }
        abstract suspend fun getEvenBoardPositionsList(): List<BoardPosition>
        abstract suspend fun getOddBoardPositionsList(): List<BoardPosition>


        object CROSS: boardDisposition(){
            override suspend fun getEvenBoardPositionsList(): List<BoardPosition> {
                return generateDisposition(
                    POSITION.TOP, POSITION.LEFT, POSITION.RIGHT,
                    POSITION.CENTER, POSITION.BOTTOM
                )
            }

            override suspend fun getOddBoardPositionsList(): List<BoardPosition> {
                return generateDisposition(
                    POSITION.TOP_LEFT, POSITION.TOP_RIGHT,
                    POSITION.BOTTOM_LEFT, POSITION.BOTTOM_RIGHT
                )
            }
        }
        object LEFT_RIGHT: boardDisposition(){
            override suspend fun getEvenBoardPositionsList(): List<BoardPosition> {
                return generateDisposition(
                    POSITION.TOP_LEFT, POSITION.CENTER, POSITION.BOTTOM_RIGHT
                )
            }
            override suspend fun getOddBoardPositionsList(): List<BoardPosition> {
                return generateDisposition(
                    POSITION.TOP, POSITION.TOP_RIGHT,
                    POSITION.LEFT,POSITION.RIGHT,
                    POSITION.BOTTOM_LEFT, POSITION.BOTTOM
                )
            }
        }
        object RIGHT_LEFT: boardDisposition() {
            override suspend fun getEvenBoardPositionsList(): List<BoardPosition> {
                return generateDisposition(
                    POSITION.BOTTOM_LEFT, POSITION.CENTER, POSITION.TOP_RIGHT
                )
            }
            override suspend fun getOddBoardPositionsList(): List<BoardPosition> {
                return generateDisposition(
                    POSITION.TOP_LEFT, POSITION.TOP,
                    POSITION.LEFT,POSITION.RIGHT,
                    POSITION.BOTTOM, POSITION.BOTTOM_RIGHT
                )
            }
        }

        private enum class POSITION {
            TOP_LEFT, TOP, TOP_RIGHT,
            LEFT, CENTER, RIGHT,
            BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT
        }

        private suspend fun getBoardPosition(position: POSITION): BoardPosition{
            return when(position){
                POSITION.TOP_LEFT -> BoardPosition(0,0)
                POSITION.TOP -> BoardPosition(1,0)
                POSITION.TOP_RIGHT -> BoardPosition(2,0)
                POSITION.LEFT -> BoardPosition(0,1)
                POSITION.CENTER -> BoardPosition(1,1)
                POSITION.RIGHT -> BoardPosition(2,1)
                POSITION.BOTTOM_LEFT -> BoardPosition(0,2)
                POSITION.BOTTOM -> BoardPosition(1,2)
                POSITION.BOTTOM_RIGHT -> BoardPosition(2,2)
            }
        }
    }

    open val generateNewBoardRule: suspend (NumberPool) -> Set<BoardNumberBox> = { pool ->
        //Pools by parity
        val evenPool: List<Int> = getPartialPool(pool, true)
        val oddPool: List<Int> = getPartialPool(pool, false)

        //Random boardDisposition
        val disposition: boardDisposition = listOf(
            boardDisposition.CROSS,
            boardDisposition.LEFT_RIGHT,
            boardDisposition.RIGHT_LEFT).random()
        val evenList: List<BoardPosition> = disposition.getEvenBoardPositionsList()
        val oddList: List<BoardPosition> = disposition.getOddBoardPositionsList()

        //Number distribution with coroutines
        val result = mutableSetOf<Deferred<BoardNumberBox>>()
        withContext(Dispatchers.Default){
            for(i in evenList.indices){
                result.add(async {
                    BoardNumberBox(
                        number = NumberBox.RegularBox(
                            value = evenPool[i]
                        ),
                        position = evenList[i]
                    )
                })
            }
            for(i in oddList.indices){
                result.add(async {
                    BoardNumberBox(
                        number = NumberBox.RegularBox(
                            value = oddPool[i]
                        ),
                        position = oddList[i]
                    )
                })
            }

        }
        val finalResult = result.awaitAll()
        finalResult.toSet()
    }

    open val generateNewQueueRule: suspend (NumberPool) -> List<NumberBox> = { pool ->
        //Pools by parity
        val isEven: Boolean = pool.areMoreEvenNumbers()
        val pool1: List<Int> = getPartialPool(pool,isEven)
        val pool2: List<Int> = getPartialPool(pool,!isEven)

        //Number distribution with coroutines
        val result = mutableListOf<Deferred<NumberBox>>()
        val numbersFromPool2: Int = lineLength / 2
        val numbersFromPool1: Int = lineLength - numbersFromPool2
        withContext(Dispatchers.Default){
            for(i in 0..<numbersFromPool1){
                result.add( async{
                    NumberBox.RegularBox(
                        value = pool1[i]
                    )
                })
            }
            for(i in 0..<numbersFromPool2){
                result.add( async{
                    NumberBox.RegularBox(
                        value = pool2[i]
                    )
                })
            }
        }
        result.awaitAll().shuffled()
    }

    /**
     * IsAnySelectionUseCase(
     *      boardNumbers: List<Int>,
     *      lineLength: Int,
     *      isWinCondition (List<Int>) -> Boolean)
     *
     * val index = 0
     *     val listSize = list.size -1
     *     var operands: MutableList<Int>
     *     var sum: Int
     * 	for(i in 0..listSize - 1){//Primer operando
     *         var firstOperand = list[i]
     *         val secondIndex = i + 1
     *         for(j in secondIndex..listSize){
     *             var secondOperand = list[j]
     *             operands = mutableListOf(firstOperand,secondOperand)
     *             val found = isWinCondition(operands)
     *             println("${operands.joinToString("")} - $found")
     *             if(found){
     *                 return true
     *             }
     *             val thirdIndex = j + 1
     *             for(k in thirdIndex..listSize){
     *                 var thirdOperand = list[k]
     *                 operands = mutableListOf(firstOperand, secondOperand, thirdOperand)
     *                 val found2 = isWinCondition(operands)
     *             	println("${operands.joinToString("")} - $found2")
     *             	if(found2){
     *                 	return true
     *             	}
     *             }
     *         }
     * 	}
     *     return false
     *
     *
     */

    open val getDisplay: suspend (List<Int>) -> String = { lineValues ->
        lineValues.joinToString(separator = " + ")
    }

    object EasyAddGameMode : GameMode() {
        //Default gameMode
    }

    object HardMultiplyGameMode : GameMode() {
        override val regularNumbers = (0..9).toSet()
        override val blockNumbers = setOf(9)

        //lineLength = DEFAULT
        //initialReloads = DEFAULT

        override val getNeededNumbers: suspend (List<Int>) -> List<Int> = { lineValues ->
            /*
                    val index = numbers.indexOf(-1) == 2;
                    when(index){
                        V: (7/17) F: ((3V - 1) / 10)
                        0 [Xyz]
                            x = ((y - Fz) * F) % V
                        1 [xYz]
                            y = (z * F) - (10x % V)
                        2 [xyZ]
                            z = V - ((100x + 10y) % V)


                    }
                 */
            val neededNumbers: MutableList<Int> = mutableListOf()
            val pos = lineValues.indexOf(-1)
            println("Posición buscada: $pos")
            val v = 7 //17
            val f = 2 //5
            val x = lineValues[0]
            val y = lineValues[1]
            val z = lineValues[2]
            val desired = when (pos) {
                0 -> ((y - f * z) * f) % v
                1 -> (z * f) - (x * 10 % v)
                2 -> v - ((x * 100 + y * 10) % v)
                else -> -1
            }
            println("Número deseado: $desired")
            if (desired in lineValues) {
                neededNumbers += desired
            }
            if (desired < 3) {
                neededNumbers += desired + 7
            }
            if (desired > 6) {
                neededNumbers += desired - 7
            }

            neededNumbers
        }


        override val isWinCondition: suspend (List<Int>) -> Boolean = { lineValues ->
            if (lineValues.isEmpty()) {
                false
            } else {
                val result = lineValues.joinToString().toInt()
                goalValues.any { value -> result % value == 0 }
            }
        }

        override val getDisplay: suspend (List<Int>) -> String = { lineValues ->
            lineValues.joinToString(separator = "")
        }


    }


}