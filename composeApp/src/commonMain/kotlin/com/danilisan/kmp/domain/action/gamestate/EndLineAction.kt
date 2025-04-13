package com.danilisan.kmp.domain.action.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.usecase.gamestate.GetDisplayMessageUseCase
import com.danilisan.kmp.ui.state.GameStateUiState
import kotlinx.coroutines.withContext

class EndLineAction(
    override val dispatcher: DispatcherProvider,
    private val getDisplayMessageUseCase: GetDisplayMessageUseCase,
    private val selectBoxAction: SelectBoxAction,
    private val updateGameAction: UpdateGameAction,
) : GameStateAction {
    override suspend operator fun invoke(
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
        gameMode: GameMode,
        params: Any?,
    ) = withContext(dispatcher.default) {
        //Does not expect any params
        if (params != null) return@withContext

        //Check out for completed lines
        if (getState().completedLines.isEmpty()) {
            //Update after no line
            getState().linedPositions
                .takeUnless { it.isEmpty() }
                ?.let { positions ->
                    val firstPos = positions.first()
                    val singleLinedBox = getState().board[firstPos]
                    //If only one lined positions -> select action
                    if (positions.size == 1 && singleLinedBox is NumberBox.RegularBox) {
                        selectBoxAction(
                            getState, updateState, gameMode,
                            params = firstPos
                        )
                    }
                    updateStateFields(
                        getState, updateState,
                        linedPositions = emptyList()
                    )
                }
        } else { //Update after line
            updateGameAction(
                getState, updateState, gameMode,
                params = UpdateGameAction.UpdateOptions.AFTER_LINE
            )
        }
    }
}