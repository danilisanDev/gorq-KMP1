package com.danilisan.kmp.domain.action.gamestate

import com.danilisan.kmp.core.provider.DispatcherProvider
import com.danilisan.kmp.domain.entity.DisplayMessage
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.Score
import com.danilisan.kmp.domain.usecase.gamestate.GetDisplayMessageUseCase
import com.danilisan.kmp.ui.state.GameStateUiState
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.displayMsgAddPoints
import kotlinproject.composeapp.generated.resources.displayMsgAddReloads
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EndLineAction(
    override val dispatcher: DispatcherProvider,
    private val getDisplayMessageUseCase: GetDisplayMessageUseCase,
    private val selectBoxAction: SelectBoxAction,
    private val reloadRandomBoardAction: ReloadRandomBoardAction,
) : GameStateAction {
    override suspend operator fun invoke(
        getState: suspend () -> GameStateUiState,
        updateState: suspend (GameStateUiState) -> Unit,
        gameMode: GameMode,
        params: Any?,
    ) = withContext(dispatcher.default) {
        //Load in variables lines in state
        val positionsToReload = getState().linedPositions

        //Empty linedPositions from state
        updateStateFields(getState,updateState,
            linedPositions = emptyList()
        )

        //If only one box aligned && is RegularBox == select action on that box
        if(positionsToReload.size == 1
            && getState().board[positionsToReload.first()] is NumberBox.RegularBox){
            selectBoxAction(getState, updateState, gameMode,
                params = positionsToReload.first())
        }else{
            val completedLines = getState().completedLines
            val readyMessage = getDisplayMessageUseCase(
                boardState = getState().boardState,
                gameMode = gameMode,
            )
            if(completedLines.isEmpty()){//If no lines were completed, update the message
                updateStateFields(getState, updateState,
                    displayMessage = readyMessage
                )
            }else{//If there is any line completed, update board
                launch {//Points and reloads increments on display message
                    gameMode.getPointsForLines(completedLines.size)
                        .let { points ->
                            updateStateFields(
                                getState, updateState,
                                scoreDifference = Score(
                                    points = points.toLong(),
                                    lines = completedLines.size
                                ),
                                displayMessage = DisplayMessage(
                                    res = Res.string.displayMsgAddPoints,
                                    arg = points.toString()
                                ),
                            )
                        }
                }
                delay(500)
                launch{
                    gameMode.getReloadIncrementForLines(completedLines.size)
                        .takeIf { it > 0 }
                        ?.let { reloads ->
                            updateStateFields(getState, updateState,
                                displayMessage = DisplayMessage(
                                    res = Res.string.displayMsgAddReloads,
                                    arg = reloads.toString()
                                ),
                                reloadsDifference = reloads
                            )
                        }
                }
                //Update board
                reloadRandomBoardAction(getState, updateState, gameMode,
                    params = positionsToReload.filterNotNull().toSet().toList()
                )
            }
        }

        //Empty completedLines from state
        updateStateFields(getState,updateState,
            completedLines = emptyList(),
        )
    }
}