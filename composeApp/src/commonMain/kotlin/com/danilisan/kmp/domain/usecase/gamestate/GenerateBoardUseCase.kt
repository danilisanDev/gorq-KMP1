package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.domain.entity.BoardNumberBox
import com.danilisan.kmp.domain.usecase.GenerationParams

/**
 * Generates a new board 3x3
 * with 9 new REGULAR numberboxes
 * from a pool of numbers
 */
class GenerateBoardUseCase: GenerateRandomUseCase<BoardNumberBox> {
    override suspend operator fun invoke(
        params: GenerationParams<BoardNumberBox>
    ): Set<BoardNumberBox> {
        return params.executeRule().toSet()
    }
}




//            val pools: NumberPool = generateNumberPoolUseCase(gameMode.regularNumbers)
//            val boardList = mutableListOf<BoardNumberBox>()
//
//            val time = measureTime {
//                //PREPARACIÓN
//                val randomizer = Random.nextBoolean()
//                var firstPool = getRandomPool(
//                    pools = pools,
//                    randomizer = randomizer)
//                var secondPool = getRandomPool(
//                    pools = pools,
//                    randomizer = !randomizer)
//
//                //CREAR BoardNumberBoxModel
//
//                //GENERAR UN NÚMERO ALEATORIO
//                /*
//                    0. Obtener pool actual = commonPool - board - queue
//                        - DESDE ORIGEN
//                    1. Determinar posicion x/y
//                        - DESDE ORIGEN
//                    2. Ver qué líneas completa (boardNumbers, gameMode) -> async
//                        - USE CASE (GetUnavailableNumbers(List<BoardNumberBox>, xPos: Int, yPos: Int, getNeededNumbers() ))
//                        2.1 Obtener números no disponibles
//                        2.2 Filtrar la pool
//                    3. Generar número (randomNumberFromPoolUseCase)
//                        - EN ORIGEN
//                        3.1 Crear BoardNumberBox y añadirlo en boardList (async)
//                        3.2 Borrar el número de la commonPool (async)
//                 */
//
//
//                //Primer turno de generacion (4 corrutinas y 0 límites)
//                val number1 = async {
//                    randomNumberFromPoolUseCase(firstPool)
//                }
//                val number2 = async {
//                    randomNumberFromPoolUseCase(secondPool)
//                }
//                val number3 = async {
//                    randomNumberFromPoolUseCase(firstPool)
//                }
//                val number4 = async {
//                    randomNumberFromPoolUseCase(secondPool)
//                }
//
//                boardList += BoardNumberBox(
//                    number = NumberBox(
//                        value = number1.await(),
//                        xPos = 1,
//                        yPos = 1,
//                    )
//                )
//                pools.commonPool.remove(number1.await())
//
//                boardList += BoardNumberBox(
//                    number = NumberBox(
//                        value = number2.await(),
//                        xPos = 1,
//                        yPos = 3,
//                    )
//                )
//                pools.commonPool.remove(number2.await())
//
//                boardList += BoardNumberBox(
//                    number = NumberBox(
//                        value = number3.await(),
//                        xPos = 2,
//                        yPos = 2,
//                    )
//                )
//                pools.commonPool.remove(number3.await())
//
//                boardList += BoardNumberBox(
//                    number = NumberBox(
//                        value = number4.await(),
//                        xPos = 3,
//                        yPos = 2,
//                    )
//                )
//                pools.commonPool.remove(number4.await())
//
//
//                //Segundo turno de generacion (2 corrutinas y 1 límite)
//                firstPool = getRandomPool(pools, randomizer)
//                val number5 = async { randomNumberFromPoolUseCase(firstPool) }
//                val number6 = async {
//                    val sameLine = boardList //Right-left diagonal
//                        .map{it.number}
//                        .filter(filterSameLine(InGameConstants.line.DIAGONAL, RIGHT_DIAGONAL))
//                        .map{it.value}
//                    val unavailableNumbers = gameMode?.getNeededNumbers(sameLine) ?: listOf()
//                    val newPool = firstPool.filter{!unavailableNumbers.contains(it)}
//                    randomNumberFromPoolUseCase(newPool) }
//
//                boardList += BoardNumberBox(
//                    number = NumberBox(
//                        value = number5.await(),
//                        xPos = 2,
//                        yPos = 3,
//                    )
//                )
//                pools.commonPool.remove(number5.await())
//
//                boardList += BoardNumberBox(
//                    number = NumberBox(
//                        value = number6.await(),
//                        xPos = 3,
//                        yPos = 1,
//                    )
//                )
//                pools.commonPool.remove(number6.await())
//
//                //Tercer turno de generacion (3 corrutinas y 3 límites)
//                secondPool = getRandomPool(pools, !randomizer)
//                val number7 = async {
//                    val sameLine = boardList //Right-left diagonal
//                        .map{it.number}
//                        .filter(filterSameLine(InGameConstants.line.DIAGONAL, RIGHT_DIAGONAL))
//                        .map{it.value}
//                    val unavailableNumbers = gameMode?.getNeededNumbers(sameLine) ?: listOf()
//                    val newPool = firstPool.filter{!unavailableNumbers.contains(it)}
//                    randomNumberFromPoolUseCase(newPool) }
//
//                boardList += BoardNumberBox(
//                    number = NumberBox(
//                        value = number7.await(),
//                        xPos = 2,
//                        yPos = 1,
//                    )
//                )
//                pools.commonPool.remove(number7.await())
//
//
//
//
//            }
//            println("Board generated in: $time ms.")
//
//        }
//
//
//        return boardList.toList()
//    }
//
//    private suspend fun getRandomPool(pools: NumberPool, randomizer: Boolean): List<Int>{
//        return if(randomizer){
//            pools.getEvenPool()
//        }else{
//            pools.getOddPool()
//        }
//    }
//
//    private suspend fun filterSameLine(
//        line: InGameConstants.line,
//        pos: Int,
//    ): (NumberBox) -> Boolean {
//        return { numberBox ->
//            when (line) {
//                InGameConstants.line.HORIZONTAL -> numberBox.xPos == pos
//                InGameConstants.line.VERTICAL -> numberBox.yPos == pos
//                else -> if (pos == LEFT_DIAGONAL) {
//                    numberBox.xPos == numberBox.yPos
//                } else {
//                    numberBox.xPos + numberBox.yPos == 4
//                }
//            }
//        }
//    }
//
//    private suspend fun newBoardNoRules(): List<BoardNumberBox>{
//        val boardPool = generateNumberPoolUseCase()
//        val boardList = mutableListOf<BoardNumberBox>()
//        for (x in 1..3) {
//            for (y in 1..3) {
//                val randomPool = boardPool.toMutableList()
//                //Check for unavailable numbers -> randomPool.removeAll()
//                val randomNumber = randomNumberFromPoolUseCase(randomPool)
//                boardList += BoardNumberBox(
//                    number = NumberBox(
//                        value = randomNumber,
//                        xPos = x,
//                        yPos = y)
//                )
//                boardPool.remove(randomNumber)
//            }
//        }
//        return boardList.toList()
//    }
