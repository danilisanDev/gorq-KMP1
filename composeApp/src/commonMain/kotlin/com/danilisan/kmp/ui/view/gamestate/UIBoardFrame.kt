package com.danilisan.kmp.ui.view.gamestate

import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import com.danilisan.kmp.domain.entity.BoardHelper.AVAILABLE_DIRECTIONS
import com.danilisan.kmp.domain.entity.BoardHelper.LineDirection
import com.danilisan.kmp.domain.entity.BoardHelper.getDirectionAndIndexFromLineId
import com.danilisan.kmp.domain.entity.BoardHelper.getLineIdsFromPositions
import com.danilisan.kmp.domain.entity.BoardHelper.isDiagonal
import com.danilisan.kmp.domain.entity.BoardHelper.lineIdBuilder
import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.ui.state.BoardState
import com.danilisan.kmp.ui.theme.Theme
import com.danilisan.kmp.ui.view.combineOver
import com.danilisan.kmp.ui.view.plus
import com.danilisan.kmp.ui.view.withAlpha

//TODO: Abstraer propiedades y medidas
private const val INDICATOR_SIZE = 0.017f
private const val CORNER_OFFSET = 0.08f
private const val SIDE_OFFSET = 0.05f
private const val H_INDICATORS = 0
private const val START_INDICATOR = 0

@Composable
fun UIBoardFrame(
    containerWidthInPx: Float,
    getBoardState: () -> BoardState,
    getLineLength: () -> Int,
    getCompletedLines: () -> List<Int>,
    getAvailableLines: () -> Set<Int>,
    getPositionsNotInLine: (Int, Int) -> List<BoardPosition>,
    showMenuBar: MutableState<Boolean>
) = getBoardState().let { boardState ->
    if (boardState == BoardState.READY || boardState == BoardState.BINGO) {
        LineIndicators(
            isBingoState = boardState == BoardState.BINGO,
            applyOffLights = { showMenuBar.value },
            getLineLength = getLineLength,
            getCompletedLines = getCompletedLines,
            getAvailableLines = getAvailableLines,
            getPositionsNotInLine = getPositionsNotInLine
        )
    } else {
        RectFrame(
            containerWidthInPx = containerWidthInPx,
            isBlocked = boardState == BoardState.BLOCKED
        )
    }
}

//region LINE INDICATORS
//Constant indexes for colors
private const val OFF_LIGHT = 0
private const val WHITE_LIGHT = 1
private const val GREEN_LIGHT = 2

/**
 * Class that enumerates all the possible states
 * for line indicators:
 * 1. Color of the indicator.
 * 2. If the state is animated,
 *      index on the list of the animated float that should be played.
 * 3. If the state is not animated,
 *      fixedGlow represents if
 *      the indicator is on or off.
 */
enum class IndicatorState (
    val color: Int = OFF_LIGHT,
    val animationIndex: Int = -1,
    val fixedGlow: Float = 0f,
){
    OFF,
    AVAILABLE(
        color = WHITE_LIGHT,
        animationIndex = 0
    ),
    NEXT(
        color = WHITE_LIGHT,
        animationIndex = 1
    ),
    COMPLETED(
        color = GREEN_LIGHT,
        fixedGlow = 1f
    ),
    BINGO_ON(
        color = GREEN_LIGHT,
        animationIndex = 0,
    ),
    BINGO_OFF(
        color = GREEN_LIGHT,
        animationIndex = 1
    ),
}

/**
 * Class that encloses the values
 * that shall be drawn for each indicator
 */
@Stable
private class IndicatorLight(
    val color: Color,
    val glow: Float,
)

/**
 * Converts the IndicatorState into a IndicatorLight.
 * Colors and animatedValues are passed by parameter
 * as they are obtained from a Composable context.
 */
private fun IndicatorState.buildIndicatorLight(
    colorList: List<Color>,
    animatedValues: List<State<Float>>,
): IndicatorLight{
    //Value of State<Flow> (if animated) / fixedGlow value (else)
    val lightValue = this.animationIndex
        .takeUnless{ it < 0 }
        ?.let{
            animatedValues[it].value
        }
    ?: this.fixedGlow

    //Higher light value intesifies main color and dims off color
    val color = colorList[this.color]
        .withAlpha(lightValue)
        .combineOver(
            other = colorList[OFF_LIGHT],
            alpha = (1f - lightValue),
        )

    return IndicatorLight(
        color = color,
        glow = lightValue
    )
}

/**
 * Function that calculates the indicator state
 * by accessing different fields from GameStateUiState.
 * Must be called from within a derivedStateOf lambda
 * so that superfluous recompositions are not triggered.
 * (Called from LineIndicators @Composable)
 */
private fun getIndicatorState(
    lineId: Int,
    lineLength: Int,
    getCompletedLines: () -> List<Int>,
    getAvailableLines: () -> Set<Int>,
    getPositionsNotInLine: (Int, Int) -> List<BoardPosition>,
): IndicatorState {
    val availableLines = getAvailableLines()
    //If there is no available lines, return OFF
    if (availableLines.isEmpty()) return IndicatorState.OFF
    val completedLines = getCompletedLines()
    val nextLines = getLineIdsFromPositions(
        lineLength = lineLength,
        targetPositions = getPositionsNotInLine(lineLength, completedLines.size)
    )
        .filter { it in availableLines }
    return when (lineId) {
        in completedLines -> IndicatorState.COMPLETED
        in nextLines -> IndicatorState.NEXT
        in availableLines -> IndicatorState.AVAILABLE
        else -> IndicatorState.OFF
    }
}

@Composable
fun LineIndicators(
    isBingoState: Boolean,
    applyOffLights: () -> Boolean,
    getLineLength: () -> Int,
    getCompletedLines: () -> List<Int>,
    getAvailableLines: () -> Set<Int>,
    getPositionsNotInLine: (Int, Int) -> List<BoardPosition>
) {
    val lineLength = getLineLength()
    //Animations from Composable context
    val transition = rememberInfiniteTransition()
    val animatedValues = generateInfiniteAnimations(
        isBingoState = isBingoState,
        transitionProvider = { transition }
    )
    //Colors from Composable context
    val colorList = listOf(
        Theme.colors.grey,      //0 - OFF_LIGHT
        Theme.colors.primary,   //1 - WHITE_LIGHT
        Theme.colors.success    //2 - GREEN_LIGHT
    )
    //Map each indicator with state
    val indicatorMap =
        if (isBingoState) {//Position to state
            generateBingoIndicatorMap()
        } else {            //LineId to state
            generateLineIndicatorMap(
                lineLength = lineLength,
                indicatorStateProvider = { lineId ->
                    getIndicatorState(
                        lineId,
                        lineLength,
                        getCompletedLines,
                        getAvailableLines,
                        getPositionsNotInLine
                    )
                }
            )
        }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
    ) {
        drawIndicators(
            lineLength = lineLength,
            indicatorLightProvider = { lineId ->
                val indicatorState = if(applyOffLights()){
                    IndicatorState.OFF
                }else{
                    val animationKey = lineIdToAnimationKey(
                        lineId, isBingoState, getLineLength
                    )
                    (indicatorMap[animationKey]?: IndicatorState.OFF)
                }
                indicatorState.buildIndicatorLight(
                    colorList, animatedValues
                )
            }
        )
    }
}



//region ANIMATIONS

//Animation constants
private const val BLINK_INTERVAL = 450
private const val INITIAL_VALUE = 0f
private const val TARGET_VALUE = 1f

/**
 * Returns a list with two animated float values in a State.
 * Bingo animations: BINGO ON and BINGO OFF.
 * Line animations: AVAILABLE and NEXT.
 */
@Composable
private fun generateInfiniteAnimations(
    isBingoState: Boolean,
    transitionProvider: () -> InfiniteTransition,
): List<State<Float>> =
    mutableListOf<State<Float>>().also { resultList ->
        if (isBingoState) {
            repeat(times = 2) { position ->
                resultList.add(
                    BingoIndicatorAnimatedValue(
                        transitionProvider = transitionProvider,
                        position = position
                    )
                )
            }
        } else {
            //INDEX 0 -> AVAILABLE LINE ANIMATION
            resultList.add(
                AvailableLineIndicatorAnimatedValue(transitionProvider)
            )
            //INDEX 1 -> NEXT LINE ANIMATION
            resultList.add(
                NextLineIndicatorAnimatedValue(transitionProvider)
            )
        }
    }

//Animated values
/**
 * Infinite-animated value for a slow-paced blinking light.
 * Position determines the initial value: ON or OFF.
 */
@Composable
private fun BingoIndicatorAnimatedValue(
    position: Int,
    transitionProvider: () -> InfiniteTransition,
): State<Float> =
    transitionProvider().animateFloat(
        initialValue = (position + 1) % 2f,
        targetValue = position.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = BLINK_INTERVAL * 2,
            ),
            repeatMode = RepeatMode.Reverse
        )
    )

/**
 * Infinite-animated value for 3 medium-paced blinks (0.90 sec/blink)
 * followed by a steady light for a long interval (4.95 sec)
 * Position determines the initial value: ON or OFF.
 */
@Composable
private fun AvailableLineIndicatorAnimatedValue(
    transitionProvider: () -> InfiniteTransition,
): State<Float> =
    transitionProvider().let { transition ->
        val duration = BLINK_INTERVAL * 17
        transition.animateFloat(
            initialValue = INITIAL_VALUE,
            targetValue = INITIAL_VALUE,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = duration
                    TARGET_VALUE at BLINK_INTERVAL
                    INITIAL_VALUE at BLINK_INTERVAL * 2
                    TARGET_VALUE at BLINK_INTERVAL * 3
                    INITIAL_VALUE at BLINK_INTERVAL * 4
                    TARGET_VALUE at BLINK_INTERVAL * 5
                    TARGET_VALUE at duration - BLINK_INTERVAL
                },
            )
        )
    }

/**
 * Infinite-animated value for a fast-paced blinking light.
 */
@Composable
private fun NextLineIndicatorAnimatedValue(
    transitionProvider: () -> InfiniteTransition,
): State<Float> =
    transitionProvider().animateFloat(
        initialValue = INITIAL_VALUE,
        targetValue = TARGET_VALUE,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = BLINK_INTERVAL / 2
            ),
            repeatMode = RepeatMode.Reverse
        )
    )

//Indicator state mappers
@Composable
private fun generateBingoIndicatorMap() = mapOf(
    0 to IndicatorState.BINGO_ON,
    1 to IndicatorState.BINGO_OFF,
)

@Composable
private fun generateLineIndicatorMap(
    lineLength: Int,
    indicatorStateProvider: (Int) -> IndicatorState
): Map<Int, IndicatorState> =
    mutableMapOf<Int, IndicatorState>()
        .also { resultMap ->
            for (direction in AVAILABLE_DIRECTIONS) {
                repeat(lineLength) { index ->
                    lineIdBuilder(
                        direction = direction,
                        position = index
                    )?.let { lineId ->
                        resultMap.addState(lineId, indicatorStateProvider)
                    }
                }
            }
        }
//endregion

/**
 * Composable to store the state for each indicator as derived state.
 */
@Composable
private fun MutableMap<Int, IndicatorState>.addState(
    lineId: Int,
    indicatorStateProvider: (Int) -> IndicatorState
){
    val indicatorState by remember {
        derivedStateOf {
            indicatorStateProvider(lineId)
        }
    }
    if(indicatorState == IndicatorState.OFF){
        this.remove(lineId)
    }else{
        this[lineId] = indicatorState
    }
}

//region ANIMATION INTERPRETER FOR DRAWING PHASE
/**
 * Indicator state is generally mapped by lineId.
 * However, for bingo state, there are only two states
 * for alternating blinking lights.
 * In this case, this function converts lineId into 0 or 1
 * to access its corresponding animated value.
 */
private fun lineIdToAnimationKey(
    lineId: Int,
    isBingoState: Boolean,
    getLineLength: () -> Int
): Int = if (isBingoState) {
    getDirectionAndIndexFromLineId(lineId)
        .let { (direction, index) ->
            if (!direction.isDiagonal()) {
                (index + 1) % 2
            } else if (
                direction == LineDirection.LEFT_DIAGONAL()
                || getLineLength() % 2 != 0
            ) {
                0//Starting on
            } else {
                1//Starting off
            }
        }
} else {
    lineId
}
//endregion

//region DRAWING FUNCTIONS
private fun DrawScope.drawIndicators(
    lineLength: Int,
    indicatorLightProvider: (Int) -> IndicatorLight
) {
    this.cornerIndicators(
        indicatorLightProvider = indicatorLightProvider
    )
    this.sideIndicators(
        lineLength = lineLength,
        indicatorLightProvider = indicatorLightProvider
    )
}

private fun DrawScope.cornerIndicators(
    indicatorLightProvider: (Int) -> IndicatorLight
) = repeat(4) { index -> //each corner
    getCornerLineId(index)
        ?.let { lineId ->
            val indicatorLight = indicatorLightProvider(lineId)
            this.lineIndicator(
                indicatorLight.color,
                indicatorLight.glow,
                offset = this.getCornerIndicatorOffset(index)
            )
        }
}

private fun getCornerLineId(
    index: Int,
): Int? = lineIdBuilder(
    direction = if (index !in 1..2) {
        LineDirection.LEFT_DIAGONAL()
    } else {
        LineDirection.RIGHT_DIAGONAL()
    }
)

private fun DrawScope.getCornerIndicatorOffset(
    index: Int
): Offset {
    val xOffset = if (index % 2 == 0) {
        size.minDimension * CORNER_OFFSET
    } else {
        size.minDimension * (1 - CORNER_OFFSET)
    }
    val yOffset = if (index < 2) {
        size.minDimension * CORNER_OFFSET
    } else {
        size.minDimension * (1 - CORNER_OFFSET)
    }
    return Offset(
        x = xOffset,
        y = yOffset,
    )
}

private fun DrawScope.sideIndicators(
    lineLength: Int,
    indicatorLightProvider: (Int) -> IndicatorLight
) =
    repeat(2) { direction -> //rows and columns
        repeat(lineLength) { index ->
            getSideLineId(
                horizontalIndicator = (direction == H_INDICATORS),
                index = index,
            )?.let { lineId ->
                repeat(2) { position ->
                    val indicatorLight = indicatorLightProvider(lineId)
                    this.lineIndicator(
                        indicatorLight.color,
                        indicatorLight.glow,
                        offset = this.getSideIndicatorOffset(
                            horizontalIndicator = (direction == H_INDICATORS),
                            startIndicator = (position == START_INDICATOR),
                            index = index,
                            lineLength = lineLength,
                        )
                    )
                }
            }
        }
    }

private fun getSideLineId(
    horizontalIndicator: Boolean,
    index: Int,
): Int? = lineIdBuilder(
    direction = if (horizontalIndicator) {
        LineDirection.HORIZONTAL()
    } else {
        LineDirection.VERTICAL()
    },
    position = index
)

private fun DrawScope.getSideIndicatorOffset(
    horizontalIndicator: Boolean,
    startIndicator: Boolean,
    index: Int,
    lineLength: Int,
): Offset {
    val sideOffset = size.minDimension * SIDE_OFFSET
    val sideEndOffset = size.minDimension * (1 - SIDE_OFFSET)
    val marginOffset =
        size.minDimension * (FRAME_WIDTH + (INNER_SIZE / lineLength * (index + 0.5f)))

    return Offset(
        x = if (!horizontalIndicator) {
            marginOffset
        } else if (startIndicator) {
            sideOffset
        } else {
            sideEndOffset
        },
        y = if (horizontalIndicator) {
            marginOffset
        } else if (startIndicator) {
            sideOffset
        } else {
            sideEndOffset
        },
    )
}

/**
 * Main drawing function for light indicator.
 */
private fun DrawScope.lineIndicator(
    color: Color,
    glow: Float,
    offset: Offset,
) {
    val glowSize = glow * 0.5f + 0.8f
    //Outer glow
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to color,
                0.75f to color.withAlpha(0.5f),
                1.0f to Color.Transparent
            ),
            radius = size.minDimension * (INDICATOR_SIZE * glowSize),
            center = offset,
        ),
        radius = size.minDimension * (INDICATOR_SIZE * glowSize),
        center = offset,
    )

    //Plain inner color
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to color + Color.White.withAlpha(0.4f),
                0.3f to color,
                0.7f to color.withAlpha(0.9f),
                1f to color + Color.Black,
            ),
            radius = size.minDimension * INDICATOR_SIZE,
            center = offset,
        ),
        radius = size.minDimension * INDICATOR_SIZE,
        center = offset,
    )
}
//endregion

//endregion

//region BLOCK FRAME
@Composable
private fun RectFrame(
    containerWidthInPx: Float,
    isBlocked: Boolean
) {
    val frameColor = if (isBlocked) {
        Theme.colors.error
    } else {
        Theme.colors.secondary.withAlpha(0.6f)
    }
    val contrastColor = frameColor + Theme.colors.secondary
    Canvas(modifier = Modifier.fillMaxSize()) {
        //Outer frame
        drawRectFrame(
            scale = 0.92f,
            color = if (isBlocked) {
                frameColor
            } else {
                contrastColor
            },
            size = size,
            strokeWidth = if (isBlocked) {
                //3f
                containerWidthInPx / 120f
            } else {
                //12f
                containerWidthInPx / 50f
            }
        )

        //Inner frame
        drawRectFrame(
            scale = 0.88f,
            color = if (isBlocked) {
                contrastColor
            } else {
                frameColor
            },
            size = size,
            strokeWidth = if (isBlocked) {
                containerWidthInPx / 60f
            } else {
                containerWidthInPx / 90f
            }
        )
    }
}

//DRAWING FUNCTION
private fun DrawScope.drawRectFrame(
    scale: Float,
    color: Color,
    size: Size,
    strokeWidth: Float,
) = this.scale(scale = scale) {
    drawRect(
        color = color,
        size = size,
        style = Stroke(strokeWidth)
    )
}
//endregion