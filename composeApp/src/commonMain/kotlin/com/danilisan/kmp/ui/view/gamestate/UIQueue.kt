package com.danilisan.kmp.ui.view.gamestate

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.ui.theme.withAlpha

@Composable
fun UIQueue(
    queueNumbers: List<NumberBox>
) {
    val queueSize = queueNumbers.size
    val shape = Theme.shapes.softBlockShape
    //RoundedCornerShape(20.dp)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(0.8f)
            .background(
                brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0.0f to Theme.colors.primary,
                        0.4f to Theme.colors.grey,
                        1f to Theme.colors.selected
                    )
                ),
                shape = shape,
            )
            .border(
                width = Theme.borders.mediumBorder,
                color = Theme.colors.grey,
                shape = shape,
            )
            .padding(7.dp)
    ) {
        val boxSize = maxWidth
        val propertiesList: List<QueueBoxProperties> = getPropertiesList(queueSize)

        if (propertiesList.size == queueSize) {
            for (i in queueNumbers.indices) {
                val index = queueSize - 1 - i
                val properties = propertiesList[index]

                Box(
                    modifier = Modifier
                        .fillMaxWidth(properties.size)
                        .aspectRatio(1f)
                        .align(properties.alignment)
                        .offset(
                            x = properties.offsetX(boxSize),
                            y = properties.offsetY(boxSize)
                        )
                ) {
                    UINumberBox(
                        numberBox = queueNumbers[index],
                        position = index,
                    )
                }
            }
        }
    }
}

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

private sealed class QueueBoxProperties {
    abstract val size: Float
    abstract val alignment: Alignment
    open val offsetX: (Dp) -> Dp = { 0.dp }
    open val offsetY: (Dp) -> Dp = { 0.dp }
    protected val size1 = 0.6f
    protected val size2 = 0.55f
    protected val size3 = 0.5f
    protected val size4 = 0.45f

    //SINGLE-BOX QUEUE
    object SingleBox : QueueBoxProperties() {
        override val size: Float = size1
        override val alignment: Alignment = Alignment.Center
    }

    //3-BOX QUEUE
    object FirstOfThree : QueueBoxProperties() {
        override val size: Float = size1
        override val alignment: Alignment = Alignment.BottomEnd
    }

    object SecondOfThree : QueueBoxProperties() {
        override val size: Float = size2
        override val alignment: Alignment = Alignment.CenterStart
    }

    object ThirdOfThree : QueueBoxProperties() {
        override val size: Float = size3
        override val alignment: Alignment = Alignment.TopStart
        override val offsetX: (Dp) -> Dp = { boxSize -> boxSize * (1 - size1) }
    }

    //4-BOX QUEUE
    object FirstOfFour : QueueBoxProperties() {
        override val size: Float = size1
        override val alignment: Alignment = Alignment.BottomEnd
    }

    object SecondOfFour : QueueBoxProperties() {
        override val size: Float = size2
        override val alignment: Alignment = Alignment.BottomStart
        override val offsetY: (Dp) -> Dp = { boxSize -> boxSize * (size1 / -3f) }
    }

    object ThirdOfFour : QueueBoxProperties() {
        override val size: Float = size3
        override val alignment: Alignment = Alignment.TopStart
        override val offsetX: (Dp) -> Dp = { boxSize -> boxSize * (size3 / 3f) }
        override val offsetY: (Dp) -> Dp = { boxSize -> boxSize * (size3 / 4f) }
    }

    object FourthOfFour : QueueBoxProperties() {
        override val size: Float = size4
        override val alignment: Alignment = Alignment.TopEnd

    }
}