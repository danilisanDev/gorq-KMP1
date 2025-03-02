package com.danilisan.kmp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.ui.theme.combineOver
import com.danilisan.kmp.ui.theme.withAlpha
import com.danilisan.kmp.ui.theme.plus
import com.danilisan.kmp.ui.view.gamestate.UINumberBox


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun NumberBoxPreview(){
    val numberBox = NumberBox.GoldenStarBox()
    Box(Modifier.fillMaxSize()){
        UINumberBox(
            numberBox = numberBox,
            tintColor = Theme.colors.success,
        )
    }


}


@Composable
fun ColorsPreview(){
    val golden = Color(0xFFFFB707)
    val starGradient = Brush.sweepGradient(
        Theme.colors.starGradient
    )
    val rainbowGradient = Brush.linearGradient(
        Theme.colors.rainbowGradient
    )
    val silver = Color(0xFFA7A7A7)
    val green = Theme.colors.success
    Column(Modifier.fillMaxSize().aspectRatio(1f)){
        Box(Modifier
            .fillMaxWidth()
            .weight(1f)
            .background(
                color = golden,
                shape = Theme.shapes.roundShape
            )
//            .background(
//                brush = starGradient,
//                shape = Theme.shapes.roundShape
//            )
        )
        Box(Modifier
            .fillMaxWidth()
            .weight(1f)
            .background(
                color = Theme.colors.primary.combineOver(Color.Transparent, alpha = -1 * 0.1f),
                shape = Theme.shapes.roundShape
            )
        )
    }
}