package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.NumberPool
import com.danilisan.kmp.domain.usecase.UseCase
import kotlinx.coroutines.withContext

/**
 * Use case that returns a NumberPool
 * with each number of the [numberSet] repeated [repetitions] times
 */
class GetNumberPoolUseCase(
    override val dispatcher: DispatcherProvider
) : UseCase {
    suspend operator fun invoke(
        numberSet: Set<Int>,
        repetitions: Int,
    ): NumberPool = withContext(dispatcher.default) {
        NumberPool(
            mutableListOf<Int>().apply {
                repeat(repetitions) {
                    addAll(numberSet)
                }
                shuffle()
            }
        )
    }

}


