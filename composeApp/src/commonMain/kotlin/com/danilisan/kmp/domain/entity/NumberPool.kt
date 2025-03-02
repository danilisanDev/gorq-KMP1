package com.danilisan.kmp.domain.entity

import kotlin.random.Random

data class NumberPool(
    private val commonPool: MutableList<Int>,
) {
    suspend fun getCommonPool(): List<Int> {
        return commonPool
    }

    suspend fun excludeFromPool(number: Int) {
        commonPool.remove(number)
    }

    suspend fun areMoreEvenNumbers(): Boolean =
        (commonPool.count { it % 2 == 0 } * 2)
            .let { evenProportion ->
                if (evenProportion == commonPool.size) {
                    Random.nextBoolean()
                } else {
                    evenProportion > commonPool.size
                }
            }

    suspend fun getPartialPool(
        parityEven: Boolean,
        excludedNumbers: Set<Int> = emptySet(),
    ): List<Int> {
        val parity = if (parityEven) 0 else 1
        val availablePool = commonPool.filter { it !in excludedNumbers }

        return availablePool.filter { it % 2 == parity }
            .takeUnless { it.isEmpty() }
            ?: availablePool

    }
}