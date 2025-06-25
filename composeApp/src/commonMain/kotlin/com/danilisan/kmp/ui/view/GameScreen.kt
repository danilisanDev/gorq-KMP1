package com.danilisan.kmp.ui.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseInOutElastic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.core.screen.Screen
import com.danilisan.kmp.domain.entity.DisplayMessage
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.Score
import com.danilisan.kmp.ui.state.BoardState
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.ui.view.gamestate.UIAdBanner
import com.danilisan.kmp.ui.view.gamestate.UIBoardContainer
import com.danilisan.kmp.ui.view.gamestate.UIMessageDisplay
import com.danilisan.kmp.ui.view.gamestate.UINewGameDialog
import com.danilisan.kmp.ui.view.gamestate.UIQueue
import com.danilisan.kmp.ui.view.gamestate.UIReloadButton
import com.danilisan.kmp.ui.view.gamestate.UIReloadsLeft
import com.danilisan.kmp.ui.view.gamestate.UIScoreDisplay
import com.danilisan.kmp.ui.view.gamestate.UISliderMenuBottom
import com.danilisan.kmp.ui.view.gamestate.UISliderMenuTop
import com.danilisan.kmp.ui.viewmodel.GameStateViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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
        val updatingPositions = viewModel.gameState.map { it.updatingPositions }.collectAsState(
            initial = emptyList()
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
        val colorList =
            listOf(
                Theme.colors.transparent,
                Theme.colors.secondary.withAlpha(0.7f),
                Theme.colors.error.withAlpha(0.3f),
                Theme.colors.success.withAlpha(0.3f) + Theme.colors.secondary,
                Theme.colors.primary.withAlpha(0.7f),
            )

        //Menu and dialogs
        val showMenuBar = remember { mutableStateOf(false) }
        val showNewGameDialog = remember { mutableStateOf(false) }
        val showTutorialDialog by remember { mutableStateOf(false) }

        //Common animations
        val progressAnimatedValue = remember{ Animatable(0f) }
        var isReloading by remember { mutableStateOf(false) }
        val starAnimatedValue = starAnimatedValue()
        val applyStarAnimation = {
            reflectAnimation(
                light = colorList[4].withAlpha(1f),
                animatedValue = starAnimatedValue.value
            ).takeIf{ (board.value.values + queue.value).any{ it is NumberBox.StarBox } && !showMenuBar.value }
        }
        val overlappedComponentsAlpha by animateFloatAsState(
            targetValue = if(showMenuBar.value) 0f else 1f,
            animationSpec = tween(500)
        )
        val menuBarOffset by animateFloatAsState(
            targetValue = if(showMenuBar.value) 0f else 1f,
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = if(showMenuBar.value) 100 else 0,
                easing = EaseInOutElastic
            )
        )

        UINewGameDialog(
            showThisDialog = showNewGameDialog,
            showMenuBar = showMenuBar,
            newGameAction = viewModel::newGameHandler
        )

        val orientationText = if(portraitMode){
            "Modo retrato"
        }else{
            "Modo paisaje"
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind{
                    drawRect(
                        brush = Brush.linearGradient(
                            listOf(
                                colorList[0],
                                when(boardState.value) {
                                    BoardState.READY -> colorList[1]
                                    BoardState.BLOCKED -> colorList[2]
                                    BoardState.BINGO -> colorList[3]
                                    BoardState.GAMEOVER -> colorList[4]
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .aspectRatio(2f)
                        .zIndex(2f)
                ) {
                    UISliderMenuTop(
                        showMenuBar = showMenuBar,
                        showNewGameDialog = showNewGameDialog,
                        getGameModeName = { gameMode.name },
                        animatedOffset = menuBarOffset,
                    )
                    Row(//Queue & button
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer{
                                alpha = overlappedComponentsAlpha
                            }
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
                                getUpdatingPositions = { updatingPositions.value },
                                applyStarAnimation = applyStarAnimation,
                                reloadingCircle = {
                                    if(isReloading && boardState.value == BoardState.READY){
                                        ProgressCircle(progressAnimatedValue.value)
                                    }
                                },
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
                                    isEnabled = { !isLoading.value
                                            && !showMenuBar.value
                                            && reloadsLeft.value > 0
                                            && !isReloading },
                                    addButtonAction = { isEnabled ->
                                        val state = boardState.value
                                        if(state == BoardState.READY) {
                                            (::reloadPress)(
                                                progressAnimatedValue,
                                                { value -> isReloading = value},
                                                isEnabled,
                                                viewModel::reloadButtonHandler
                                            )
                                        }else {
                                            (::singlePress)(
                                                { isEnabled() || state == BoardState.GAMEOVER },
                                                viewModel::reloadButtonHandler
                                            )
                                        }
                                    }
                                )
                            }
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
                        showMenuBar = showMenuBar,
                        getLineLength = { gameMode.lineLength },
                        getBoard = { board.value },
                        getUpdatingPositions = { updatingPositions.value },
                        getSelectedPositions = { selectedPositions.value },
                        getLinedPositions = { linedPositions.value.filterNotNull() },
                        getCompletedLines = { completedLines.value },
                        getAvailableLines = { availableLines.value },
                        getBoardState = {  boardState.value },
                        applyStarAnimation = applyStarAnimation,
                        isEnabled = { isSelectAction ->
                            !showMenuBar.value &&
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(6f)
                ){
                    UISliderMenuBottom(
                        animatedOffset = menuBarOffset,
                        topScore = { score.value.maxPoints },
                    )
                    UIScoreDisplay(
                        getScore = { score.value },
                        applyAlpha = { overlappedComponentsAlpha },
                        applyStarAnimation = applyStarAnimation,
                        isGoldenStar = { score -> gameMode.isGoldenStar(score) },
                    )
                }

                UIAdBanner("ANUNCIO")
            }
        }
    }

//Star animation
@Composable
private fun starAnimatedValue(): State<Float> {
    val transition = rememberInfiniteTransition()
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 7000
                0f at 1000
                1f at 1510
                2f at 1770
                0f at 2000
            },
        )
    )
}

private fun reflectAnimation(
    light: Color,
    animatedValue: Float
): Brush{
    val reflection = mapOf(
        0.45f to light.withAlpha(0f),
        0.6f to light.withAlpha(0.2f),
        0.65f to light.withAlpha(0f),
        0.70f to light.withAlpha(0.3f),
        0.75f to light.withAlpha(0.2f),
        0.9f to light.withAlpha(0f)
    )
    return Brush.linearGradient(
        colorStops = reflection.map{ (stop, color) ->
            Pair(
                stop.animateColorStop(animatedValue),
                color.combineOver(
                    other = light.withAlpha(animatedValue * 0.3f)
                )
            )
        }.toTypedArray()
    )
}

private fun Float.animateColorStop(animatedValue: Float) =
    (this + animatedValue).let{
        if(it > 2f) it - 2f
        else if(it > 1f) it - 1f
        else it
    }



//Reloading circle
@Composable
private fun BoxScope.ProgressCircle(
    animatedValue: Float
){
    val progressColor = Theme.colors.primary
    val bgColor = Theme.colors.secondary.withAlpha(0.5f)
    val brush = Brush.radialGradient(
        listOf(
            progressColor,
            progressColor.withAlpha(0.4f)
        )
    )

    Canvas(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .align(Alignment.Center)
            .graphicsLayer{
                alpha = 0.4f + (animatedValue / 360f) * 0.4f
            }
    ) {
        scale(0.9f) {
            drawCircle(
                brush = brush
            )
            drawArc(
                color = bgColor,
                size = Size(size.width, size.width),
                startAngle = 270f,
                sweepAngle = -360f + animatedValue,
                useCenter = true,
            )
        }
        scale(0.95f) {
            drawCircle(
                color = progressColor.withAlpha(0.7f),
                style = Stroke(size.width / 50)
            )
        }
    }
}
fun Modifier.singlePress(
    isEnabled: () -> Boolean,
    buttonAction: () -> Unit,
): Modifier = this.clickable(
    onClick = buttonAction,
    enabled = isEnabled(),
)

fun Modifier.reloadPress(
    animatedValue: Animatable<Float, AnimationVector1D>,
    setIsReloading: (Boolean) -> Unit,
    isEnabled: () -> Boolean,
    buttonAction: () -> Unit,
): Modifier = this.clickable(
    onClick = {},
    enabled = isEnabled()
)
    .pointerInput(Unit){
        detectTapGestures(
            onPress = {
                if(isEnabled()){
                    coroutineScope {
                        setIsReloading(true)
                        val duration = 717
                        val animationJob = launch{
                            animatedValue.animateTo(
                                targetValue = 360f,
                                animationSpec = tween(
                                    durationMillis = duration,
                                    easing = LinearEasing,
                                )
                            )
                            buttonAction()
                            animatedValue.snapTo(0f)
                            setIsReloading(false)
                        }

                        delay(duration / 2L)
                        tryAwaitRelease()
                        animationJob.cancel()
                        animatedValue.snapTo(0f)
                        setIsReloading(false)
                    }
                }
            }
        )
}

//Background
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
