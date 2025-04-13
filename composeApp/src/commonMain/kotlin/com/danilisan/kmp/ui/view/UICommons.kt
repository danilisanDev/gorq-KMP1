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
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt


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

//Dimension utils
@Composable
fun Dp.toPx(): Float = with(LocalDensity.current){ this@toPx.toPx() }

fun Offset.toIntOffset(): IntOffset =
    IntOffset(
        this.x.roundToInt(),
        this.y.roundToInt(),
    )

//Color utils
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