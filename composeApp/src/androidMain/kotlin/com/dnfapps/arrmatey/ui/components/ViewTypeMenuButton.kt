package com.dnfapps.arrmatey.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.dnfapps.arrmatey.ui.theme.ViewType

@Composable
fun ViewTypeMenuButton(
    viewType: ViewType,
    onViewTypeChanged: (ViewType) -> Unit,
) {
    val newType =
        when (viewType) {
            ViewType.Grid -> ViewType.List
            ViewType.List -> ViewType.Grid
        }
    IconButton(
        onClick = {
            onViewTypeChanged(newType)
        },
    ) {
        Icon(
            imageVector =
                when (viewType) {
                    ViewType.Grid -> Icons.AutoMirrored.Default.List
                    ViewType.List -> Icons.Default.GridView
                },
            contentDescription = "Switch to ${newType.name} view",
        )
    }
}
