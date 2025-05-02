package com.danilisan.kmp.ui.view.gamestate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.NumberBox.Companion.EMPTY_VALUE
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.ui.view.combineOver
import com.danilisan.kmp.ui.view.plus
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.star7
import org.jetbrains.compose.resources.vectorResource

const val CONTRAST = 0.3f

@Composable
fun UINumberBox(
    faceUp: Boolean = true,
    getNumberBox: () -> NumberBox,
    boxSize: Dp,
    applyBorderStyle: (BoxType) -> Brush? = { null },
    applyShaderColor: () -> Pair<Color, Boolean>? = { null },
    applyDarkBackground: () -> Boolean = { false },
) {
    println("Recomposition of UINumberBox")

    if(faceUp) {
        FaceUpBox(
            getNumberBox = getNumberBox,
            boxSize = boxSize,
            applyBorderStyle = applyBorderStyle,
            applyShaderColor = applyShaderColor,
        )
    }else{
        FaceDownBox(
            applyDarkBackground = applyDarkBackground
        )
    }
}

@Composable
private fun FaceUpBox(
    getNumberBox: () -> NumberBox,
    boxSize: Dp,
    applyBorderStyle: (BoxType) -> Brush?,
    applyShaderColor: () -> Pair<Color, Boolean>?,
){
    val numberBox by remember {
        derivedStateOf{
            getNumberBox()
        }
    }
    val boxType by remember{
        derivedStateOf{
            when (numberBox) {
                is NumberBox.BlockBox -> BoxType.BLOCK
                is NumberBox.RegularBox -> BoxType.REGULAR
                is NumberBox.GoldenStarBox -> BoxType.GOLDEN
                is NumberBox.SilverStarBox -> BoxType.SILVER
                else -> BoxType.EMPTY
            }
        }
    }

    //Impossible case
    if (boxType == BoxType.EMPTY) return

    //Shape
    val shapeList = listOf(
        Theme.shapes.regularShape,
        Theme.shapes.softBlockShape,
        Theme.shapes.roundShape,
    )
    val shape = when (boxType) {
        BoxType.REGULAR -> shapeList[0]
        BoxType.BLOCK -> shapeList[1]
        else -> shapeList[2]
    }

    //Delegated colors
    val colorList = listOf(
        Theme.colors.primary,
        Theme.colors.secondary,
        Theme.colors.grey,
    )
    val borderColors by remember {
        derivedStateOf {
            applyBorderStyle(boxType)
                ?: when (boxType) {
                    BoxType.REGULAR ->
                        colorList[2]
                    BoxType.BLOCK ->
                        colorList[0].combineOver(
                            other = colorList[1],
                            alpha = CONTRAST
                        )
                    else -> null
                }
        }
    }
    val borderWidthList = listOf(
        Theme.borders.regularBorder(boxSize),
        Theme.borders.blockBorder(boxSize),
    )
    val borderWidth = when (boxType) {
        BoxType.REGULAR -> borderWidthList[0]
        BoxType.BLOCK -> borderWidthList[1]
        else -> null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .padding(1.dp)
            .boxBackground(
                boxType = boxType,
                shape = shape,
            )
            .boxBorder(
                borderWidth = borderWidth,
                borderColor = borderColors,
                shape = shape,
            )
            .boxShader(
                shape = shape,
                shaderColor = { applyShaderColor()?.first }
            )
        ,
        contentAlignment = Alignment.Center,
    ) {
        //Star background icon
        if (boxType.isStar) {
            //TODO: Crear pequeño halo brillante alrededor
            Image(
                imageVector = vectorResource(Res.drawable.star7),
                contentDescription = "star",
                modifier = Modifier
                    .matchParentSize()
            )
        }

        //Number value
        if (numberBox.value != EMPTY_VALUE) {
            TextNumberForUIBox(
                boxSize = boxSize,
                value = numberBox.value,
                applyWhiteText = {
                    boxType != BoxType.REGULAR
                            || (applyShaderColor()?.second ?: false)
                }
            )
        }

        //TODO: Reflect animation (franja diagonal animada)
    }
}

@Composable
private fun FaceDownBox(
    applyDarkBackground: () -> Boolean,
) {
    val shape = Theme.shapes.softBlockShape
    val darkBackground = remember { applyDarkBackground() }
    Box(Modifier
        .fillMaxSize()
        .aspectRatio(1f)
        .background(
            color = if(darkBackground){
                Theme.colors.secondary.combineOver(
                    other = Theme.colors.primary,
                    alpha = CONTRAST
                )
            }else{
                Theme.colors.secondary.combineOver(
                    other = Theme.colors.primary,
                    alpha = CONTRAST * 2
                )
            },
            shape = shape,
        )
        .border(
            width = Theme.borders.mediumBorder,
            color = Theme.colors.primary,
            shape = shape,
        )
    )
}


@Composable
private fun TextNumberForUIBox(
    boxSize: Dp,
    value: Int,
    applyWhiteText: () -> Boolean,
) {
    val isWhiteText by remember {
        derivedStateOf {
            applyWhiteText()
        }
    }

    val valueString = when {
        value < 0 -> "$value "
        value > 9 -> value.toChar().toString()
        value == EMPTY_VALUE -> "" //Impossible case
        else -> "$value"
    }
    val fontSizeDiv = when {
        value < 0 -> 2.2f               //Negative values
        value in 10..96 -> 1.9f   //Upper case letters
        value > 96 -> 1.4f              //Lower case letters
        else -> 1.9f                    //Positive numbers
    }
    val textColor = if (isWhiteText) {
        Theme.colors.primary.combineOver(
            other = Theme.colors.secondary,
            alpha = CONTRAST,
        )
    } else {
        Theme.colors.secondary.combineOver(
            other = Theme.colors.primary,
            alpha = CONTRAST,
        )
    }

    Text(
        text = valueString,
        color = textColor,
        //fontFamily = FontFamily(Font(Res.font.Quantico_Bold)),
        fontWeight = FontWeight.Bold,
        fontSize = with(LocalDensity.current) {
            boxSize.toSp() / fontSizeDiv
        },
    )
}

@Composable
private fun Modifier.boxShader(
    shape: Shape,
    shaderColor: () -> Color?
): Modifier = this
    .clip(shape)
    .drawBehind {
        shaderColor()?.let{
            drawRect(it)
        }
    }

@Composable
private fun Modifier.boxBorder(
    borderWidth: Dp?,
    borderColor: Any?,
    shape: Shape,
): Modifier = if(borderWidth == null){
    this
}else{
    when(borderColor){
        is Color -> {
            this.border(
                width = borderWidth,
                color = borderColor,
                shape = shape
            )
        }
        is Brush -> {
            this.border(
                border = BorderStroke(
                    width = borderWidth,
                    brush = borderColor,
                ),
                shape = shape
            )
        }
        else -> this //Impossible
    }
}

@Composable
private fun Modifier.boxBackground(
    boxType: BoxType,
    shape: Shape,
): Modifier = this.background(
    brush = boxType.getBgGradient(),
    shape = shape
)

enum class BoxType(val isStar: Boolean = false) {
    REGULAR,
    BLOCK,
    GOLDEN(isStar = true),
    SILVER(isStar = true),
    EMPTY
}

@Composable
private fun BoxType.getBgGradient(): Brush = when (this) {
    BoxType.GOLDEN -> Brush.sweepGradient(
        Theme.colors.starGradient.map { it + Theme.colors.golden }
    )

    BoxType.SILVER -> Brush.sweepGradient(
        Theme.colors.starGradient.map { it + Theme.colors.grey }
    )

    BoxType.BLOCK -> {
        val bgColor = Theme.colors.secondary
            .combineOver(
                other = Theme.colors.primary,
                alpha = CONTRAST
            )
        Brush.linearGradient(listOf(bgColor, bgColor))
    }

    else -> Brush.linearGradient(
        Theme.colors.regularGradient.map { it + Theme.colors.primary }
    )
}