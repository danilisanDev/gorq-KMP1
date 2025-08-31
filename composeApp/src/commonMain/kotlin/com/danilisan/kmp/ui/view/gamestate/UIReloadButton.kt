package com.danilisan.kmp.ui.view.gamestate

import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.danilisan.kmp.ui.state.BoardState
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.ui.view.combineOver
import com.danilisan.kmp.ui.view.plus
import com.danilisan.kmp.ui.view.toSp
import com.danilisan.kmp.ui.view.withAlpha
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.bingoButton
import kotlinproject.composeapp.generated.resources.gameOverButton
import kotlinproject.composeapp.generated.resources.refresh
import kotlinproject.composeapp.generated.resources.reloadMultiplierButton

import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun UIReloadButton(
    getBoardState: () -> BoardState,
    getReloadCost: (BoardState) -> Int,
    isEnabled: () -> Boolean,
    addButtonAction: Modifier.(() -> Boolean) -> Modifier,
) {
    val transition = rememberInfiniteTransition()
    val properties = getProperties(getBoardState, getReloadCost, isEnabled)
    val outsetBorder = BorderStroke(
        width = Theme.borders.mediumBorder,
        brush = Brush.linearGradient(
            colors = Theme.colors.outsetGradient
                .map { it + properties.borderColor },
        )
    )
    val pulseColor = Theme.colors.primary
    val pulseAnimation =  getBoardState()
        .takeIf{ it == BoardState.BLOCKED || it == BoardState.BINGO }
        ?.let{
            getPulseAnimation{ transition }
        }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f)
            .background(
                color = properties.primaryColor,
                shape = properties.shape
            )
            .border(
                border = outsetBorder,
                shape = properties.shape
            )
            .clip(
                shape = properties.shape
            )
            .addButtonAction(isEnabled)
            .drawWithContent{
                drawContent()
                if(pulseAnimation != null){
                    drawRect(
                        color = pulseColor.withAlpha(pulseAnimation.value)
                    )
                }
            }
        ,
        contentAlignment = Alignment.Center,
    ){
        val boxSize = maxHeight
        Row(
            horizontalArrangement = Arrangement.Center
        ) {
            if(properties.hasIcon){
                ReloadIcon(properties.textColor)
            }

            val textStyle = if(getBoardState() == BoardState.BINGO){
                TextStyle(
                    color = Theme.colors.golden
                )
            }else{
                TextStyle(
                    color = properties.textColor
                )
            }
            Text(
                text = properties.text,
                color = properties.textColor,
                fontSize = (boxSize / properties.textSizeDiv).toSp(),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                style = textStyle
            )
        }
    }
}

@Composable
private fun ReloadIcon(tint: Color){
    Image(
        imageVector = vectorResource(Res.drawable.refresh),
        contentDescription = "refresh",
        modifier = Modifier
            .fillMaxHeight(0.7f)
            .aspectRatio(1f),
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(tint)
    )
    Spacer(modifier = Modifier.width(5.dp))
}

@Composable private fun getPulseAnimation(
    transitionProvider: () -> InfiniteTransition,
): State<Float> {
    val blinkInterval = 140
    val initialValue = 0f
    val targetValue = 0.25f
    val duration = 4500
    return transitionProvider().animateFloat(
        initialValue = initialValue,
        targetValue = initialValue,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = duration
                initialValue at blinkInterval * 2
                targetValue at blinkInterval * 4
                initialValue at blinkInterval * 5
                targetValue at blinkInterval * 7
                initialValue at blinkInterval * 8
            },
        )
    )
}

@Composable
private fun getProperties(
    getBoardState: () -> BoardState,
    getReloadCost: (BoardState) -> Int,
    isEnabled: () -> Boolean,
): ReloadBtnProperties = getBoardState().let{ boardState ->
     when(boardState){
        BoardState.READY -> ReloadBtnProperties(
            shape = Theme.shapes.softBlockShape,
            primaryColor = Theme.colors.primary,
            borderColor = if(isEnabled()){
                Theme.colors.selected
            }else{
                Theme.colors.secondary.withAlpha(0.4f)
            },
            hasIcon = true,
            text = stringResource(Res.string.reloadMultiplierButton) + "${getReloadCost(boardState)}",
            textColor = if(isEnabled()){
                Theme.colors.secondary.withAlpha(0.9f)
            }else{
                Theme.colors.secondary.withAlpha(0.4f)
            }
        )
        BoardState.BINGO -> ReloadBtnProperties(
            shape = Theme.shapes.roundShape,
            primaryColor = Theme.colors.success,
            borderColor = Theme.colors.display,
            hasIcon = false,
            text = stringResource(Res.string.bingoButton),
            textColor = Theme.colors.display
        )
        BoardState.BLOCKED -> ReloadBtnProperties(
            shape = Theme.shapes.hardBlockShape,
            primaryColor = Theme.colors.secondary
                .combineOver(
                    other = Theme.colors.primary,
                    alpha = 0.4f
                ),
            borderColor = Theme.colors.error,
            hasIcon = true,
            text = stringResource(Res.string.reloadMultiplierButton) + "${getReloadCost(boardState)}",
            textColor = Theme.colors.primary
        )
        BoardState.GAMEOVER -> ReloadBtnProperties(
            shape = Theme.shapes.regularShape,
            primaryColor = Theme.colors.selected.withAlpha(0.8f),
            borderColor = Theme.colors.primary,
            textSizeDiv = 4f,
            hasIcon = false,
            text = stringResource(Res.string.gameOverButton),
            textColor = Theme.colors.primary
        )
    }
}

private class ReloadBtnProperties(
    val shape: Shape,
    val hasIcon: Boolean,
    val text: String,
    val textSizeDiv: Float = 3.5f,
    val primaryColor: Color,
    val borderColor: Color,
    val textColor: Color,
)