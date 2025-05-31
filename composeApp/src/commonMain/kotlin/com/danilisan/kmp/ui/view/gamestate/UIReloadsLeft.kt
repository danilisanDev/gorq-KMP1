package com.danilisan.kmp.ui.view.gamestate

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.ui.view.toSp
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.refresh
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.vectorResource

const val RELOADS_LEFT_TEXT_DIV = 1.5f

@Composable
fun UIReloadsLeft(
    getTurnsLeft: () -> Int
) {
    println("Recomposition reloadsLeft")
    val turnsLeft = getTurnsLeft()
    var currentTurnsLeft by remember{ mutableStateOf(turnsLeft) }
    var difference by remember{ mutableStateOf(0) }

    LaunchedEffect(turnsLeft){
        difference = turnsLeft.compareTo(currentTurnsLeft)
        if(difference != 0){
            while(currentTurnsLeft != turnsLeft){
                delay(200)
                currentTurnsLeft += difference
            }
            delay(200)
            difference = 0
        }
    }

    val animatedColor: Color by animateColorAsState(
        targetValue = when(difference){
            -1 -> Theme.colors.error
            1 -> Theme.colors.success
            else -> Theme.colors.primary
        },
    )


    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth(4f / 6)
            .fillMaxHeight(0.5f)
            .drawBehind{
                drawRect(
                    color = animatedColor,
                    style = Stroke(width = 3f)
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        val fontSize = (maxHeight / RELOADS_LEFT_TEXT_DIV).toSp()
        val animationSpec: FiniteAnimationSpec<IntOffset> = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AnimatedContent(
                targetState = currentTurnsLeft,
                label = "Animated turn counter",
                transitionSpec = {
                    val diff = targetState.compareTo(initialState)
                    slideInVertically(animationSpec) { height -> height * diff } + fadeIn() togetherWith
                            slideOutVertically { height -> height * diff * -1 } + fadeOut()
                }
            ) {
                Text(
                    text = currentTurnsLeft.toString(),
                    style = TextStyle(
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = animatedColor
                    )
                )
            }
            Image(
                imageVector = vectorResource(Res.drawable.refresh),
                contentDescription = "refresh",
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(animatedColor)
            )
        }
    }
}