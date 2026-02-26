package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.ui.theme.ViewType

@Composable
fun MediaView(
    type: InstanceType,
    items: List<ArrMedia>,
    onItemClick: (ArrMedia) -> Unit,
    itemIsActive: (ArrMedia) -> Boolean,
    viewType: ViewType
) {
    when (viewType) {
        ViewType.List -> MediaList(
            aspectRatio = type.aspectRatio,
            items = items,
            onItemClick = onItemClick,
            itemIsActive = itemIsActive,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxSize()
        )
        ViewType.Grid -> PosterGrid(
            aspectRatio = type.aspectRatio,
            items = items,
            onItemClick = onItemClick,
            itemIsActive = itemIsActive,
            modifier = Modifier
                .fillMaxSize()
        )
    }
}