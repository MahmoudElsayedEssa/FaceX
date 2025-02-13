package com.example.facex.ui.screens.modelcontrol.components


import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SaveButtonState {
    INITIAL, SHRINKING, PROCESSING, PROCESSING_TO_DONE, DONE, SAVED, ERROR
}

@Composable
fun SaveButton(
    onSave:  () -> Unit, modifier: Modifier = Modifier,
    key: Any? = null // Add key parameter
) {
    var buttonState by remember(key) { mutableStateOf(SaveButtonState.INITIAL) }

    val scope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme
    val hapticFeedback = LocalHapticFeedback.current

    // Enhanced color transitions
    val containerColor by animateColorAsState(
        targetValue = when (buttonState) {
            SaveButtonState.INITIAL -> colorScheme.secondaryContainer
            SaveButtonState.SHRINKING -> colorScheme.secondaryContainer
            SaveButtonState.PROCESSING, SaveButtonState.PROCESSING_TO_DONE, SaveButtonState.DONE -> colorScheme.primary.copy(
                alpha = 0.9f
            )

            SaveButtonState.SAVED -> Color(0xFF2E7D32)
            SaveButtonState.ERROR -> colorScheme.errorContainer
        }, animationSpec = tween(300)
    )

    val contentColor by animateColorAsState(
        targetValue = when (buttonState) {
            SaveButtonState.INITIAL, SaveButtonState.SHRINKING -> colorScheme.onSecondaryContainer

            SaveButtonState.PROCESSING, SaveButtonState.PROCESSING_TO_DONE, SaveButtonState.DONE -> Color.White

            SaveButtonState.SAVED -> Color.White
            SaveButtonState.ERROR -> colorScheme.onErrorContainer
        }, animationSpec = tween(300)
    )

    val transition = updateTransition(
        targetState = buttonState, label = "ButtonTransition"
    )

    // Button size animations with bounce effect
    val buttonWidth by transition.animateFloat(label = "ButtonWidth", transitionSpec = {
        when {
            SaveButtonState.INITIAL isTransitioningTo SaveButtonState.SHRINKING -> tween(
                200, easing = FastOutSlowInEasing
            )

            SaveButtonState.SHRINKING isTransitioningTo SaveButtonState.PROCESSING -> spring(
                dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessVeryLow
            )

            SaveButtonState.DONE isTransitioningTo SaveButtonState.SAVED -> spring(
                dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessVeryLow
            )

            else -> tween(100, easing = FastOutSlowInEasing)
        }
    }) { state ->
        when (state) {
            SaveButtonState.INITIAL -> 92f
            SaveButtonState.SHRINKING -> 72f
            SaveButtonState.PROCESSING, SaveButtonState.PROCESSING_TO_DONE, SaveButtonState.DONE -> 40f
            SaveButtonState.SAVED -> 108f
            SaveButtonState.ERROR -> 108f
        }
    }

    // Corner radius animation
    val cornerRadius by transition.animateFloat(label = "CornerRadius", transitionSpec = {
        when {
            SaveButtonState.SHRINKING isTransitioningTo SaveButtonState.PROCESSING -> spring(
                dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
            )

            else -> tween(200, easing = FastOutSlowInEasing)
        }
    }) { state ->
        when (state) {
            SaveButtonState.INITIAL, SaveButtonState.SHRINKING -> 16f
            SaveButtonState.PROCESSING, SaveButtonState.PROCESSING_TO_DONE, SaveButtonState.DONE -> 20f
            SaveButtonState.SAVED -> 16f
            SaveButtonState.ERROR -> 16f
        }
    }

    // Save text animations
    val textSlideOffset by transition.animateFloat(label = "TextSlide", transitionSpec = {
        tween(200, easing = FastOutSlowInEasing)
    }) { state ->
        when (state) {
            SaveButtonState.INITIAL -> 0f
            else -> -48f
        }
    }

    val textAlpha by transition.animateFloat(label = "TextAlpha", transitionSpec = {
        tween(150, easing = LinearEasing)
    }) { state ->
        when (state) {
            SaveButtonState.INITIAL -> 1f
            else -> 0f
        }
    }

    val progressAlpha by transition.animateFloat(label = "ProgressAlpha", transitionSpec = {
        when {
            SaveButtonState.SHRINKING isTransitioningTo SaveButtonState.PROCESSING -> tween(
                200, delayMillis = 200
            )

            SaveButtonState.PROCESSING isTransitioningTo SaveButtonState.PROCESSING_TO_DONE -> tween(
                300, delayMillis = 300, easing = LinearOutSlowInEasing
            )

            else -> tween(150)
        }
    }) { state ->
        when (state) {
            SaveButtonState.PROCESSING -> 1f
            SaveButtonState.PROCESSING_TO_DONE -> 0f
            else -> 0f
        }
    }

    val checkScale by transition.animateFloat(label = "CheckScale", transitionSpec = {
        when {
            SaveButtonState.PROCESSING isTransitioningTo SaveButtonState.PROCESSING_TO_DONE -> spring(
                dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
            )

            else -> tween(150)
        }
    }) { state ->
        when (state) {
            // Start very small to appear in center
            SaveButtonState.PROCESSING_TO_DONE -> 0.1f
            // Grow to full size
            SaveButtonState.DONE -> 1f
            SaveButtonState.SAVED -> 1f
            else -> 0f
        }
    }
    // Saved text animation
    val savedTextAlpha by transition.animateFloat(label = "SavedTextAlpha", transitionSpec = {
        tween(200, delayMillis = 200, easing = FastOutSlowInEasing)
    }) { state ->
        if (state == SaveButtonState.SAVED) 1f else 0f
    }

    Surface(
        modifier = modifier
            .height(40.dp)
            .width(buttonWidth.dp),
        shape = RoundedCornerShape(cornerRadius.dp),
        color = containerColor,
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .noRippleClickable {
                    if (buttonState == SaveButtonState.INITIAL) {
                        scope.launch {
                            buttonState = SaveButtonState.SHRINKING
                            delay(200)
                            buttonState = SaveButtonState.PROCESSING
                            try {
                                onSave()
                                delay(500)
                                buttonState = SaveButtonState.PROCESSING_TO_DONE
                                delay(300)
                                buttonState = SaveButtonState.DONE
                                delay(500)
                                buttonState = SaveButtonState.SAVED
                            } catch (e: Exception) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                buttonState = SaveButtonState.ERROR
                                delay(2000)
                                buttonState = SaveButtonState.INITIAL
                            }
                        }
                    }
                }, contentAlignment = Alignment.Center
        ) {
            if (textAlpha > 0f) {
                Text(text = "Save",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.graphicsLayer {
                        alpha = textAlpha
                        translationY = textSlideOffset
                    })
            }



            if (progressAlpha > 0f) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer {
                            alpha = progressAlpha
                            // Add slight scale animation as it fades out
                            scaleX = progressAlpha.coerceIn(0.8f, 1f)
                            scaleY = progressAlpha.coerceIn(0.8f, 1f)
                        },
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                    strokeWidth = 2.5.dp
                )
            }


            // Error state
            if (buttonState == SaveButtonState.ERROR) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = "Failed",
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor
                    )
                }
            }

            if (checkScale > 0f) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .scale(checkScale),
                        tint = contentColor
                    )

                    if (buttonState == SaveButtonState.SAVED) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Saved", style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Medium
                        ), color = contentColor, modifier = Modifier.graphicsLayer {
                            alpha = savedTextAlpha
                        })
                    }
                }
            }


        }
    }
}


fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}

// Utility function to remove ripple effect
@Preview(showBackground = true)
@Composable
fun ApplyButtonPreview() {

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {

        SaveButton(
            onSave = {}, modifier = Modifier.align(Alignment.Center)
        )
    }
}