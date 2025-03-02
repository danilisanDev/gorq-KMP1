package com.danilisan.kmp.domain.action.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.ui.state.GameStateUiState
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
        if(params !is BoardPosition) return@withContext

        //Add starting position to linedPositions
        val linedPositions = listOf(params)
        println("Starting position: $params")

        //Update state
        updateStateFields(getState,updateState,
            linedPositions = linedPositions
        )
    }
}