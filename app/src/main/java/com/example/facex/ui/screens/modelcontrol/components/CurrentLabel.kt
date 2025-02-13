//package com.example.facex.ui.screens.modelcontrol.components
//
//import androidx.compose.animation.core.LinearEasing
//import androidx.compose.animation.core.RepeatMode
//import androidx.compose.animation.core.StartOffset
//import androidx.compose.animation.core.StartOffsetType
//import androidx.compose.animation.core.animate
//import androidx.compose.animation.core.infiniteRepeatable
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.wrapContentSize
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableFloatStateOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import com.example.facex.ui.components.AnimationMode
//import com.example.facex.ui.components.EasingFunction
//import com.example.facex.ui.components.EmissionPattern
//import com.example.test.Particle.ParticleCore
//import kotlinx.coroutines.coroutineScope
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//
//@Composable
//fun CurrentLabel(
//    emphasisProgress: Float, modifier: Modifier = Modifier
//) {
//    val colorScheme = MaterialTheme.colorScheme
//
//    // Keep all the existing animation states and effects
//    val explosionEffect = remember {
////        ParticleEffectBuilder().withWind(angle = -45f, strength = 200f, turbulence = 1000f)
////            .withParticleAppearance(
////                minSize = 5f, maxSize = 5f, shape = ParticleShape.SQUARE, alphaVariation = 0.3f
////            ).withAnimation(
////                mode = AnimationMode.EXPLODE, easing = EasingFunction.EASE_IN_OUT
////            ).withEmission(
////                pattern = EmissionPattern.RADIAL, angle = 45f, spread = 0f
////            ).build()
//
//        ParticleCore.ParticleConfig.disintegrationConfig()
//    }
//
//    val assembleEffect = remember {
////        ParticleEffectBuilder().withParticleAppearance(
////            minSize = 5f, maxSize = 5f, shape = ParticleShape.SQUARE
////        ).withAnimation(
////            mode = AnimationMode.ASSEMBLE, assemblySpeed = 800f, easing = EasingFunction.EASE_IN_OUT
////        ).withEmission(pattern = EmissionPattern.RADIAL).build()
//        ParticleCore.ParticleConfig.assemblyConfig()
//    }
//
////    // States for particle effects
////    val explosionState = remember { ParticleEffectState.create(explosionEffect) }
////    val assembleState = remember { ParticleEffectState.create(assembleEffect) }
//    val explosionState = remember { ParticleEffectController(explosionEffect) }
//    val assembleState = remember { ParticleEffectController(assembleEffect) }
//
//    var isIncreasing by remember { mutableStateOf(true) }
//    var previousProgress by remember { mutableFloatStateOf(emphasisProgress) }
//    var shimmerOffset by remember { mutableFloatStateOf(0f) }
//
//    LaunchedEffect(emphasisProgress) {
//        isIncreasing = emphasisProgress > previousProgress
//        previousProgress = emphasisProgress
//
////        when {
////            !isIncreasing && emphasisProgress <= 0.9f -> explosionState.trigger()
////            isIncreasing && emphasisProgress > 0.01f -> assembleState.trigger()
////        }
//        when {
//            !isIncreasing && emphasisProgress <= 0.9f -> explosionState.triggerEffect(ParticleCore.EffectType.DISINTEGRATION)
//            isIncreasing && emphasisProgress > 0.01f -> assembleState.triggerEffect(ParticleCore.EffectType.ASSEMBLY)
//        }
//
//        coroutineScope {
//            launch {
//                while (true) {
//                    animate(
//                        initialValue = 0f,
//                        targetValue = 1f,
//                        animationSpec = tween(1500, easing = LinearEasing)
//                    ) { value, _ -> shimmerOffset = value }
//
//                    delay(600)
//                }
//            }
//        }    }
//
//    val alpha = if (isIncreasing) {
//        emphasisProgress.coerceIn(0.1f, 1f)
//    } else {
//        emphasisProgress.coerceIn(0f, 1f)
//    }
//
//    Box(
//        modifier = modifier.particleEffect(
//            controller = if (isIncreasing) assembleState else explosionState)
//    ) {
//        Surface(
//            color = colorScheme.primary.copy(alpha = alpha * 0.12f),
//            shape = RoundedCornerShape(8.dp),
//            border = BorderStroke(
//                width = 0.5.dp, // Reduced border width
//                color = colorScheme.primary.copy(alpha = alpha * 0.5f)
//            ),
//            modifier = Modifier.wrapContentSize()
//        ) {
//            Box {
//
//                Text(
//                    text = "Current",
//                    style = MaterialTheme.typography.labelSmall,
//                    color = colorScheme.primary.copy(alpha = alpha),
//                    modifier = Modifier.padding(
//                        horizontal = 6.dp, vertical = 2.dp
//                    )
//                )
//
//                if (emphasisProgress > 0.95f) {
//                    Box(
//                        modifier = Modifier
//                            .matchParentSize()
//                            .background(
//                                Brush.linearGradient(
//                                    colors = listOf(
//                                        Color.White.copy(alpha = 0f),
//                                        Color.White.copy(alpha = 0.4f),
//                                        Color.White.copy(alpha = 0f)
//                                    ),
//                                    start = Offset(
//                                        -50f + (shimmerOffset * 200f),
//                                        -50f + (shimmerOffset * 200f)
//                                    ), end = Offset(
//                                        50f + (shimmerOffset * 200f),
//                                        50f + (shimmerOffset * 200f)
//                                    )
//                                )
//                            )
//                    )
//                }
//            }
//        }
//    }
//}
//
//
//@Preview(showBackground = true)
//@Composable
//fun CurrentLabelPreview() {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        Text(
//            text = "Current Label - Preview",
//            style = MaterialTheme.typography.titleMedium,
//            modifier = Modifier.padding(bottom = 8.dp)
//        )
//
//        // Mock values for emphasisProgress
//        CurrentLabel(emphasisProgress = 0.5f)
//        CurrentLabel(emphasisProgress = 0.8f)
//        CurrentLabel(emphasisProgress = 1.0f)
//    }
//}


package com.example.facex.ui.screens.modelcontrol.components

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.facex.ui.components.ItemPhase
import com.example.particleeffect.particlize.controller.EffectType
import com.example.particleeffect.particlize.system.core.ParticleConfig
import com.example.particleeffect.particlize.system.core.ParticleConfig.AnimationProperties
import com.example.particleeffect.particlize.system.core.ParticleConfig.AppearanceConfig
import com.example.particleeffect.particlize.system.core.ParticleConfig.EmissionConfig
import com.example.particleeffect.particlize.system.core.ParticleConfig.EmissionPattern
import com.example.particleeffect.particlize.system.core.ParticleConfig.MotionConfig
import com.example.particleeffect.particlize.system.core.ParticleConfig.StaggerConfig
import com.example.test.new.compose.particleEffect
import com.example.test.new.controller.ParticleEffectController
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun CurrentLabel(
    itemPhase: ItemPhase,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()

    var effectCompleted by remember { mutableStateOf(false) }
    var shimmerOffset by remember { mutableFloatStateOf(0f) }

    val particleConfig = remember {
        ParticleConfig(
            particleDensity = 4,
            animation = AnimationProperties(
                duration = 20.milliseconds,
                easing = LinearEasing
            ),
            emission = EmissionConfig(
                pattern = EmissionPattern.Line(
                    angle = -90f,
                    spread = 3000f
                ),
                lifetime = 1800.milliseconds,
                stagger = StaggerConfig(
                    mode = StaggerConfig.StaggerMode.Random,
                    delayPerParticle = 1.milliseconds
                )
            ),
            appearance = AppearanceConfig(),
            motion = MotionConfig(
                initialVelocity = 40f,
                randomizeDirection = true,
                angle = -45f
            )
        )
    }

    val controller = remember { ParticleEffectController(scope, particleConfig) }
    var prevPhase = remember { itemPhase }

    // Phase handling with stable graphics layer
    LaunchedEffect(itemPhase) {
        Log.d("CurrentLabel", "Phase changed to: $itemPhase")
        when (itemPhase) {
            ItemPhase.PreSwap -> {
                try {
                    effectCompleted = false
                    if (prevPhase != ItemPhase.Idle) {
                        controller.trigger(
                            effectType = EffectType.DISINTEGRATE,
                        )
                    }
                } catch (e: Exception) {
                    Log.e("CurrentLabel", "Failed to trigger disintegration", e)
                }
            }

            ItemPhase.PostSwap -> {
                try {
                    effectCompleted = false
                    controller.trigger(
                        effectType = EffectType.ASSEMBLE,
                    )
                } catch (e: Exception) {
                    Log.e("CurrentLabel", "Failed to trigger assembly", e)
                }
            }

            ItemPhase.Idle -> {
                effectCompleted = false
                while (true) {
                    animate(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = tween(1500, easing = LinearEasing)
                    ) { value, _ -> shimmerOffset = value }
                    delay(600)
                }
            }

            ItemPhase.MovingToFront -> {
                effectCompleted = true
            }
        }
        prevPhase = itemPhase
    }

    if (!effectCompleted) {
        Surface(
            color = colorScheme.primary.copy(alpha = 0.12f),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(0.5.dp, colorScheme.primary.copy(alpha = 0.5f)),
            modifier = modifier
                .wrapContentSize()
                .particleEffect(controller = controller)
        ) {
            Box(modifier = Modifier.wrapContentSize()) {
                Text(
                    text = "Current",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 4.dp)
                )

                if (itemPhase == ItemPhase.Idle) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0f),
                                        Color.White.copy(alpha = 0.4f),
                                        Color.White.copy(alpha = 0f)
                                    ), start = Offset(
                                        x = shimmerOffset * 200f - 50f,
                                        y = shimmerOffset * 200f - 50f
                                    ), end = Offset(
                                        x = shimmerOffset * 200f + 50f,
                                        y = shimmerOffset * 200f + 50f
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CurrentLabelPreview() {
    var itemPhase by remember { mutableStateOf(ItemPhase.Idle) }
    LaunchedEffect(Unit) {
        val phases = listOf(
            ItemPhase.Idle, ItemPhase.PreSwap, ItemPhase.MovingToFront, ItemPhase.PostSwap
        )
        while (true) {
            for (phase in phases) {
                itemPhase = phase
                delay(2000) // 2 seconds per phase
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Current Label - Preview",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
        ) {


            Text(
                text = "Dynamic Preview",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

        }
    }
}