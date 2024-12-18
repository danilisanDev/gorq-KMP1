package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.domain.entity.NumberPool
import com.danilisan.kmp.domain.usecase.UseCase

class GenerateNumberPoolUseCase : UseCase< () -> NumberPool, NumberPool> {
    override suspend fun invoke(
        params: () -> NumberPool
    ): NumberPool = params()
}


