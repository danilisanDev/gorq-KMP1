package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.usecase.UseCase
import kotlinx.coroutines.withContext

class AddBoxOnQueueUseCase(
    override val dispatcher: DispatcherProvider
) : UseCase {
    suspend operator fun invoke(
        currentQueue: List<NumberBox>,
        newBox: NumberBox,
        queueSize: Int,
        replaceLast: Boolean = false,
    ): List<NumberBox> = withContext(dispatcher.default) {
        if (queueSize <= 0) {//Impossible case but in order to avoid IndexOutOfBoundsException
            return@withContext emptyList()
        } else {
            val resultQueue = currentQueue.toMutableList()
            resultQueue.run {
                if (replaceLast && isNotEmpty()) {
                    this[lastIndex] = newBox
                } else {
                    if (size >= queueSize) {
                        removeAt(index = 0)
                        retainAll(take(queueSize - 1))
                    }
                    add(newBox)
                }
            }
            return@withContext resultQueue
        }
    }
}