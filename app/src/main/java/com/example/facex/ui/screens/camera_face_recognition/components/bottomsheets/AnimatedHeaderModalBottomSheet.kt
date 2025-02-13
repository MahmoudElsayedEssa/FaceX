package com.example.facex.ui.screens.camera_face_recognition.components.bottomsheets

import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import kotlin.math.roundToInt


@Composable
fun AnimatedHeaderModalBottomSheet(
    modifier: Modifier = Modifier,
    currentOffsetProvider: () -> Float,
    header: @Composable () -> Unit,
) {
    // Remember density to avoid recomposition
    val density = LocalDensity.current

    // Calculate status bar height once and remember it
    val statusBarHeightPx =
        with(density) {
            WindowInsets.statusBars.asPaddingValues().calculateTopPadding().toPx()
        }

    val defaultPadding = remember { 16.dp }
    val topOffset = remember { 4.dp }

    // Convert dp to px once
    val defaultPaddingPx = remember(density) { with(density) { defaultPadding.toPx() } }
    val topOffsetPx = remember(density) { with(density) { topOffset.toPx() } }

    // Remember container size state
    val containerSize = remember { mutableStateOf(IntSize.Zero) }
    val headerSize = remember { mutableStateOf(IntSize.Zero) }

    // Calculate alpha based on offset
    val alpha by remember(currentOffsetProvider) {
        derivedStateOf {
            (currentOffsetProvider() / 100f).fastCoerceIn(0f, 1f)
        }
    }

    // Animate alpha changes
    val animatedAlpha by animateFloatAsState(
        targetValue = alpha,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "alpha"
    )

    // Calculate translations based on sizes
    val translations by remember(
        containerSize.value,
        headerSize.value,
        statusBarHeightPx,
        defaultPaddingPx,
        topOffsetPx
    ) {
        derivedStateOf {
            val maxVerticalTranslation = minOf(
                containerSize.value.height.toFloat(),
                statusBarHeightPx - topOffsetPx
            )

            val horizontalTranslation =
                if (containerSize.value.width > 0 && headerSize.value.width > 0) {
                    (containerSize.value.width - headerSize.value.width) / 2f - defaultPaddingPx
                } else 0f

            Pair(maxVerticalTranslation, horizontalTranslation)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                containerSize.value = coordinates.size
            }
            .graphicsLayer {
                translationY = -translations.first * (1 - animatedAlpha)
            }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .onGloballyPositioned { coordinates ->
                    headerSize.value = coordinates.size
                }
                .graphicsLayer {
                    translationX = -translations.second * (1 - animatedAlpha)
                }
        ) {
            header()
        }
    }
}

//@Composable
//fun AnimatedHeaderModalBottomSheet(
//    modifier: Modifier = Modifier,
//    currentOffsetProvider: () -> Float,
//    header: @Composable () -> Unit,
//) {
//    var containerWidth by remember { mutableFloatStateOf(0f) }
//    val alpha = remember(currentOffsetProvider()) {
//        (currentOffsetProvider() / 100f).fastCoerceIn(0f, 1f)
//    }
//    val animatedAlpha by animateFloatAsState(
//        targetValue = alpha,
//        animationSpec = spring(
//            dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow
//        ),
//    )
//    val statusBarHeightPx = with(LocalDensity.current) {
//        WindowInsets.statusBars.asPaddingValues().calculateTopPadding().toPx()
//    }
//
//    Box(
//        modifier = modifier
//            .onGloballyPositioned { coordinates ->
//                containerWidth = coordinates.parentCoordinates?.size?.width?.toFloat() ?: 0f
//            }
//            .graphicsLayer {
//                val maxVerticalTranslation = minOf(size.height, statusBarHeightPx - 4.dp.toPx())
//                translationY = -maxVerticalTranslation * (1 - animatedAlpha)
//            }
//    ) {
//        Box(
//            modifier = Modifier
//                .align(Alignment.Center)
//                .graphicsLayer {
//                    val requiredTranslation = (containerWidth - size.width) / 2 - 16.dp.toPx()
//                    Log.d(
//                        "NANAAAA",
//                        "AnimatedHeaderModalBottomSheet:requiredTranslation: $requiredTranslation" +
//                                "containerWidth: $containerWidth, size.width: ${size.width} "
//                    )
//                    translationX = -requiredTranslation * (1 - animatedAlpha)
//                }
//        ) {
//            header()
//        }
//    }
//}
