package com.dnfapps.arrmatey.ui.helpers

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

data class MinColumnsAdaptiveGridCells(
    private val minSize: Dp,
    private val minColumns: Int = 3,
) : GridCells {
    override fun Density.calculateCrossAxisCellSizes(
        availableSize: Int,
        spacing: Int,
    ): List<Int> {
        val count = maxOf((availableSize + spacing) / (minSize.roundToPx() + spacing), minColumns)
        val totalSpacing = spacing * (count - 1)
        val availableForItems = maxOf(0, availableSize - totalSpacing)
        val itemSize = availableForItems / count
        val remainder = availableForItems % count
        return List(count) { index ->
            itemSize + if (index < remainder) 1 else 0
        }
    }
}
