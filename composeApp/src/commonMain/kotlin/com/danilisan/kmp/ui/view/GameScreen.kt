package com.danilisan.kmp.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.danilisan.kmp.ui.viewmodel.GameStateViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.component.KoinComponent

class GameScreen: Screen, KoinComponent {
    @OptIn(KoinExperimentalAPI::class)
    @Composable
    override fun Content() {
        val viewModel = koinViewModel<GameStateViewModel>()
        val gameState by viewModel.gameState.collectAsState()

        LaunchedEffect(Unit){
            viewModel.initData()
        }

        LoadingText(gameState.isLoading)

        Column(verticalArrangement = Arrangement.Center){
            Queue(
                gameState.queue
            )
            Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))
            Board(
                gameState.board,
                gameState.gameMode.lineLength)
            ReloadButton { viewModel.reloadBoard() }
            ReloadButton2 { viewModel.reloadQueue() }
        }
    }
}