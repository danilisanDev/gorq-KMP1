package com.danilisan.kmp.ui.view.gamestate

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.danilisan.kmp.domain.action.gamestate.GameStateActionManager.Companion.TOTAL_ACTION_DELAY
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.ui.view.combineOver
import com.danilisan.kmp.ui.view.withAlpha
import com.danilisan.kmp.ui.view.toIntOffset
import com.danilisan.kmp.ui.view.toPx
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.down_arrow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.vectorResource

const val ASPECT_RATIO = 0.8f
const val CONTAINER_PADDING = 7
const val QUEUEBOX_ANIMATED_OFFSET = 0
const val QUEUEBOX_ANIMATED_SIZE = 1
const val TRAVELBOX_ANIMATED_OFFSET = 2
const val TRAVELBOX_ANIMATED_SIZE = 3
const val TRAVELBOX_ANIMATED_SHADOW = 4

@Composable
fun UIQueue(
    getQueueSize: () -> Int,
    getQueueBoxes: () -> List<NumberBox>,
    getSelectedSize: () -> Int = { -1 },
    getTravellingBox: () -> NumberBox,
    getTargetPosition: () -> BoardPosition,
    getLineLength: () -> Int,
) {
    getQueueSize()
        .takeIf{ it > 0 }
        ?.let{ queueSize ->
            val coroutineScope = rememberCoroutineScope()
            UIQueueContainer(
                queueSize = queueSize,
                getSelectedSize = getSelectedSize,
            )
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
    println("Recomposicion de container")
    val shape = Theme.shapes.softBlockShape
    val selectedSize = getSelectedSize()

    //Animated coloring
    val greyStop: Float by animateFloatAsState(
        when (selectedSize) {
            -1 -> -0.2f
            0 -> 0f
            queueSize -> 0.8f
            else -> (selectedSize * 7 / queueSize) / 10f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "greyStop"
    )
    val colorList = listOf(
        Theme.colors.primary,
        Theme.colors.grey,
        Theme.colors.selected
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
        val propertiesList by remember { mutableStateOf(getPropertiesList(queueSize)) }
        UIQueueArrow(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(3f)
                .align(Alignment.BottomCenter)
        )
        UIQueueBoxes(
            queueSize = queueSize,
            getQueueBoxes = getQueueBoxes,
            propertiesList = propertiesList,
            containerWidth = maxWidth,
            scopeProvider = scopeProvider,
        )
        UITravellingBox(
            getTravellingBox = getTravellingBox,
            getTargetPosition = getTargetPosition,
            getLineLength = getLineLength,
            properties = propertiesList.first(),
            containerWidth = maxWidth,
            scopeProvider = scopeProvider,
        )
    }
}

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
        )
    }
}

@Composable
private fun UIQueueBoxes(
    queueSize: Int,
    getQueueBoxes: () -> List<NumberBox>,
    propertiesList: List<QueueBoxProperties>,
    containerWidth: Dp,
    scopeProvider: () -> CoroutineScope,
) {
    val totalDuration = TOTAL_ACTION_DELAY.toInt() / queueSize

    val queueBoxes = getQueueBoxes()
    println("Recomposicion de cajas: $queueBoxes")

    if (propertiesList.size == queueSize) {
        for (index in queueBoxes.indices.reversed()) {
            //Animate offset and size
            QueueBoxesAnimatedValues(
                propertiesList = propertiesList,
                index = index,
                containerWidth = containerWidth,
                scopeProvider = scopeProvider,
            ).let { animationValues ->
                Box(
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
                        numberBox = queueBoxes[index],
                        position = index,
                    )
                }
            }
        }
    }
}

@Composable
private fun UITravellingBox(
    getTravellingBox: () -> NumberBox,
    getTargetPosition: () -> BoardPosition,
    getLineLength: () -> Int,
    properties: QueueBoxProperties,
    containerWidth: Dp,
    scopeProvider: () -> CoroutineScope,
) {
    val travellingBox = getTravellingBox()
    val targetPosition = getTargetPosition()
    val lineLength = getLineLength()

    if (travellingBox !is NumberBox.EmptyBox
        && targetPosition.isValidPosition(lineLength)
    ) {
        println("IN Travelling box")

        val shadowShape = when (travellingBox) {
            is NumberBox.BlockBox -> Theme.shapes.softBlockShape
            is NumberBox.RegularBox -> Theme.shapes.regularShape
            else -> Theme.shapes.roundShape
        }

        TravellingBoxAnimatedValues(
            properties = properties,
            containerWidth = containerWidth,
            targetPosition = targetPosition,
            lineLength = lineLength,
            scopeProvider = scopeProvider,
        ).let { animationValues ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .offset {
                        (animationValues[TRAVELBOX_ANIMATED_OFFSET]?.value as Offset?)
                            ?.toIntOffset()
                            ?: IntOffset(0, 0)
                    }
                    .graphicsLayer {
                        (animationValues[TRAVELBOX_ANIMATED_SIZE]?.value as Float?)
                            ?.let { animatedSize ->
                                scaleX = animatedSize
                                scaleY = animatedSize
                            }
                        transformOrigin = TransformOrigin(0f, 0f)
                        (animationValues[TRAVELBOX_ANIMATED_SHADOW]?.value as Float?)
                            ?.let { animatedShadow ->
                                shadowElevation = animatedShadow
                            }
                        shape = shadowShape
                    }
            ) {
                UINumberBox(
                    numberBox = travellingBox,
                )
            }
        }
    }
}

//Animations for QueueBoxes and TravellingBox
@Composable
private fun QueueBoxesAnimatedValues(
    propertiesList: List<QueueBoxProperties>,
    index: Int,
    containerWidth: Dp,
    scopeProvider: () -> CoroutineScope,
): Map<Int, Animatable<*, *>> {
    //Initial and target values
    val offsetValues = getInitialAndTargetOffset(
        propertiesList = propertiesList,
        index = index,
        containerWidth = containerWidth,
    )
    val sizeValues = getInitialAndTargetSize(
        propertiesList, index
    )
    val offsetAnimation = Animatable(
        initialValue = offsetValues.first,
        typeConverter = Offset.VectorConverter
    )
    val sizeAnimation = Animatable(
        initialValue = sizeValues.first,
    )

    //Animation of values
    scopeProvider().launch {
        println("Animating queue numbers")
        offsetAnimation.animateTo(
            targetValue = offsetValues.second,
            animationSpec = tween(
                durationMillis = 450 - (70 * index),
                delayMillis = 70 * index,
                easing = EaseInOutCubic
            )
        )
    }
    scopeProvider().launch {
        sizeAnimation.animateTo(
            targetValue = sizeValues.second,
            animationSpec = tween(
                durationMillis = 450 - (70 * index),
                delayMillis = 70 * index,
                easing = EaseInOutCubic
            )
        )
    }
    return mapOf(
        QUEUEBOX_ANIMATED_OFFSET to offsetAnimation,
        QUEUEBOX_ANIMATED_SIZE to sizeAnimation,
    )
}

@Composable
private fun TravellingBoxAnimatedValues(
    properties: QueueBoxProperties,
    containerWidth: Dp,
    lineLength: Int,
    targetPosition: BoardPosition,
    scopeProvider: () -> CoroutineScope,
): Map<Int, Animatable<*,*>> {
    //TODO Abstraer estos calculos a GameScreen OR UICommons, con las constantes de medidas
    val boxHeight = containerWidth / ASPECT_RATIO
    val boardSide = (containerWidth + CONTAINER_PADDING.dp * 2) / 0.36f
    val marginX = containerWidth / -8f
    val boardMargin = boardSide * 0.1f
    val boardBoxSize = boardSide * 0.8f / lineLength

    //Size values
    val initialSize = properties.getSize()
    val targetSize = boardBoxSize / containerWidth
    val middleSize = targetSize + 0.15f

    val sizeAnimation = Animatable(
        initialValue = initialSize,
    )

    //Offset values
    val initialOffset = calculateOffsetValue(
        properties,
        containerWidth,
    )
    val targetOffset = targetPosition.let { position ->
        val xOffset = marginX - 10.dp + boardMargin + (boardBoxSize * position.column)
        //10.dp = PADDING (7.dp) + MEDIUM_BORDER (3.dp)
        val yOffset = boxHeight + 22.dp + boardMargin + (boardBoxSize * position.row)
        //22.dp = (PADDING (7.dp) + MEDIUM_BORDER (3.dp) + 1.dp) * 2
        Offset(
            x = xOffset.toPx(),
            y = yOffset.toPx(),
        )
    }
    val offsetAnimation = Animatable(
        initialValue = initialOffset,
        typeConverter = Offset.VectorConverter
    )

    //Elevation values
    val shadowAnimation = Animatable(
        initialValue = 0f,
    )

    //Animate values
    scopeProvider().launch {
        offsetAnimation.animateTo(
            targetValue = targetOffset,
            animationSpec = tween(
                durationMillis = 400,
                easing = EaseInOutCubic
            )
        )
    }
    scopeProvider().launch {
        sizeAnimation.animateTo(
            targetValue = middleSize,
            animationSpec = tween(
                durationMillis = 150,
                easing = EaseInOutCubic
            )
        )
        sizeAnimation.animateTo(
            targetValue = targetSize,
            animationSpec = tween(
                durationMillis = 250,
                easing = EaseInOutCubic
            )
        )
    }
    scopeProvider().launch {
        shadowAnimation.animateTo(
            targetValue = 21f,
            animationSpec = tween(
                durationMillis = 150,
                easing = EaseInOutCubic
            )
        )
        shadowAnimation.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = 250,
                easing = EaseInOutCubic
            )
        )
    }

    return mapOf(
        TRAVELBOX_ANIMATED_OFFSET to offsetAnimation,
        TRAVELBOX_ANIMATED_SIZE to sizeAnimation,
        TRAVELBOX_ANIMATED_SHADOW to shadowAnimation,
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

@Composable
private fun getInitialAndTargetOffset(
    propertiesList: List<QueueBoxProperties>,
    index: Int,
    containerWidth: Dp,
): Pair<Offset, Offset> =
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

@Composable
private fun calculateOffsetValue(
    properties: QueueBoxProperties,
    containerWidth: Dp,
): Offset = Offset(
    x = properties.offsetX(containerWidth).toPx(),
    y = properties.offsetY(containerWidth).toPx()
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