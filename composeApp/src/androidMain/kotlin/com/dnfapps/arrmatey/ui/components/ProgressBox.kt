package com.dnfapps.arrmatey.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.dnfapps.arrmatey.ui.theme.ArrGreen

@Composable
fun ProgressBox(
    animate: Boolean,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    gradientColors: List<Color> =
        listOf(
            ArrGreen.copy(alpha = .2f),
            ArrGreen.copy(alpha = .7f),
        ),
    content: @Composable BoxScope.() -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition("gradientProgress-transition")

    val gradientProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    keyframes {
                        durationMillis = 1200
                        0f at 0 using LinearEasing
                        1f at 1000 using FastOutSlowInEasing
                        1f at 1200
                    },
                repeatMode = RepeatMode.Restart,
            ),
        label = "gradientProgress",
    )

    Box(
        modifier =
            modifier
                .drawBehind {
                    if (animate) {
                        val gradientWidth = size.width * gradientProgress
                        drawRect(
                            brush =
                                Brush.horizontalGradient(
                                    colors = gradientColors,
                                    startX = 0f,
                                    endX = gradientWidth,
                                ),
                            size = Size(gradientWidth, size.height),
                        )

                        if (gradientWidth < size.width) {
                            drawRect(
                                color = backgroundColor,
                                topLeft = Offset(gradientWidth, 0f),
                                size = Size(size.width - gradientWidth, size.height),
                            )
                        } else {
                            drawRect(color = backgroundColor)
                        }
                    }
                },
        content = content,
    )
}
