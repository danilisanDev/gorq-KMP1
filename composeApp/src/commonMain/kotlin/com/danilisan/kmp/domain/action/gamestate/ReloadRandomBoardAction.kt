package com.danilisan.kmp.domain.action.gamestate

import com.danilisan.kmp.Mocks
import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.BoardHelper.getEmptyPositionsSortedDiagonally
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.usecase.gamestate.AddBoxOnBoardUseCase
import com.danilisan.kmp.domain.usecase.gamestate.CreateEmptyBoardUseCase
import com.danilisan.kmp.domain.usecase.gamestate.CreateParityOrderListUseCase
import com.danilisan.kmp.domain.usecase.gamestate.CreateRandomBoxForBoardUseCase
import com.danilisan.kmp.domain.usecase.gamestate.GetNumberPoolUseCase
import com.danilisan.kmp.ui.state.GameStateUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class ReloadRandomBoardAction(
    override val dispatcher: DispatcherProvider,
    val getNumberPoolUseCase: GetNumberPoolUseCase,
    val createParityOrderListUseCase: CreateParityOrderListUseCase,
    val createEmptyBoardUseCase: CreateEmptyBoardUseCase,
    val createRandomBoxForBoardUseCase: CreateRandomBoxForBoardUseCase,
    val addBoxOnBoardUseCase: AddBoxOnBoardUseCase,
    val checkBoardStateAction: CheckBoardStateAction,
) : GameStateAction {
    override suspend operator fun invoke(
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
        gameMode: GameMode,
        params: Any?,
    ) = withContext(dispatcher.default) {
        //Empty availableLines
        updateStateFields(
            getState, updateState,
            availableLines = emptySet()
        )

        //var updateWithMock = false

        //Get target positions to be reloaded
        /*  If params is null or the board is incomplete (impossible case),
            an empty board is created and all positions are reloaded    */
        val targetPositions = if (getState().board.size != gameMode.getBoardSize() ||
            params == null || params !is List<*> || params.none { it is BoardPosition }
        ) {
            //updateWithMock = true
            createEmptyBoardUseCase(gameMode.lineLength).getEmptyPositionsSortedDiagonally()
        } else {
            @Suppress("UNCHECKED_CAST")
            params as List<BoardPosition>
        }

        //Get complete pool and exclude current queueNumbers and remaining boardNumbers
        val currentPool = getNumberPoolUseCase(
            numberSet = gameMode.regularNumbers,
            repetitions = gameMode.numberRepetitions,
        ).also { currentPool ->
            (getState().queue +
                    getState().board.mapNotNull { (position, box) ->
                        if (position !in targetPositions) box else null
                    })
                .forEach { box ->
                    currentPool.excludeFromPool(box.value)
                }
        }

        //In order to ensure balanced parity on board and queue
        val parityOrderList = createParityOrderListUseCase(
            currentPool.areMoreEvenNumbers(),
            targetPositions.size,
        )

        //Iterate for all empty positions and indexed parity
        for ((i, targetPosition) in targetPositions.withIndex()) {
            //Replace target position for empty box (if there isn't already one)
            getState().board.let { currentBoard ->
                if (currentBoard[targetPosition] !is NumberBox.EmptyBox) {
                    addBoxOnBoardUseCase(
                        newBox = NumberBox.EmptyBox(),
                        targetPosition = targetPosition,
                        board = currentBoard,
                    ).let { boardWithNewEmptyBox ->
                        updateStateFields(
                            getState, updateState,
                            board = boardWithNewEmptyBox,
                        )
                    }
                }
                delay(119)
            }
            //Replace empty box for a random regular box
            getState().board.let { currentBoard ->
                createRandomBoxForBoardUseCase(
                    currentBoard = currentBoard,
                    targetPosition = targetPosition,
                    pool = currentPool,
                    parity = parityOrderList[i],
                    getNeededNumbers = gameMode::getWinConditionNumbers,
                )
                    ?.let { newRegularBox ->
                        currentPool.excludeFromPool(newRegularBox.value)
                        addBoxOnBoardUseCase(
                            newBox = newRegularBox,
                            targetPosition = targetPosition,
                            board = currentBoard,
                        ).let { boardWithNewRegularBox ->
                            updateStateFields(
                                getState, updateState,
                                board = boardWithNewRegularBox
                            )
                        }
                    }
            }
        }


//        //Test mocks
//        if(updateWithMock){
//            updateStateFields(getState, updateState,
//                board = Mocks.threeSilverStarsBoard
//            )
//        }


        //Invoke check board state Action
        checkBoardStateAction(getState, updateState, gameMode)
    }
}