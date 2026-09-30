package com.dnfapps.arrmatey.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun AlphabetFastScroller(
    alphabet: List<String>,
    onLetterSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (alphabet.isEmpty()) return

    val haptic = LocalHapticFeedback.current
    var isDragging by remember { mutableStateOf(false) }
    var activeLetter by remember { mutableStateOf(alphabet.first()) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var railHeightPx by remember { mutableFloatStateOf(0f) }

    val currentOnLetterSelected by rememberUpdatedState(onLetterSelected)
    val density = LocalDensity.current
    val bubbleSize = 56.dp
    val bubbleSizePx = with(density) { bubbleSize.toPx() }

    Box(
        modifier = modifier.fillMaxHeight(),
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(22.dp)
                .pointerInput(alphabet) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        isDragging = true
                        val height = if (railHeightPx > 0f) railHeightPx else size.height.toFloat()
                        val initialY = down.position.y.coerceIn(0f, height)
                        dragOffsetY = initialY

                        val index = ((initialY / height) * alphabet.size)
                            .toInt()
                            .coerceIn(0, alphabet.lastIndex)
                        val letter = alphabet[index]
                        if (letter != activeLetter) {
                            activeLetter = letter
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        currentOnLetterSelected(letter)
                        down.consume()

                        while (true) {
                            val event = awaitPointerEvent()
                            val drag = event.changes.firstOrNull() ?: break
                            if (!drag.pressed) {
                                isDragging = false
                                break
                            }

                            val y = drag.position.y.coerceIn(0f, height)
                            dragOffsetY = y
                            val newIndex = ((y / height) * alphabet.size)
                                .toInt()
                                .coerceIn(0, alphabet.lastIndex)
                            val newLetter = alphabet[newIndex]
                            if (newLetter != activeLetter) {
                                activeLetter = newLetter
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentOnLetterSelected(newLetter)
                            }
                            drag.consume()
                        }
                    }
                }
                .onGloballyPositioned { coordinates ->
                    railHeightPx = coordinates.size.height.toFloat()
                },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 6.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                alphabet.forEach { letter ->
                    val isCurrent = isDragging && letter == activeLetter
                    Text(
                        text = letter,
                        fontSize = 9.sp,
                        fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,
                        color = if (isCurrent) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        val clampedY = (dragOffsetY - bubbleSizePx / 2f).coerceIn(
            0f,
            (railHeightPx - bubbleSizePx).coerceAtLeast(0f),
        )

        AnimatedVisibility(
            visible = isDragging,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset {
                    IntOffset(
                        x = with(density) { -32.dp.roundToPx() },
                        y = clampedY.roundToInt(),
                    )
                },
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shadowElevation = 8.dp,
                modifier = Modifier.size(bubbleSize),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = activeLetter,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
