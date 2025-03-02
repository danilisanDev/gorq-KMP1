package com.danilisan.kmp.ui.view.gamestate

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.danilisan.kmp.domain.entity.DisplayMessage
import com.danilisan.kmp.ui.state.BoardState
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.ui.theme.withAlpha
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun UIMessageDisplay(
    message: DisplayMessage,
    boardState: BoardState
){
    val bgColor = if(boardState == BoardState.BLOCKED){
        Theme.colors.error
    }else{
        Theme.colors.primary.withAlpha(0.2f)
    }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize(0.8f)
            .background(
                color = bgColor
            )
            .background(
                color = Theme.colors.primary.withAlpha(0.4f)
            ),
        contentAlignment = Alignment.Center
    ) {
        val linesFilter = Theme.colors.secondary.withAlpha(0.3f)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLine(
                color = linesFilter,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 5f
            )
            drawLine(
                color = linesFilter,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 3f
            )
        }

        val boxSize = maxHeight
        val fontSize = with(LocalDensity.current) { boxSize.toSp() / 1.8}
        Text(
            text = message.toStringMessage(),
            maxLines = 1,
            letterSpacing = 3.sp,
            modifier = Modifier
                .fillMaxWidth()
                .basicMarquee(),
            style = TextStyle(
                fontSize = fontSize,
                textAlign = TextAlign.Center,
                color = Theme.colors.secondary.withAlpha(0.4f)
            )
        )

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