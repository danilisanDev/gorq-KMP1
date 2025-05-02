package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.BoardHelper.filterByLine
import com.danilisan.kmp.domain.entity.BoardHelper.getBoardPositionFromLineIdAndIndex
import com.danilisan.kmp.domain.entity.BoardHelper.getLineLength
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.NumberBox.Companion.EMPTY_VALUE
import com.danilisan.kmp.domain.usecase.UseCase
import kotlinx.coroutines.withContext

class UpdateSilverStarValuesUseCase(
    override val dispatcher: DispatcherProvider
) : UseCase {
    suspend operator fun invoke(
        board: Map<BoardPosition, NumberBox>,
        completedLines: List<Int>,
        getWinConditionNumbers: suspend (List<Int>) -> Set<Int>,
    ): Map<BoardPosition, NumberBox> = withContext(dispatcher.default) {
        completedLines.reversed().forEach { lineId ->
            board.filterByLine(lineId).takeUnless { boxes ->
                boxes.count { it is NumberBox.SilverStarBox && it.value == EMPTY_VALUE } != 1
                        || boxes.any { it is NumberBox.GoldenStarBox }
            }?.let { boxes ->
                val silverPosition = getBoardPositionFromLineIdAndIndex(
                    lineId = lineId,
                    index = boxes.indexOfFirst { it is NumberBox.SilverStarBox && it.value == EMPTY_VALUE },
                    lineLength = board.getLineLength(),
                )
                getWinConditionNumbers(boxes.map { it.value }).takeIf { it.isNotEmpty() }
                    ?.let { winningNumbers ->
                        board[silverPosition]?.setValue(winningNumbers.max())
                    }
            }
        }
        board
    }
}

