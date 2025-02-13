package com.example.facex.ui.screens.camera_face_recognition.components.bottomsheets

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.fastCoerceIn


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DragHandlerModalBottomSheet(
    modifier: Modifier = Modifier,
    sheetOffset: Float
) {
    val alpha = remember(sheetOffset) {
        (sheetOffset / 100f).fastCoerceIn(0f, 1f)
    }

    val dragHandleAlpha by animateFloatAsState(
        targetValue = alpha, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        ), label = "dragHandleAlpha"
    )

    val dragHandleScale by animateFloatAsState(
        targetValue = if (alpha > 0f) 1f else 0.6f, animationSpec = tween(
            durationMillis = 300, easing = FastOutSlowInEasing
        ), label = "dragHandleScale"
    )

    val dragHandleWidth by animateFloatAsState(
        targetValue = if (alpha > 0f) 1f else 0.5f, animationSpec = tween(
            durationMillis = 400, easing = LinearOutSlowInEasing
        ), label = "dragHandleWidth"
    )

    Box(modifier = modifier
        .fillMaxWidth()
        .graphicsLayer {
            this.alpha = dragHandleAlpha
            scaleY = dragHandleScale
            scaleX = dragHandleWidth * (0.5f + (dragHandleAlpha * 0.5f))
        }
    ) {
        BottomSheetDefaults.DragHandle(
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
