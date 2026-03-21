package com.dnfapps.arrmatey.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import dev.icerock.moko.resources.ImageResource

data class SettingItem(
    val icon: IconSource,
    val title: String,
    val subtitle: String? = null,
    val backgroundColor: Color? = null,
    val titleExtraContent: @Composable (() -> Unit)? = null,
    val trailingContent: @Composable (() -> Unit)? = null,
    val onClick: () -> Unit
)

sealed interface IconSource {
    data class Vector(val imageVector: ImageVector): IconSource
    data class Resource(val resource: ImageResource): IconSource
    data class Radio(val selected: Boolean): IconSource
}