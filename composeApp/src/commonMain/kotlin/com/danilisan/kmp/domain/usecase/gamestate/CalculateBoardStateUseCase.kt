package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.usecase.UseCase
import com.danilisan.kmp.ui.state.BoardState
import kotlinx.coroutines.withContext

/**
 * Use case for calculating the BoardState.
 */
class CalculateBoardStateUseCase(
    override val dispatcher: DispatcherProvider
) : UseCase {
    suspend operator fun invoke(
        availableLines: Int,
        maxLines: Int,
        isSelectionPossible: Boolean,
        isBingoPossible: Boolean,
        enoughReloadsLeft: Boolean,
    ): BoardState = withContext(dispatcher.default) {
        when{
            availableLines == maxLines && isBingoPossible
                -> BoardState.BINGO
            availableLines == 0 && !isSelectionPossible
                -> if(enoughReloadsLeft) {
                    BoardState.BLOCKED
                }else{
                    BoardState.GAMEOVER
                }
            else -> BoardState.READY
        }
    }
}