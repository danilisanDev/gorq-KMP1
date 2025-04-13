package com.danilisan.kmp.ui.view

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.core.screen.Screen
import com.danilisan.kmp.ui.state.BoardState
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.ui.view.gamestate.UIAdBanner
import com.danilisan.kmp.ui.view.gamestate.UIBoardContainer
import com.danilisan.kmp.ui.view.gamestate.UIMessageDisplay
import com.danilisan.kmp.ui.view.gamestate.UIQueue
import com.danilisan.kmp.ui.view.gamestate.UIReloadButton
import com.danilisan.kmp.ui.view.gamestate.UIReloadsLeft
import com.danilisan.kmp.ui.view.gamestate.UIScoreDisplay
import com.danilisan.kmp.ui.viewmodel.GameStateViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.component.KoinComponent
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
                .background(color = Theme.colors.secondary.withAlpha(0.7f)),
            contentAlignment = Alignment.TopCenter,
        ){
            val dimensions by remember{ mutableStateOf(constraints) }
            val isPortraitMode = (dimensions.maxHeight / dimensions.maxWidth) >= 16f / 9f
            if(isPortraitMode){
                PortraitGameScreen(
                    isPortraitMode,
                    maxWidth = dimensions.maxWidth.toFloat(),
                    maxHeight = dimensions.maxHeight.toFloat(),
                )
            }else{
                PortraitGameScreen(
                    isPortraitMode,
                    maxWidth = dimensions.maxWidth.toFloat(),
                    maxHeight = dimensions.maxHeight.toFloat(),
                )
            }
        }
    }
}

    @OptIn(KoinExperimentalAPI::class)
    @Composable
    private fun PortraitGameScreen(
        portraitMode: Boolean,
        maxWidth: Float,
        maxHeight: Float,
    ){
        val viewModel = koinViewModel<GameStateViewModel>()
        val gameState by viewModel.gameState.collectAsState()
        val gameMode by viewModel.gameModeState.collectAsState()

        //TODO Susituir acceso directo por lambdas en los elementos hijo
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
        val bgGradient = listOf(
            Theme.colors.transparent,
            when(state){
                BoardState.READY -> Theme.colors.secondary.withAlpha(0.7f)
                BoardState.BLOCKED -> Theme.colors.error.withAlpha(0.3f)
                BoardState.BINGO -> Theme.colors.success.withAlpha(0.3f)
                BoardState.GAMEOVER -> Theme.colors.primary.withAlpha(0.7f)
            },
        )


        val isLoading = gameState.isLoading

        val enabledReload = (!isLoading && turnsLeft >= reloadCost)
        val enabledBoard = (!isLoading || gameState.incompleteSelection) && state == BoardState.READY

        val orientationText = if(portraitMode){
            "Modo retrato"
        }else{
            "Modo paisaje"
        }
        UIMosaicBackground(
            maxWidth = maxWidth,
            maxHeight = maxHeight,
        )
        Canvas(modifier = Modifier
            .fillMaxSize()
        ){
            println("Pintando fondo")
            drawRect(
                brush = Brush.linearGradient(bgGradient)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            Column(//2/3 Upper part
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .weight(3f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier
                    .fillMaxWidth()
                    .weight(2f)
                )
                Row(//Queue & button
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .aspectRatio(2f)
                        .zIndex(2f)
                ) {
                    Box(
                        //Queue
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(2f),
                    ) {
                        UIQueue(
                            getQueueSize = { gameMode.queueSize },
                            getQueueBoxes = { gameState.queue },
                            getSelectedSize = { gameState.selectedPositions.size
                                                .takeIf{ gameState.boardState == BoardState.READY }
                                                ?: -1
                                           },
                            getTravellingBox = { gameState.travellingBox },
                            getTargetPosition = { gameState.targetPositionFromQueue },
                            getLineLength = { gameMode.lineLength },
                        )
                    }
                    Spacer(Modifier
                        .fillMaxWidth()
                        .weight(1f)
                    )
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
                                isEnabled = enabledReload,
                                buttonAction = viewModel::reloadButtonHandler
                            )
                        }
                    }
                }
                Spacer(Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                )
                Box(//Board & Line Indicators
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .zIndex(1f)
                ) {
                    UIBoardContainer(
                        lineLength = gameMode.lineLength,
                        board = board,
                        selectedPositions = gameState.selectedPositions,
                        linedPositions = gameState.linedPositions.filterNotNull(),
                        completedLines = gameState.completedLines,
                        availableLines = lines,
                        boardState = state,
                        isEnabled = enabledBoard,
                        selectAction = viewModel::selectBoxHandler,
                        dragAction = viewModel::dragLineHandler,
                    )
                }
                Spacer(Modifier
                    .fillMaxWidth()
                    .weight(1f)
                )
                Box(//AuxDisplay
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(10f),
                    contentAlignment = Alignment.Center
                ) {
                    UIMessageDisplay(message)
                }
                Spacer(Modifier
                    .fillMaxWidth()
                    .weight(2f)
                )
            }
            Column(//1/3 Lower part
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ){
                UIScoreDisplay(
                    score = score,
                    goldenStar = gameMode.isGoldenStar(score)
                )
                UIAdBanner("ANUNCIO")
            }
        }
    }

@Composable
fun UIMosaicBackground(
    maxWidth: Float,
    maxHeight: Float,
) {
    val color1 = Theme.colors.primary.withAlpha(0.03f)
    val color2 = Theme.colors.primary.withAlpha(0.07f)

    Canvas(
        modifier = Modifier.fillMaxSize()
    ){
        println("Dibujando mosaico")
        val radius = maxWidth / 20
        val numSides = 7
        val angleStep = 2 * PI / numSides
        val angleEvenX = radius * 2 * cos(PI / 7).toFloat()
        val angleEvenY = radius * 2 * sin(PI / 14).toFloat()

        var centerY = 0f
        var evenY = false
        while (centerY - radius < maxHeight) {
            var centerX = 0f
            var evenX = evenY
            while (centerX - radius < maxWidth) {
                val initialAngle = PI * if(evenX) 2 else 1
                val color = if (evenX) color1 else color2

                val path = Path().apply {
                    moveTo(
                        centerX + radius * cos(initialAngle).toFloat(),
                        centerY + radius * sin(initialAngle).toFloat()
                    )
                    for (i in 1 until numSides) {
                        lineTo(
                            centerX + radius * cos(initialAngle + angleStep * i).toFloat(),
                            centerY + radius * sin(initialAngle + angleStep * i).toFloat()
                        )
                    }
                }

                drawPath(
                    path = path,
                    color = color,
                )

                if (evenX) {
                    centerY += angleEvenY
                    centerX += angleEvenX
                } else {
                    centerY -= angleEvenY
                    centerX += angleEvenX

                }
                evenX = !evenX
            }
            centerY += if (evenY) {
                (radius + angleEvenY) * 1.9f
            } else {
                radius * 1.9f
            }
            evenY = !evenY
        }
    }
}
