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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.core.screen.Screen
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.DisplayMessage
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.Score
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
import kotlinx.coroutines.flow.map
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
                .background(
                    Theme.colors.secondary.withAlpha(0.7f)
                ),
            contentAlignment = Alignment.TopCenter,
        ){
            val isPortraitMode = (maxHeight / maxWidth) >= 16f / 9f

            println("Pintando fondo")

            //Static background
            UIMosaicBackground(
                maxWidth = maxWidth.toPx(),
                maxHeight = maxHeight.toPx(),
            )
            GameScaffold(isPortraitMode)
        }
    }
}

    @OptIn(KoinExperimentalAPI::class)
    @Composable
    private fun GameScaffold(
        portraitMode: Boolean,
    ){
        //Viewmodel injection
        val viewModel = koinViewModel<GameStateViewModel>()

        //State collection
        val gameMode by viewModel.gameModeState.collectAsState()
        val board = viewModel.gameState.map { it.board }.collectAsState(
            initial = emptyMap()
        )
        val queue = viewModel.gameState.map { it.queue }.collectAsState(
            initial = emptyList()
        )
        val reloadsLeft = viewModel.gameState.map { it.reloadsLeft }.collectAsState(
            initial = 0
        )
        val score = viewModel.gameState.map { it.score }.collectAsState(
            initial = Score()
        )
        val boardState = viewModel.gameState.map { it.boardState }.collectAsState(
            initial = BoardState.READY
        )
        val availableLines = viewModel.gameState.map { it.availableLines }.collectAsState(
            initial = emptySet()
        )
        val displayMessage = viewModel.gameState.map { it.displayMessage }.collectAsState(
            initial = DisplayMessage()
        )
        val selectedPositions = viewModel.gameState.map { it.selectedPositions }.collectAsState(
            initial = emptyList()
        )
        val incompleteSelection = viewModel.gameState.map { it.incompleteSelection }.collectAsState(
            initial = false
        )
        val travellingBox = viewModel.gameState.map { it.travellingBox }.collectAsState(
            initial = NumberBox.EmptyBox()
        )
        val targetPositionFromQueue = viewModel.gameState.map { it.targetPositionFromQueue }.collectAsState(
            initial = BoardPosition()
        )
        val linedPositions = viewModel.gameState.map { it.linedPositions }.collectAsState(
            initial = emptyList()
        )
        val completedLines = viewModel.gameState.map { it.completedLines }.collectAsState(
            initial = emptyList()
        )
        val isLoading = viewModel.gameState.map { it.isLoading }.collectAsState(
            initial = true
        )

        //Background gradient colors
        val bgColors =
            listOf(
                Theme.colors.transparent,
                Theme.colors.secondary.withAlpha(0.7f),
                Theme.colors.error.withAlpha(0.3f),
                Theme.colors.success.withAlpha(0.3f) + Theme.colors.secondary,
                Theme.colors.primary.withAlpha(0.7f),
            )

        val orientationText = if(portraitMode){
            "Modo retrato"
        }else{
            "Modo paisaje"
        }

        //
        Column(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind{
                    drawRect(
                        brush = Brush.linearGradient(
                            listOf(
                                bgColors[0],
                                when(boardState.value) {
                                    BoardState.READY -> bgColors[1]
                                    BoardState.BLOCKED -> bgColors[2]
                                    BoardState.BINGO -> bgColors[3]
                                    BoardState.GAMEOVER -> bgColors[4]
                                }
                            )
                        )
                    )
                },
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
                            getLineLength = { gameMode.lineLength },
                            getQueueBoxes = { queue.value },
                            getSelectedSize = { selectedPositions.value.size
                                                .takeIf{ boardState.value == BoardState.READY }
                                                ?: -1
                                           },
                            getTravellingBox = { travellingBox.value },
                            getTargetPosition = { targetPositionFromQueue.value },
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
                            UIReloadsLeft(
                                getTurnsLeft = { reloadsLeft.value }
                            )
                        }
                        Box(//Reload button
                            modifier = Modifier
                                .weight(3f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            UIReloadButton(
                                getBoardState = { boardState.value },
                                getReloadCost = { boardState ->
                                    when(boardState){
                                        BoardState.READY -> gameMode.reloadQueueCost
                                        BoardState.BLOCKED -> gameMode.reloadBoardCost
                                        else -> 0
                                    }
                                },
                                isEnabled = { !isLoading.value && reloadsLeft.value > 0 },
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
                        getLineLength = { gameMode.lineLength },
                        getBoard = { board.value },
                        getTargetPositionFromQueue = { targetPositionFromQueue.value },
                        getSelectedPositions = { selectedPositions.value },
                        getLinedPositions = { linedPositions.value.filterNotNull() },
                        getCompletedLines = { completedLines.value },
                        getAvailableLines = { availableLines.value },
                        getBoardState = {  boardState.value },
                        isEnabled = { isSelectAction ->
                            boardState.value == BoardState.READY &&
                                     ( !isLoading.value ||
                                             (isSelectAction && incompleteSelection.value)
                                     )
                        },
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
                    UIMessageDisplay(
                        getMessage = { displayMessage.value }
                    )
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
                    getScore = { score.value },
                    isGoldenStar = { score -> gameMode.isGoldenStar(score) }
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

    val radius = maxWidth / 20
    val numSides = 7
    val angleStep = 2 * PI / numSides
    val angleEvenX = radius * 2 * cos(PI / 7).toFloat()
    val angleEvenY = radius * 2 * sin(PI / 14).toFloat()

    println("Calculando dibujo de mosaico")

    Canvas(
        modifier = Modifier
            .fillMaxSize()
    ){
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
