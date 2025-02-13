//package com.example.facex.ui.screens.camera_face_recognition.components.bottomsheets
//
//import android.view.Window
//import androidx.compose.animation.AnimatedContent
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.AnimatedVisibilityScope
//import androidx.compose.animation.ExperimentalSharedTransitionApi
//import androidx.compose.animation.SharedTransitionLayout
//import androidx.compose.animation.SharedTransitionScope
//import androidx.compose.animation.core.CubicBezierEasing
//import androidx.compose.animation.core.Spring
//import androidx.compose.animation.core.animate
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.animation.core.spring
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.expandVertically
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.animation.shrinkVertically
//import androidx.compose.animation.togetherWith
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.gestures.detectTapGestures
//import androidx.compose.foundation.horizontalScroll
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.navigationBarsPadding
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.statusBarsPadding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.itemsIndexed
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.FilterChip
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedCard
//import androidx.compose.material3.SuggestionChip
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.ReadOnlyComposable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableFloatStateOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.rotate
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.platform.LocalView
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.window.DialogWindowProvider
//import androidx.compose.ui.zIndex
//import com.example.facex.ui.components.icons.animated.AnimatedMLKitLogo
//import com.example.facex.ui.components.icons.animated.AnimatedOpenCvLogo
//import com.example.facex.ui.components.icons.animated.AnimatedTensorFlowLogo
//
//
//@Composable
//fun MainScreen() {
//    MLServiceScreen()
//
//}
//
//// Example usage
//@Preview(showBackground = true)
//@Composable
//fun MLServiceDetailsScreenPreview() {
//    MainScreen()
//}
//
//@Composable
//fun MLServiceScreen() {
//
//
//    val selectedFaceDetection = ServiceOption(id = 1, "ML Kit", "Google's On-Device ML Kit", {
//        AnimatedMLKitLogo(
//            modifier = Modifier.size(24.dp),
//            primaryColor = MaterialTheme.colorScheme.primary,
//            lightColor = MaterialTheme.colorScheme.onPrimary
//        )
//
//    })
//    val selectedFaceRecognition = ServiceOption(id = 2, "LiteRT", "TFLite Models", {
//        AnimatedTensorFlowLogo(
//            Modifier.size(24.dp),
//        )
//
//
//    })
//
//    val detectionModels = listOf(
//        ServiceOption(id = 4, "MediaPipe", "TFLite Models", {
//            AnimatedMediaPipe(
//                color = MaterialTheme.colorScheme.primary, size = 24.dp
//            )
//
//        }),
//        ServiceOption(id = 5, "OpenCV", "ONNX Models", {
//            AnimatedOpenCvLogo(Modifier.size(24.dp))
//        }),
//        ServiceOption(id = 6, "LiteRT", "TFLite Models", {
//
//            AnimatedTensorFlowLogo(
//                Modifier.size(24.dp),
//            )
//
//        }),
//    )
//    val recognitionModels: List<ServiceOption> = listOf(
//        ServiceOption(id = 7, "OpenCV", "ONNX Models", {
//            AnimatedOpenCvLogo(Modifier.size(24.dp))
//        }),
//    )
//
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp),
//        verticalArrangement = Arrangement.spacedBy(12.dp),
//        contentPadding = PaddingValues(vertical = 8.dp)
//    ) {
//        // Face Detection Section
//        stickyHeader(key = "detection_header") {
//            Text(
//                text = "Face Detection",
//                style = MaterialTheme.typography.titleMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 8.dp)
//            )
//        }
//
//        item {
//            CurrentModelCard(
//                option = selectedFaceDetection,
//            )
//        }
//
//        itemsIndexed(items = detectionModels,
//            key = { _, model -> model.id } // Ensure a unique key for each model
//        ) { index, model ->
//            val itemProgress = 1f
//
//            AnimatedVisibility(
//                visible = itemProgress > 0f, enter = fadeIn(
//                    animationSpec = tween(
//                        durationMillis = 300,
//                        delayMillis = index * 100, // Adjust delay for staggered animations
//                        easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
//                    )
//                ) + expandVertically(
//                    animationSpec = spring(
//                        dampingRatio = Spring.DampingRatioMediumBouncy,
//                        stiffness = Spring.StiffnessLow
//                    )
//                ), exit = fadeOut() + shrinkVertically()
//            ) {
//                SharedTransitionAlternativeModel(
//                    serviceOption = model,
//                    appearanceProgress = itemProgress
//                )
//            }
//        }
//
//        // Face Recognition Section
//        stickyHeader(key = "recognition_header") {
//            Text(
//                text = "Face Recognition",
//                style = MaterialTheme.typography.titleMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 8.dp)
//            )
//        }
//
//        item {
//            CurrentModelCard(
//                option = selectedFaceRecognition,
//            )
//        }
//
//        itemsIndexed(items = recognitionModels,
//            key = { _, model -> model.id } // Ensure a unique key for each model
//        ) { index, model ->
//            val itemProgress = 1F
//
//            AnimatedVisibility(
//                visible = itemProgress > 0f, enter = fadeIn(
//                    animationSpec = tween(
//                        durationMillis = 300,
//                        delayMillis = index * 100, // Adjust delay for staggered animations
//                        easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
//                    )
//                ) + expandVertically(
//                    animationSpec = spring(
//                        dampingRatio = Spring.DampingRatioMediumBouncy,
//                        stiffness = Spring.StiffnessLow
//                    )
//                ), exit = fadeOut() + shrinkVertically()
//            ) {
////                AlternativeModelCard(
////                    option = model,
////                    onClick = { /* Handle selection */ },
////                    appearanceProgress = itemProgress
////                )
//                SharedTransitionAlternativeModel(
//                    serviceOption = model,
//                    appearanceProgress = itemProgress
//                )
//            }
//        }
//    }
//}
//
//
//@OptIn(ExperimentalSharedTransitionApi::class)
//@Composable
//fun SharedTransitionAlternativeModel(serviceOption: ServiceOption, appearanceProgress: Float) {
//
//    var showDetails by remember { mutableStateOf(false) }
//    SharedTransitionLayout {
//        AnimatedContent(
//            targetState = showDetails,
//            label = "service_transition",
//            transitionSpec = {
//                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(
//                    animationSpec = tween(300)
//                )
//            }) { isShowingDetails ->
//            if (!isShowingDetails) {
//                AlternativeModelCard(
//                    option = serviceOption,
//                    appearanceProgress = appearanceProgress,
//                    onClick = { showDetails = true },
//                    animatedVisibilityScope = this,
//                    sharedTransitionScope = this@SharedTransitionLayout
//                )
//            } else {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .background(Color.Black.copy(alpha = 0.5f))
//                        .clickable(
//                            interactionSource = remember { MutableInteractionSource() },
//                            indication = null
//                        ) {
//                            showDetails = false
//                        }
//                ) {
//                    ServiceDetails(
//                        option = serviceOption,
//                        onDismiss = { showDetails = false },
//                        animatedVisibilityScope = this@AnimatedContent,
//                        sharedTransitionScope = this@SharedTransitionLayout
//                    )
//
//                }
//            }
//        }
//    }
//}
//
//
//@OptIn(ExperimentalSharedTransitionApi::class)
//@Composable
//private fun AlternativeModelCard(
//    option: ServiceOption,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
//    appearanceProgress: Float,
//    sharedTransitionScope: SharedTransitionScope,
//    animatedVisibilityScope: AnimatedVisibilityScope,
//) {
//    // Enhanced card animations
//    val elevation by animateFloatAsState(
//        targetValue = appearanceProgress * 8f, animationSpec = MotionSpec.quickSpringy
//    )
//
//
//    val scale by animateFloatAsState(
//        targetValue = 0.9f + (appearanceProgress * 0.1f), animationSpec = spring(
//            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
//        )
//
//    )
//
//    var isPressed by remember { mutableStateOf(false) }
//
//    val pressScale by animateFloatAsState(
//        targetValue = if (isPressed) 0.95f else 1f, animationSpec = spring(
//            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh
//        )
//    )
//
//    OutlinedCard(onClick = onClick, modifier = modifier
//        .fillMaxWidth()
//        .graphicsLayer {
//            this.scaleX = scale * pressScale
//            this.scaleY = scale * pressScale
//            alpha = appearanceProgress
//            translationY = (1f - appearanceProgress) * 50f
//        }
//        .pointerInput(Unit) {
//            detectTapGestures(onPress = {
//                isPressed = true
//                tryAwaitRelease()
//                isPressed = false
//            })
//        }, elevation = CardDefaults.outlinedCardElevation(
//        defaultElevation = elevation.dp
//    )
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(12.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                // Animated icon
//
//                with(sharedTransitionScope) {
//                    // Shared service icon
//                    option.icon(
//                        Modifier.sharedElement(
//                            rememberSharedContentState(key = "service_icon"),
//                            animatedVisibilityScope = animatedVisibilityScope
//                        )
//                    )
//
//                    Column(modifier = Modifier.graphicsLayer {
//                        translationX = (1f - appearanceProgress) * 20f
//                        alpha = appearanceProgress
//                    }) {
//                        Text(
//                            text = option.name,
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.Medium,
//                            modifier = Modifier.sharedElement(
//                                rememberSharedContentState(key = "service_name"),
//                                animatedVisibilityScope = animatedVisibilityScope
//                            )
//
//                        )
//                        Text(
//                            text = option.description,
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant,
//                            modifier = Modifier.sharedElement(
//                                rememberSharedContentState(key = "service_description"),
//                                animatedVisibilityScope = animatedVisibilityScope
//                            )
//
//                        )
//                    }
//
//                }
//
//                // Animated text content
//            }
//
//            with(sharedTransitionScope) {
//                IconButton(
//                    onClick = onClick, modifier = Modifier
//                        .sharedElement(
//                            rememberSharedContentState(key = "toggle_button"),
//                            animatedVisibilityScope = animatedVisibilityScope
//                        )
//                        .zIndex(1f)
//                ) {
//                    Icon(
//                        Icons.Default.Add,
//                        contentDescription = "Add",
//                        tint = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//
//        }
//    }
//}
//
//
//@OptIn(ExperimentalSharedTransitionApi::class)
//@Composable
//private fun ServiceDetails(
//    option: ServiceOption,
//    onDismiss: () -> Unit,
//    sharedTransitionScope: SharedTransitionScope,
//    animatedVisibilityScope: AnimatedVisibilityScope
//) {
//    var rotation by remember { mutableFloatStateOf(0f) }
//
//    LaunchedEffect(Unit) {
//        animate(0f, 45f) { value, _ -> rotation = value }
//    }
//
//    Surface(
//        modifier = Modifier.fillMaxSize(),
//        color = MaterialTheme.colorScheme.surface
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .statusBarsPadding()
//                .navigationBarsPadding()
//        ) {
//            // Top Bar with shared elements
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(12.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    with(sharedTransitionScope) {
//                        option.icon(
//                            Modifier.sharedElement(
//                                rememberSharedContentState(key = "service_icon"),
//                                animatedVisibilityScope = animatedVisibilityScope
//                            )
//                        )
//
//                        Column {
//                            Text(
//                                text = option.name,
//                                style = MaterialTheme.typography.titleLarge,
//                                modifier = Modifier.sharedElement(
//                                    rememberSharedContentState(key = "service_name"),
//                                    animatedVisibilityScope = animatedVisibilityScope
//                                )
//                            )
//                            Text(
//                                text = option.description,
//                                style = MaterialTheme.typography.bodyMedium,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                                modifier = Modifier.sharedElement(
//                                    rememberSharedContentState(key = "service_description"),
//                                    animatedVisibilityScope = animatedVisibilityScope
//                                )
//                            )
//                        }
//                    }
//                }
//
//                with(sharedTransitionScope) {
//                    IconButton(
//                        onClick = {  },
//                        modifier = Modifier
//                            .sharedElement(
//                                rememberSharedContentState(key = "toggle_button"),
//                                animatedVisibilityScope = animatedVisibilityScope
//                            )
//                            .rotate(rotation)
//                    ) {
//                        Icon(
//                            Icons.Default.Add,
//                            contentDescription = "Back",
//                            tint = MaterialTheme.colorScheme.primary
//                        )
//                    }
//                }
//            }
//
//            // Content
//            AnimatedVisibility(
//                visible = true,
//                enter = fadeIn() + expandVertically()
//            ) {
//                Column(
//                    modifier = Modifier.padding(horizontal = 16.dp)
//                ) {
//                    Spacer(modifier = Modifier.height(24.dp))
//
//                    Text(
//                        text = "Select Model",
//                        style = MaterialTheme.typography.titleMedium
//                    )
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .horizontalScroll(rememberScrollState()),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        SuggestionChip(
//                            onClick = { },
//                            label = { Text("Face Detection v1") }
//                        )
//                        SuggestionChip(
//                            onClick = { },
//                            label = { Text("Face Detection v2") }
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    Text(
//                        text = "Acceleration",
//                        style = MaterialTheme.typography.titleMedium
//                    )
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .horizontalScroll(rememberScrollState()),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        FilterChip(
//                            selected = true,
//                            onClick = { },
//                            label = { Text("CPU") }
//                        )
//                        FilterChip(
//                            selected = false,
//                            onClick = { },
//                            label = { Text("GPU") }
//                        )
//                        FilterChip(
//                            selected = false,
//                            onClick = { },
//                            label = { Text("NPU") }
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@ReadOnlyComposable
//@Composable
//fun getDialogWindow(): Window? = (LocalView.current.parent as? DialogWindowProvider)?.window
