package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.usecase.UseCase
import kotlinx.coroutines.withContext

//TODO Eliminar
class CheckSelectionResultUseCase(
    override val dispatcher: DispatcherProvider
) : UseCase {
    suspend operator fun invoke(
        minSelection: Int,
        maxSelection: Int,
        selectedNumbers: List<Int>,
        getWinConditionResult: suspend (List<Int>) -> Int
    ): Int = withContext(dispatcher.default) {
        getWinConditionResult(selectedNumbers)
    }
}
