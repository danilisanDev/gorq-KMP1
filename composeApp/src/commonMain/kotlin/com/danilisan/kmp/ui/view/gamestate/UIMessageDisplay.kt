package com.danilisan.kmp.ui.view.gamestate

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danilisan.kmp.domain.entity.DisplayMessage
import com.danilisan.kmp.domain.entity.DisplayMessage.Companion.DISPLAY_TEXT_ERROR
import com.danilisan.kmp.domain.entity.DisplayMessage.Companion.DISPLAY_TEXT_GOLDEN
import com.danilisan.kmp.domain.entity.DisplayMessage.Companion.DISPLAY_TEXT_PRIMARY
import com.danilisan.kmp.domain.entity.DisplayMessage.Companion.DISPLAY_TEXT_SELECTED
import com.danilisan.kmp.domain.entity.DisplayMessage.Companion.DISPLAY_TEXT_SUCCESS
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.ui.view.withAlpha
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.refresh
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun UIMessageDisplay(
    message: DisplayMessage,
){
    val gradient = Brush.horizontalGradient(
        colorStops = arrayOf(
            0f to Theme.colors.transparent,
            0.1f to message.getBgColor().withAlpha(0.9f),
            0.9f to message.getBgColor().withAlpha(0.8f),
            1f to Theme.colors.transparent,
        )
    )

    val highlightColor = message.getHighlightColor().withAlpha(0.8f)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(7f)
            .background(
                brush = gradient
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLine(
                color = highlightColor,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 4f
            )
            drawLine(
                color = highlightColor,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 3f
            )
        }

        val boxSize = maxHeight
        val fontSize = with(LocalDensity.current) {
            boxSize.toSp() / (2.1 - message.sizeDiff * 0.2) }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text = message.toStringMessage(),
                maxLines = 1,
                letterSpacing = 3.sp,
                modifier = Modifier
                    .basicMarquee(),
                style = TextStyle(
                    fontSize = fontSize,
                    fontWeight = message.weight,
                    textAlign = TextAlign.Center,
                    color = highlightColor
                )
            )
            if(message.icon != null){
                Image(
                    imageVector = vectorResource(Res.drawable.refresh),
                    contentDescription = "refresh",
                    contentScale = ContentScale.FillHeight,
                    colorFilter = ColorFilter.tint(highlightColor),
                    modifier = Modifier
                        .fillMaxHeight(0.7f + (0.05f * message.sizeDiff))
                )
            }
        }
    }
}

@Composable
private fun DisplayMessage.toStringMessage(): String {
    return res?.let { resId ->
        when(resId){
            is StringResource -> stringResource(resId, arg)
            is PluralStringResource -> pluralStringResource(resId, arg.toIntOrNull()?: 0, arg.toIntOrNull()?: 0)
            else -> ""
        }
    } ?: arg
}

@Composable
private fun Int.toColor() = when(this){
    DISPLAY_TEXT_PRIMARY -> Theme.colors.primary
    DISPLAY_TEXT_SELECTED -> Theme.colors.selected
    DISPLAY_TEXT_SUCCESS -> Theme.colors.success
    DISPLAY_TEXT_ERROR -> Theme.colors.error
    DISPLAY_TEXT_GOLDEN -> Theme.colors.golden
    else -> Theme.colors.secondary
}

@Composable
private fun DisplayMessage.getBgColor(): Color = this.bgColor.toColor()

@Composable
private fun DisplayMessage.getHighlightColor(): Color = this.highlightColor.toColor()