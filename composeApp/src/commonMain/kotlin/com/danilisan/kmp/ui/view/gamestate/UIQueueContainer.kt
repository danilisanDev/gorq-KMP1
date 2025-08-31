package com.danilisan.kmp.ui.view.gamestate

import UIQueueNumbers
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.danilisan.kmp.domain.action.gamestate.GameStateActionManager.Companion.TRAVEL_ACTION_DELAY
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.ui.view.combineOver
import com.danilisan.kmp.ui.view.toPx
import com.danilisan.kmp.ui.view.withAlpha
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.down_arrow
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.vectorResource

const val ASPECT_RATIO = 0.8f
const val CONTAINER_PADDING = 7

@Composable
fun BoxScope.UIQueueContainer(
    getQueueBoxes: () -> List<NumberBox>,
    getSelectedSize: () -> Int = { -1 },
    getUpdatingPositions: () -> List<BoardPosition?> = { emptyList() },
    getLineLength: () -> Int,
    applyStarAnimation: () -> Brush?,
    reloadingCircle: @Composable () -> Unit,
) {
    val queueSize by remember {
        derivedStateOf{ getQueueBoxes().size }
    }
    UIQueueContainer(
        queueSize = queueSize,
        getSelectedSize = getSelectedSize,
    )
    UIQueueArrow()
    if(queueSize > 0){
        UIQueueContent(
            queueSize = queueSize,
            getQueueBoxes = getQueueBoxes,
            getUpdatingPositions = getUpdatingPositions,
            getLineLength = getLineLength,
            applyStarAnimation = applyStarAnimation,
        )
    }
    reloadingCircle()
}

@Composable
private fun UIQueueContainer(
    queueSize: Int,
    getSelectedSize: () -> Int,
) {
    val shape = Theme.shapes.softBlockShape
    val animatedValue = QueueContainerAnimatedValue(
        queueSize = queueSize,
        getSelectedSize = getSelectedSize,
    )

    val colorList = listOf(
        Theme.colors.primary,
        Theme.colors.grey,
        Theme.colors.selected,
    )

    //Queue container
    Box(modifier = Modifier
        .fillMaxSize()
        .aspectRatio(ASPECT_RATIO)
        .border(
            width = Theme.borders.mediumBorder,
            color = Theme.colors.grey,
            shape = shape,
        )
        .clip(shape = shape)
        .drawBehind {
            animatedValue.value.let{ greyStop ->
                drawRect(
                    brush = Brush.linearGradient(
                        colorStops = arrayOf(
                            0.0f to colorList[0],
                            (0.8f - greyStop) to colorList[1]
                                .combineOver(colorList[0].withAlpha(greyStop + 0.2f)),
                            1f to colorList[2]
                        )
                    )
                )
            }
        }
    )
}

@Composable
private fun QueueContainerAnimatedValue(
    queueSize: Int,
    getSelectedSize: () -> Int,
): State<Float> {
    val selectedSize = getSelectedSize()
    return animateFloatAsState(
        targetValue = if(queueSize > 0){
            when (selectedSize) {
                -1 -> -0.2f
                0 -> 0f
                queueSize -> 0.8f
                else -> (selectedSize * 7 / queueSize) / 10f
            }
        }else{
            -0.2f
        },
        animationSpec = if(selectedSize == -1) {
            tween(0)
        }else{
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "animatedQueueContainer"
    )
}

@Composable
private fun UIQueueContent(
    queueSize: Int,
    getQueueBoxes: () -> List<NumberBox>,
    getUpdatingPositions: () -> List<BoardPosition?>,
    getLineLength: () -> Int,
    applyStarAnimation: () -> Brush?,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(ASPECT_RATIO)
            .padding(CONTAINER_PADDING.dp)
    ) {
        //State data
        var currentQueue by remember{ mutableStateOf(getQueueBoxes()) }
        var travellingBox: TravellingBox? by remember { mutableStateOf(null) }

        LaunchedEffect(getQueueBoxes()){
            val updatingPositions = getUpdatingPositions().filterNotNull()
            val newQueue = getQueueBoxes()

            if(getUpdatingPositions().any{ it == null} ){
                for(index in updatingPositions.indices){
                    //Setting travellingBox
                    travellingBox = TravellingBox(
                        content = currentQueue.first(),
                        targetPosition = updatingPositions[index],
                    )
                    //Updating current queue
                    (newQueue.size - updatingPositions.size + index).let{ i ->
                        currentQueue = currentQueue.drop(n = 1) + newQueue[i]
                    }
                    //Complete animation
                    delay(TRAVEL_ACTION_DELAY)
                }
                travellingBox = null
            }else{
                currentQueue = newQueue
            }
        }

        //Style properties
        val propertiesList = getPropertiesList(queueSize)

        UIQueueNumbers(
            queueSize = queueSize,
            getQueueBoxes = { currentQueue },
            propertiesList = propertiesList,
            applyStarAnimation = applyStarAnimation,
        )
        UITravellingBox(
            getTravellingBox = { travellingBox },
            getLineLength = getLineLength,
            boxProperties = propertiesList.first(),
            containerWidthInPx = maxWidth.toPx(),
            applyStarAnimation = applyStarAnimation,
        )
    }
}

@Composable
private fun BoxScope.UIQueueArrow() =
    Image(
        imageVector = vectorResource(Res.drawable.down_arrow),
        contentDescription = "arrow",
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .aspectRatio(1f)
            .align(Alignment.BottomStart)
            .scale(0.7f)
    )


//QueueBox properties
private fun getPropertiesList(queueSize: Int): List<QueueBoxProperties> {
    return when (queueSize) {
        1 -> listOf(QueueBoxProperties.SingleBox)
        3 -> listOf(
            QueueBoxProperties.FirstOfThree,
            QueueBoxProperties.SecondOfThree,
            QueueBoxProperties.ThirdOfThree,
        )

        4 -> listOf(
            QueueBoxProperties.FirstOfFour,
            QueueBoxProperties.SecondOfFour,
            QueueBoxProperties.ThirdOfFour,
            QueueBoxProperties.FourthOfFour,
        )

        else -> listOf()
    }
}

sealed class QueueBoxProperties {
    //Position
    open fun getOffsetX(containerWidth: Float): Float = 0f
    open fun getOffsetY(containerWidth: Float): Float = 0f

    //Size
    protected abstract val index: Int
    protected val firstSize = 0.6f
    protected val sizeDiff = 0.05f
    open fun getSize() = firstSize - (sizeDiff * index)

    //SINGLE-BOX QUEUE
    object SingleBox : QueueBoxProperties() {
        override val index = 0
        override fun getOffsetX(containerWidth: Float): Float =
            containerWidth * 0.5f * (1 - getSize())

        override fun getOffsetY(containerWidth: Float): Float =
            (containerWidth / ASPECT_RATIO) * 0.5f - (containerWidth * getSize() * 0.5f)
    }

    //3-BOX QUEUE
    object FirstOfThree : QueueBoxProperties() {
        override val index = 0
        override fun getOffsetX(containerWidth: Float): Float =
            containerWidth * (1 - getSize())

        override fun getOffsetY(containerWidth: Float): Float =
            (containerWidth / ASPECT_RATIO) - (containerWidth * getSize())
    }

    object SecondOfThree : QueueBoxProperties() {
        override val index = 1
        override fun getOffsetY(containerWidth: Float): Float =
            (containerWidth / ASPECT_RATIO) * 0.5f - (containerWidth * getSize() * 0.5f)
    }

    object ThirdOfThree : QueueBoxProperties() {
        override val index = 2
        override fun getOffsetX(containerWidth: Float): Float =
            containerWidth * (1 - firstSize)
    }

    //4-BOX QUEUE
    //TODO Revisar offset relativos
    object FirstOfFour : QueueBoxProperties() {
        override val index = 0
        //override val alignment: Alignment = Alignment.BottomEnd
    }

    object SecondOfFour : QueueBoxProperties() {
        override val index = 1

        //override val alignment: Alignment = Alignment.BottomStart
        override fun getOffsetY(containerWidth: Float): Float = containerWidth * (firstSize / -3f)
    }

    object ThirdOfFour : QueueBoxProperties() {
        override val index = 2

        //override val alignment: Alignment = Alignment.TopStart
        override fun getOffsetX(containerWidth: Float): Float = containerWidth * (getSize() / 3f)
        override fun getOffsetY(containerWidth: Float): Float = containerWidth * (getSize() / 4f)
    }

    object FourthOfFour : QueueBoxProperties() {
        override val index = 3
        //override val alignment: Alignment = Alignment.TopEnd
    }
}