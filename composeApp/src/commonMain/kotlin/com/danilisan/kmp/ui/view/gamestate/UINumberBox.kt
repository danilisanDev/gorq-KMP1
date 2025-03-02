package com.danilisan.kmp.ui.view.gamestate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.NumberBox.Companion.EMPTY_VALUE
import com.danilisan.kmp.ui.state.BoardState
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.ui.theme.combineOver
import com.danilisan.kmp.ui.theme.plus
import com.danilisan.kmp.ui.theme.withAlpha
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.star
import org.jetbrains.compose.resources.vectorResource

@Composable
fun UINumberBox(
    numberBox: NumberBox,
    position: Int = 0,
    tintColor: Color = Color.Transparent,
    state: BoardState = BoardState.BLOCKED,
    boardPosition: BoardPosition? = null,
    isEnabled: Boolean = true,
    selectAction: (BoardPosition) -> Unit = {}
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .padding(1.dp)
    ) {
        val boxSize = maxWidth
        val tintFilter = (position.takeUnless { it < 0 } ?: 0)
            .let { pos ->
                Theme.colors.secondary.withAlpha(pos * 0.1f)
            }

        when (numberBox) {
            is NumberBox.RegularBox -> UIRegularBox(
                value = numberBox.value,
                tintColor = (tintColor.takeUnless { it == Color.Transparent }
                    ?: Theme.colors.primary),
                tintFilter = tintFilter,
                boxSize = boxSize,
                state = state,
                boardPosition = boardPosition,
                isEnabled = isEnabled,
                selectAction = selectAction
            )

            is NumberBox.BlockBox -> UIBlockBox(
                value = numberBox.value,
                tintColor = tintColor + tintFilter,
                boxSize = boxSize,
            )

            is NumberBox.StarBox -> UIStarBox(
                isGolden = numberBox is NumberBox.GoldenStarBox,
                tintFilter = tintColor + tintFilter,
                boxSize = boxSize,
                value = numberBox.value,
            )

            is NumberBox.EmptyBox -> UIEmptyBox(
                boxSize = boxSize,
                position = boardPosition,
            )
        }
    }
}

@Composable
private fun TextNumberForUIBox(
    boxSize: Dp,
    value: Int,
    whiteColor: Boolean = false,
) {
    if (value != EMPTY_VALUE) {
        val valueString = if(value < 0){
            "$value "
        }else{
            "$value"
        }
        Text(
            text = "$value" + if(value < 0) " " else "",
            color = if (!whiteColor) {
                Theme.colors.secondary.withAlpha(0.7f)
            } else {
                Theme.colors.primary.withAlpha(0.7f)
            },
            fontWeight = FontWeight.ExtraBold,
            fontSize = with(LocalDensity.current) {
                boxSize.toSp() / (if(value < 0) 2.1 else 1.7) },
        )
    }
}

@Composable
private fun UIEmptyBox(
    boxSize: Dp,
    position: BoardPosition?,
) {
    val bgColor = if (position?.getRightDiagonalParity() == true) {
        Theme.colors.secondary.withAlpha(0.5f)
    } else {
        Theme.colors.secondary.withAlpha(0.2f)
    }
    val shape = Theme.shapes.softBlockShape

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = bgColor, shape = shape)
            .border(
                width = Theme.borders.regularBorder(boxSize),
                color = Theme.colors.primary,
                shape = shape
            )
    )
}

@Composable
private fun UIRegularBox(
    value: Int,
    tintColor: Color,
    tintFilter: Color,
    boxSize: Dp,
    state: BoardState,
    boardPosition: BoardPosition?,
    isEnabled: Boolean,
    selectAction: (BoardPosition) -> Unit,
) {
    val boxModifier = RegularBoxModifier(
        tintColor = tintColor,
        tintFilter = tintFilter,
        borderWidth = Theme.borders.regularBorder(boxSize),
        state = state,
    ).run {
        if (boardPosition != null && isEnabled) {
            clickable {
                selectAction(boardPosition)
            }
        } else {
            this
        }
    }
    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        TextNumberForUIBox(
            boxSize = boxSize,
            value = value,
            whiteColor = tintColor == Theme.colors.selected
        )
    }
}

@Composable
private fun RegularBoxModifier(
    tintColor: Color,
    tintFilter: Color,
    borderWidth: Dp,
    state: BoardState,
): Modifier {
    val shape = Theme.shapes.regularShape
    val borderGradient = Brush.linearGradient(
        when (tintColor) {
        Theme.colors.selected ->
            Theme.colors.insetGradient
        Theme.colors.primary -> if(state == BoardState.READY){
            Theme.colors.outsetGradient
        }else{
            listOf(Theme.colors.grey,Theme.colors.grey)
        }
        else ->
            listOf(tintColor, tintColor)
        }
    )
    val borderColor =
        when (state) {
            BoardState.READY -> {
                Theme.colors.selected
            }
            BoardState.BINGO -> {
                Theme.colors.success
            }
            else -> {
                Theme.colors.grey
            }
        }
    val bgGradient = Brush.linearGradient(
        Theme.colors.regularGradient
    )

    return Modifier
        .fillMaxSize()
        .background(
            //Solid color
            color = tintColor + tintFilter,
            shape = shape,
        )
        .background(
            //Filtered gradient
            brush = bgGradient,
            shape = shape,
        )
        .clip(shape = shape)
        .border(
            //Border gradient
            border = BorderStroke(borderWidth, borderGradient),
            shape = shape,
        )
        .border(
            width = borderWidth,
            color = borderColor + tintFilter,
            shape = shape,
        )
}

@Composable
private fun UIBlockBox(
    value: Int,
    tintColor: Color,
    boxSize: Dp,
) {
    val boxModifier = BlockBoxModifier(
        tintColor = tintColor,
        borderWidth = Theme.borders.blockBorder(boxSize)
    )
    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        TextNumberForUIBox(
            boxSize = boxSize,
            value = value,
            whiteColor = true,
        )
    }
}

@Composable
private fun BlockBoxModifier(
    tintColor: Color,
    borderWidth: Dp
): Modifier {
    val shape = Theme.shapes.softBlockShape
    return Modifier
        .fillMaxSize()
        .background(
            color = Theme.colors.secondary
                .combineOver(
                    other = Theme.colors.primary,
                    alpha = 0.4f
                )
                .combineOver(tintColor),
            shape = shape,
        )
        .border(
            width = borderWidth,
            color = Theme.colors.primary.withAlpha(0.7f) + tintColor,
            shape = shape
        )
}

@Composable
private fun UIStarBox(
    isGolden: Boolean,
    boxSize: Dp,
    tintFilter: Color,
    value: Int,
) {
    val boxModifier = StarBoxModifier(
        isGolden = isGolden,
        tintFilter = tintFilter,
        shape = Theme.shapes.roundShape,
    )
    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
            imageVector = vectorResource(Res.drawable.star),
            contentDescription = "star",
            modifier = Modifier
                .matchParentSize()
        )
        if(value != EMPTY_VALUE) {
            TextNumberForUIBox(
                boxSize = boxSize,
                value = value,
            )
        }
    }
}

@Composable
private fun StarBoxModifier(
    isGolden: Boolean,
    tintFilter: Color,
    shape: Shape,
): Modifier {
    //TODO Animacion de reflejo
    val bgColor = if(tintFilter == Theme.colors.success){
        tintFilter
    }else{
        if (isGolden) {
            Theme.colors.golden
        } else {
            Theme.colors.grey
        }.combineOver(tintFilter)
    }

    return Modifier
        .fillMaxSize()
        .background(
            color = bgColor,
            shape = shape,
        )
        .background(
            brush = Brush.sweepGradient(Theme.colors.starGradient),
            shape = shape,
        )
}