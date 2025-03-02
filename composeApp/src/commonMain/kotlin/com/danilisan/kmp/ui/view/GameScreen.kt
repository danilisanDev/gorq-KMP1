package com.danilisan.kmp.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
import com.danilisan.kmp.ui.state.BoardState
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.ui.theme.withAlpha
import com.danilisan.kmp.ui.view.gamestate.UIAdBanner
import com.danilisan.kmp.ui.view.gamestate.UIBoard
import com.danilisan.kmp.ui.view.gamestate.UIMessageDisplay
import com.danilisan.kmp.ui.view.gamestate.UIQueue
import com.danilisan.kmp.ui.view.gamestate.UIReloadButton
import com.danilisan.kmp.ui.view.gamestate.UIReloadsLeft
import com.danilisan.kmp.ui.view.gamestate.UIScoreDisplay
import com.danilisan.kmp.ui.viewmodel.GameStateViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.component.KoinComponent

class GameScreen: Screen, KoinComponent {
    @Composable
    override fun Content() {
        initApp()
    }

    @Composable
    fun initApp(){
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Theme.colors.secondary.withAlpha(0.8f)),
            contentAlignment = Alignment.TopCenter
        ){
            val isPortraitMode = (maxHeight / maxWidth) >= 16f / 9f
            if(isPortraitMode){
                PortraitGameScreen(isPortraitMode)
            }else{
                PortraitGameScreen(isPortraitMode)
            }
        }
    }
}

    @OptIn(KoinExperimentalAPI::class)
    @Composable
    private fun PortraitGameScreen(
        portraitMode: Boolean
    ){
        val viewModel = koinViewModel<GameStateViewModel>()
        val gameState by viewModel.gameState.collectAsState()
        val gameMode by viewModel.gameModeState.collectAsState()

        val turnsLeft = gameState.reloadsLeft
        val queue = gameState.queue
        val board = gameState.board
        val lines = gameState.availableLines
        val score = gameState.score
        val state = gameState.boardState
        val message = gameState.displayMessage
        val reloadCost = when(state){
            BoardState.READY -> gameMode.reloadQueueCost
            BoardState.BLOCKED -> gameMode.reloadBoardCost
            else -> 0
        }

        val isLoading = gameState.isLoading

        val orientationText = if(portraitMode){
            "Modo retrato"
        }else{
            "Modo paisaje"
        }
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            Column(//2/3 Upper part
                modifier = Modifier
                    .fillMaxWidth(0.8f),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                UISpacer()
                Row(//Queue & button
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .aspectRatio(2f)
                ) {
                    Box(
                        //Queue
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(2f),
                    ) {
                        UIQueue(queue)
                    }
                    Spacer(modifier = Modifier.weight(0.5f))
                    Column(//Reload
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(2f)
                    ) {
                        Box(//Reloads Left
                            modifier = Modifier
                                .weight(2f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            UIReloadsLeft(turnsLeft = turnsLeft)
                        }
                        Box(//Reload button
                            modifier = Modifier
                                .weight(3f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            UIReloadButton(
                                boardState = state,
                                reloadCost = reloadCost,
                                isEnabled = (turnsLeft >= reloadCost && !isLoading),
                                buttonAction = viewModel::reloadButtonHandler
                            )
                        }
                    }
                }
                UISpacer()
                Box(//Board & Line Indicators
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    UIBoard(
                        board = board,
                        selectedPositions = gameState.selectedPositions,
                        linedPositions = gameState.linedPositions.filterNotNull(),
                        completedLines = gameState.completedLines,
                        availableLines = lines,
                        boardState = state,
                        isEnabled = !isLoading || gameState.incompleteSelection,
                        selectAction = viewModel::selectBoxHandler,
                        dragAction = viewModel::dragLineHandler,
                    )
                }
                UISpacer(10)
                Box(//AuxDisplay
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(10f),
                    contentAlignment = Alignment.Center
                ) {
                    UIMessageDisplay(message,state)
                }
                UISpacer(10)
            }
            Column(//1/3 Lower part
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Top
            ){
                UIScoreDisplay(score)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    UIAdBanner(orientationText)
                }
            }
        }
    }