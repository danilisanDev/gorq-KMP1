package com.danilisan.kmp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilisan.kmp.domain.entity.BoardHelper.getLineLength
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.DisplayMessage
import com.danilisan.kmp.domain.entity.GameMode
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.Score
import com.danilisan.kmp.ui.state.BoardState
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.ui.view.combineOver
import com.danilisan.kmp.ui.view.plus
import com.danilisan.kmp.ui.view.withAlpha
import com.danilisan.kmp.ui.view.gamestate.UIAdBanner
import com.danilisan.kmp.ui.view.gamestate.UIBoardContainer
import com.danilisan.kmp.ui.view.gamestate.UIMessageDisplay
import com.danilisan.kmp.ui.view.gamestate.UIQueue
import com.danilisan.kmp.ui.view.gamestate.UIReloadButton
import com.danilisan.kmp.ui.view.gamestate.UIReloadsLeft
import com.danilisan.kmp.ui.view.gamestate.UIScoreDisplay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Composable
fun ScorePreview(){
    val score = Score(9_999_999_999_999L, 101, 12)
    UIScoreDisplay(
        score,
        false
    )
}

@Composable
fun Semaforo(){
    Column(Modifier
        .height(600.dp)
        .width(200.dp)
        .padding(10.dp)
        .background(Color.LightGray)
    ){
        BlinkingLight(color = Theme.colors.primary, on = 1f) //Encendida
        BlinkingLight(color = Theme.colors.primary + Theme.colors.grey, on = 0.5f) //A medias
        BlinkingLight(color = Theme.colors.grey, on = 0f) //Apagada
        BlinkingLight(color = Theme.colors.success, on = 1f) //Encendida

    }
}

@Preview
@Composable
fun BoardFramePreview(){
    Box(modifier =
        Modifier
            .size(500.dp)
            .aspectRatio(1f)
            .background(Color.DarkGray)
    ){
        UIBoardContainer(
            lineLength = 3,
            board = Mocks.easyAddBoard,
            boardState = BoardState.READY,
        )
    }
}

@Preview
@Composable
fun BlinkingLight(color: Color = Color.Green , on: Float = 1f){
    // Configura la animación de color
    val grey = Theme.colors.grey
    val white = Theme.colors.primary
    Box(
        modifier = Modifier
            .size(50.dp) // Tamaño del círculo
            .padding(10.dp)
            .drawBehind {
                //Brillo exterior
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to color,
                            0.75f to color.withAlpha(0.5f),
                            1.0f to Color.Transparent
                        ),
                        radius = size.minDimension * (0.5f + (0.25f * on)),
                    ),
                    radius = size.minDimension * (0.5f + (0.25f * on)),
                )

                //Luz apagada (sin transparencia)
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to color + white.withAlpha(0.4f),
                            0.7f to color.withAlpha(0.9f),
                            1f to color + grey,
                        ),
                        radius = size.minDimension * 0.5f
                    ),
                    radius = size.minDimension * 0.5f
                )
            }
    )
}




@Composable
fun HeptaMosaic(){
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Theme.colors.secondary)
    ){
        val maxWidth = constraints.maxWidth.toFloat()
        val maxHeight = constraints.maxHeight.toFloat()
        val radius = constraints.maxWidth.toFloat() / 20
        val numSides = 7
        val angleStep = 2 * PI / numSides
        val angleEvenX = radius * 2 * cos(PI / 7).toFloat()
        val angleEvenY = radius * 2 * sin(PI / 14).toFloat()

        val color1 = Theme.colors.primary.withAlpha(0.3f)
        val color2 = Theme.colors.primary.withAlpha(0.7f)

        Box(
            Modifier
                .fillMaxSize()
                .drawBehind {
                    var centerY = 0f
                    var evenY = false
                    while (centerY - radius < maxHeight) {
                        var centerX = 0f
                        var evenX = evenY
                        while (centerX - radius < maxWidth) {
                            val startingStep = 0
                            val initialAngle = if (evenX) PI else 2 * PI
                            //((if (evenX) 1 else -1) * PI) / 2 + (angleStep * startingStep)
                            val color = if (evenX) color1 else color2

                            val path = Path().apply {
                                moveTo(
                                    centerX + radius * cos(initialAngle).toFloat(),
                                    centerY + radius * sin(initialAngle).toFloat()
                                )
                                for (i in 1 until numSides) {
                                    lineTo(
                                        centerX + radius * cos(initialAngle + angleStep * i).toFloat(),
                                        centerY + radius * sin(initialAngle + angleStep * i).toFloat()
                                    )
                                }
                                //close()
                            }

                            drawPath(
                                path = path,
                                color = color,
                            )

                            if (evenX) {
                                centerY += angleEvenY
                                centerX += angleEvenX
                            } else {
                                centerY -= angleEvenY
                                centerX += angleEvenX

                            }

                            evenX = !evenX
                        }
                        centerY += if (evenY) {
                            (radius + angleEvenY) * 1.9f
                        } else {
                            radius * 1.9f
                        }
                        evenY = !evenY
                    }
                }
        )
    }
}

@Composable
fun CheckedBackground(){
    BoxWithConstraints(Modifier.fillMaxSize()){
        val columns = 7
        val rows = 17
        val width = constraints.maxWidth.toFloat() / columns
        val height = constraints.maxHeight.toFloat() / rows
        val color = Theme.colors.grey
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    color = Theme.colors.primary.withAlpha(0.04f)
                )
                .drawBehind {
                    for (row in 0 until rows + 5) {
                        for (col in 0 until columns + 6) {
                            if ((row + col) % 2 == 0) continue

                            val diffX = height * 0.3f
                            val diffY = width * 0.3f
                            val posX = col * (width) - (row * diffX)
                            val posY = row * (height) - (col * diffY)
                            if (posY < -height || posX < -width
                                || posY > height * rows || posX > width * columns
                            ) {
                                continue
                            }

                            val path = Path().apply {
                                moveTo(posX, posY)
                                lineTo(posX + width, posY - diffY)
                                lineTo(posX + width - diffX, posY + height - diffY)
                                lineTo(posX - diffX, posY + height)
                            }
                            drawPath(
                                path = path,
                                color = color,
                            )
                        }
                    }
                })
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
    Column(
        Modifier
            .fillMaxSize()
            .aspectRatio(1f)){
        Box(
            Modifier
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
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    color = Theme.colors.primary.combineOver(Color.Transparent, alpha = -1 * 0.1f),
                    shape = Theme.shapes.roundShape
                )
        )
    }
}