package com.danilisan.kmp.ui.view.gamestate

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import com.danilisan.kmp.domain.action.gamestate.GameStateActionManager.Companion.TRAVEL_ACTION_DELAY
import com.danilisan.kmp.domain.action.gamestate.GameStateActionManager.Companion.UPDATE_BOARD_TOTAL_DELAY
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.ui.state.BoardState
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.ui.view.CONTRAST
import com.danilisan.kmp.ui.view.combineOver
import com.danilisan.kmp.ui.view.createRelativeShader
import com.danilisan.kmp.ui.view.plus
import com.danilisan.kmp.ui.view.toArrayWithAbsoluteColorStops
import com.danilisan.kmp.ui.view.withAlpha
import kotlinx.coroutines.delay

const val WHITE = 0
const val BLACK = 1
const val SELECTED = 2
const val LINED = 3
const val BINGO = 4

const val INSET = 0
const val OUTSET = 1
const val STAR = 2


@Composable
fun UIBoardNumbers(
    showMenuBar: MutableState<Boolean>,
    getLineLength: () -> Int,
    getBoard: () -> Map<BoardPosition, NumberBox>,
    getTargetPositionFromQueue: () -> BoardPosition? = { null },
    getSelectedPositions: () -> List<BoardPosition> = { emptyList() },
    getLinedPositions: () -> List<BoardPosition> = { emptyList() },
    getCompletedLinesSize: () -> Int = { 0 },
    getBoardState: () -> BoardState = { BoardState.BLOCKED },
    applyStarAnimation: () -> Brush?,
    isSelectionEnabled: () -> Boolean = { false },
    selectAction: (BoardPosition) -> Unit = {}
) {
    val lineLength = getLineLength()
    val relativeSize = 1f / lineLength
    val boardSize by remember {
        derivedStateOf {
            getBoard().size
        }
    }

    val colorList = listOf(
        Theme.colors.primary,
        Theme.colors.secondary,
        Theme.colors.selected,
        Theme.colors.success,
        Theme.colors.golden
    )
    val gradientList = listOf(
        Theme.colors.insetGradient,
        Theme.colors.outsetGradient,
        Theme.colors.starGradient,
    )

    if(boardSize > 0) {
        Column(verticalArrangement = Arrangement.Center) {
            for (row in 0 until lineLength) {
                Row(horizontalArrangement = Arrangement.Center) {
                    for (column in 0 until lineLength) {
                        val position = BoardPosition(row = row, column = column)
                        BoxTile(
                            weight = relativeSize,
                            applyDarkBackground = { position.getRightDiagonalParity() },
                            getNumberBox = { getBoard()[position] ?: NumberBox.EmptyBox() },
                            comesFromQueue = { position == getTargetPositionFromQueue() },
                            isSelected = { (position in getSelectedPositions()).takeUnless{ position in getLinedPositions() } },
                            applyShader = { shaderSize ->
                                getShader(
                                    colorList = colorList,
                                    shaderSize = shaderSize,
                                    getBoardState = getBoardState,
                                    boardPosition = position,
                                    getSelectedPositions = getSelectedPositions,
                                    getLinedPositions = getLinedPositions,
                                    getCompletedLinesSize = getCompletedLinesSize,
                                    incompleteLineLength = lineLength - 1
                                )
                            },
                            applyBorderBrush = { boxType ->
                                if(!showMenuBar.value) {
                                    getBorderStyle(
                                        colorList = colorList,
                                        gradientList = gradientList,
                                        boardPosition = position,
                                        boxType = boxType,
                                        getBoardState = getBoardState,
                                        getSelectedPositions = getSelectedPositions,
                                        getLinedPositions = getLinedPositions,
                                    )
                                }else{ null }
                            },
                            applyStarAnimation = applyStarAnimation,
                            isSelectionEnabled = isSelectionEnabled,
                            selectAction = { selectAction(position) },
                        )
                    }
                }
            }
        }
    }
}

const val BOXTILE_ANIMATED_ROTATION = 0
const val BOXTILE_ANIMATED_SIZE = 1
const val BOXTILE_ANIMATED_ALPHA = 2
const val BOXTILE_ANIMATED_OFFSET = 3


@Composable
private fun RowScope.BoxTile(
    weight: Float,
    applyDarkBackground: () -> Boolean,
    getNumberBox: () -> NumberBox,
    isSelected: () -> Boolean?,
    comesFromQueue: () -> Boolean,
    applyShader: (Float) -> BoxShader?,
    applyBorderBrush: (BoxType) -> Brush?,
    applyStarAnimation: () -> Brush?,
    isSelectionEnabled: () -> Boolean,
    selectAction: () -> Unit,
){
    //Number box values
    val newNumberBox by remember{
        derivedStateOf {
            getNumberBox()
        }
    }
    var currentNumberBox by remember{ mutableStateOf(newNumberBox)}

    //Animation key
    var isLandingPosition by remember{
        mutableStateOf (false)
    }
    LaunchedEffect(newNumberBox){
        isLandingPosition = comesFromQueue()
    }

    LaunchedEffect(newNumberBox){
        if(isLandingPosition){
            (TRAVEL_ACTION_DELAY * 0.85f).toLong()
        }else{
            UPDATE_BOARD_TOTAL_DELAY / 2
        }.run{
            delay(this)
        }
        currentNumberBox = newNumberBox
    }


    //Animation values
    var animatedValues: Map<Int, Animatable<*, *>> by remember{
        mutableStateOf(emptyMap())
    }
    animatedValues = if(newNumberBox is NumberBox.EmptyBox){
        emptyMap()
    }else if(isLandingPosition){
        replacingAnimationValues(
            newNumberBox
        )
    }else{
        flipAnimationValues(
            newNumberBox,
        )
    }

    //Selection click animations
    val tileSelected by remember{
        derivedStateOf{
            isSelected() == true
        }
    }
    val animatedShader by animateFloatAsState(
        targetValue = if(tileSelected) 1f else 0f,
        animationSpec = if(tileSelected) spring() else spring(stiffness = Spring.StiffnessHigh)
    )

    var fullSizeClick by remember{mutableStateOf(true)}

    val animatedClickSize by animateFloatAsState(
        targetValue = if(fullSizeClick) 1f else 0.8f,
        animationSpec = if(tileSelected){
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh)
        } else {
            tween(100)
        }
    )

    LaunchedEffect(tileSelected){
        fullSizeClick = false
        delay(100)
        fullSizeClick = true
    }

    //Flip properties
    val backSideColor = Theme.colors.secondary.combineOver(
        other = Theme.colors.primary,
        alpha = CONTRAST * if(applyDarkBackground()) 1f else 2f
    )
    val borderBackColor = Theme.colors.primary

    BoxWithConstraints(
        modifier = Modifier
            .weight(weight)
            .aspectRatio(1f)
            .graphicsLayer{
                (animatedValues[BOXTILE_ANIMATED_SIZE]?.value as Float?)
                    ?.let { animatedSize ->
                        scaleX = animatedSize
                        scaleY = animatedSize
                    }
                (animatedValues[BOXTILE_ANIMATED_ALPHA]?.value as Float?)
                    ?.let { animatedAlpha ->
                        alpha = animatedAlpha
                    }
                (animatedValues[BOXTILE_ANIMATED_ROTATION]?.value as Float?)
                    ?.let { animatedRotation ->
                        rotationX = animatedRotation
                        calculateAnimatedScale(animatedRotation).let{ animatedScale ->
                            scaleX = animatedScale
                            scaleY = animatedScale
                            cameraDistance = density * (4f - animatedScale)
                        }
                    }
                (animatedValues[BOXTILE_ANIMATED_OFFSET]?.value as Float?)
                    ?.let { animatedOffset ->
                        translationY = animatedOffset
                    }
            }
            .graphicsLayer{
                scaleX = animatedClickSize
                scaleY = animatedClickSize
            }
            .drawWithContent{
                val faceUp = (animatedValues[BOXTILE_ANIMATED_ROTATION]?.value as Float?)?.let{
                    it < 90f || it > 270f
                }?: true
                if(faceUp){
                    drawContent()
                }else{
                    drawBackBox(
                        backgroundColor = backSideColor,
                        borderColor = borderBackColor,
                    )
                }
            }
            .clickable(
                indication = null,
                interactionSource = remember{ MutableInteractionSource() }
            ){
                if(isSelectionEnabled() && currentNumberBox is NumberBox.RegularBox){
                    selectAction()
                }
            }
    ) {
        UINumberBox(
            getNumberBox = { currentNumberBox },
            boxSize = maxWidth,
            applyShader = {
                applyShader(
                    if(isSelected() == null) 1f else animatedShader
                )
            },
            applyBorderStyle = applyBorderBrush,
            applyStarAnimation = applyStarAnimation,
        )
    }
}

private fun DrawScope.drawBackBox(
    backgroundColor: Color,
    borderColor: Color,
){
    (this.size.width / 10f).let{
        CornerRadius(x = it, y = it)
    }
        .let{ radius ->
            drawRoundRect(
                color = backgroundColor,
                cornerRadius = radius
            )
            drawRoundRect(
                color = borderColor,
                style = Stroke(size.width / 49f),
                cornerRadius = radius
            )
        }
}

private fun calculateAnimatedScale(
    animatedRotation: Float
): Float = (if(animatedRotation < 180f){
    animatedRotation
} else{
    (360f - animatedRotation)
} / 717f) + 1f

@Composable
private fun flipAnimationValues(
    newNumberBox: NumberBox,
): Map<Int, Animatable<*, *>> {
    if(newNumberBox is NumberBox.EmptyBox){
        return emptyMap()
    }

    //Initial and target values
    val rotationAnimation = remember {
        Animatable(
            initialValue = 0f,
        )
    }

    LaunchedEffect(newNumberBox) {
        rotationAnimation.snapTo(
            targetValue = 0f
        )
        rotationAnimation.animateTo(
            targetValue = 360f,
            animationSpec = tween(
                durationMillis = UPDATE_BOARD_TOTAL_DELAY.toInt(),
                easing = LinearEasing
            )
        )
    }

    val offsetAnimation = remember{
        Animatable(0f)
    }
    LaunchedEffect(newNumberBox) {
        offsetAnimation.animateTo(
            targetValue = -50f,
            animationSpec = spring()
        )
        offsetAnimation.animateTo(
            targetValue = 30f,
            animationSpec = spring()
        )
        offsetAnimation.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                Spring.DampingRatioMediumBouncy,
                Spring.StiffnessMedium
            )
        )
    }


    return mapOf(
        BOXTILE_ANIMATED_ROTATION to rotationAnimation,
        BOXTILE_ANIMATED_OFFSET to offsetAnimation,
    )
}


@Composable
private fun replacingAnimationValues(
    newNumberBox: NumberBox
): Map<Int, Animatable<*, *>> {
    if(newNumberBox is NumberBox.EmptyBox){
        return emptyMap()
    }
    //Initial and target values
    val sizeAnimation = remember {
        Animatable(
            initialValue = 1f,
        )
    }
    val alphaAnimation = remember {
        Animatable(
            initialValue = 1f,
        )
    }

    val animationSpec: AnimationSpec<Float> = remember {
        tween(
            durationMillis = (TRAVEL_ACTION_DELAY * 0.85f).toInt(),
            easing = EaseInOutCubic
        )
    }

    LaunchedEffect(newNumberBox){
        alphaAnimation.animateTo(
            targetValue = 0f,
            animationSpec = animationSpec
        )
        alphaAnimation.snapTo(
            targetValue = 1f
        )
    }
    LaunchedEffect(newNumberBox){
        sizeAnimation.animateTo(
            targetValue = 0f,
            animationSpec = animationSpec
        )
        sizeAnimation.snapTo(
            targetValue = 0.5f,
        )
        sizeAnimation.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
            )
        )
    }
    return mapOf(
        BOXTILE_ANIMATED_SIZE to sizeAnimation,
        BOXTILE_ANIMATED_ALPHA to alphaAnimation,
    )
}

private fun getShader(
    colorList: List<Color>,
    shaderSize: Float,
    getBoardState: () -> BoardState,
    boardPosition: BoardPosition,
    getSelectedPositions: () -> List<BoardPosition>,
    getLinedPositions: () -> List<BoardPosition>,
    getCompletedLinesSize: () -> Int,
    incompleteLineLength: Int,
): BoxShader {
    val selectedPositions = getSelectedPositions()
    val linedPositions = getLinedPositions()

    return when{
        //Bingo
        (getBoardState() == BoardState.BINGO) -> {
            BoxShader(
                color = colorList[LINED].withAlpha(0.8f),
                whiteText = true,
            )
        }

        //Lined position (last line -> previous line -> future line)
        (boardPosition in linedPositions) -> {
            val completedLinesSize = getCompletedLinesSize()
            var whiteText = false
            val color = if(completedLinesSize == 0 || linedPositions.size < 2){
                colorList[BLACK].withAlpha(0.1f)
            }else{
                val lastPositionInCompletedLine = completedLinesSize * incompleteLineLength
                linedPositions.lastIndexOf(boardPosition)
                    .takeUnless{ it > lastPositionInCompletedLine }
                    ?.let { positionLastIndex ->
                        val lineIndex = (completedLinesSize - (positionLastIndex / incompleteLineLength) - 1)
                            .takeUnless{ it < 0 }?: 0
                        whiteText = true
                        createRelativeShader(
                            bgColor = colorList[LINED],
                            shaderColor = colorList[WHITE],
                            index = lineIndex,
                            maxIndex = completedLinesSize
                        )
                    }?: colorList[BLACK].withAlpha(0.1f)
            }
            BoxShader(
                color = color,
                whiteText = whiteText,
            )
        }
        //Selected
        else -> {
            val color = createRelativeShader(
                bgColor = colorList[SELECTED],
                shaderColor = colorList[BLACK],
                index = selectedPositions.run{
                    size - indexOf(boardPosition) - 1
                },
                maxIndex = selectedPositions.size
            )
            val isSelected = (boardPosition in selectedPositions)
            BoxShader(
                color = color,
                whiteText = isSelected,
                size = shaderSize,
            )
        }
    }
}
private fun getBorderStyle(
    colorList: List<Color>,
    gradientList: List<List<Color>>,
    boardPosition: BoardPosition,
    boxType: BoxType,
    getBoardState: () -> BoardState,
    getSelectedPositions: () -> List<BoardPosition>,
    getLinedPositions: () -> List<BoardPosition>,
): Brush? = when{
    (getBoardState() == BoardState.BINGO) -> {//BINGO border
        Brush.sweepGradient(
            colors = gradientList[STAR].map { it + colorList[BINGO] }
        )
    }
    (boardPosition in getSelectedPositions()) -> {//SELECTED border
        buildSetGradient(
            mainColor = colorList[SELECTED],
            gradientColors = gradientList[INSET],
        )
    }
    (boardPosition in getLinedPositions() && getLinedPositions().size > 1) -> {//LINED border
        Brush.linearGradient(
            colors = gradientList[STAR].map { it + colorList[LINED] }
        )
    }
    (getBoardState() == BoardState.READY && boxType == BoxType.REGULAR) -> {//SELECTABLE border
        buildSetGradient(
            mainColor = colorList[SELECTED],
            gradientColors = gradientList[OUTSET],
        )
    }
    else -> null
}

private fun buildSetGradient(
    mainColor: Color,
    gradientColors: List<Color>,
): Brush{
    return Brush.linearGradient(
        colorStops = gradientColors
            .map { it + mainColor }
            .toArrayWithAbsoluteColorStops(blur = 0.14f),
    )
}