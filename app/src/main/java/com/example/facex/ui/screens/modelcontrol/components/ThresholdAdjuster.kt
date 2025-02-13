package com.example.facex.ui.screens.modelcontrol.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThresholdAdjuster(
    threshold: Float, onThresholdChanged: (Float) -> Unit, modifier: Modifier = Modifier
) {
    var scaleX by remember { mutableFloatStateOf(1f) }
    var scaleY by remember { mutableFloatStateOf(1f) }
    var translateX by remember { mutableFloatStateOf(0f) }
    var transformOrigin by remember { mutableStateOf(TransformOrigin.Center) }
    val density = LocalDensity.current

    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Threshold",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            AnimatedThresholdValue(
                threshold = threshold, modifier = Modifier.width(48.dp)
            )
        }

        AnimatedFeedbackText(
            threshold = threshold, modifier = Modifier
                .fillMaxWidth()
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            Slider(value = threshold,
                onValueChange = onThresholdChanged,
                valueRange = 0f..1f,
                modifier = Modifier
                    .graphicsLayer {
                        this.transformOrigin = transformOrigin
                        this.scaleX = scaleX
                        this.scaleY = scaleY
                        this.translationX = translateX
                    }
                    .padding(horizontal = 16.dp),
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary, shape = CircleShape
                            )
                    )
                },
                track = { sliderState ->
                    Box(modifier = Modifier
                        .observeOverscroll(value = threshold) { overslide ->
                            transformOrigin = TransformOrigin(
                                pivotFractionX = if (threshold < .5f) 2f else -1f,
                                pivotFractionY = .5f,
                            )

                            when {
                                threshold < .5f -> {
                                    scaleY = 1f + (overslide * .2f)
                                    scaleX = 1f - (overslide * .2f)
                                }

                                else -> {
                                    scaleY = 1f - (overslide * .2f)
                                    scaleX = 1f + (overslide * .2f)
                                }
                            }

                            translateX = with(density) { overslide * 24.dp.toPx() }
                        }
                        .height(4.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)) {
                        // Track progress without animation
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(threshold)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                })
        }
    }
}


@Composable
private fun Modifier.observeOverscroll(
    value: Float,
    onOverscrollChange: (Float) -> Unit,
): Modifier {
    val valueState = rememberUpdatedState(value)
    val scope = rememberCoroutineScope()
    val overscrollAnimation = remember { Animatable(0f) }
    var length by remember { mutableFloatStateOf(1f) }

    // Use a more efficient way to handle overscroll updates
    LaunchedEffect(overscrollAnimation.value) {
        val normalizedValue = overscrollAnimation.value / length
        val easedValue = CubicBezierEasing(0.5f, 0.5f, 1.0f, 0.25f).transform(normalizedValue)
        onOverscrollChange(easedValue)
    }

    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr

    return onSizeChanged { length = it.width.toFloat() }.pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown()
            awaitHorizontalTouchSlopOrCancellation(down.id) { _, _ -> }
            var overscrollDelta: Float
            var lastValue = 0f

            horizontalDrag(down.id) {
                if (it.positionChange() != Offset.Zero) {
                    val deltaX = it.positionChange().x * if (isLtr) 1f else -1f

                    // More efficient overscroll calculation
                    overscrollDelta = when (valueState.value) {
                        0f -> (lastValue + deltaX / length).coerceAtMost(0f)
                        1f -> (lastValue + deltaX / length).coerceAtLeast(0f)
                        else -> 0f
                    }

                    // Update without animation during drag
                    scope.launch {
                        overscrollAnimation.snapTo(overscrollDelta * length)
                    }

                    lastValue = overscrollDelta
                }
            }

            // Only animate at the end of drag
            if (lastValue != 0f) {
                scope.launch {
                    overscrollAnimation.animateTo(
                        targetValue = 0f, animationSpec = spring(
                            dampingRatio = 0.45f, stiffness = Spring.StiffnessLow
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedFeedbackText(threshold: Float, modifier: Modifier = Modifier) {
    val intensityWord = remember(threshold) {
        when {
            threshold < 0.3f -> "Low"
            threshold < 0.7f -> "Medium"
            else -> "High"
        }
    }

    val targetColor = when {
        threshold < 0.3f -> MaterialTheme.colorScheme.error
        threshold < 0.7f -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.tertiary
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "color"
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            // Animate the intensity word (Low/Medium/High)
            AnimatedContent(
                targetState = intensityWord,
                transitionSpec = {
                    (slideInVertically { height -> height / 2 } + fadeIn()) togetherWith
                            (slideOutVertically { height -> -height / 2 } + fadeOut())
                },
                label = "intensity transition"
            ) { word ->
                Row {
                    word.forEachIndexed { index, char ->
                        CharacterWithBlur(
                            char = char,
                            color = animatedColor,
                            delayMillis = index * 50L
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Static "Sensitivity" word with animated characters
            "Sensitivity".forEachIndexed { index, char ->
                CharacterWithBlur(
                    char = char,
                    color = animatedColor,
                    delayMillis = (intensityWord.length + index) * 50L
                )
            }
        }
    }
}


@Composable
private fun AnimatedThresholdValue(
    threshold: Float,
    modifier: Modifier = Modifier,
    format: String = "%.2f",
    animationSpec: AnimationSpec<IntSize> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
    ),
    blurRadius: Float = 5f,
    scaleFrom: Float = 0.95f,
    scaleTo: Float = 1f
) {
    val formattedNumber = remember(threshold) {
        format.format(threshold)
    }

    Row(
        modifier = modifier.animateContentSize(animationSpec = animationSpec as FiniteAnimationSpec<IntSize>),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        formattedNumber.mapIndexed { index, char ->
            val digit = remember(char, formattedNumber, index) {
                Digit(
                    digitChar = char,
                    fullNumber = formattedNumber.replace(".", "").toIntOrNull() ?: 0,
                    place = index
                )
            }

            AnimatedContent(
                targetState = digit, transitionSpec = {
                    val slideDir = if (targetState > initialState) -1 else 1
                    val slideAnim = if (targetState.digitChar.isDigit()) {
                        slideInVertically { it * slideDir } + fadeIn() togetherWith slideOutVertically { -it * slideDir } + fadeOut()
                    } else {
                        fadeIn() togetherWith fadeOut()
                    }

                    slideAnim.using(SizeTransform(clip = false))
                }, label = "digit"
            ) { digit ->
                DigitDisplay(
                    digit = digit, blurRadius = blurRadius, scaleFrom = scaleFrom, scaleTo = scaleTo
                )
            }
        }
    }
}