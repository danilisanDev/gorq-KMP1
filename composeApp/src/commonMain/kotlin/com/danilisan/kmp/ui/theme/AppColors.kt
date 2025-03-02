package com.danilisan.kmp.ui.theme

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Stable
data class AppColors(
    val primary: Color,
    val secondary: Color,
    val grey: Color,
    val golden: Color,
    val starGradient: List<Color>,
    val transparent: Color = Color.Transparent,
    val success: Color,
    val error: Color,
    val selected: Color,
    val display: Color,
    val rainbowGradient: List<Color>,
    val outsetGradient: List<Color>,
    val insetGradient: List<Color>,
    val regularGradient: List<Color>,
)

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

private val golden = Color(0xFFFFB707)
private val blue = Color(0xFF077AD7)
private val green = Color(0xFF49A77A)
private val purple = Color(0xFF9575CD)
private val magenta = Color(0xFFE71E77)
private val red = Color(0xFFCC444B)
private val yellow = Color(0xFFFFFF70)
private val orange = Color(0xFFE5A700)



val LightAppColors = AppColors(
    primary = Color.White,
    secondary = Color.Black,
    grey = Color.White + Color.Black,
    golden = golden,
    starGradient = listOf(
        Color.White.withAlpha(0.3f),
        Color.Transparent,
        Color.White.withAlpha(0.4f),
        Color.Transparent,
        Color.White.withAlpha(0.4f),
        Color.Transparent,
        Color.White.withAlpha(0.3f),
    ),
    success = green,
    error = red,
    selected = blue,
    display = yellow,
    rainbowGradient = listOf(
        purple,magenta,red,orange,yellow,green,blue,purple
    ),
    outsetGradient = listOf(
        Color.Transparent, Color.Black.withAlpha(0.6f)
    ),
    insetGradient = listOf(
        Color.Transparent,Color.White.withAlpha(0.6f)
    ),
    regularGradient = listOf(
        Color.Transparent, Color.Black.withAlpha(0.1f)
    )
)

