package com.danilisan.kmp.ui.view.gamestate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danilisan.kmp.domain.entity.Score
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.ui.view.toSp
import com.danilisan.kmp.ui.view.withAlpha
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.lines
import kotlinproject.composeapp.generated.resources.score
import kotlinproject.composeapp.generated.resources.thousandSeparator
import kotlinproject.composeapp.generated.resources.trillion
import org.jetbrains.compose.resources.stringResource

const val MAX_DISPLAY: Long = 1_000_000_000_000L

@Composable
fun UIScoreDisplay(
    getScore: () -> Score,
    isGoldenStar: (Score) -> Boolean? = { null },
){
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(6f)
    ){
        val boxSize = maxWidth
        val shape = RoundedCornerShape(topStartPercent = 14, topEndPercent = 14)
        val borderGradient = Brush.verticalGradient(
            listOf(
                Theme.colors.secondary.withAlpha(0.6f),
                Theme.colors.secondary
            )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
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
        ){
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.75f),
                verticalAlignment = Alignment.Bottom
            ){
                val score = getScore()
                val size: @Composable (Int) -> TextUnit = { div ->
                    (boxSize / div).toSp()
                }

                val bigNumbers = getBigNumbersString(score.points)
                val smallNumbers = getSmallNumbersString(score.points)
                Column(Modifier
                    .weight(2f)
                    .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ){
                    Text(
                        text = stringResource(Res.string.score),
                        textAlign = TextAlign.Start,
                        fontSize = size(21),
                        fontWeight = FontWeight.SemiBold,
                        color = Theme.colors.primary,
                    )
                    Text(
                        text = "${stringResource(Res.string.lines).uppercase()} ${score.lines}",
                        style = TextStyle(
                            fontSize = size(42),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start,
                            color = Theme.colors.primary.withAlpha(0.4f)
                        )
                    )
                }

                Text(
                    text = smallNumbers,
                    letterSpacing = 2.sp,
                    color = Theme.colors.primary.withAlpha(0.7f),
                    modifier = Modifier
                        .fillMaxHeight(0.7f)
                        .weight(4f),
                    textAlign = TextAlign.End,
                    fontSize = size(17),
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = bigNumbers,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 2.dp),
                    style = TextStyle(
                        fontSize = size(10),
                        fontWeight = FontWeight.Bold,
                        brush = Brush.linearGradient(
                            when(isGoldenStar(score)){
                                true -> Theme.colors.getCombinedStarGradient(Theme.colors.golden)
                                false -> Theme.colors.getCombinedStarGradient(Theme.colors.grey)
                                else -> listOf(Theme.colors.primary, Theme.colors.primary)
                            }
                        ),
                    )
                )
            }
        }
    }
}
private fun getBigNumbersString(
    number: Long
): String{
    return (number % 100).toString().padStart(length = 2, padChar = '0')
}

@Composable
private fun getSmallNumbersString(
    number: Long,
): String {
    val separator = stringResource(Res.string.thousandSeparator)
    return if(number < MAX_DISPLAY){
        val smallNumbersString = (number / 100).toString()
        toStringWithDisplacedSeparator(smallNumbersString, separator)
    }else{
        val trillions = number / MAX_DISPLAY
        val smallNumber = (number / 100) % 10
        val trillionSymbol = stringResource(Res.string.trillion)
        "$trillions${trillionSymbol}${separator}$smallNumber"
    }
}

private fun toStringWithDisplacedSeparator(
    number: String,
    separator: String
): String{
    if(number == "0") return ""
    val lastDigit = number.takeLast(n = 1)
    val restDigits = number.dropLast(n= 1)
    return if(restDigits.isNotEmpty()) {
        restDigits
            .reversed()
            .chunked(3)
            .joinToString(separator = separator)
            .reversed() + separator + lastDigit
    }else{
        lastDigit
    }
}