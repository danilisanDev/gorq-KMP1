package com.danilisan.kmp.domain.entity

import io.github.aakira.napier.Napier

data class NumberPool(
    private val commonPool: MutableList<Int>,
) {
    suspend fun getCommonPool(): List<Int> {
        return commonPool
    }

    suspend fun excludeFromPool(number: Int) {
            commonPool.remove(number)
    }

    suspend fun areMoreEvenNumbers(): Boolean{
        Napier.d("Pool: $commonPool")
        return commonPool.filter{it % 2 == 0}.size > (commonPool.size / 2)
    }

    suspend fun getPartialPool(
        parityEven: Boolean? = null,
        higherValues: Boolean? = null,
        noRepeats: Boolean = false
    ): MutableList<Int>{
        val partialFilter: (Int) -> Boolean = {number ->
            var filter = true
            if(parityEven != null){
                if(parityEven){
                    filter = number % 2 == 0
                }else{
                    filter = number % 2 != 0
                }
            }
            if(higherValues != null){
                val middleValue: Int = (commonPool.max() + 1) / 2
                if(higherValues){
                    filter = filter && number >= middleValue
                }else{
                    filter = filter && number < middleValue
                }
            }
            filter
        }

        return if(noRepeats){
            commonPool.distinct().filter{
                partialFilter(it)
            }.toMutableList()
        }else{
            commonPool.filter{
                partialFilter(it)
            }.toMutableList()
        }
    }

    companion object{
        suspend fun MutableList<Int>.getRandomNumberFromPool(): Int{
            val randomNumber = this.random()
            this.remove(randomNumber)
            return randomNumber
        }
    }
}