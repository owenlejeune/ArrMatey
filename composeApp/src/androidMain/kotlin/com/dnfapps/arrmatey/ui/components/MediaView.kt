package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.ui.theme.ViewType
import com.dnfapps.arrmatey.utils.MultiSelectState

@Composable
fun MediaView(
    type: InstanceType,
    items: List<ArrMedia>,
    onItemClick: (ArrMedia) -> Unit,
    itemIsActive: (ArrMedia) -> Boolean,
    preferences: InstancePreferences,
    multiSelectState: MultiSelectState<Long> = MultiSelectState(selectionModeAvailable = false)
) {
    when (preferences.viewType) {
        ViewType.List -> MediaList(
            aspectRatio = type.aspectRatio,
            items = items,
            onItemClick = onItemClick,
            itemIsActive = itemIsActive,
            showBannerBackground = preferences.showBannerBackground,
            includeOverview = preferences.includeOverview,
            blur = preferences.bannerBlur,
            posterElevation = preferences.posterElevation,
            posterRadius = preferences.posterRadius,
            multiSelectState = multiSelectState,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxSize()
        )
        ViewType.Grid -> PosterGrid(
            aspectRatio = type.aspectRatio,
            items = items,
            onItemClick = onItemClick,
            itemIsActive = itemIsActive,
            showFullDetails = preferences.showFullDetails,
            showOverlay = preferences.showOverlay,
            gridDensity = preferences.gridDensity,
            gridSpacing = preferences.gridSpacing,
            posterElevation = preferences.posterElevation,
            posterRadius = preferences.posterRadius,
            multiSelectState = multiSelectState,
            modifier = Modifier
                .fillMaxSize()
        )
    }
}
