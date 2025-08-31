package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.usecase.UseCase
import kotlinx.coroutines.withContext

/**
 * Use case for adding a new box on the target position on the board
 * replacing the former box.
 */
class AddBoxOnBoardUseCase(
    override val dispatcher: DispatcherProvider
) : UseCase {
    suspend operator fun invoke(
        board: Map<BoardPosition, NumberBox>,
        targetPosition: BoardPosition,
        newBox: NumberBox,
    ): Map<BoardPosition, NumberBox> = withContext(dispatcher.default) {
        val resultBoard = board.toMutableMap()
        resultBoard[targetPosition] = newBox
        return@withContext resultBoard
    }
}