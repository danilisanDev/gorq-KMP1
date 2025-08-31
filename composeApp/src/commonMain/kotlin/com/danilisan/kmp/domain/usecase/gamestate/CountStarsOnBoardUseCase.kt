package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.usecase.UseCase
import kotlinx.coroutines.withContext

/**
 * Use case for counting the number of StarBoxes on the board.
 */
class CountStarsOnBoardUseCase (
    override val dispatcher: DispatcherProvider
) : UseCase {
    suspend operator fun invoke(
        board: Map<BoardPosition, NumberBox>,
    ): Int = withContext(dispatcher.default){
            board.values.count{ it is NumberBox.StarBox}
        }
}