package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.usecase.UseCase
import kotlinx.coroutines.withContext

class IsSelectionPossibleUseCase(
    override val dispatcher: DispatcherProvider
) : UseCase {
    suspend operator fun invoke(
        board: Map<BoardPosition, NumberBox>,
        minSelection: Int,
        maxSelection: Int,
        isWinCondition: suspend (List<Int>) -> Boolean,
    ): Boolean = withContext(dispatcher.default) {
        board.values
            .filterIsInstance<NumberBox.RegularBox>()
            .map { it.value }
            .takeUnless { it.size < minSelection }
            ?.let { boardValues ->
                (minSelection..maxSelection).forEach { size ->
                    boardValues.getCombinations(size).forEach { combination ->
                        if (isWinCondition(combination)) {
                            return@withContext true
                        }
                    }
                }
                false
            } ?: false
    }

    /**
     * Returns a list with all possible combinations of numbers
     * with the given size, and with every order permutation
     */
    private fun List<Int>.getCombinations(size: Int): List<List<Int>> {
        if (size == 0) return listOf(emptyList())
        if (this.isEmpty()) return emptyList()
        return this.flatMapIndexed { index, element ->
            val sublist = this.filterIndexed { i, _ -> i != index }
            sublist.getCombinations(size - 1).map { listOf(element) + it }
        }
    }
}