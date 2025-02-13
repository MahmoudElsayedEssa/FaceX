package com.example.facex.ui.screens.camera_face_recognition.components.bottomsheets

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.facex.ui.screens.modelcontrol.ModelControlRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelControlBottomSheet(
    sheetState: SheetState,
    onDismissDialog: () -> Unit
) {
    var currentOffset by remember { mutableFloatStateOf(0f) }
    var draggedHeight by remember { mutableFloatStateOf(0f) }


    val density = LocalDensity.current
    val maxOffset = with(density) { 400.dp.toPx() }

    val dragProgress by animateFloatAsState(
        targetValue = (currentOffset / maxOffset).coerceIn(0f, 1f), animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        )

    )

    val expandedRatio by animateFloatAsState(
        targetValue = (1f - dragProgress).coerceIn(0f, 1f), animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium
        )

    )


    ModalBottomSheet(
        onDismissRequest = onDismissDialog, sheetState = sheetState,
        modifier = Modifier.fillMaxSize(),
        dragHandle = {
            DragHandlerModalBottomSheet(
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    draggedHeight = coordinates.size.height.toFloat()
                },
                sheetOffset = currentOffset
            )
        }
    ) {
        Box(
            modifier = Modifier.onGloballyPositioned {
                currentOffset = sheetState.requireOffset()
            },
        ) {
            Column(
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
            ) {
                AnimatedHeaderModalBottomSheet(
                    currentOffsetProvider = {currentOffset},
                ) {
                    Text(
                        text = "Models Control",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
                ModelControlRoute(
                    expandedRatioProvider = { expandedRatio },
                )
            }
        }
    }
}



