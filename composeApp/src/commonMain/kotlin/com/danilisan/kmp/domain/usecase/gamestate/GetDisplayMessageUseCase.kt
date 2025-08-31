package com.danilisan.kmp.domain.usecase.gamestate

import androidx.compose.ui.text.font.FontWeight
import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.DisplayMessage
import com.danilisan.kmp.domain.entity.DisplayMessage.Companion.DISPLAY_TEXT_ERROR
import com.danilisan.kmp.domain.entity.DisplayMessage.Companion.DISPLAY_TEXT_GOLDEN
import com.danilisan.kmp.domain.entity.DisplayMessage.Companion.DISPLAY_TEXT_PRIMARY
import com.danilisan.kmp.domain.entity.DisplayMessage.Companion.DISPLAY_TEXT_SECONDARY
import com.danilisan.kmp.domain.entity.DisplayMessage.Companion.DISPLAY_TEXT_SELECTED
import com.danilisan.kmp.domain.entity.DisplayMessage.Companion.DISPLAY_TEXT_SUCCESS
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.usecase.UseCase
import com.danilisan.kmp.ui.state.BoardState
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.displayMsgBingo
import kotlinproject.composeapp.generated.resources.displayMsgGameOver
import kotlinproject.composeapp.generated.resources.displayMsgNoMoves
import kotlinx.coroutines.withContext

/**
 * Use case that returns a DisplayMessage
 * depending on the BoardState.
 */
class GetDisplayMessageUseCase(
    override val dispatcher: DispatcherProvider
) : UseCase {
    suspend operator fun invoke(
        boardState: BoardState,
        gameMode: GameMode,
        selectedNumbers: List<Int> = emptyList(),
        wrongSelection: Boolean = false,
    ):DisplayMessage = withContext(dispatcher.default) {
        when (boardState) {
            BoardState.BLOCKED -> DisplayMessage(
                res = Res.string.displayMsgNoMoves,
                highlightColor = DISPLAY_TEXT_SECONDARY,
                bgColor = DISPLAY_TEXT_ERROR,
            )
            BoardState.BINGO -> DisplayMessage(
                res = Res.string.displayMsgBingo,
                highlightColor = DISPLAY_TEXT_GOLDEN,
                bgColor = DISPLAY_TEXT_SUCCESS,
                weight = FontWeight.ExtraBold,
                sizeDiff = 3,
            )
            BoardState.GAMEOVER -> DisplayMessage(
                res = Res.string.displayMsgGameOver,
                highlightColor = DISPLAY_TEXT_PRIMARY,
                bgColor = DISPLAY_TEXT_SECONDARY,
            )
            BoardState.READY -> if (selectedNumbers.isNotEmpty()) {
                DisplayMessage(
                    arg = selectedNumbers.joinToString(gameMode.selectedNumbersSeparator),
                    highlightColor = if(wrongSelection){
                        DISPLAY_TEXT_ERROR
                    }else{
                        DISPLAY_TEXT_SELECTED
                    },
                    weight = FontWeight.Bold,
                    sizeDiff = 2
                )
            } else {
                DisplayMessage(
                    res = gameMode.readyDisplayMessage,
                    arg = gameMode.getGoalValuesToString()
                )
            }
        }
    }
}
