package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.BoardHelper.getLineIdsWithCondition
import com.danilisan.kmp.domain.entity.BoardHelper.checkWinningLine
import com.danilisan.kmp.domain.entity.BoardHelper.getLineLength
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.usecase.UseCase
import kotlinx.coroutines.withContext

class GetWinningLinesUseCase(
    override val dispatcher: DispatcherProvider
) : UseCase {
    suspend operator fun invoke(
        board: Map<BoardPosition, NumberBox>,
        isWinCondition: suspend (List<Int>) -> Boolean,
    ): Set<Int> = withContext(dispatcher.default) {
        getLineIdsWithCondition(
            lineLength = board.getLineLength(),
            condition = { lineId ->
                board.checkWinningLine(lineId, isWinCondition)
            }
        )
    }
}

