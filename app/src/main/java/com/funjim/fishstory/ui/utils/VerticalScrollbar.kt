package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.ui.theme.AppIcons
import kotlinx.coroutines.launch

@Composable
fun VerticalScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier,
    thumbColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
    trackColor: Color = Color.Transparent
) {
    val thumbSizeDp = 32.dp
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    var trackHeightPx by remember { mutableStateOf(0f) }
    val thumbSizePx = with(density) { thumbSizeDp.toPx() } // <-- here

    val thumbOffsetFraction by remember {
        derivedStateOf {
            val info = state.layoutInfo
            val totalItems = info.totalItemsCount.takeIf { it > 0 } ?: return@derivedStateOf 0f
            val visibleItems = info.visibleItemsInfo.size
            val firstVisible = state.firstVisibleItemIndex
            val firstVisibleOffset = state.firstVisibleItemScrollOffset.toFloat()
            val itemSize = info.visibleItemsInfo.firstOrNull()?.size?.toFloat() ?: 1f

            // The scrollable range is only (totalItems - visibleItems), not totalItems
            // When firstVisible == totalItems - visibleItems, we're at the bottom
            val scrollableItems = (totalItems - visibleItems).coerceAtLeast(1)
            val smoothIndex = firstVisible + (firstVisibleOffset / itemSize)
            (smoothIndex / scrollableItems).coerceIn(0f, 1f)
        }
    }

    Box(
        modifier = modifier
            .width(thumbSizeDp)
            .fillMaxHeight()                   // <-- this is what was missing
            .background(trackColor, RoundedCornerShape(50))
    ) {
        Box(
            modifier = Modifier                    // <-- fresh Modifier, not the passed-in one
                .width(thumbSizeDp)
                .fillMaxHeight()                   // <-- this is what was missing
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
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        val layoutInfo = state.layoutInfo
                        val totalItems = layoutInfo.totalItemsCount
                        if (totalItems == 0) return@rememberDraggableState

                        // 1. How much is currently hidden off-screen?
                        // This is the total scrollable "distance" the list can travel
                        val totalHeightOfItems = layoutInfo.visibleItemsInfo.sumOf { it.size }
                        val averageItemHeight = if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
                            totalHeightOfItems.toFloat() / layoutInfo.visibleItemsInfo.size
                        } else 0f

                        val totalScrollableRange = (totalItems * averageItemHeight) - layoutInfo.viewportSize.height

                        // 2. The ratio of the track vs the scrollable range
                        val trackRange = trackHeightPx - thumbSizePx
                        if (trackRange <= 0f) return@rememberDraggableState

                        // 3. This is the magic conversion:
                        // How many pixels of list content does 1 pixel of finger drag equal?
                        val scrollRatio = totalScrollableRange / trackRange
                        val scrollAmount = delta * scrollRatio

                        coroutineScope.launch {
                            state.scrollBy(scrollAmount)
                        }
                    }
                )
        ) {
            Icon(imageVector = AppIcons.Default.LeapingFish2,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
            )
        }
    }
}
