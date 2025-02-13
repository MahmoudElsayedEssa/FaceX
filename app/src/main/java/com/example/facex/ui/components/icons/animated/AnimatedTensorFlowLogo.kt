package com.example.facex.ui.components.icons.animated


import androidx.compose.animation.animateColor
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.floor


data class CharOrder(
    val char: Char,
    val order: Int
)

data class CubePosition(
    val x: Int,
    val y: Int,
    val z: Int,
    val letters: Set<CharOrder>
)

val logoStructure = listOf(
    // Top horizontal bar (front to back)
    CubePosition(
        0, 0, 0, setOf(
            CharOrder('L', 4),
            CharOrder('T', 3),
            CharOrder('F', 4)
        )
    ),
    CubePosition(
        0, -1, 0, setOf(
            CharOrder('L', 5),
            CharOrder('T', 4),
            CharOrder('F', 3)
        )
    ),
    CubePosition(
        1, -1, 0, setOf(
            CharOrder('L', 6),
            CharOrder('T', 5),
            CharOrder('F', 2)
        )
    ),
    CubePosition(
        2, -1, 0, setOf(
            CharOrder('L', 7),
            CharOrder('T', 6),
            CharOrder('F', 1)
        )
    ),

//                // Vertical part (top to bottom)
    CubePosition(
        0, 1, 0, setOf(
            CharOrder('L', 2),
            CharOrder('T', 2),
        )
    ),
    CubePosition(
        0, 2, 0, setOf(
            CharOrder('L', 1),
            CharOrder('T', 1)
        )
    ),

//                 T vertical column (top to bottom)
    CubePosition(
        0, 0, -1, setOf(
            CharOrder('L', 3),
            CharOrder('T', 7),
            CharOrder('F', 5)
        )
    ),
    CubePosition(
        0, 0, -2, setOf(
            CharOrder('T', 8),
            CharOrder('F', 6)
        )
    ),
    CubePosition(
        0, 0, -3, setOf(
            CharOrder('T', 9),
            CharOrder('F', 7)
        )
    ),
    CubePosition(
        0, 0, -4, setOf(
            CharOrder('T', 10),
            CharOrder('F', 8)
        )
    ),

    // F extension
    CubePosition(
        1, 0, -2, setOf(
            CharOrder('F', 9)
        )
    )
)

@Composable
fun AnimatedTensorFlowLogo(
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF4184F3),
    lightColor: Color = Color(0xFFAACBFB)
) {
    val transition = rememberInfiniteTransition(label = "enhanced_logo_transition")

    val maxTSteps = logoStructure.count { it.letters.any { char -> char.char == 'T' } }
    val maxFSteps = logoStructure.count { it.letters.any { char -> char.char == 'F' } }
    val maxLSteps = logoStructure.count { it.letters.any { char -> char.char == 'L' } }

    val cubeDuration = 300
    val initialFadeDuration = 2500
    val letterDuration = maxOf(maxTSteps, maxFSteps, maxLSteps) * cubeDuration * 2
    val totalDuration = (letterDuration * 3) * 2 - letterDuration * 2


    val initialColor = transition.animateColor(
        initialValue = lightColor,
        targetValue = primaryColor,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = initialFadeDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "enhanced_initial_color"
    )

    val phaseProgress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(totalDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "enhanced_phase_animation"
    )

    val tCubeColors = createCubeColors(
        transition,
        maxTSteps,
        cubeDuration,
        letterDuration,
        lightColor,
        primaryColor,
        initialFadeDuration
    )
    val fCubeColors = createCubeColors(
        transition,
        maxFSteps,
        cubeDuration,
        letterDuration,
        lightColor,
        primaryColor,
        initialFadeDuration + letterDuration
    )
    val lCubeColors = createCubeColors(
        transition,
        maxLSteps,
        cubeDuration,
        letterDuration,
        lightColor,
        primaryColor,
        initialFadeDuration + letterDuration * 2
    )

    val currentPhase = floor(phaseProgress.value).toInt()

    fun getFaceColor(letters: Set<CharOrder>): Color {
        return when (currentPhase) {
            0 -> initialColor.value
            1 -> letters.find { it.char == 'T' }?.let { tCubeColors[it.order - 1].value }
                ?: lightColor

            2 -> letters.find { it.char == 'F' }?.let { fCubeColors[it.order - 1].value }
                ?: lightColor

            3 -> letters.find { it.char == 'L' }?.let { lCubeColors[it.order - 1].value }
                ?: lightColor

            else -> lightColor
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val canvasWidth = constraints.maxWidth.toFloat()
        val canvasHeight = constraints.maxHeight.toFloat()

        Box(Modifier.align(Alignment.Center)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cubeSize = minOf(canvasWidth / 8, canvasHeight / 6)
                val isometricFactor = 0.5773503f

                fun gridToIso(x: Int, y: Int, z: Int): Pair<Float, Float> {
                    val isoX = (x - y) * cubeSize
                    val isoY = ((x + y) * cubeSize * isometricFactor) - (z * cubeSize)
                    return Pair(
                        isoX + canvasWidth / 4,
                        isoY + canvasHeight / 4
                    )
                }

                logoStructure.forEach { cube ->
                    val (x, y) = gridToIso(cube.x, cube.y, cube.z)

                    val frontPath = Path().apply {
                        moveTo(x, y)
                        lineTo(x + cubeSize, y + cubeSize * isometricFactor)
                        lineTo(x + cubeSize, y + cubeSize * (1 + isometricFactor))
                        lineTo(x, y + cubeSize)
                        close()
                    }

                    val topPath = Path().apply {
                        moveTo(x, y)
                        lineTo(x + cubeSize, y + cubeSize * isometricFactor)
                        lineTo(x + cubeSize * 2, y)
                        lineTo(x + cubeSize, y - cubeSize * isometricFactor)
                        close()
                    }

                    val rightPath = Path().apply {
                        moveTo(x + cubeSize, y + cubeSize * isometricFactor)
                        lineTo(x + cubeSize * 2, y)
                        lineTo(x + cubeSize * 2, y + cubeSize)
                        lineTo(x + cubeSize, y + cubeSize * (1 + isometricFactor))
                        close()
                    }

                    val cubeColor = getFaceColor(cube.letters)
                    drawPath(frontPath, cubeColor)
                    drawPath(topPath, cubeColor)
                    drawPath(rightPath, cubeColor)
                }
            }
        }
    }
}

@Composable
private fun createCubeColors(
    transition: InfiniteTransition,
    maxSteps: Int,
    cubeDuration: Int,
    letterDuration: Int,
    lightColor: Color,
    primaryColor: Color,
    startOffset: Int
): List<State<Color>> {
    val totalLightingTime = cubeDuration * maxSteps // Time to light up all cubes
    val pauseTime = 500 // Pause time (adjust as needed)
    val fadeOutTime = cubeDuration * maxSteps // Time to fade out all cubes sequentially

    return List(maxSteps) { order ->
        transition.animateColor(
            initialValue = lightColor,
            targetValue = primaryColor,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = totalLightingTime + pauseTime + fadeOutTime
                    primaryColor at (order * cubeDuration) // Sequential lighting up
                    primaryColor at (totalLightingTime + pauseTime) // Hold after all are lit
                    lightColor at (totalLightingTime + pauseTime + (maxSteps - order - 1) * cubeDuration) // Reverse fade-out
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(startOffset)
            ),
            label = "cube_${order}_color"
        )
    }
}


@Preview
@Composable
fun AnimatedTensorFlowLogoPreview() {
    Box(
        modifier = Modifier
            .size(500.dp)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        AnimatedTensorFlowLogo(
            Modifier.size(200.dp)
        )
    }
}