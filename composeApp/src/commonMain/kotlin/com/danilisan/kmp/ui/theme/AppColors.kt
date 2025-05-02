package com.danilisan.kmp.ui.theme

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.danilisan.kmp.ui.view.plus
import com.danilisan.kmp.ui.view.withAlpha

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
){
    fun getCombinedStarGradient(filter: Color): List<Color> =
        this.starGradient.map{ it + filter }
}

private val golden = Color(0xFFFFB707)
private val blue = Color(0xFF077AD7)
private val green = Color(0xFF287D49)
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
    success = green,
    error = red,
    selected = blue,
    display = yellow,
    rainbowGradient = listOf(
        purple,magenta,red,orange,yellow,green,blue,purple
    ),
    outsetGradient = listOf(
        Color.White.withAlpha(0.4f),
        Color.Black.withAlpha(0.4f)
    ),
    insetGradient = listOf(
        Color.Black.withAlpha(0.8f),
        Color.White.withAlpha(0.2f)
    ),
    regularGradient = listOf(
        Color.Transparent, Color.Black.withAlpha(0.3f)
    ),
    starGradient = listOf(
        Color.White.withAlpha(0.3f),
        Color.Transparent,
        Color.White.withAlpha(0.4f),
        Color.Transparent,
        Color.White.withAlpha(0.4f),
        Color.Transparent,
        Color.White.withAlpha(0.3f),
    ),
)

