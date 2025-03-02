package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.NumberPool
import com.danilisan.kmp.domain.usecase.UseCase
import kotlinx.coroutines.withContext


//TODO Eliminar
class ExcludeFromPoolUseCase(
    override val dispatcher: DispatcherProvider
) : UseCase {
    suspend operator fun invoke(
        pool: NumberPool,
        presentNumbers: IntArray
    ) = withContext(dispatcher.default) {
            presentNumbers.forEach { number ->
                pool.excludeFromPool(number)
            }
    }
}