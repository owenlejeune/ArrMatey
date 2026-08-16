package com.dnfapps.arrmatey.entensions

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier
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
): Modifier = this.then(
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
    )
)

fun Modifier.breakPadding(horizontal: Dp): Modifier = this.then(
    Modifier.layout { measurable, constraints ->
        val paddingPx = horizontal.roundToPx()
        val targetWidth = constraints.maxWidth + (paddingPx * 2)
        val placeable = measurable.measure(
            constraints.copy(
                minWidth = targetWidth,
                maxWidth = targetWidth
            )
        )
        layout(constraints.maxWidth, placeable.height) {
            placeable.placeRelative(-paddingPx, 0)
        }
    }
)