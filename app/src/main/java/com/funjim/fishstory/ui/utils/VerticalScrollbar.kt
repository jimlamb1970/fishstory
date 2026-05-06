package com.funjim.fishstory.ui.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.ui.theme.AppIcons
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

// This one does not appear to work when the lazy column has items that are not the
// same size as the rest of the items in the list (ie the trip list)
@Composable
fun VerticalScrollToItemBar(
    state: LazyListState,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.CenterStart,
    thumbColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
    trackColor: Color = Color.Transparent,
    onToggleAlignment: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var hideJob by remember { mutableStateOf<Job?>(null) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.isScrollInProgress) {
        if (state.isScrollInProgress) {
            hideJob?.cancel()
            isVisible = true
        } else {
            hideJob = coroutineScope.launch {
                delay(2000)
                isVisible = false
            }
        }
    }

    val thumbSizeDp = 32.dp
    var trackHeightPx by remember { mutableStateOf(0f) }

    var tapCount by remember { mutableIntStateOf(0) }

    val thumbSizePx = with(LocalDensity.current) { thumbSizeDp.toPx() }

    val thumbOffsetFraction by remember(state) {
        derivedStateOf {
            val info = state.layoutInfo
            val totalItems = info.totalItemsCount.takeIf { it > 0 } ?: return@derivedStateOf 0f
            val firstItem = info.visibleItemsInfo.firstOrNull() ?: return@derivedStateOf 0f
            val avgItemHeight = info.visibleItemsInfo
                .map { it.size }
                .average()
                .takeIf { it > 0.0 } ?: return@derivedStateOf 0f

            // Pixel-accurate scroll position: how far down the full content have we scrolled?
            val scrolledPx = firstItem.index * avgItemHeight + (-firstItem.offset).coerceAtLeast(0)
            val totalScrollablePx = (totalItems * avgItemHeight - info.viewportSize.height).coerceAtLeast(1.0)

            (scrolledPx / totalScrollablePx).coerceIn(0.0, 1.0).toFloat()
        }
    }
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = fadeIn() + expandHorizontally(),
        exit = fadeOut() + shrinkHorizontally()
    ) {
        Box(
            modifier = modifier
                .width(thumbSizeDp)
                .fillMaxHeight()
                .background(trackColor, RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .width(thumbSizeDp)
                    .fillMaxHeight()
                    .background(trackColor, RoundedCornerShape(8.dp))
                    .onGloballyPositioned { trackHeightPx = it.size.height.toFloat() }
            )

            Box(
                modifier = Modifier
                    .size(thumbSizeDp)
                    .offset {
                        // trackHeightPx is the full container height
                        // 32.dp.toPx() is the thumb size
                        // If you want it to hit the very bottom, the range is 0 to (trackHeight - thumbSize)
                        val thumbSizePx = 32.dp.toPx()
                        val maxOffset = trackHeightPx - thumbSizePx

                        // Ensure we don't calculate negative values
                        val currentOffset = (thumbOffsetFraction * maxOffset).coerceAtLeast(0f)

                        IntOffset(0, currentOffset.toInt())
                    }
                    .background(thumbColor, RoundedCornerShape(8.dp))
                    .pointerInput(Unit) {
                        detectTapGestures {
                            tapCount++
                            if (tapCount >= 5) {
                                onToggleAlignment()
                                tapCount = 0
                            }
                        }
                    }
                    .draggable(
                        orientation = Orientation.Vertical,
                        onDragStarted = {
                            hideJob?.cancel()
                            isVisible = true
                        },
                        onDragStopped = {
                            hideJob = coroutineScope.launch {
                                delay(2000)
                                isVisible = false
                            }
                        },
                        state = rememberDraggableState { delta ->
                            hideJob?.cancel()
                            isVisible = true
                            val layoutInfo = state.layoutInfo
                            val totalItems = layoutInfo.totalItemsCount
                            if (totalItems == 0) return@rememberDraggableState

                            val firstItem = layoutInfo.visibleItemsInfo.firstOrNull()
                                ?: return@rememberDraggableState
                            val avgItemHeight = layoutInfo.visibleItemsInfo
                                .map { it.size }
                                .average()
                                .takeIf { it > 0.0 } ?: return@rememberDraggableState

                            val totalScrollablePx =
                                (totalItems * avgItemHeight - layoutInfo.viewportSize.height).coerceAtLeast(1.0)
                            val trackRange = (trackHeightPx - thumbSizePx).coerceAtLeast(1f)

                            // How many content-pixels does 1 track-pixel correspond to?
                            val contentPixelsPerTrackPixel = totalScrollablePx / trackRange

                            // Current scroll position in content-pixels
                            val currentScrollPx = firstItem.index * avgItemHeight + (-firstItem.offset).coerceAtLeast(0)
                            val targetScrollPx = (currentScrollPx + delta * contentPixelsPerTrackPixel)
                                .coerceIn(0.0, totalScrollablePx)

                            val targetIndex = (targetScrollPx / avgItemHeight).toInt().coerceIn(0, totalItems - 1)
                            val targetOffset = (targetScrollPx - targetIndex * avgItemHeight).toInt().coerceAtLeast(0)

                            if (delta != 0f) tapCount = 0

                            coroutineScope.launch {
                                state.scrollToItem(targetIndex, targetOffset)
                            }
                        }
                    )
            ) {
                Icon(
                    imageVector = AppIcons.Default.LeapingFish2,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun VerticalScrollByBar(
    state: LazyListState,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.CenterStart,
    thumbColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
    trackColor: Color = Color.Transparent,
    onToggleAlignment: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var hideJob by remember { mutableStateOf<Job?>(null) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.isScrollInProgress) {
        if (state.isScrollInProgress) {
            hideJob?.cancel()
            isVisible = true
        } else {
            hideJob = coroutineScope.launch {
                delay(2000)
                isVisible = false
            }
        }
    }

    val thumbSizeDp = 32.dp
    val density = LocalDensity.current
    var trackHeightPx by remember { mutableStateOf(0f) }

    var tapCount by remember { mutableIntStateOf(0) }

    val thumbSizePx = with(density) { thumbSizeDp.toPx() }

    val thumbOffsetFraction by remember(state) {
        derivedStateOf {
            val info = state.layoutInfo
            val totalItems = info.totalItemsCount.takeIf { it > 0 } ?: return@derivedStateOf 0f

            // 1. Get the exact fractional visible count (e.g., 3.5)
            var totalVisible = 0.0
            for (item in info.visibleItemsInfo) {
                val visibleHeight = (min(item.offset + item.size, info.viewportEndOffset) -
                        max(item.offset, info.viewportStartOffset))
                if (item.size > 0) {
                    totalVisible += max(0, visibleHeight).toDouble() / item.size.toDouble()
                }
            }

            // 2. Calculate the progress based on the fractional count
            // We use the first visible item index plus its own fractional offset
            val firstItem = info.visibleItemsInfo.firstOrNull()
            val firstVisibleIndex = firstItem?.index ?: 0
            val firstVisibleFraction = if (firstItem != null && firstItem.size > 0) {
                // How much of the first item is scrolled off the top
                (-firstItem.offset).coerceAtLeast(0).toDouble() / firstItem.size.toDouble()
            } else 0.0

            val currentPosition = firstVisibleIndex.toDouble() + firstVisibleFraction

            // 3. Scrollable range based on total items minus the fractional visible count
            val scrollableRange = (totalItems.toDouble() - totalVisible).coerceAtLeast(0.01)

            ((currentPosition / scrollableRange).coerceIn(0.0, 1.0)).toFloat()
        }
    }
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = fadeIn() + expandHorizontally(),
        exit = fadeOut() + shrinkHorizontally()
    ) {
        Box(
            modifier = modifier
                .width(thumbSizeDp)
                .fillMaxHeight()
                .background(trackColor, RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .width(thumbSizeDp)
                    .fillMaxHeight()
                    .background(trackColor, RoundedCornerShape(8.dp))
                    .onGloballyPositioned { trackHeightPx = it.size.height.toFloat() }
            )

            Box(
                modifier = Modifier
                    .size(thumbSizeDp)
                    .offset {
                        // trackHeightPx is the full container height
                        // 32.dp.toPx() is the thumb size
                        // If you want it to hit the very bottom, the range is 0 to (trackHeight - thumbSize)
                        val thumbSizePx = 32.dp.toPx()
                        val maxOffset = trackHeightPx - thumbSizePx

                        // Ensure we don't calculate negative values
                        val currentOffset = (thumbOffsetFraction * maxOffset).coerceAtLeast(0f)

                        IntOffset(0, currentOffset.toInt())
                    }
                    .background(thumbColor, RoundedCornerShape(8.dp))
                    .pointerInput(Unit) {
                        detectTapGestures {
                            tapCount++
                            if (tapCount >= 5) {
                                onToggleAlignment()
                                tapCount = 0
                            }
                        }
                    }
                    .draggable(
                        orientation = Orientation.Vertical,
                        onDragStarted = {
                            hideJob?.cancel()
                            isVisible = true
                        },
                        onDragStopped = {
                            hideJob = coroutineScope.launch {
                                delay(2000)
                                isVisible = false
                            }
                        },
                        state = rememberDraggableState { delta ->
                            hideJob?.cancel()
                            isVisible = true
                            val layoutInfo = state.layoutInfo
                            val totalItems = layoutInfo.totalItemsCount
                            if (totalItems == 0) return@rememberDraggableState

                            // 1. How much is currently hidden off-screen?
                            // This is the total scrollable "distance" the list can travel
                            val totalHeightOfItems = layoutInfo.visibleItemsInfo.sumOf { it.size }
                            val averageItemHeight = if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
                                totalHeightOfItems.toFloat() / layoutInfo.visibleItemsInfo.size
                            } else 0f

                            val totalScrollableRange =
                                (totalItems * averageItemHeight) - layoutInfo.viewportSize.height

                            // 2. The ratio of the track vs the scrollable range
                            val trackRange = trackHeightPx - thumbSizePx
                            if (trackRange <= 0f) return@rememberDraggableState

                            // 3. This is the magic conversion:
                            // How many pixels of list content does 1 pixel of finger drag equal?
                            val scrollRatio = totalScrollableRange / trackRange
                            val scrollAmount = delta * scrollRatio

                            if (scrollAmount.toDouble() != 0.0) {
                                tapCount = 0
                            }

                            coroutineScope.launch {
                                state.scrollBy(scrollAmount)
                            }
                        }
                    )
            ) {
                Icon(
                    imageVector = AppIcons.Default.LeapingFish2,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                )
            }
        }
    }
}
