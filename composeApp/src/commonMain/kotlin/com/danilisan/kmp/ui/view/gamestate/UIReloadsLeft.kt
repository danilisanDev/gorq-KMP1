package com.danilisan.kmp.ui.view.gamestate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.danilisan.kmp.ui.theme.Theme
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.refresh
import org.jetbrains.compose.resources.vectorResource

@Composable
fun UIReloadsLeft(turnsLeft: Int) {
    val highlightColor = Theme.colors.primary
//    val insetBorder = BorderStroke(
//        Theme.borders.thinBorder,
//        Brush.horizontalGradient(
//            colors = Theme.colors.insetFilter
//        )
//    )
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth(4f / 6)
            .fillMaxHeight(0.5f)
//            .background(
//                color = Theme.colors.primary,
//            )
            .border(
                width = Theme.borders.thinBorder,
                color = Theme.colors.primary
            )
        ,
        contentAlignment = Alignment.Center,
    ) {
        val fontSize = with(LocalDensity.current) { maxHeight.toSp() / 1.5}
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = turnsLeft.toString(),
                style = TextStyle(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = highlightColor
                )
            )
            Image(
                imageVector = vectorResource(Res.drawable.refresh),
                contentDescription = "refresh",
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(highlightColor)
            )
        }
    }
}

@Composable
private fun highLightLine(top: Boolean) {

}