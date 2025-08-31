package com.danilisan.kmp.ui.view.gamestate

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.danilisan.kmp.domain.action.gamestate.GameStateActionManager.Companion.UPDATE_BOARD_TOTAL_DELAY
import com.danilisan.kmp.domain.entity.Score
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.ui.view.formatUIScore
import com.danilisan.kmp.ui.view.toSp
import com.danilisan.kmp.ui.view.withAlpha
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.lines
import kotlinproject.composeapp.generated.resources.score
import kotlinproject.composeapp.generated.resources.thousandSeparator
import kotlinproject.composeapp.generated.resources.trillion
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
fun UIScoreDisplay(
    getScore: () -> Score,
    applyAlpha: () -> Float = { 1f },
    isGoldenStar: (Score) -> Boolean? = { null },
    applyStarAnimation: () -> Brush? = { null },
) {
    val score = getScore()
    //Container modifiers
    val shape = RoundedCornerShape(topStartPercent = 14, topEndPercent = 14)
    val borderGradient = Brush.verticalGradient(
        listOf(
            Theme.colors.secondary.withAlpha(0.6f),
            Theme.colors.secondary
        )
    )
    val colorList = listOf(
        Theme.colors.selected,
        Theme.colors.success
    )

    //Text utils
    val textMeasurer = rememberTextMeasurer()
    val thousandSeparator = stringResource(Res.string.thousandSeparator)
    val trillionSymbol = stringResource(Res.string.trillion)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = applyAlpha()
            }
            .background(
                brush = borderGradient,
                shape = shape
            )
            .border(
                border = BorderStroke(
                    width = Theme.borders.mediumBorder,
                    brush = borderGradient,
                ),
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        val boxSize = maxHeight
        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.75f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //Score tag and lines
            ScoreTagAndLines(
                getLines = { score.lines },
                fontSize = (boxSize / 4f).toSp()
            )

            //Small numbers
            DisplaySmallNumbers(
                getSmallNumbers = {
                    formatUIScore(
                        number = score.points,
                        thousandSeparator = thousandSeparator,
                        trillionSymbol = trillionSymbol,
                    ).dropLast(n = 2)
                },
                fontSize = (boxSize / 3f).toSp()
            )

            //Big numbers
            DisplayBigNumbers(
                getBigNumbers = {
                    formatUIScore(
                        number = score.points,
                        thousandSeparator = thousandSeparator,
                        trillionSymbol = trillionSymbol,
                    ).takeLast(n = 2).padStart(2, '0')
                },
                fontSize = (boxSize / 2f).toSp(),
                isGoldenStar = isGoldenStar(score),
                applyStarAnimation = applyStarAnimation,
                textMeasurer = textMeasurer,
            )
        }
    }
    //Score increment animation
    var showIncrement by remember { mutableStateOf(false) }
    LaunchedEffect(score.points) {
        showIncrement = true
        delay(UPDATE_BOARD_TOTAL_DELAY * 2 + 200)
        showIncrement = false
    }
    if(showIncrement){
        ScoreIncrement(
            getIncrementString = {
                score.increment
                    .takeIf { it > 0 }
                    ?.let { increment ->
                        "+ ${
                            formatUIScore(
                                number = increment,
                                thousandSeparator = thousandSeparator,
                                trillionSymbol = trillionSymbol
                            )
                        }"
                    }
            },
            getPrimaryColor = {
                score.increment.let { increment ->
                    if (increment < 100L) {
                        colorList[0]
                    } else {
                        colorList[1]
                    }
                }
            },
            fontSize = 24.sp,
            textMeasurer = textMeasurer
        )
    }
}

@Composable
private fun ScoreIncrement(
    getIncrementString: () -> String?,
    getPrimaryColor: () -> Color,
    fontSize: TextUnit,
    textMeasurer: TextMeasurer
) {
    //Animated values
    var isVisible by remember { mutableStateOf(false) }
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f
    )
    var liftAnimation by remember { mutableStateOf(false) }
    val animatedTranslateY by animateFloatAsState(
        targetValue = if (liftAnimation) 1f else 0f
    )

    LaunchedEffect(Unit) {
        if (getIncrementString() != null) {
            isVisible = true
            liftAnimation = true
            delay(UPDATE_BOARD_TOTAL_DELAY * 2)
            isVisible = false
        }
    }

    val shadowColor = Theme.colors.primary
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = animatedAlpha
                translationY = size.height * -0.2f * animatedTranslateY
            }
            .zIndex(20f)
    ) {
        getIncrementString()?.let { incrementString ->
            drawScoreIncrement(
                incrementString = incrementString,
                getColor = { shadowColor },
                fontSize = fontSize,
                textMeasurer = textMeasurer,
                shadow = true
            )
            drawScoreIncrement(
                incrementString = incrementString,
                getColor = getPrimaryColor,
                fontSize = fontSize,
                textMeasurer = textMeasurer,
            )
        }
    }
}

private fun DrawScope.drawScoreIncrement(
    incrementString: String,
    getColor: () -> Color,
    fontSize: TextUnit,
    textMeasurer: TextMeasurer,
    shadow: Boolean = false,
) {
    textMeasurer.measure(
        text = incrementString,
        style = TextStyle(
            color = getColor(),
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
        )
    ).let { textLayout ->
        val shadowOffset = if (shadow) size.height / 51f else 0f
        drawText(
            textLayoutResult = textLayout,
            topLeft = Offset(
                x = (size.width * 0.9f) - textLayout.size.width + shadowOffset,
                y = 0f + shadowOffset
            )
        )
    }
}

@Composable
private fun RowScope.ScoreTagAndLines(
    getLines: () -> Int,
    fontSize: TextUnit,
) = Column(
    Modifier
        .fillMaxHeight()
        .weight(2f),
    verticalArrangement = Arrangement.Center
) {
    Text(
        text = stringResource(Res.string.score),
        style = TextStyle(
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Start,
            color = Theme.colors.primary,
        )
    )
    Text(
        text = "${stringResource(Res.string.lines).uppercase()} ${getLines()}",
        style = TextStyle(
            fontSize = fontSize / 1.7f,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            color = Theme.colors.primary.withAlpha(0.4f)
        )
    )
}

@Composable
private fun RowScope.DisplaySmallNumbers(
    getSmallNumbers: () -> String,
    fontSize: TextUnit,
) = Text(
    modifier = Modifier
        .weight(4f)
        .fillMaxHeight(0.5f)
        .padding(end = 2.dp),
    text = getSmallNumbers(),
    fontSize = fontSize,
    fontWeight = FontWeight.SemiBold,
    textAlign = TextAlign.End,
    color = Theme.colors.primary.withAlpha(0.7f),
    letterSpacing = 2.sp,
)

@Composable
private fun RowScope.DisplayBigNumbers(
    getBigNumbers: () -> String,
    fontSize: TextUnit,
    isGoldenStar: Boolean?,
    applyStarAnimation: () -> Brush?,
    textMeasurer: TextMeasurer,
) {
    val gradientList = listOf(
        Theme.colors.getCombinedStarGradient(Theme.colors.golden),
        Theme.colors.getCombinedStarGradient(Theme.colors.grey),
        listOf(Theme.colors.primary, Theme.colors.primary)
    )
    Canvas(
        modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
    ) {
        //Static numbers with gradient
        drawBigNumbers(
            bigNumbers = getBigNumbers(),
            fontSize = fontSize,
            brush = Brush.linearGradient(
                when (isGoldenStar) {
                    true -> gradientList[0]
                    false -> gradientList[1]
                    else -> gradientList[2]
                }
            ),
            textMeasurer = textMeasurer,
        )

        //Star animated reflection
        applyStarAnimation()?.let { animatedBrush ->
            if (isGoldenStar != null) {
                drawBigNumbers(
                    bigNumbers = getBigNumbers(),
                    brush = animatedBrush,
                    fontSize = fontSize,
                    textMeasurer = textMeasurer,
                )
            }
        }
    }
}

private fun DrawScope.drawBigNumbers(
    bigNumbers: String,
    brush: Brush,
    fontSize: TextUnit,
    textMeasurer: TextMeasurer,
) {
    drawText(
        textMeasurer.measure(
            text = bigNumbers,
            style = TextStyle(
                brush = brush,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
            )
        )
    )
}
