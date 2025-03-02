package com.danilisan.kmp.domain.action.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.usecase.gamestate.AddBoxOnQueueUseCase
import com.danilisan.kmp.domain.usecase.gamestate.CreateParityOrderListUseCase
import com.danilisan.kmp.domain.usecase.gamestate.CreateRandomBoxForQueueUseCase
import com.danilisan.kmp.domain.usecase.gamestate.GetNumberPoolUseCase
import com.danilisan.kmp.ui.state.GameStateUiState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext

class ReloadQueueAction(
    override val dispatcher: DispatcherProvider,
    val getNumberPoolUseCase: GetNumberPoolUseCase,
    val createParityOrderListUseCase: CreateParityOrderListUseCase,
    val getRandomBoxForQueueUseCase: CreateRandomBoxForQueueUseCase,
    val addBoxOnQueueUseCase: AddBoxOnQueueUseCase,
): GameStateAction {
    override suspend operator fun invoke(
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
        gameMode: GameMode,
        params: Any?,
    ) = withContext(dispatcher.default){
        //Does not expect any params
        if(params != null) return@withContext

        //Get complete pool and exclude current board numbers
        val currentPool = getNumberPoolUseCase(
            numberSet = gameMode.regularNumbers,
            repetitions = gameMode.numberRepetitions,
        ).also{ currentPool ->
            getState().board.values.forEach { box ->
                currentPool.excludeFromPool(box.value)
            }
        }

        val queueSize = gameMode.queueSize

        //In order to ensure balanced parity on board and queue
        val parityOrderList = createParityOrderListUseCase(
            moreEvenNumbers = currentPool.areMoreEvenNumbers(),
            listSize = queueSize,
        )

        //Generate new queue
        var newQueue = emptyList<NumberBox>()
        (0 until queueSize).forEach { index ->
            //Create new box
            getRandomBoxForQueueUseCase(
                pool = currentPool.getPartialPool(parityEven = parityOrderList[index]),
                isBlockBox = false,
            ).let { newBox ->
                //Add new box to Queue
                newQueue = addBoxOnQueueUseCase(
                    newQueue, newBox, queueSize,
                )
                //Exclude new box value from pool
                currentPool.excludeFromPool(newBox.value)
            }
        }
        
        //Size check
        if(newQueue.size != queueSize) {
            Napier.d(message = """Generated queue size [${newQueue.size}] 
                doesn't match the required size of $queueSize""")
            newQueue = emptyList()
        }
        
        //Update queue
        updateStateFields(getState, updateState,
            queue = newQueue,
        )
    }
}