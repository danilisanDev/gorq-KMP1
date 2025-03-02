package com.danilisan.kmp.domain.usecase.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.DisplayMessage
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.usecase.UseCase
import com.danilisan.kmp.ui.state.BoardState
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.displayMsgBingo
import kotlinproject.composeapp.generated.resources.displayMsgGameOver
import kotlinproject.composeapp.generated.resources.displayMsgNoMoves
import kotlinx.coroutines.withContext

class GetDisplayMessageUseCase(
    override val dispatcher: DispatcherProvider
) : UseCase {
    suspend operator fun invoke(
        boardState: BoardState,
        gameMode: GameMode,
        selectedNumbers: List<Int> = emptyList(),
    ):DisplayMessage = withContext(dispatcher.default) {
        when (boardState) {
            BoardState.BLOCKED -> DisplayMessage(res = Res.string.displayMsgNoMoves)
            BoardState.BINGO -> DisplayMessage(res = Res.string.displayMsgBingo)
            BoardState.GAMEOVER -> DisplayMessage(res = Res.string.displayMsgGameOver)
            BoardState.READY -> if (selectedNumbers.isNotEmpty()) {
                DisplayMessage(arg = selectedNumbers.joinToString(gameMode.selectedNumbersSeparator))
            } else {
                DisplayMessage(
                    res = gameMode.readyDisplayMessage,
                    arg = gameMode.getGoalValuesToString()
                )
            }
        }
    }
}
