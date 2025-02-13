package com.example.facex.ui.screens.camera_face_recognition.components.bottomsheets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtLeast
import androidx.compose.ui.util.fastCoerceAtMost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    header: @Composable () -> Unit,
    content: @Composable () -> Unit,
    footer: @Composable () -> Unit
) {
    val density = LocalDensity.current

    var footerHeight by remember { mutableFloatStateOf(0f) }
    var headerHeight by remember { mutableFloatStateOf(0f) }
    var draggedHeight by remember { mutableFloatStateOf(0f) }
    var sheetOffset by remember { mutableFloatStateOf(0f) }

    val spacing = with(density) { 12.dp.toPx() }
    val screenHeight = with(density) {
        androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp.toPx()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            DragHandlerModalBottomSheet(
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    draggedHeight = coordinates.size.height.toFloat()
                },
                sheetOffset = sheetOffset
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned {
                sheetOffset = sheetState.requireOffset()
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                AnimatedHeaderModalBottomSheet(
                    header = header,
                    currentOffsetProvider = { sheetOffset },
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            headerHeight = coordinates.size.height.toFloat()
                        }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Content section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = with(density) { footerHeight.toDp() })
                        .graphicsLayer {
                            translationY = (sheetOffset - headerHeight + spacing)
                                .fastCoerceAtMost(0f)
                        }
                ) {
                    content()
                }
            }


            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        footerHeight = coordinates.size.height.toFloat()
                    }
                    .graphicsLayer {
                        translationY =
                            (screenHeight - sheetOffset - size.height).fastCoerceAtLeast(0f)
                    },
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    footer()
                }
            }
        }
    }
}


