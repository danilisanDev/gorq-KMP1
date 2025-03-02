package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.usecase.UseCase
import kotlinx.coroutines.withContext

class CreateEmptyBoardUseCase(
    override val dispatcher: DispatcherProvider
) : UseCase {
    suspend operator fun invoke(
        lineLength: Int
    ): Map<BoardPosition, NumberBox> = withContext(dispatcher.default) {
        mutableMapOf<BoardPosition, NumberBox>().also { resultMap ->
            for (row in 0 until lineLength) {
                for (column in 0 until lineLength) {
                    BoardPosition(row = row, column = column)
                        .let { keyPosition ->
                            resultMap[keyPosition] = NumberBox.EmptyBox()
                        }
                }
            }
        }
    }
}