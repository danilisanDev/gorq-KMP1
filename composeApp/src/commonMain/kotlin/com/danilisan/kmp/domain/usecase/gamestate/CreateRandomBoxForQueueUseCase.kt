package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.usecase.UseCase
import kotlinx.coroutines.withContext

/**
 * Use case that returns a random NumberBox for the Queue
 */
class CreateRandomBoxForQueueUseCase(
    override val dispatcher: DispatcherProvider
) : UseCase {
    suspend operator fun invoke(
        pool: List<Int>,
        isBlockBox: Boolean,
    ): NumberBox = withContext(dispatcher.default) {
        (pool.takeUnless{ it.isEmpty() }?.random() ?: 0).let { value ->
            if (isBlockBox) NumberBox.BlockBox(value)
            else NumberBox.RegularBox(value)
        }
    }
}


