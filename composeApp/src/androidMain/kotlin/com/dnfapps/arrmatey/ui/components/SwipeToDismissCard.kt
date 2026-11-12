package com.dnfapps.arrmatey.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Direction of a horizontal swipe gesture.
 */
enum class SwipeDirection {
    StartToEnd, // Swiping Right
    EndToStart, // Swiping Left
}

/**
 * A reusable swipeable container component using physics spring animations.
 *
 * @param modifier Modifier for the container
 * @param swipeSensitivity Positional threshold fraction required to trigger the swipe action (e.g. 0.60f = 60% of card width)
 * @param enableStartToEnd Whether swiping right (StartToEnd) is enabled
 * @param enableEndToStart Whether swiping left (EndToStart) is enabled
 * @param onSwipeStartToEnd Callback invoked when a right swipe crosses the sensitivity threshold and is released
 * @param onSwipeEndToStart Callback invoked when a left swipe crosses the sensitivity threshold and is released
 * @param shape Shape used for clipping the swipe background surface
 * @param backgroundContent Content displayed underneath the card during dragging
 * @param content The foreground card content
 */
@Composable
fun SwipeToDismissCard(
    modifier: Modifier = Modifier,
    swipeSensitivity: Float = 0.60f,
    enableStartToEnd: Boolean = true,
    enableEndToStart: Boolean = true,
    onSwipeStartToEnd: (() -> Unit)? = null,
    onSwipeEndToStart: (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    backgroundContent: @Composable (direction: SwipeDirection, isThresholdCrossed: Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    var cardWidth by remember { mutableFloatStateOf(0f) }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val currentOnSwipeStartToEnd by rememberUpdatedState(onSwipeStartToEnd)
    val currentOnSwipeEndToStart by rememberUpdatedState(onSwipeEndToStart)

    val currentDirection = remember(offsetX.value) {
        if (offsetX.value >= 0f) SwipeDirection.StartToEnd else SwipeDirection.EndToStart
    }

    val isSwipeAllowed = remember(currentDirection, enableStartToEnd, enableEndToStart) {
        when (currentDirection) {
            SwipeDirection.StartToEnd -> enableStartToEnd
            SwipeDirection.EndToStart -> enableEndToStart
        }
    }

    val isThresholdCrossed = remember(offsetX.value, cardWidth, swipeSensitivity, isSwipeAllowed) {
        isSwipeAllowed && cardWidth > 0f && abs(offsetX.value) >= cardWidth * swipeSensitivity
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { cardWidth = it.width.toFloat() },
    ) {
        val alignment = when (currentDirection) {
            SwipeDirection.StartToEnd -> Alignment.CenterStart
            SwipeDirection.EndToStart -> Alignment.CenterEnd
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape),
            contentAlignment = alignment,
        ) {
            backgroundContent(currentDirection, isThresholdCrossed)
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(swipeSensitivity, enableStartToEnd, enableEndToStart) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val finalOffset = offsetX.value
                            val direction = if (finalOffset >= 0f) SwipeDirection.StartToEnd else SwipeDirection.EndToStart
                            val allowed = when (direction) {
                                SwipeDirection.StartToEnd -> enableStartToEnd
                                SwipeDirection.EndToStart -> enableEndToStart
                            }
                            val thresholdCrossed = allowed && cardWidth > 0f && abs(finalOffset) >= cardWidth * swipeSensitivity

                            if (thresholdCrossed) {
                                when (direction) {
                                    SwipeDirection.StartToEnd -> currentOnSwipeStartToEnd?.invoke()
                                    SwipeDirection.EndToStart -> currentOnSwipeEndToStart?.invoke()
                                }
                            }
                            scope.launch {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow,
                                    ),
                                )
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                                )
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            val newOffset = offsetX.value + dragAmount
                            val allowed = when {
                                newOffset > 0f -> enableStartToEnd
                                newOffset < 0f -> enableEndToStart
                                else -> true
                            }
                            if (allowed) {
                                change.consume()
                                scope.launch {
                                    offsetX.snapTo(newOffset)
                                }
                            }
                        },
                    )
                },
        ) {
            content()
        }
    }
}
