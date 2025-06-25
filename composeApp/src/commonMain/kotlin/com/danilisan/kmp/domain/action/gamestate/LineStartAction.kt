package com.danilisan.kmp.domain.action.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.ui.state.GameStateUiState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext

/**
 * **LINE-START** -> LINE_DRAG -> LINE_END
 * Action for the drag gesture performed on the Board (before LineDragAction).
 * The starting BoardPosition is saved into GameState (linedPositions).
 * @param param (expected BoardPosition) BoardPosition where the pointer starts
 *      If the BoardPosition is null (out of bounds of the board), do nothing.
 */
class LineStartAction(
    override val dispatcher: DispatcherProvider,
) : GameStateAction {
    override suspend operator fun invoke(
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
        gameMode: GameMode,
        params: Any?,
    ): Boolean = withContext(dispatcher.default) {
        //Check expected params type (BoardPosition)
        val startingPosition = if(params is BoardPosition){
            params
        }else{
            return@withContext false
        }
        Napier.d(message = "Starting position: $startingPosition")

        //Update state adding starting position to linedPositions
        updateStateFields(getState,updateState,
            linedPositions = listOf(startingPosition),
        )

        return@withContext true
    }
}