package com.danilisan.kmp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Composable
fun StarBox() {
    val goldenColors =
        listOf(
            Color(0xDFE5A700),
            Color(0xDFFFFF00),
            Color(0xFFE5A700),
            Color(0xDFFFFF00),
            Color(0xDFE5A700),
            Color(0xDFFFFF00),
            Color(0xFFE5A700),
        )
    val rainbowColors =
        listOf(
            Color(0xFF9575CD),
            Color(0xFFBA68C8),
            Color(0xFFE57373),
            Color(0xFFFFB74D),
            Color(0xFFFFF176),
            Color(0xFFAED581),
            Color(0xFF4DD0E1),
            Color(0xFF9575CD)
        )
    val rainbowColorsBrush = remember {
        Brush.sweepGradient(
            colors = rainbowColors,
        )
    }
    val goldenBrush = remember {
        Brush.sweepGradient(
            colors = goldenColors,
        )
    }
    val lightBeamBrush = remember {
        Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to Color(0x00000000),
                0.7f to Color(0xABFFFFFF),
                1.0f to Color(0x00000000),
            )
        )
    }
    val shape = RoundedCornerShape(35.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(100.dp)
            .background(
                brush = goldenBrush,
                //color = Color.Green,
                shape = shape
            )
            .border(
                BorderStroke(1.dp, rainbowColorsBrush),
                shape = shape
            )
    ) {
        Image(
            painter = androidx.compose.ui.res.painterResource(
                id = R.drawable.star
            ),
            contentDescription = "star",
            modifier = Modifier
                .matchParentSize()
        )
//        Text(
//            text = "6",
//            style = TextStyle(
//                brush = goldenBrush,
//                fontWeight = FontWeight.ExtraBold,
//                fontSize = 60.sp,
//            )
//        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = lightBeamBrush,
                    shape = shape
                )


        )
    }
}

@Composable
fun BlockBox() {
    val shape = RoundedCornerShape(5.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(100.dp)
            .background(
                color = Color.DarkGray,
                shape = shape
            )
            .border(
                width = 7.dp,
                color = Color.LightGray,
                shape = shape
            )
    ) {
        Text(
            text = "9",
            color = Color.LightGray,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 60.sp
        )
    }
}

@Composable
fun RegularBox(position: Int) {
    val shape = CutCornerShape(14.dp)
    val outsetBorder = Brush.linearGradient(
        colors = listOf(Color(0xFF5992C0), Color(0xFF000028))
    )
    val colorFilter = when(position){
        2 -> Color(0x49000049)
        3 -> Color(0x77000077)
        else -> Color.Transparent
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color.White, Color(0xFFcccccc)),
                ),
                shape = shape
            )
            .border(
                BorderStroke(2.dp, outsetBorder),
                shape = shape
            )
    ) {
        Text(
            text = "1",
            color = Color.DarkGray,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 90.sp
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = colorFilter,
                    shape = shape)
        )
    }
}

@Composable
fun Queue(
    containerHeight: Dp,
    containerWidth: Dp,
    lineLength: Int
){
    val shape = RoundedCornerShape(20.dp)
    val containerPadding = 5.dp
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(containerPadding)
            .background(
                color = Color.LightGray,
                shape = shape
            )
            .border(
                2.dp,
                Color.White,
                shape
            ),
        contentAlignment = Alignment.TopStart

    ){
        val boxSize = containerWidth / 2f
        Box(
            modifier = Modifier
                .absoluteOffset(
                    containerWidth - boxSize - containerPadding * 2,
                    0.dp)
                .size(boxSize)
                .aspectRatio(1f)
                .background(Color.Black)
        )
        Box(
            modifier = Modifier
                .absoluteOffset(
                    0.dp,
                    containerHeight / 2 - boxSize / 2)
                .size(boxSize)
                .aspectRatio(1f)
                .background(Color.Blue)
        )
        Box(
            modifier = Modifier
                .absoluteOffset(
                    containerWidth - boxSize - containerPadding * 2,
                    containerHeight - boxSize - containerPadding * 2)
                .size(boxSize)
                .aspectRatio(1f)
                .background(Color.Red)
        )



    }



}

@Composable
fun QueuePreview(){
    fun translationX(size: Size): Float = size.width * 0.5f / 2f
    fun translationY(size: Size): Float = size.height * 0.5f / 2f
    Box(//Caja 3
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .graphicsLayer{
                scaleX = 0.35f
                scaleY = 0.35f
                translationX = translationX(size) * 1f
                translationY = translationY(size) * -1.3f
            }
    ){
        RegularBox(3)
    }
    Box(//Caja 2
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .graphicsLayer{
                scaleX = 0.4f
                scaleY = 0.4f
                translationX = translationX(size) * -0.2f
                translationY = translationY(size) * -0.2f
            }
    ){
        RegularBox(2)
    }
    Box(//Caja 1
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .graphicsLayer{
                scaleX = 0.45f
                scaleY = 0.45f
                translationX = translationX(size)
                translationY = translationY(size)
            }
    ){
        RegularBox(1)
    }
}

@Composable
fun ReloadButton(){
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(25.dp)
            .background(
                color = Color.Blue,
                shape = shape
            )
            .border(
                width = 2.dp,
                color = Color.White,
                shape = shape
            ),
        contentAlignment = Alignment.Center,
    ){
        Row(
            horizontalArrangement = Arrangement.SpaceBetween) {
            Image(
                painter = androidx.compose.ui.res.painterResource(
                    id = R.drawable.refresh
                ),
                contentDescription = "refresh",
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color.White)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "x1",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
fun ReloadsLeft(){
    val specialStyle = buildAnnotatedString {
        withStyle(style = SpanStyle(fontSize = 14.sp)) {
            append(" R")
        }
        append("ESTANTES")

    }
    Box(
        modifier = Modifier
            .padding(25.dp)
            .fillMaxSize()
            .background(
                color = Color.LightGray,
            ),
        contentAlignment = Alignment.Center,

    ){
        Canvas(modifier = Modifier.fillMaxSize()){
            drawLine(
                color = Color.DarkGray,
                start = Offset(0f, 0f),
                end = Offset(size.width,0f),
                strokeWidth = 5f
            )
            drawLine(
                color = Color.DarkGray,
                start = Offset(0f, size.height),
                end = Offset(size.width,size.height),
                strokeWidth = 5f
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween){
            Text(
                text = "10",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray
                )
            )
            Image(
                painter = androidx.compose.ui.res.painterResource(
                    id = R.drawable.refresh
                ),
                contentDescription = "refresh",
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color.DarkGray)
            )
            Text(
                text = specialStyle,
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray
                )
            )
        }
    }

}

@Preview
@Composable
fun AppAndroidPreview() {
    Row(
        modifier = Modifier.fillMaxSize()
    ){
        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.5f)
                .background(color = Color.LightGray)
        )
        Column(
            modifier = Modifier
                .weight(14f)
                .background(Color.LightGray),
            verticalArrangement = Arrangement.Center
        ){
            Spacer(
                modifier = Modifier
                    .weight(0.25f)
            )
            Row(//Queue & button
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .weight(1f)
            ){
                BoxWithConstraints(//Queue
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(2f)
                        .background(Color(0xFF12EF12)),
                ){
                    Queue(maxHeight, maxWidth,3)
                }
                Column(//Reload
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(3f)
                        .background(Color(0xFF89D589))
                ){
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.Yellow)
                    ){
                        ReloadsLeft()
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.Magenta),
                    ){
                        ReloadButton()
                    }


                }
            }
            Box(//Board & Line Indicators
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Blue)
                    .weight(2f)
            ){

            }

            Box(//Display
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Magenta)
                    .weight(0.5f)
            )

            Box(//Score
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Yellow)
                    .weight(0.5f)
            )

            Spacer(
                modifier = Modifier
                    .weight(0.25f)
            )
        }
        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.5f)
                .background(color = Color.LightGray)
        )
    }

}