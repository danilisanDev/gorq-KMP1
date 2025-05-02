package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.NumberPool
import com.danilisan.kmp.domain.usecase.UseCase

class GetPoolAndParityUseCase(
    override val dispatcher: DispatcherProvider,
    private val getNumberPoolUseCase: GetNumberPoolUseCase,
    private val createParityOrderListUseCase: CreateParityOrderListUseCase,
) : UseCase {
    suspend operator fun invoke(
        numberSet: Set<Int>,
        repetitions: Int = -1,
        otherNumbers: List<Int> = emptyList(),
        newValuesLength: Int,
    ): Pair<NumberPool, List<Boolean>> {
        //Available number values
        val pool = if(repetitions < 0) {
            NumberPool(numberSet.toMutableList())
        }else {
            getNumberPoolUseCase(
                numberSet = numberSet,
                repetitions = repetitions,
            ).also { currentPool ->
                otherNumbers.forEach { number ->
                    currentPool.excludeFromPool(number)
                }
            }
        }

        //In order to ensure balanced parity on board and queue
        val parityOrderList = if(repetitions < 0) {
            otherNumbers.run {
                count { it % 2 == 0 } > size
            }.let { parity ->
                createParityOrderListUseCase(parity, newValuesLength)
            }
        }else {
            createParityOrderListUseCase(
                pool.areMoreEvenNumbers(),
                newValuesLength,
            )
        }

        return Pair(pool, parityOrderList)
    }
}