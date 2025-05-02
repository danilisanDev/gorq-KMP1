package com.danilisan.kmp.ui.view.gamestate

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Ease
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.danilisan.kmp.domain.action.gamestate.GameStateActionManager.Companion.TRAVEL_ACTION_DELAY
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.ui.view.OffsetDp
import com.danilisan.kmp.ui.view.toPx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

const val TRAVELLING_ZOOM = 1.4f

@Composable
fun UITravellingBox(
    getTravellingBox: () -> NumberBox,
    getTargetPosition: () -> BoardPosition,
    getLineLength: () -> Int,
    boxRelativeSize: Float,
    offset: OffsetDp,
    containerWidth: Dp,
    scopeProvider: () -> CoroutineScope,
) {
    val travellingBox = getTravellingBox()
    val targetPosition = getTargetPosition()
    val lineLength = getLineLength()

    val boardOffsetValues = remember{
        calculateBoardDimensionValues(
            containerWidth = containerWidth,
            boxRelativeSize = boxRelativeSize,
            lineLength = lineLength,
        )
    }

    if (travellingBox !is NumberBox.EmptyBox
        && targetPosition.isValidPosition(lineLength)
    ) {
        println("IN Travelling box: $travellingBox")
        TravellingBoxAnimatedValues(
            boardDimensionValues = boardOffsetValues,
            lineLength = lineLength,
            targetPosition = targetPosition,
            scopeProvider = scopeProvider,
        ).let { animatedValues ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth(boxRelativeSize)
                    .aspectRatio(1f)
                    .offset (
                        x = offset.x,
                        y = offset.y,
                    )
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0f, 0f)
                        (animatedValues[TRAVELBOX_ANIMATED_OFFSET]?.value as Offset?)
                            ?.let{ offset ->
                                translationX = offset.x
                                translationY = offset.y
                            }
                        (animatedValues[TRAVELBOX_ANIMATED_SIZE]?.value as Float?)
                            ?.let { animatedSize ->
                                scaleX = animatedSize
                                scaleY = animatedSize
                            }
                    }
            ) {
                TravellingShadow(
                    shadowShape = when (travellingBox) {
                        is NumberBox.BlockBox -> Theme.shapes.softBlockShape
                        is NumberBox.RegularBox -> Theme.shapes.regularShape
                        else -> Theme.shapes.roundShape
                    },
                    elevation = (animatedValues[SHADOW_ANIMATED_ELEVATION]?.value as Float?),
                    offset = (animatedValues[SHADOW_ANIMATED_OFFSET]?.value as Offset?),
                )
                UINumberBox(
                    getNumberBox = getTravellingBox,
                    boxSize = maxWidth
                )
            }
        }
    }
}

@Composable
private fun TravellingShadow(
    shadowShape: Shape,
    elevation: Float?,
    offset: Offset?,
){
    val shadowColor = Theme.colors.secondary
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer{
                transformOrigin = TransformOrigin(0f, 0f)
                elevation?.let { elevation ->
                    //Size
                    scaleX = 0.8f + elevation * 0.4f
                    scaleY = 0.8f + elevation * 0.4f

                    //Offset
                    offset?.let{ offset ->
                        translationX = offset.x * elevation * 0.2f
                        translationY = offset.y * elevation * 0.2f
                    }

                    //Alpha
                    alpha = 0.5f - elevation * 0.3f
                }
            }
            .padding(1.dp)
            .background(
                color = shadowColor,
                shape = shadowShape
            )
    )
}

@Composable
private fun TravellingBoxAnimatedValues(
    boardDimensionValues: BoardDimensionValues,
    lineLength: Int,
    targetPosition: BoardPosition,
    scopeProvider: () -> CoroutineScope,
): Map<Int, Animatable<*, *>> {
    //Size values
    val targetSize = boardDimensionValues.targetSize
    val middleSize = targetSize * TRAVELLING_ZOOM
    val sizeAnimation = Animatable(
        initialValue = 1f,
    )

    //Elevation values
    val elevationAnimation = Animatable(
        initialValue = 0f,
    )

    //Box offset values
    val initialBoxOffset = Offset.Zero
    val targetBoxOffset = targetPosition.let { position ->
        Offset(
            x = (boardDimensionValues.getOffsetXFromColumn(position.column)).toPx(),
            y = (boardDimensionValues.getOffsetYFromRow(position.row)).toPx(),
        )
    }
    val boxOffsetAnimation = Animatable(
        initialValue = initialBoxOffset,
        typeConverter = Offset.VectorConverter
    )

    //Shadow offset values
    val initialShadowOffset = Offset(
        x = boardDimensionValues.getCenterOffsetX(lineLength).toPx() * -1,
        y = boardDimensionValues.getCenterOffsetY().toPx() * -1,
    )
    val targetShadowOffset = Offset(
        x = targetBoxOffset.x + initialShadowOffset.x,
        y = targetBoxOffset.y + initialShadowOffset.y,
    )
    val shadowOffsetAnimation = Animatable(
        initialValue = initialShadowOffset,
        typeConverter = Offset.VectorConverter,
    )

    val totalDuration = rememberSaveable { TRAVEL_ACTION_DELAY.toInt() }

    val fullAnimationSpec: AnimationSpec<Offset> = remember {
        tween(
            durationMillis = totalDuration,
            easing = EaseInOut,
        )
    }
    val firstPhaseSpec: AnimationSpec<Float> = remember {
        tween(
            durationMillis = totalDuration * 2 / 5,
            easing = Ease,
        )
    }
    val secondPhaseSpec: AnimationSpec<Float> = remember {
        tween(
            durationMillis = totalDuration * 3 / 5,
            easing = Ease,
        )
    }

    //Animate values
    scopeProvider().launch {
        boxOffsetAnimation.animateTo(
            targetValue = targetBoxOffset,
            animationSpec = fullAnimationSpec
        )
    }
    scopeProvider().launch {
        sizeAnimation.animateTo(
            targetValue = middleSize,
            animationSpec = firstPhaseSpec
        )
        sizeAnimation.animateTo(
            targetValue = targetSize,
            animationSpec = secondPhaseSpec
        )
    }
    scopeProvider().launch {
        elevationAnimation.animateTo(
            targetValue = 1f,
            animationSpec = firstPhaseSpec
        )
        elevationAnimation.animateTo(
            targetValue = 0f,
            animationSpec = secondPhaseSpec
        )
    }
    scopeProvider().launch {
        shadowOffsetAnimation.animateTo(
            targetValue = targetShadowOffset,
            animationSpec = fullAnimationSpec
        )
    }

    return mapOf(
        TRAVELBOX_ANIMATED_OFFSET to boxOffsetAnimation,
        TRAVELBOX_ANIMATED_SIZE to sizeAnimation,
        SHADOW_ANIMATED_ELEVATION to elevationAnimation,
        SHADOW_ANIMATED_OFFSET to shadowOffsetAnimation,
    )
}

private fun calculateBoardDimensionValues(
    containerWidth: Dp,
    lineLength: Int,
    boxRelativeSize: Float,
): BoardDimensionValues{
    val boardSide = (containerWidth + CONTAINER_PADDING.dp * 2) / 0.36f
    val boardMargin = boardSide * 0.1f
    val boardBoxSize = boardSide * 0.8f / lineLength
    val targetSize = boardBoxSize / containerWidth / boxRelativeSize

    val targetTranslationX = boardMargin - containerWidth * ( 1 / 8f + 1 - boxRelativeSize) - 10.dp
    //1/8f = Queue container margin
    //10.dp = PADDING (7.dp) + MEDIUM_BORDER (3.dp)

    val targetTranslationY = boardMargin + containerWidth * boxRelativeSize + 22.dp
    //22.dp = (PADDING (7.dp) + MEDIUM_BORDER (3.dp) + 1.dp) * 2

    return BoardDimensionValues(
        targetSize = targetSize,
        xZero = targetTranslationX,
        yZero = targetTranslationY,
        boardBoxSize = boardBoxSize,
    )
}

//TODO Abstraer estos calculos a GameScreen OR UICommons, con las constantes de medidas
private data class BoardDimensionValues(
    val targetSize: Float,
    private val xZero: Dp,
    private val yZero: Dp,
    private val boardBoxSize: Dp,
){
    fun getOffsetXFromColumn(column: Int): Dp = xZero + (boardBoxSize * column)
    fun getOffsetYFromRow(row: Int): Dp = yZero + (boardBoxSize * row)
    //Light source (Top left-center of board)
    fun getCenterOffsetX(lineLength: Int) = xZero + (boardBoxSize * (lineLength - 1).toFloat() / 2)
    fun getCenterOffsetY() = yZero
}