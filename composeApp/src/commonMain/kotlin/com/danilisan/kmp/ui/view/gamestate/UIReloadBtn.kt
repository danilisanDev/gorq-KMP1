package com.danilisan.kmp.ui.view.gamestate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.danilisan.kmp.ui.state.BoardState
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.ui.theme.withAlpha
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.refresh
import org.jetbrains.compose.resources.vectorResource

@Composable
fun UIReloadButton(
    boardState: BoardState,
    reloadCost: Int,
    isEnabled: Boolean,
    buttonAction: () -> Unit = { },
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f)
    ) {
        val properties = getProperties(boardState, reloadCost, isEnabled)
        val outsetBorder = BorderStroke(
            Theme.borders.mediumBorder,
            Brush.linearGradient(
                Theme.colors.outsetGradient
            )
        )
        val boxSize = maxHeight

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = properties.primaryColor,
                    shape = properties.shape
                )
                .border(
                    border = outsetBorder,
                    shape = properties.shape
                )
                .border(
                    width = Theme.borders.mediumBorder,
                    color = properties.borderColor,
                    shape = properties.shape
                )
                .clip(
                    shape = properties.shape
                )
                .clickable(
                    onClick = buttonAction,
                    enabled = isEnabled,
                ),
            contentAlignment = Alignment.Center,
        ){
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if(properties.hasIcon){
                    ReloadIcon(properties.textColor)
                }
                val fontSize = with(LocalDensity.current) { boxSize.toSp() / 3.5}
                val textStyle = if(boardState == BoardState.BINGO){
                    TextStyle(
                        color = Theme.colors.golden
                    )
                }else{
                    TextStyle(
                        color = properties.borderColor
                    )
                }
                Text(
                    text = properties.text,
                    color = properties.textColor,
                    fontSize = fontSize,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    style = textStyle
                )
            }
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

@Composable
private fun getProperties(
    boardState: BoardState,
    reloadCost: Int,
    isEnabled: Boolean,
): ReloadBtnProperties{
    return when(boardState){
        BoardState.READY -> ReloadBtnProperties(
            shape = Theme.shapes.softBlockShape,
            primaryColor = Theme.colors.primary,
            borderColor = if(isEnabled){
                Theme.colors.selected
            }else{
                Theme.colors.secondary.withAlpha(0.4f)
            },
            hasIcon = true,
            text = "x${reloadCost}",
            textColor = if(isEnabled){
                Theme.colors.secondary.withAlpha(0.7f)
            }else{
                Theme.colors.secondary.withAlpha(0.4f)
            }
        )
        BoardState.BINGO -> ReloadBtnProperties(
            shape = Theme.shapes.roundShape,
            primaryColor = Theme.colors.success,
            borderColor = Theme.colors.display,
            hasIcon = false,
            text = "¡BINGO!",
            textColor = Theme.colors.display
        )
        BoardState.BLOCKED -> ReloadBtnProperties(
            shape = Theme.shapes.hardBlockShape,
            primaryColor = Theme.colors.error,
            borderColor = Theme.colors.primary,
            hasIcon = true,
            text = "x${reloadCost}",
            textColor = Theme.colors.primary
        )
        BoardState.GAMEOVER -> ReloadBtnProperties(
            shape = Theme.shapes.hardBlockShape,
            primaryColor = Theme.colors.secondary.withAlpha(0.3f),
            borderColor = Theme.colors.error,
            hasIcon = false,
            text = "X",
            textColor = Theme.colors.primary
        )
    }
}

private class ReloadBtnProperties(
    val shape: Shape,
    val hasIcon: Boolean,
    val text: String,
    val primaryColor: Color,
    val borderColor: Color,
    val textColor: Color,
)