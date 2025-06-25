package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.NumberPool
import com.danilisan.kmp.domain.usecase.UseCase
import kotlinx.coroutines.withContext

class UpdateQueueValuesUseCase(
    override val dispatcher: DispatcherProvider,
    private val createParityOrderListUseCase: CreateParityOrderListUseCase,
    private val addBoxOnQueueUseCase: AddBoxOnQueueUseCase,
    private val createRandomBoxForQueueUseCase: CreateRandomBoxForQueueUseCase,
) : UseCase {
    suspend operator fun invoke(
        currentPool: NumberPool,
        parityOrderList: List<Boolean>,
        queue: List<NumberBox>,
        queueSize: Int,
        isWithBlockBox: Boolean,
    ): List<NumberBox> = withContext(dispatcher.default) {
        var resultQueue = queue
        val parityForQueue = parityOrderList
            .takeIf { it.size == queueSize }
            ?: try {
                parityOrderList.reversed().subList(0, queueSize)
            } catch (e: Exception) {
                createParityOrderListUseCase(true, queueSize)
            }
        parityForQueue.indices.forEach { index ->
            //Create new box
            createRandomBoxForQueueUseCase(
                pool = currentPool.getPartialPool(parityEven = parityForQueue[index]),
                isBlockBox = isWithBlockBox,
            ).let { newBox ->
                //Exclude new box value from pool
                currentPool.removeFromPool(newBox.value)
                addBoxOnQueueUseCase(
                    currentQueue = resultQueue,
                    newBox = newBox,
                    queueSize = queueSize,
                ).let {
                    resultQueue = it
                }
            }
        }
        return@withContext resultQueue
    }
}