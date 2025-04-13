package com.danilisan.kmp.domain.action.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.ui.state.GameStateUiState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext

class StartLineAction(
    override val dispatcher: DispatcherProvider,
) : GameStateAction {
    override suspend operator fun invoke(
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
        gameMode: GameMode,
        params: Any?,
    ) = withContext(dispatcher.default) {
        //Check expected params type (BoardPosition)
        val startingPosition = if(params is BoardPosition){
            params
        }else{
            return@withContext
        }

        //Add starting position to linedPositions
        val linedPositions = listOf(startingPosition)
        Napier.d(message = "Starting position: $startingPosition")

        //Update state
        updateStateFields(getState,updateState,
            linedPositions = linedPositions
        )
    }
}