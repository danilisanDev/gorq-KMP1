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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danilisan.kmp.domain.entity.Score
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.ui.theme.withAlpha
import com.danilisan.kmp.ui.view.UISpacer
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.lines
import kotlinproject.composeapp.generated.resources.score
import kotlinproject.composeapp.generated.resources.thousandSeparator
import kotlinproject.composeapp.generated.resources.trillion
import kotlinproject.composeapp.generated.resources.turns
import org.jetbrains.compose.resources.stringResource

const val MAX_DISPLAY: Long = 1_000_000_000_000L

@Composable
fun UIScoreDisplay(score: Score){
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(5f)
    ){
        val boxSize = maxWidth
        val shape = RoundedCornerShape(topStartPercent = 14, topEndPercent = 14)
        val borderGradient = Brush.verticalGradient(
            listOf(
                Theme.colors.secondary.withAlpha(0.1f),
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
                    .fillMaxWidth(0.9f),
                verticalAlignment = Alignment.CenterVertically,
            ){
                val bigNumbers = getBigNumbersString(score.points)
                val smallNumbers = getSmallNumbersString(score.points)
                Column(
                    modifier = Modifier
                        .fillMaxHeight(0.6f)
                        .weight(4f),
                    verticalArrangement = Arrangement.Center,
                ){
                    val smallSize = with(LocalDensity.current) { boxSize.toSp() / 21}
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ){
                        val style = TextStyle(
                            fontSize = smallSize,
                            fontWeight = FontWeight.SemiBold,
                            color = Theme.colors.primary
                        )
                        Text(
                            text = stringResource(Res.string.score),
                            modifier = Modifier.weight(2f),
                            textAlign = TextAlign.Start,
                            style = style
                        )
                        Text(
                            text = smallNumbers,
                            letterSpacing = 2.sp,
                            modifier = Modifier.weight(5f),
                            textAlign = TextAlign.End,
                            style = style
                            )
                    }
                    UISpacer(5)
                    Row(modifier = Modifier.fillMaxWidth()){
                        val smallerSize = with(LocalDensity.current) { boxSize.toSp() / 35}
                        Text(
                            text = "${stringResource(Res.string.lines).uppercase()} ${score.lines}",
                            modifier = Modifier
                                .weight(1f),
                            style = TextStyle(
                                fontSize = smallerSize,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                color = Theme.colors.primary.withAlpha(0.4f)
                            )
                        )
                        Text(
                            text = "${stringResource(Res.string.turns).uppercase()} ${score.turns}",
                            modifier = Modifier
                                .weight(1f),
                            style = TextStyle(
                                fontSize = smallerSize,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = Theme.colors.primary.withAlpha(0.4f)
                            )
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ){
                    val bigSize = with(LocalDensity.current) { boxSize.toSp() / 10}
                    Text(
                        text = bigNumbers,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.7f)
                            .padding(start = 2.dp),
                        style = TextStyle(
                            fontSize = bigSize,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start,
                            color = Theme.colors.display
                        )
                    )
                }
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