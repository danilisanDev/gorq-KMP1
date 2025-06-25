package com.danilisan.kmp.domain.entity

import kotlin.random.Random

/**
 * Represents a list of numbers available
 * to be included as new values
 * for NumberBoxes on Board or Queue
 */

data class NumberPool(
    private val commonPool: MutableList<Int>,
) {
    fun getCommonPool(): List<Int> {
        return commonPool
    }

    fun removeFromPool(number: Int) {
        commonPool.remove(number)
    }

    /**
     * Returns true if there are more even numbers than odd numbers in the pool;
     * returns false if there are more odd numbers;
     * returns random boolean in case of equal proportion.
     */
    fun areMoreEvenNumbers(): Boolean =
        (commonPool.count { it % 2 == 0 } * 2)
            .let { evenProportion ->
                if (evenProportion == commonPool.size) {
                    Random.nextBoolean()
                } else {
                    evenProportion > commonPool.size
                }
            }

    /**
     * Returns the pool, but filtered by parity, and excluded numbers.
     * @param parityEven true -> get even numbers; false -> get odd numbers.
     * @param excludedNumbers numbers to be excluded from the pool.
     */
    fun getPartialPool(
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