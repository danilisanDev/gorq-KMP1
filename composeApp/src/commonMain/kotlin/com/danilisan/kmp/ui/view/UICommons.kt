package com.danilisan.kmp.ui.view

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

const val MAX_DISPLAY: Long = 1_000_000_000_000L
const val CONTRAST = 0.3f

//Spacer
@Composable
fun UISpacer(
    size: Int = 20,
    horizontal: Boolean = true
) {
    Spacer(
        modifier = if (horizontal) {
            Modifier.height(size.dp)
        } else {
            Modifier.width(size.dp)
        }
    )
}

class OffsetDp(val x: Dp, val y: Dp){
    @Composable
    fun toPx(): Offset = Offset(this.x.toPx(), this.y.toPx())
}

//Score format
fun formatUIScore(
    number: Long,
    thousandSeparator: String,
    trillionSymbol: String,
): String =
    if(number == 0L){
        "0"
    }else if(number < MAX_DISPLAY){
        number
            .toString()
            .reversed()
            .chunked(size = 3)
            .joinToString(thousandSeparator)
            .reversed()
    }else {
        "${(number / MAX_DISPLAY).toInt()}"+ trillionSymbol + "${number % 1000}"
    }

//Unit converters
@Composable
fun Dp.toPx(): Float = with(LocalDensity.current){ this@toPx.toPx() }

@Composable
fun Dp.toSp(): TextUnit = with(LocalDensity.current){ this@toSp.toSp() }


fun Offset.toIntOffset(): IntOffset =
    IntOffset(
        this.x.roundToInt(),
        this.y.roundToInt(),
    )

fun List<Color>.toArrayWithAbsoluteColorStops(
    blur: Float = 0f
): Array<Pair<Float, Color>>{
    if(isEmpty() || blur > 1f) return emptyArray()

    val stopInterval = (1f - blur) / size
    var stop = 0f
    val result = mutableListOf<Pair<Float,Color>>()
    repeat(size){ index ->
        result.add(Pair(stop, this[index]))
        stop += stopInterval
        result.add(Pair(stop, this[index]))
        if(blur > 0f && index != lastIndex){
            val blurColor = this[index] + this[index + 1]
            result.add(Pair(stop, blurColor))
            stop += blur
            result.add(Pair(stop,blurColor))
        }
    }
    return result.toTypedArray()
}

//Color utils
fun createRelativeShader(
    shaderColor: Color,
    bgColor: Color? = null,
    index: Int,
    maxIndex: Int,
    highlightFirst: Boolean = true
): Color {
    val alpha = ((index.toFloat() / (maxIndex + 1)) / 1f) + if(index != 0 && highlightFirst) 0.1f else 0f
    return shaderColor.withAlpha(alpha) + bgColor
}

fun Color.combineOver(other: Color? = this, times: Int = 1, alpha: Float = -1f): Color =
    if(other == null){
        this
    }else{
        var result = this
        val newColor = other.withAlpha(alpha)
        repeat(times) { result += newColor }
        result
    }

fun Color.withAlpha(newAlpha: Float = -1f): Color =
    if(newAlpha !in 0f..1f){
        this
    }else{
        Color(
            red = this.red,
            green = this.green,
            blue = this.blue,
            alpha = newAlpha
        )
    }

/**
 * Operator function to combine two overlapped colors
 * into a single instance of Color
 */
operator fun Color.plus(other: Color?): Color =
    if(other == null){
        this
    } else{
        (this.alpha + other.alpha).let{ alphaSum ->
            Color(
                red = (this.colorAlpha(0) + other.colorAlpha(0)) / alphaSum,
                green = (this.colorAlpha(1) + other.colorAlpha(1)) / alphaSum,
                blue = (this.colorAlpha(2) + other.colorAlpha(2)) / alphaSum,
                alpha = alphaSum.takeIf { it < 1f } ?: 1f
            )
        }
    }

private fun Color.colorAlpha(color: Int) = when(color){
    0 -> this.red
    1 -> this.green
    2 -> this.blue
    else -> 1f
} * this.alpha