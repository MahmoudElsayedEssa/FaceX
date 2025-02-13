import android.graphics.Rect
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import com.example.facex.domain.logger.LogLevel
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.logger.logDebug
import com.example.facex.ui.screens.camera_face_recognition.BoundingBox
import com.example.facex.ui.screens.camera_face_recognition.UIFace
import com.example.facex.ui.screens.camera_face_recognition.UIRecognitionStatus
import com.example.facex.ui.screens.camera_face_recognition.components.FaceBox
import com.example.facex.ui.screens.camera_face_recognition.components.FaceLabel
import kotlin.math.abs

//@Composable
//fun FacesOverlay(
//    modifier: Modifier = Modifier,
//    faces: List<UIFace>,
//    analyzerWidth: Int,
//    analyzerHeight: Int,
//    previewWidth: Int,
//    previewHeight: Int,
//    isFlippedHorizontally: Boolean = false,
//    isLandscape: Boolean,
//    onFaceTap: (UIFace) -> Unit,
//) {
//    Box(modifier = modifier) {
//        val density = LocalDensity.current
//
//        // Adjust analyzer dimensions based on orientation
//        val effectiveAnalyzerWidth = if (isLandscape) analyzerHeight else analyzerWidth
//        val effectiveAnalyzerHeight = if (isLandscape) analyzerWidth else analyzerHeight
//
//        // Calculate scaling factors
//        val scaleX = previewWidth.toDouble() / effectiveAnalyzerWidth
//        val scaleY = previewHeight.toDouble() / effectiveAnalyzerHeight
//        val scale = maxOf(scaleX, scaleY)
//
//        // Calculate aspect ratios and proportions
//        val analyzerRatio = effectiveAnalyzerWidth.toFloat() / effectiveAnalyzerHeight
//        val proportionalDifference = (scale - scaleX) / scaleX
//        val proportionalRatio = proportionalDifference / analyzerRatio
//
//        // Determine inversion based on screen size and orientation
//        val shouldInvertX = when {
//            previewWidth >= 1400 -> proportionalRatio > 0.3  // Large screens
//            previewWidth >= 1100 -> proportionalRatio < 0.3  // Medium screens
//            else -> proportionalRatio > 0.75 && proportionalRatio < 0.77  // Phones
//        }
//
//        // Calculate scaled dimensions and offsets
//        val scaledAnalyzerWidth = effectiveAnalyzerWidth * scale
//        val scaledAnalyzerHeight = effectiveAnalyzerHeight * scale
//        val offsetX = (previewWidth - scaledAnalyzerWidth) / 2
//        val offsetY = (previewHeight - scaledAnalyzerHeight) / 2
//        """
//    Orientation Debug:
//    isLandscape: $isLandscape
//
//    Original Dimensions:
//    analyzerWidth: $analyzerWidth
//    analyzerHeight: $analyzerHeight
//    previewWidth: $previewWidth
//    previewHeight: $previewHeight
//
//    Effective Dimensions:
//    effectiveAnalyzerWidth: $effectiveAnalyzerWidth
//    effectiveAnalyzerHeight: $effectiveAnalyzerHeight
//
//    Scaling Factors:
//    scaleX: $scaleX
//    scaleY: $scaleY
//    scale: $scale
//
//    Ratios:
//    analyzerRatio: $analyzerRatio
//    proportionalDifference: $proportionalDifference
//    proportionalRatio: $proportionalRatio
//
//    Scaled Dimensions:
//    scaledAnalyzerWidth: ${effectiveAnalyzerWidth * scale}
//    scaledAnalyzerHeight: ${effectiveAnalyzerHeight * scale}
//    offsetX: ${(previewWidth - effectiveAnalyzerWidth * scale) / 2}
//    offsetY: ${(previewHeight - effectiveAnalyzerHeight * scale) / 2}
//""".trimIndent()
//
//        faces.fastForEach { face ->
//            with(face.boundingBox) {
//                // Adjust face box dimensions based on orientation
//                val (boxWidth, boxHeight) = if (isLandscape) {
//                    height to width
//                } else {
//                    width to height
//                }
//
//                val scaledWidth = boxWidth * scale.toFloat()
//                val scaledHeight = boxHeight * scale.toFloat()
//
//                // Calculate position with orientation consideration
//                val effectiveLeft = getEffectiveLeft(
//                    analyzerWidth = effectiveAnalyzerWidth, shouldInvertX = shouldInvertX
//                )
//
//                val effectiveTop = if (isLandscape) {
//                    effectiveAnalyzerHeight - right
//                } else {
//                    top
//                }
//
//                val scaledLeft = (effectiveLeft * scale.toFloat()) + offsetX.toFloat()
//                val scaledTop = (effectiveTop * scale.toFloat()) + offsetY.toFloat()
//
//                // Apply horizontal flip if needed
//                val finalLeft = if (isFlippedHorizontally) {
//                    previewWidth - scaledLeft - scaledWidth
//                } else {
//                    scaledLeft
//                }
//
//                Box(
//                    Modifier
//                        .offset(x = with(density) { finalLeft.toDp() },
//                            y = with(density) { scaledTop.toDp() })
//                        .width(with(density) { scaledWidth.toDp() })
//                        .height(with(density) { scaledHeight.toDp() })
//                        .clickable { onFaceTap(face) }) {
//                    FaceBox(
//                        width = scaledWidth,
//                        height = scaledHeight,
//                        isRecognized = face.recognitionState is UIRecognitionStatus.Known
//                    )
//
//                    AnimatedVisibility(
//                        visible = true,
//                        enter = fadeIn() + expandVertically(),
//                        modifier = Modifier
//                            .align(Alignment.TopCenter)
//                            .offset(y = with(density) {
//                                val offset =
//                                    -scaledHeight * 0.15f - (20f * (1f - scaledHeight / 500f))
//                                offset
//                                    .fastCoerceAtMost(-scaledHeight * 0.05f)
//                                    .toDp()
//                            })
//                    ) {
//                        if (face.recognitionState is UIRecognitionStatus.Known) {
//                            FaceLabel(
//                                text = face.recognitionState.name,
//                                isRecognized = true,
//                                faceWidth = scaledWidth,
//                                faceHeight = scaledHeight
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}



@Composable
private fun rememberScalingParameters(
    previewWidth: Int,
    previewHeight: Int,
    isLandscape: Boolean
): ScalingParameters {
    return remember(previewWidth, previewHeight, isLandscape) {
        val effectiveAnalyzerWidth = if (isLandscape) ANALYZER_HEIGHT else ANALYZER_WIDTH
        val effectiveAnalyzerHeight = if (isLandscape) ANALYZER_WIDTH else ANALYZER_HEIGHT

        val scaleX = previewWidth.toDouble() / effectiveAnalyzerWidth
        val scaleY = previewHeight.toDouble() / effectiveAnalyzerHeight
        val scale = maxOf(scaleX, scaleY)

        val analyzerRatio = effectiveAnalyzerWidth.toFloat() / effectiveAnalyzerHeight
        val proportionalDifference = (scale - scaleX) / scaleX
        val proportionalRatio = proportionalDifference / analyzerRatio

        val shouldInvertX = when {
            previewWidth >= 1400 -> proportionalRatio > 0.3
            previewWidth >= 1100 -> proportionalRatio < 0.3
            else -> proportionalRatio > 0.75 && proportionalRatio < 0.77
        }

        val scaledAnalyzerWidth = effectiveAnalyzerWidth * scale
        val scaledAnalyzerHeight = effectiveAnalyzerHeight * scale
        val offsetX = ((previewWidth - scaledAnalyzerWidth) / 2).toFloat()
        val offsetY = ((previewHeight - scaledAnalyzerHeight) / 2).toFloat()

        ScalingParameters(
            scale = scale,
            offsetX = offsetX,
            offsetY = offsetY,
            shouldInvertX = shouldInvertX,
            effectiveAnalyzerWidth = effectiveAnalyzerWidth,
            effectiveAnalyzerHeight = effectiveAnalyzerHeight
        )
    }
}

// Constants for analyzer dimensions
private const val ANALYZER_WIDTH = 480
private const val ANALYZER_HEIGHT = 640
private const val ANIMATION_DURATION = 50
private const val ANIMATION_MIN_CHANGE = 2f // Increased threshold to 2 pixels
private const val SIZE_CHANGE_THRESHOLD = 5f // Threshold for size changes

@Stable
private data class ScalingParameters(
    val scale: Double,
    val offsetX: Float,
    val offsetY: Float,
    val shouldInvertX: Boolean,
    val effectiveAnalyzerWidth: Int,
    val effectiveAnalyzerHeight: Int
)

@Stable
private data class FaceDisplayParameters(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
)


@Composable
private fun FaceOverlayItem(
    face: UIFace,
    displayParams: FaceDisplayParameters,
    onFaceTap: (UIFace) -> Unit,
    density: Density
) {
    // Track previous values
    var previousLeft by remember { mutableFloatStateOf(displayParams.left) }
    var previousTop by remember { mutableFloatStateOf(displayParams.top) }
    var previousWidth by remember { mutableFloatStateOf(displayParams.width) }
    var previousHeight by remember { mutableFloatStateOf(displayParams.height) }

    // Create fast animation spec
    val positionAnimSpec = remember {
        tween<Float>(
            durationMillis = ANIMATION_DURATION,
            easing = LinearEasing // Fastest easing
        )
    }

    // Even faster spec for size changes
    val sizeAnimSpec = remember {
        tween<Float>(
            durationMillis = ANIMATION_DURATION / 2,
            easing = LinearEasing
        )
    }

    // Calculate position changes
    val leftDelta = abs(displayParams.left - previousLeft)
    val topDelta = abs(displayParams.top - previousTop)
    val widthDelta = abs(displayParams.width - previousWidth)
    val heightDelta = abs(displayParams.height - previousHeight)

    // Determine if we should animate or snap
    val shouldAnimatePosition = remember(leftDelta, topDelta) {
        leftDelta > ANIMATION_MIN_CHANGE || topDelta > ANIMATION_MIN_CHANGE
    }
    val shouldAnimateSize = remember(widthDelta, heightDelta) {
        widthDelta > SIZE_CHANGE_THRESHOLD || heightDelta > SIZE_CHANGE_THRESHOLD
    }

    // Update previous values
    LaunchedEffect(displayParams) {
        previousLeft = displayParams.left
        previousTop = displayParams.top
        previousWidth = displayParams.width
        previousHeight = displayParams.height
    }

    // Animated or snap values based on change magnitude
    val left = if (shouldAnimatePosition) {
        animateFloatAsState(
            targetValue = displayParams.left,
            animationSpec = positionAnimSpec,
            label = "left"
        ).value
    } else displayParams.left

    val top = if (shouldAnimatePosition) {
        animateFloatAsState(
            targetValue = displayParams.top,
            animationSpec = positionAnimSpec,
            label = "top"
        ).value
    } else displayParams.top

    val width = if (shouldAnimateSize) {
        animateFloatAsState(
            targetValue = displayParams.width,
            animationSpec = sizeAnimSpec,
            label = "width"
        ).value
    } else displayParams.width

    val height = if (shouldAnimateSize) {
        animateFloatAsState(
            targetValue = displayParams.height,
            animationSpec = sizeAnimSpec,
            label = "height"
        ).value
    } else displayParams.height

    // Performance optimization: Skip recomposition for small changes
    val boxModifier = remember(left, top, width, height) {
        Modifier
            .offset(
                x = with(density) { left.toDp() },
                y = with(density) { top.toDp() }
            )
            .width(with(density) { width.toDp() })
            .height(with(density) { height.toDp() })
            .clickable { onFaceTap(face) }
    }

    Box(boxModifier) {
        FaceBox(
            width = width,
            height = height,
            isRecognized = face.recognitionState is UIRecognitionStatus.Known
        )

        if (face.recognitionState is UIRecognitionStatus.Known) {
            val labelOffset = remember(height) {
                val offset = -height * 0.15f - (20f * (1f - height / 500f))
                offset.fastCoerceAtMost(-height * 0.05f)
            }

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(
                    animationSpec = tween(ANIMATION_DURATION / 2)
                ) + expandVertically(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = with(density) { labelOffset.toDp() })
            ) {
                FaceLabel(
                    text = face.recognitionState.name,
                    faceWidth = width,
                    faceHeight = height
                )
            }
        }
    }
}


@Composable
fun FacesOverlay(
    modifier: Modifier = Modifier,
    faces: List<UIFace>,
    previewWidth: Int,
    previewHeight: Int,
    isFlippedHorizontally: Boolean = false,
    isLandscape: Boolean,
    onFaceTap: (UIFace) -> Unit,
) {
    val density = LocalDensity.current
    val params = rememberScalingParameters(
        previewWidth = previewWidth,
        previewHeight = previewHeight,
        isLandscape = isLandscape
    )

    // Pre-calculate face display parameters
    val faceParams = remember(faces, params, isFlippedHorizontally) {
        faces.map { face ->
            with(face.boundingBox) {
                val (boxWidth, boxHeight) = if (isLandscape) {
                    height() to width()
                } else {
                    width() to height()
                }

                val scaledWidth = (boxWidth * params.scale).toFloat()
                val scaledHeight = (boxHeight * params.scale).toFloat()

                val effectiveLeft = getEffectiveLeft(
                    analyzerWidth = params.effectiveAnalyzerWidth,
                    shouldInvertX = params.shouldInvertX
                )

                val effectiveTop = if (isLandscape) {
                    params.effectiveAnalyzerHeight - right
                } else {
                    top
                }

                val scaledLeft = (effectiveLeft * params.scale.toFloat()) + params.offsetX
                val scaledTop = (effectiveTop * params.scale.toFloat()) + params.offsetY

                val finalLeft = if (isFlippedHorizontally) {
                    previewWidth - scaledLeft - scaledWidth
                } else {
                    scaledLeft
                }

//                val finalLeft = if (isFlippedHorizontally) {
//                    previewWidth - scaledLeft - scaledWidth
//                } else {
//                    scaledLeft
//                }
                FaceDisplayParameters(
                    left = finalLeft,
                    top = scaledTop,
                    width = scaledWidth,
                    height = scaledHeight
                )
            }
        }
    }

    Box(modifier = modifier) {
        faces.fastForEachIndexed { index, face ->
            key(face.id) {
                FaceOverlayItem(
                    face = face,
                    displayParams = faceParams[index],
                    onFaceTap = onFaceTap,
                    density = density
                )
            }
        }
    }
}

private fun Rect.getEffectiveLeft(analyzerWidth: Int, shouldInvertX: Boolean): Float {
    return if (shouldInvertX) {
        analyzerWidth - right.toFloat()
    } else {
        left.toFloat()
    }
}

private fun BoundingBox.getEffectiveLeft(analyzerWidth: Int, shouldInvertX: Boolean): Float {
    return if (shouldInvertX) {
        analyzerWidth - right
    } else {
        left
    }
}
