package com.example.facex.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

//
//enum class ItemPhase { Idle, PreSwap, MovingToFront, PostSwap }
//data class SelectableQueueAnimationConfig(
//    val preSwapDuration: Int = 500,
//    val moveDuration: Int = 500,
//    val postSwapDuration: Int = 500,
//    val easing: Easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f),
//)

//@Composable
//fun <T : Any> AnimatedSelectableQueue(
//    items: List<T>,
//    onListUpdated: (List<T>) -> Unit,
//    modifier: Modifier = Modifier,
//    animationConfig: SelectableQueueAnimationConfig = SelectableQueueAnimationConfig(),
//    onPreSwap: suspend (item: T) -> Unit = { },
//    onPostSwap: suspend (item: T) -> Unit = { },
//    onPhaseChange: (ItemPhase) -> Unit = { },
//    enableItemSelection: (T, Int) -> Boolean = { _, index -> index != 0 },
//    key: ((item: T) -> Any)? = null,
//    selectedItemIndex: Int = -1,
//    contentPadding: PaddingValues = PaddingValues(16.dp),
//    itemSpacing: Dp = 8.dp,
//    itemContent: @Composable (
//        item: T, phase: ItemPhase, isAnimatingItem: Boolean, index: Int
//    ) -> Unit
//) {
//    val currentPhase = remember { mutableStateOf(ItemPhase.Idle) }
//    val animatingItemIndex = remember { mutableIntStateOf(-1) }
//    val currentItems = remember(items) { items.toMutableStateList() }
//
//    // Handle empty list case
//    if (items.isEmpty()) return
//
//    LaunchedEffect(selectedItemIndex) {
//        Log.d("NANA", "selectedItemIndex:$selectedItemIndex ")
//        if (selectedItemIndex != -1 && currentPhase.value == ItemPhase.Idle) {
//            animatingItemIndex.intValue = selectedItemIndex
//            currentPhase.value = ItemPhase.PreSwap
//        }
//    }
//
//    LaunchedEffect(items) {
//        if (currentPhase.value == ItemPhase.Idle) {
//            currentItems.clear()
//            currentItems.addAll(items)
//        }
//    }
//
//    LaunchedEffect(currentPhase.value) {
//        onPhaseChange(currentPhase.value)
//        try {
//            when (currentPhase.value) {
//                ItemPhase.PreSwap -> {
//                    val selectedItem = currentItems.getOrNull(animatingItemIndex.intValue)
//                    selectedItem?.let {
//                        onPreSwap(it)
//                        delay(animationConfig.preSwapDuration.toLong())
//                        currentPhase.value = ItemPhase.MovingToFront
//                    } ?: run {
//                        currentPhase.value = ItemPhase.Idle
//                    }
//                }
//
//                ItemPhase.MovingToFront -> {
//                    delay(animationConfig.moveDuration.toLong())
//                    currentItems.moveToFront(animatingItemIndex.intValue)
//                    currentPhase.value = ItemPhase.PostSwap
//                }
//
//                ItemPhase.PostSwap -> {
//                    currentItems.firstOrNull()?.let {
//                        onPostSwap(it)
//                        delay(animationConfig.postSwapDuration.toLong())
//                    }
//                    currentPhase.value = ItemPhase.Idle
//                    animatingItemIndex.intValue = -1
//                    onListUpdated(currentItems.toList())
//                }
//
//                ItemPhase.Idle -> { /* No action needed */
//                }
//            }
//        } catch (e: Exception) {
//            // Reset to idle state if any operation fails
//            currentPhase.value = ItemPhase.Idle
//            animatingItemIndex.intValue = -1
//        }
//    }
//
//    LazyColumn(
//        modifier = modifier.fillMaxSize(), contentPadding = contentPadding
//    ) {
//        itemsIndexed(items = currentItems,
//            key = key?.let { selector -> { _, item -> selector(item) } }
//                ?: { _, item -> item }) { index, item ->
//            val isAnimatingItem = index == animatingItemIndex.intValue
//
//            Box(modifier = Modifier
//                .animateItem(
//                    fadeInSpec = tween(
//                        durationMillis = 500,
//                        delayMillis = index * 50, // Stagger effect
//                        easing = LinearOutSlowInEasing
//                    ),
//                    placementSpec = if (isAnimatingItem) {
//                        spring(
//                            dampingRatio = Spring.DampingRatioLowBouncy,
//                            stiffness = Spring.StiffnessLow,
//                            visibilityThreshold = IntOffset(2, 2)
//                        )
//                    } else {
//                        spring(
//                            dampingRatio = Spring.DampingRatioNoBouncy,
//                            stiffness = Spring.StiffnessHigh,
//                            visibilityThreshold = IntOffset(2, 2)
//                        )
//                    }
//                )
//                .fillMaxWidth()
//                .padding(vertical = itemSpacing)
//                .clickable(
//                    enabled = currentPhase.value == ItemPhase.Idle && enableItemSelection(
//                        item, index
//                    )
//                ) {
//                    animatingItemIndex.intValue = index
//                    currentPhase.value = ItemPhase.PreSwap
//                }) {
//                itemContent(item, currentPhase.value, isAnimatingItem, index)
//            }
//        }
//    }
//}

enum class ItemPhase { Idle, PreSwap, MovingToFront, PostSwap }


enum class CascadeStyle {
    WATERFALL,    // Items drop from previous item
    ELASTIC,      // Elastic bounce effect
    SPIRAL,       // Items spiral in with rotation
    WAVE          // Wave-like motion
}

data class SelectableQueueAnimationConfig(
    val preSwapDuration: Int = 200,
    val moveDuration: Int = 0,
    val postSwapDuration: Int = 200,
    val cascadeDelay: Int = 80,
    val dropDuration: Int = 0,
    val cascadeStyle: CascadeStyle = CascadeStyle.WATERFALL,
    val easing: Easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f),
)

@Composable
private fun rememberCascadeAnimationState(
    index: Int,
    config: SelectableQueueAnimationConfig,
    itemHeight: Dp
): Triple<State<Float>, State<Float>, State<Float>> {
    val scope = rememberCoroutineScope()

    val offsetY = remember { Animatable(0f) }
    val scale = remember { Animatable(0.8f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(index * config.cascadeDelay.toLong())

        when (config.cascadeStyle) {
            CascadeStyle.WATERFALL -> {
                launch {
                    offsetY.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessVeryLow
                        )
                    )
                }
                launch {
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = config.dropDuration,
                            easing = CubicBezierEasing(0.2f, 0.9f, 0.1f, 1.0f)
                        )
                    )
                }
            }

            CascadeStyle.ELASTIC -> {
                launch {
                    offsetY.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = 0.5f,
                            stiffness = 300f
                        )
                    )
                }
                launch {
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = 0.3f,
                            stiffness = 300f
                        )
                    )
                }
            }

            CascadeStyle.SPIRAL -> {
                launch {
                    offsetY.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = config.dropDuration,
                            easing = LinearOutSlowInEasing
                        )
                    )
                }
                launch {
                    rotation.animateTo(
                        targetValue = 360f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
            }

            CascadeStyle.WAVE -> {
                launch {
                    offsetY.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioHighBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
                launch {
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = config.dropDuration,
                            easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
                        )
                    )
                }
            }
        }
    }

    return Triple(offsetY.asState(), scale.asState(), rotation.asState())
}


@Composable
fun <T : Any> AnimatedSelectableQueue(
    items: List<T>,
    selectedIndex: Int = -1,
    onTopItemSelected: (T) -> Unit = {},
    visibilityRatio: Float = 1f,
    modifier: Modifier = Modifier,
    animationConfig: SelectableQueueAnimationConfig = SelectableQueueAnimationConfig(),
    key: ((item: T) -> Any)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    itemSpacing: Dp = 8.dp,
    itemContent: @Composable (
        item: T,
        itemPhase: ItemPhase,
        isAnimating: Boolean,
        index: Int
    ) -> Unit
) {
    val currentPhase = remember { mutableStateOf(ItemPhase.Idle) }
    val animatingItemIndex = remember { mutableIntStateOf(-1) }

    if (items.isEmpty()) return


    val visibleItemCount = remember(items.size, visibilityRatio) {
        // Start showing items earlier and progress linearly
        val adjustedRatio = (visibilityRatio * 2f).coerceIn(0f, 1f)
        (1 + (items.size - 1) * adjustedRatio).toInt().coerceIn(1, items.size)
    }
    val visibleItems = remember(items, visibleItemCount) {
        items.take(visibleItemCount).toMutableStateList()
    }
    // Handle external selection
    LaunchedEffect(selectedIndex) {
        if (selectedIndex != -1 && currentPhase.value == ItemPhase.Idle) {
            animatingItemIndex.intValue = selectedIndex
            currentPhase.value = ItemPhase.PreSwap
        }
    }

    LaunchedEffect(currentPhase.value) {
        try {
            when (currentPhase.value) {
                ItemPhase.PreSwap -> {
                    val selectedItem = visibleItems.getOrNull(animatingItemIndex.intValue)
                    selectedItem?.let {
                        delay(animationConfig.preSwapDuration.toLong())
                        currentPhase.value = ItemPhase.MovingToFront
                    } ?: run {
                        currentPhase.value = ItemPhase.Idle
                    }
                }

                ItemPhase.MovingToFront -> {
                    delay(animationConfig.moveDuration.toLong())
                    visibleItems.getOrNull(animatingItemIndex.intValue)?.let { selectedItem ->
                        onTopItemSelected(selectedItem)
                    }
                    currentPhase.value = ItemPhase.PostSwap
                }

                ItemPhase.PostSwap -> {
                    delay(animationConfig.postSwapDuration.toLong())
                    currentPhase.value = ItemPhase.Idle
                    animatingItemIndex.intValue = -1
                }

                ItemPhase.Idle -> { /* No action needed */
                }
            }
        } catch (e: Exception) {
            currentPhase.value = ItemPhase.Idle
            animatingItemIndex.intValue = -1
        }
    }

    LazyColumn(
        modifier = modifier.wrapContentSize(),
        contentPadding = contentPadding
    ) {
        itemsIndexed(
            items = visibleItems,
            key = key?.let { selector -> { _, item -> selector(item) } }
                ?: { _, item -> item.hashCode() }
        ) { index, item ->
            val isAnimatingItem = index == animatingItemIndex.intValue
            val itemHeight = 72.dp
            val (offsetY, scale, rotation) = rememberCascadeAnimationState(
                index = index,
                config = animationConfig,
                itemHeight = itemHeight
            )

            Box(
                modifier = Modifier
                    .animateItem()
                    .graphicsLayer {
                        val startOffset = if (index == 0) 0f else itemHeight.toPx()
                        translationY = startOffset * (1f - offsetY.value)
                        scaleX = scale.value
                        scaleY = scale.value
                        rotationZ = if (animationConfig.cascadeStyle == CascadeStyle.SPIRAL) {
                            rotation.value
                        } else 0f

                        if (animationConfig.cascadeStyle == CascadeStyle.WAVE) {
                            val wave = sin(offsetY.value * PI * 2) * 20
                            translationX = wave.toFloat()
                        }

                        alpha = offsetY.value
                    }
                    .fillMaxWidth()
                    .padding(vertical = itemSpacing)
            ) {
                itemContent(item, currentPhase.value,isAnimatingItem, index)
            }
        }
    }
}

//@Composable
//fun <T : Any> AnimatedSelectableQueue(
//    items: List<T>,
//    visibilityRatio: Float,
//    onListUpdated: (List<T>) -> Unit = {},
//    modifier: Modifier = Modifier,
//    animationConfig: SelectableQueueAnimationConfig = SelectableQueueAnimationConfig(),
//    onPreSwap: suspend (item: T) -> Unit = { },
//    onPostSwap: suspend (item: T) -> Unit = { },
//    onPhaseChange: (ItemPhase) -> Unit = { },
//    enableItemSelection: (T, Int) -> Boolean = { _, index -> index != 0 },
//    key: ((item: T) -> Any)? = null,
//    selectedItemIndex: Int = -1,
//    contentPadding: PaddingValues = PaddingValues(16.dp),
//    itemSpacing: Dp = 8.dp,
//    itemContent: @Composable (
//        item: T, phase: ItemPhase, isAnimatingItem: Boolean, index: Int
//    ) -> Unit
//) {
//    val currentPhase = remember { mutableStateOf(ItemPhase.Idle) }
//    val animatingItemIndex = remember { mutableIntStateOf(-1) }
//    val currentItems = remember(items) { items.toMutableStateList() }
//
//
//    val visibleItemCount = remember(items.size, visibilityRatio) {
//        // Start showing items earlier and progress linearly
//        val adjustedRatio = (visibilityRatio * 2f).coerceIn(0f, 1f)
//        (1 + (items.size - 1) * adjustedRatio).toInt().coerceIn(1, items.size)
//    }
//    val visibleItems = remember(currentItems, visibleItemCount) {
//        currentItems.take(visibleItemCount).toMutableStateList()
//    }
//
//    if (items.isEmpty()) return
//
//    LaunchedEffect(selectedItemIndex) {
//        if (selectedItemIndex != -1 && currentPhase.value == ItemPhase.Idle) {
//            animatingItemIndex.intValue = selectedItemIndex
//            currentPhase.value = ItemPhase.PreSwap
//        }
//    }
//
//    LaunchedEffect(items) {
//        if (currentPhase.value == ItemPhase.Idle) {
//            currentItems.clear()
//            currentItems.addAll(items)
//        }
//    }
//
//    LaunchedEffect(currentPhase.value) {
//        onPhaseChange(currentPhase.value)
//        try {
//            when (currentPhase.value) {
//                ItemPhase.PreSwap -> {
//                    val selectedItem = visibleItems.getOrNull(animatingItemIndex.intValue)
//                    selectedItem?.let {
//                        onPreSwap(it)
//                        delay(animationConfig.preSwapDuration.toLong())
//                        currentPhase.value = ItemPhase.MovingToFront
//                    } ?: run {
//                        currentPhase.value = ItemPhase.Idle
//                    }
//                }
//                ItemPhase.MovingToFront -> {
//                    delay(animationConfig.moveDuration.toLong())
//                    currentItems.moveToFront(animatingItemIndex.intValue)
//                    currentPhase.value = ItemPhase.PostSwap
//                }
//                ItemPhase.PostSwap -> {
//                    visibleItems.firstOrNull()?.let {
//                        onPostSwap(it)
//                        delay(animationConfig.postSwapDuration.toLong())
//                    }
//                    currentPhase.value = ItemPhase.Idle
//                    animatingItemIndex.intValue = -1
//                    onListUpdated(currentItems.toList())
//                }
//                ItemPhase.Idle -> { /* No action needed */ }
//            }
//        } catch (e: Exception) {
//            currentPhase.value = ItemPhase.Idle
//            animatingItemIndex.intValue = -1
//        }
//    }
//
//    LazyColumn(
//        modifier = modifier.wrapContentSize(),
//        contentPadding = contentPadding
//    ) {
//        itemsIndexed(
//            items = visibleItems,
//            key = key?.let { selector -> { _, item -> selector(item) } }
//                ?: { _, item -> item }
//        ) { index, item ->
//            val isAnimatingItem = index == animatingItemIndex.intValue
//            val itemHeight = 72.dp // Adjust based on your item size
//            val (offsetY, scale, rotation) = rememberCascadeAnimationState(
//                index = index,
//                config = animationConfig,
//                itemHeight = itemHeight
//            )
//
//            Box(
//                modifier = Modifier
//                    .animateItem()
//                    .graphicsLayer {
//                        // Calculate drop position based on previous item
//                        val startOffset = if (index == 0) 0f else itemHeight.toPx()
//                        translationY = startOffset * (1f - offsetY.value)
//
//                        // Apply scale and rotation based on animation style
//                        scaleX = scale.value
//                        scaleY = scale.value
//                        rotationZ = if (animationConfig.cascadeStyle == CascadeStyle.SPIRAL) {
//                            rotation.value
//                        } else 0f
//
//                        // Apply wave effect if needed
//                        if (animationConfig.cascadeStyle == CascadeStyle.WAVE) {
//                            val wave = sin(offsetY.value * PI * 2) * 20
//                            translationX = wave.toFloat()
//                        }
//
//                        alpha = offsetY.value
//                    }
//                    .fillMaxWidth()
//                    .padding(vertical = itemSpacing)
//                    .clickable(
//                        enabled = currentPhase.value == ItemPhase.Idle && enableItemSelection(
//                            item,
//                            index
//                        )
//                    ) {
//                        animatingItemIndex.intValue = index
//                        currentPhase.value = ItemPhase.PreSwap
//                    }
//            ) {
//                itemContent(item, currentPhase.value, isAnimatingItem, index)
//            }
//        }
//    }
//}


//@Composable
//fun <T : Any> AnimatedSelectableQueue(
//    items: List<T>,
//    visibilityRatio: Float, // New parameter for controlling item visibility
//    onListUpdated: (List<T>) -> Unit,
//    modifier: Modifier = Modifier,
//    animationConfig: SelectableQueueAnimationConfig = SelectableQueueAnimationConfig(),
//    onPreSwap: suspend (item: T) -> Unit = { },
//    onPostSwap: suspend (item: T) -> Unit = { },
//    onPhaseChange: (ItemPhase) -> Unit = { },
//    enableItemSelection: (T, Int) -> Boolean = { _, index -> index != 0 },
//    key: ((item: T) -> Any)? = null,
//    selectedItemIndex: Int = -1,
//    contentPadding: PaddingValues = PaddingValues(16.dp),
//    itemSpacing: Dp = 8.dp,
//    itemContent: @Composable (
//        item: T, phase: ItemPhase, isAnimatingItem: Boolean, index: Int
//    ) -> Unit
//) {
//    val currentPhase = remember { mutableStateOf(ItemPhase.Idle) }
//    val animatingItemIndex = remember { mutableIntStateOf(-1) }
//    val currentItems = remember(items) { items.toMutableStateList() }
//
//    // Calculate how many items should be visible based on the ratio
//    val visibleItemCount = remember(items.size, visibilityRatio) {
//        (items.size * visibilityRatio.coerceIn(0f, 1f)).toInt().coerceAtLeast(1)
//    }
//
//    // Create a filtered list of visible items
//    val visibleItems = remember(currentItems, visibleItemCount) {
//        currentItems.take(visibleItemCount).toMutableStateList()
//    }
//
//    // Handle empty list case
//    if (items.isEmpty()) return
//
//    LaunchedEffect(selectedItemIndex) {
//        if (selectedItemIndex != -1 && currentPhase.value == ItemPhase.Idle) {
//            animatingItemIndex.intValue = selectedItemIndex
//            currentPhase.value = ItemPhase.PreSwap
//        }
//    }
//
//    LaunchedEffect(items) {
//        if (currentPhase.value == ItemPhase.Idle) {
//            currentItems.clear()
//            currentItems.addAll(items)
//        }
//    }
//
//    LaunchedEffect(currentPhase.value) {
//        onPhaseChange(currentPhase.value)
//        try {
//            when (currentPhase.value) {
//                ItemPhase.PreSwap -> {
//                    val selectedItem = visibleItems.getOrNull(animatingItemIndex.intValue)
//                    selectedItem?.let {
//                        onPreSwap(it)
//                        delay(animationConfig.preSwapDuration.toLong())
//                        currentPhase.value = ItemPhase.MovingToFront
//                    } ?: run {
//                        currentPhase.value = ItemPhase.Idle
//                    }
//                }
//
//                ItemPhase.MovingToFront -> {
//                    delay(animationConfig.moveDuration.toLong())
//                    visibleItems.moveToFront(animatingItemIndex.intValue)
//                    currentItems.moveToFront(animatingItemIndex.intValue)
//                    currentPhase.value = ItemPhase.PostSwap
//                }
//
//                ItemPhase.PostSwap -> {
//                    visibleItems.firstOrNull()?.let {
//                        onPostSwap(it)
//                        delay(animationConfig.postSwapDuration.toLong())
//                    }
//                    currentPhase.value = ItemPhase.Idle
//                    animatingItemIndex.intValue = -1
//                    onListUpdated(currentItems.toList())
//                }
//
//                ItemPhase.Idle -> { /* No action needed */
//                }
//            }
//        } catch (e: Exception) {
//            // Reset to idle state if any operation fails
//            currentPhase.value = ItemPhase.Idle
//            animatingItemIndex.intValue = -1
//        }
//    }
//
//    LazyColumn(
//        modifier = modifier.fillMaxSize(),
//        contentPadding = contentPadding
//    ) {
//        itemsIndexed(
//            items = visibleItems,
//            key = key?.let { selector -> { _, item -> selector(item) } }
//                ?: { _, item -> item }
//        ) { index, item ->
//            val isAnimatingItem = index == animatingItemIndex.intValue
//
//            Box(
//                modifier = Modifier
//                    .animateItem(
//                        fadeInSpec = tween(
//                            durationMillis = 500,
//                            delayMillis = index * 50, // Stagger effect
//                            easing = LinearOutSlowInEasing
//                        ),
//                        placementSpec = if (isAnimatingItem) {
//                            spring(
//                                dampingRatio = Spring.DampingRatioLowBouncy,
//                                stiffness = Spring.StiffnessLow,
//                                visibilityThreshold = IntOffset(2, 2)
//                            )
//                        } else {
//                            spring(
//                                dampingRatio = Spring.DampingRatioNoBouncy,
//                                stiffness = Spring.StiffnessHigh,
//                                visibilityThreshold = IntOffset(2, 2)
//                            )
//                        }
//                    )
//                    .fillMaxWidth()
//                    .padding(vertical = itemSpacing)
//                    .clickable(
//                        enabled = currentPhase.value == ItemPhase.Idle && enableItemSelection(
//                            item,
//                            index
//                        )
//                    ) {
//                        animatingItemIndex.intValue = index
//                        currentPhase.value = ItemPhase.PreSwap
//                    }
//            ) {
//                itemContent(item, currentPhase.value, isAnimatingItem, index)
//            }
//        }
//    }
//}

fun <T> MutableList<T>.moveToFront(selectedIndex: Int) {
    if (selectedIndex != 0) {
        val previousFront = this[0]
        val selectedItem = removeAt(selectedIndex)
        add(0, selectedItem)
        removeAt(1)
        add(previousFront)
    }
}