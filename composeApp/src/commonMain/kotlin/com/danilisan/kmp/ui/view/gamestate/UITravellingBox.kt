package com.danilisan.kmp.ui.view.gamestate

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.danilisan.kmp.domain.action.gamestate.GameStateActionManager.Companion.TRAVEL_ACTION_DELAY
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.ui.view.toPx
import com.danilisan.kmp.ui.view.withAlpha
import kotlinx.coroutines.launch

const val ANIMATED_TRAVEL = 0
const val ANIMATED_ELEVATION = 1

@Composable
fun UITravellingBox(
    getTravellingBox: () -> TravellingBox?,
    getLineLength: () -> Int,
    boxProperties: QueueBoxProperties,
    containerWidthInPx: Float,
    applyStarAnimation: () -> Brush?,
) {
    val dpToPx = 1.dp.toPx()
    val boardDimensions by remember {
        derivedStateOf {
            BoardLayoutValues(
                containerWidth = containerWidthInPx,
                lineLength = getLineLength(),
                dpToPx = dpToPx,
                properties = boxProperties
            )
        }
    }

    //Animations
    val animationMap = getAnimatedValuesMap(
        getTravellingBox = getTravellingBox
    )


    if (getTravellingBox() != null) {
        val shadowColor = Theme.colors.secondary
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth(boxProperties.getSize())
                .aspectRatio(1f)
                .offset {
                    IntOffset(
                        x = boxProperties.getOffsetX(containerWidthInPx).toInt(),
                        y = boxProperties.getOffsetY(containerWidthInPx).toInt(),
                    )
                }
                .graphicsLayer {
                    animateProperties(
                        position = getTravellingBox()?.targetPosition,
                        targetSizeDiff = boardDimensions.getTargetSizeDiff(),
                        getTargetOffset = boardDimensions::getTargetOffset,
                        animatedTravel = animationMap[ANIMATED_TRAVEL]?.value ?: 0f,
                        animatedElevation = animationMap[ANIMATED_ELEVATION]?.value ?: 0f,
                    )
                    transformOrigin = TransformOrigin(0f, 0f)
                }
                .drawBehind {
                    drawTravellingShadow(
                        circularShadow = getTravellingBox()?.content is NumberBox.StarBox,
                        position = getTravellingBox()?.targetPosition,
                        targetSizeDiff = boardDimensions.getTargetSizeDiff(),
                        getShadowOffset = boardDimensions::getShadowOffset,
                        shadowColor = shadowColor,
                        animatedTravel = animationMap[ANIMATED_TRAVEL]?.value ?: 0f,
                        animatedElevation = animationMap[ANIMATED_ELEVATION]?.value ?: 0f,
                    )
                }
        ) {
            UINumberBox(
                getNumberBox = { getTravellingBox()?.content ?: NumberBox.EmptyBox() },
                boxSize = maxWidth,
                applyStarAnimation = applyStarAnimation,
            )
        }
    }
}

@Composable
private fun getAnimatedValuesMap(
    getTravellingBox: () -> TravellingBox?
): Map<Int, Animatable<Float, AnimationVector1D>>{
    val totalDuration = remember { (TRAVEL_ACTION_DELAY).toInt() }
    val animatedTravel = remember {
        Animatable(
            initialValue = 0f
        )
    }
    val animatedElevation = remember {
        Animatable(
            initialValue = 0f
        )
    }
    LaunchedEffect(getTravellingBox()){
        if(getTravellingBox() != null){
            launch{
                animatedTravel.snapTo(
                    targetValue = 0f
                )
                animatedTravel.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = totalDuration,
                        easing = EaseInOutCubic
                    )
                )
            }
            launch{
                animatedElevation.snapTo(
                    targetValue = 0f
                )
                animatedElevation.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = totalDuration * 2 / 5,
                    )
                )
                animatedElevation.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = totalDuration * 3 / 5,
                    )
                )
            }
        }
    }
    return mapOf(
        ANIMATED_TRAVEL to animatedTravel,
        ANIMATED_ELEVATION to animatedElevation
    )
}

private fun DrawScope.drawTravellingShadow(
    circularShadow: Boolean,
    position: BoardPosition?,
    targetSizeDiff: Float,
    getShadowOffset: (BoardPosition, Float, Float) -> Offset,
    shadowColor: Color,
    animatedTravel: Float,
    animatedElevation: Float,
) {
    if (position == null) return
    shadowColor.withAlpha(0.3f - (animatedElevation * 0.2f)).let { color ->
        getShadowOffset(position, animatedTravel, animatedElevation).let { (x, y) ->
            translate(
                left = x,
                top = y
            ) {
                scale(0.8f + animatedTravel * targetSizeDiff * animatedElevation) {
                    if (circularShadow) {
                        drawCircle(
                            color = color,
                        )
                    } else {
                        drawRoundRect(
                            color = color,
                            cornerRadius = CornerRadius(14f, 14f)
                        )
                    }
                }
            }
        }
    }
}

private fun GraphicsLayerScope.animateProperties(
    position: BoardPosition?,
    targetSizeDiff: Float,
    getTargetOffset: (BoardPosition) -> Offset,
    animatedTravel: Float,
    animatedElevation: Float,
) {
    if (position == null) return
    (1f + animatedTravel * targetSizeDiff + animatedElevation * 0.7f).let { size ->
        scaleX = size
        scaleY = size
    }
    getTargetOffset(position).let { (x, y) ->
        translationX = x * animatedTravel
        translationY = y * animatedTravel
    }
}

@Stable
data class TravellingBox(
    val content: NumberBox,
    val targetPosition: BoardPosition,
)

data class BoardLayoutValues(
    private val containerWidth: Float,
    private val lineLength: Int,
    private val dpToPx: Float,
    private val properties: QueueBoxProperties,
) {
    private val boardBoxSize = (1f / 0.395f) / lineLength
    private val boardTopLeftOffsetX = (containerWidth + 10 * dpToPx) * (1f / 3.6f - 1f / 8f)
    private val boardTopLeftOffsetY = (containerWidth + 10 * dpToPx) * (1f / 3.6f + 1f / 0.8f)
    private val centerLightOffsetX = (boardTopLeftOffsetX / 0.88f)
    private val centerLightOffsetY = (boardTopLeftOffsetY / 0.88f)

    fun getTargetSizeDiff(): Float = (boardBoxSize / properties.getSize()) - 1f
    fun getTargetOffset(position: BoardPosition): Offset = Offset(
        x = boardTopLeftOffsetX.toPosition(position.column) - properties.getOffsetX(containerWidth) - (7 * dpToPx),
        y = boardTopLeftOffsetY.toPosition(position.row) - properties.getOffsetY(containerWidth) + (11 * dpToPx)
    )

    fun getShadowOffset(position: BoardPosition, travel: Float, elevation: Float): Offset = Offset(
        x = (travel * boardTopLeftOffsetX.toPosition(position.column) - centerLightOffsetX) * (elevation * 0.3f),
        y = (travel * boardTopLeftOffsetY.toPosition(position.row) - centerLightOffsetY) * (elevation * 0.3f)
    )

    private fun Float.toPosition(position: Int) = this + (containerWidth * boardBoxSize * position)
}