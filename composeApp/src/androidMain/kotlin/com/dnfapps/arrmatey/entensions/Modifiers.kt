package com.dnfapps.arrmatey.entensions

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp

fun Modifier.breakPadding(horizontal: Dp): Modifier = this.then(
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
