package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.usecase.UseCase
import kotlinx.coroutines.withContext

/**
 * Use case for adding a new box
 * on the last position of the queue,
 * replacing the former box (add StarBox),
 * OR removing the first box out
 * and pushing the following to lower indexes.
 */
class AddBoxOnQueueUseCase(
    override val dispatcher: DispatcherProvider
) : UseCase {
    suspend operator fun invoke(
        currentQueue: List<NumberBox>,
        newBox: NumberBox,
        queueSize: Int,
        replaceLast: Boolean = false,
    ): List<NumberBox> = withContext(dispatcher.default) {
        return@withContext if (queueSize <= 0) {//Impossible case but in order to avoid IndexOutOfBoundsException
            emptyList()
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
            resultQueue
        }
    }
}