package com.danilisan.kmp.domain.usecase

import com.danilisan.kmp.domain.entity.NumberPool

class GenerationParams<E>(
    private val pool: NumberPool,
    private val rule: suspend (NumberPool) -> Collection<E>,
){
    suspend fun executeRule(): Collection<E> = rule(pool)
}


