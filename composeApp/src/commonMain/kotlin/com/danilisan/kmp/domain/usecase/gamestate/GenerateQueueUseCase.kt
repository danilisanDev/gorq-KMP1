package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.usecase.GenerationParams

/**
 * Generates (gameMode.lineLength) numbers for the Queue
 * with (gameMode.lineLength) new REGULAR boxes
 * from a pool of numbers
 */
class GenerateQueueUseCase: GenerateRandomUseCase<NumberBox> {
    override suspend fun invoke(
        params: GenerationParams<NumberBox>
    ): List<NumberBox> {
        return params.executeRule().toList()
    }
}