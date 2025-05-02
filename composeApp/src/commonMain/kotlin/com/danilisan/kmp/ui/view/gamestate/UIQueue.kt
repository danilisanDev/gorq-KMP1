package com.danilisan.kmp.ui.view.gamestate

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.danilisan.kmp.domain.action.gamestate.GameStateActionManager.Companion.BASE_ACTION_DELAY
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.ui.view.OffsetDp
import com.danilisan.kmp.ui.view.createRelativeShader
import com.danilisan.kmp.ui.view.combineOver
import com.danilisan.kmp.ui.view.withAlpha
import com.danilisan.kmp.ui.view.toIntOffset
import com.danilisan.kmp.ui.view.toPx
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.down_arrow
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.vectorResource

const val ASPECT_RATIO = 0.8f
const val CONTAINER_PADDING = 7
const val QUEUEBOX_ANIMATED_OFFSET = 0
const val QUEUEBOX_ANIMATED_SIZE = 1
const val TRAVELBOX_ANIMATED_OFFSET = 2
const val TRAVELBOX_ANIMATED_SIZE = 3
const val SHADOW_ANIMATED_ELEVATION = 4
const val SHADOW_ANIMATED_OFFSET = 5

//TODO: Separar en UIQueueContainer y UIQueueContent

@Composable
fun BoxScope.UIQueue(
    getQueueBoxes: () -> List<NumberBox>,
    getSelectedSize: () -> Int = { -1 },
    getTravellingBox: () -> NumberBox,
    getTargetPosition: () -> BoardPosition,
    getLineLength: () -> Int,
) {
    val coroutineScope = rememberCoroutineScope()
    val queueSize by remember {
        derivedStateOf{ getQueueBoxes().size }
    }
    UIQueueContainer(
        queueSize = queueSize,
        getSelectedSize = getSelectedSize,
    )
    UIQueueArrow(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .aspectRatio(3f)
            .align(Alignment.BottomCenter)
    )
    if(queueSize > 0){
        UIQueueContent(
            queueSize = queueSize,
            getQueueBoxes = getQueueBoxes,
            getTravellingBox = getTravellingBox,
            getTargetPosition = getTargetPosition,
            getLineLength = getLineLength,
            scopeProvider = { coroutineScope }
        )
    }
}

@Composable
private fun UIQueueContainer(
    queueSize: Int,
    getSelectedSize: () -> Int,
) {
    //println("Recomposicion de container")
    val shape = Theme.shapes.softBlockShape
    val animatedValue = QueueContainerAnimatedValue(
        queueSize = queueSize,
        getSelectedSize = getSelectedSize,
    )

    val colorList = listOf(
        Theme.colors.primary,
        Theme.colors.grey,
        createRelativeShader(
            bgColor = Theme.colors.selected,
            shaderColor = Theme.colors.secondary,
            index = getSelectedSize(),
            maxIndex = queueSize,
        ),
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
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "animatedQueueContainer"
    )
}

@Composable
private fun UIQueueContent(
    queueSize: Int,
    getQueueBoxes: () -> List<NumberBox>,
    getTravellingBox: () -> NumberBox,
    getTargetPosition: () -> BoardPosition,
    getLineLength: () -> Int,
    scopeProvider: () -> CoroutineScope
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(ASPECT_RATIO)
            .padding(CONTAINER_PADDING.dp)
    ) {
        val propertiesList = getPropertiesList(queueSize)

        UIQueueBoxes(
            queueSize = queueSize,
            getQueueBoxes = getQueueBoxes,
            propertiesList = propertiesList,
            containerWidth = maxWidth,
        )
        UITravellingBox(
            getTravellingBox = getTravellingBox,
            getTargetPosition = getTargetPosition,
            getLineLength = getLineLength,
            boxRelativeSize = propertiesList.first().getSize(),
            offset = propertiesList.first().let{
                OffsetDp(
                    x = it.offsetX(maxWidth),
                    y = it.offsetY(maxWidth),
                )
            },
            containerWidth = maxWidth,
            scopeProvider = scopeProvider,
        )
    }
}

//TODO: Eliminar Box intermedia
@Composable
private fun UIQueueArrow(modifier: Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        Image(
            imageVector = vectorResource(Res.drawable.down_arrow),
            contentDescription = "arrow",
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .padding(2.dp)
        )
    }
}

@Composable
private fun UIQueueBoxes(
    queueSize: Int,
    getQueueBoxes: () -> List<NumberBox>,
    propertiesList: List<QueueBoxProperties>,
    containerWidth: Dp,
) {
    val queueIndices by remember {
        derivedStateOf{
            getQueueBoxes().indices
        }
    }
    val shaderColor = Theme.colors.secondary.withAlpha(0.5f)
    if (propertiesList.size == queueSize) {
        for (index in queueIndices.reversed()) {
            //Animate offset and size
            QueueBoxesAnimatedValues(
                propertiesList = propertiesList,
                index = index,
                containerWidth = containerWidth,
                getNumberBox = { getQueueBoxes()[index] },
            ).let { animationValues ->
                BoxWithConstraints (
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .offset {
                            (animationValues[QUEUEBOX_ANIMATED_OFFSET]?.value as Offset?)
                                ?.toIntOffset()
                                ?: IntOffset(0,0)
                        }
                        .graphicsLayer {
                            (animationValues[QUEUEBOX_ANIMATED_SIZE]?.value as Float?)
                                ?.let{ animatedSize ->
                                    scaleX = animatedSize
                                    scaleY = animatedSize
                                }
                            transformOrigin = TransformOrigin(0f, 0f)
                        }
                ) {
                    UINumberBox(
                        getNumberBox = { getQueueBoxes()[index] },
                        boxSize = maxWidth,
                        applyShaderColor = {
                            val color = createShaderColor(
                                shaderColor,
                                propertiesList.first().getSize(),
                                (animationValues[QUEUEBOX_ANIMATED_SIZE]?.value as Float?)
                            )
                            Pair(color, false)
                        },
                    )
                }
            }
        }
    }
}

private fun createShaderColor(
    color: Color,
    firstSize: Float,
    animatedSize: Float?,
): Color{
    val alpha = animatedSize?.let{
        0f + (firstSize - animatedSize) * 2f
    } ?: 0f
    return color.withAlpha(alpha)
}


//Animations for QueueBoxes and TravellingBox
@Composable
private fun QueueBoxesAnimatedValues(
    propertiesList: List<QueueBoxProperties>,
    index: Int,
    containerWidth: Dp,
    getNumberBox: () -> NumberBox,
): Map<Int, Animatable<*, *>> {
    val box = getNumberBox()

    //Offset values
    val offsetValues =
        getInitialAndTargetOffset(
                propertiesList = propertiesList,
                index = index,
                containerWidth = containerWidth,
            )
    val initialOffset = offsetValues.first.toPx()
    val targetOffset = offsetValues.second.toPx()
    val offsetAnimation = Animatable(
        initialValue = initialOffset,
        typeConverter = Offset.VectorConverter
    )

    //Size values
    val sizeValues =
            getInitialAndTargetSize(
                propertiesList = propertiesList,
                index = index
            )
    val sizeAnimation = Animatable(
        initialValue = sizeValues.first,
    )

    //Animation of values
    val duration = remember{ BASE_ACTION_DELAY.toInt() }
    val delay = remember { duration / 5 }
    LaunchedEffect(box) {
        offsetAnimation.snapTo(initialOffset)
        offsetAnimation.animateTo(
            targetValue = targetOffset,
            animationSpec = tween(
                durationMillis = duration - (delay * index),
                delayMillis = delay * index,
                easing = EaseInOutCubic
            )
        )
    }
    LaunchedEffect(box) {
        sizeAnimation.animateTo(sizeValues.first)
        sizeAnimation.animateTo(
            targetValue = sizeValues.second,
            animationSpec = tween(
                durationMillis = duration - (delay * index),
                delayMillis = delay * index,
                easing = EaseInOutCubic
            )
        )
    }
    return mapOf(
        QUEUEBOX_ANIMATED_OFFSET to offsetAnimation,
        QUEUEBOX_ANIMATED_SIZE to sizeAnimation,
    )
}

//Size and offset calculations
private fun getInitialAndTargetSize(
    propertiesList: List<QueueBoxProperties>,
    index: Int,
): Pair<Float, Float> =
    Pair(
        first = propertiesList.getInitialProperties(index).getSize(),
        second = propertiesList[index].getSize(),
    )


private fun getInitialAndTargetOffset(
    propertiesList: List<QueueBoxProperties>,
    index: Int,
    containerWidth: Dp,
): Pair<OffsetDp, OffsetDp> =
    Pair(
        first = calculateOffsetValue(
            properties = propertiesList.getInitialProperties(index),
            containerWidth = containerWidth,
        ),
        second = calculateOffsetValue(
            properties = propertiesList[index],
            containerWidth = containerWidth,
        )
    )

private fun calculateOffsetValue(
    properties: QueueBoxProperties,
    containerWidth: Dp,
): OffsetDp = OffsetDp(
    x = properties.offsetX(containerWidth),
    y = properties.offsetY(containerWidth)
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

private fun List<QueueBoxProperties>.getInitialProperties(index: Int): QueueBoxProperties =
    when {
        index >= size -> this.first()
        index == lastIndex -> this[index]
        else -> this[index + 1]
    }

private sealed class QueueBoxProperties {
    //Position
    open fun offsetX(containerWidth: Dp): Dp = 0.dp
    open fun offsetY(containerWidth: Dp): Dp = 0.dp

    //Size
    protected abstract val index: Int
    protected val sizesList = listOf(0.6f, 0.55f, 0.5f, 0.45f)
    open fun getSize() = try {
        sizesList[index]
    } catch (e: IndexOutOfBoundsException) {
        sizesList.first()
    }

    //SINGLE-BOX QUEUE
    object SingleBox : QueueBoxProperties() {
        override val index = 0
        override fun offsetX(containerWidth: Dp): Dp =
            containerWidth * 0.5f * (1 - getSize())

        override fun offsetY(containerWidth: Dp): Dp =
            (containerWidth / ASPECT_RATIO) * 0.5f - (containerWidth * getSize() * 0.5f)
    }

    //3-BOX QUEUE
    object FirstOfThree : QueueBoxProperties() {
        override val index = 0
        override fun offsetX(containerWidth: Dp): Dp =
            containerWidth * (1 - getSize())

        override fun offsetY(containerWidth: Dp): Dp =
            (containerWidth / ASPECT_RATIO) - (containerWidth * getSize())
    }

    object SecondOfThree : QueueBoxProperties() {
        override val index = 1
        override fun offsetY(containerWidth: Dp): Dp =
            (containerWidth / ASPECT_RATIO) * 0.5f - (containerWidth * getSize() * 0.5f)
    }

    object ThirdOfThree : QueueBoxProperties() {
        override val index = 2
        override fun offsetX(containerWidth: Dp): Dp = containerWidth * (1 - sizesList[0])
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
        override fun offsetY(containerWidth: Dp): Dp = containerWidth * (sizesList[0] / -3f)
    }

    object ThirdOfFour : QueueBoxProperties() {
        override val index = 2

        //override val alignment: Alignment = Alignment.TopStart
        override fun offsetX(containerWidth: Dp): Dp = containerWidth * (sizesList[2] / 3f)
        override fun offsetY(containerWidth: Dp): Dp = containerWidth * (sizesList[2] / 4f)
    }

    object FourthOfFour : QueueBoxProperties() {
        override val index = 3
        //override val alignment: Alignment = Alignment.TopEnd
    }
}