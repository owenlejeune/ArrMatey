package com.dnfapps.arrmatey.entensions

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.utils.MultiSelectState

/**
 * Modifier for handling click and long-click with selection state
 */
fun Modifier.selectionClickable(
    item: ArrMedia,
    selectionState: MultiSelectState<ArrMedia>,
    onClick: () -> Unit,
    enabled: Boolean = true,
    role: Role? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
    hapticFeedbackEnabled: Boolean = true,
): Modifier =
    this.then(
        Modifier.combinedClickable(
            onClick = {
                if (selectionState.isInSelectionMode.value) {
                    selectionState.toggle(item)
                } else {
                    onClick()
                }
            },
            onLongClick = {
                selectionState.toggle(item)
                onLongClick?.invoke()
            },
            enabled = enabled,
            role = role,
            onLongClickLabel = onLongClickLabel,
            interactionSource = interactionSource,
            hapticFeedbackEnabled = hapticFeedbackEnabled,
        ),
    )

fun Modifier.breakPadding(horizontal: Dp): Modifier =
    this.then(
        Modifier.layout { measurable, constraints ->
            val paddingPx = horizontal.roundToPx()
            val targetWidth = constraints.maxWidth + (paddingPx * 2)
            val placeable =
                measurable.measure(
                    constraints.copy(
                        minWidth = targetWidth,
                        maxWidth = targetWidth,
                    ),
                )
            layout(constraints.maxWidth, placeable.height) {
                placeable.placeRelative(-paddingPx, 0)
            }
        },
    )

fun Modifier.colouredDropShadow(shadowColor: Color?): Modifier =
    this.then(
        if (shadowColor == null) {
            Modifier
        } else {
            Modifier.drawBehind {
                drawIntoCanvas { canvas ->
                    val nativePaint =
                        Paint().apply {
                            color = shadowColor.copy(alpha = 1.0f).toArgb()
                            maskFilter =
                                BlurMaskFilter(
                                    16.dp.toPx(),
                                    BlurMaskFilter.Blur.NORMAL,
                                )
                        }
                    canvas.nativeCanvas.drawRoundRect(
                        2.dp.toPx(),
                        size.height * 0.2f,
                        size.width - 2.dp.toPx(),
                        size.height,
                        12.dp.toPx(),
                        12.dp.toPx(),
                        nativePaint,
                    )
                }
            }
        },
    )
