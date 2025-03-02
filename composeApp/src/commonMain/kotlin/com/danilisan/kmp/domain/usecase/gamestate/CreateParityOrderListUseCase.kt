package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.usecase.UseCase
import kotlinx.coroutines.withContext

/**
 * Returns a shuffled list of alternating boolean values:
 * true represents a pool of even numbers;
 * false stands for a pool of odd numbers.
 * First boolean (before shuffling) will be
 * of the most frequent parity from current pool
 */
class CreateParityOrderListUseCase(
    override val dispatcher: DispatcherProvider
) : UseCase {
    suspend operator fun invoke(
        moreEvenNumbers: Boolean,
        listSize: Int,
    ): List<Boolean> = withContext(dispatcher.default) {
        val firstParity = if (moreEvenNumbers) 0 else 1
        mutableListOf<Boolean>()
            .apply {
                repeat(listSize) { index ->
                    add(index % 2 == firstParity)
                }
                shuffle()
            }
    }
}