package com.danilisan.kmp.domain.action.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.ui.state.GameStateUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class LineEndAction(
    override val dispatcher: DispatcherProvider,
    private val selectBoxAction: SelectBoxAction,
    private val updateGameAction: UpdateGameAction,
) : GameStateAction {
    override suspend operator fun invoke(
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
        gameMode: GameMode,
        params: Any?,
    ): Boolean = withContext(dispatcher.default) {
        //Does not expect any params
        if (params != null) {
            return@withContext false //Impossible case
        }

        //Little delay to let lineStartAction complete
        delay(70)

        //If drag action didn't start, return false
        val linedPositions = getState().linedPositions
        println("End line: $linedPositions")
        if(linedPositions.isEmpty()) return@withContext false

        //Check out for completed lines
        if (getState().completedLines.isEmpty()) {
            //Update after no line
            updateStateFields(
                getState, updateState,
                linedPositions = emptyList()
            )
            linedPositions
                .let { positions ->
                    val firstPos = positions.first()
                    val singleLinedBox = getState().board[firstPos]
                    //If only one lined positions -> select action
                    return@withContext if (positions.size == 1 && singleLinedBox is NumberBox.RegularBox) {
                        selectBoxAction(
                            getState, updateState, gameMode,
                            params = firstPos
                        )
                    }else{
                        true
                    }
                }
        } else { //Update after line
            updateGameAction(
                getState, updateState, gameMode,
                params = UpdateGameAction.UpdateOptions.AFTER_LINE
            )
        }
    }
}