package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.compose.utils.isTitleSort
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.ui.helpers.LocalFloatingBarBottomPadding
import com.dnfapps.arrmatey.ui.theme.ViewType
import com.dnfapps.arrmatey.utils.FastScrollUtils
import com.dnfapps.arrmatey.utils.MultiSelectState
import kotlinx.coroutines.launch

@Composable
fun MediaView(
    type: InstanceType,
    items: List<ArrMedia>,
    onItemClick: (ArrMedia) -> Unit,
    itemIsActive: (ArrMedia) -> Boolean,
    preferences: InstancePreferences,
    multiSelectState: MultiSelectState<Long> = MultiSelectState(selectionModeAvailable = false),
    qualityProfiles: List<QualityProfile> = emptyList(),
    tags: List<Tag> = emptyList(),
) {
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val bottomPadding = LocalFloatingBarBottomPadding.current

    val isSortByTitle = preferences.sortBy.isTitleSort
    val alphabet = remember(preferences.sortOrder) {
        FastScrollUtils.getAlphabet(preferences.sortOrder)
    }
    val letterIndexMap = remember(items, preferences.sortBy) {
        FastScrollUtils.buildLetterIndexMap(items, preferences.sortBy)
    }

    val contentModifier = Modifier
        .fillMaxSize()
        .padding(end = if (isSortByTitle && items.isNotEmpty()) 24.dp else 0.dp)

    Box(modifier = Modifier.fillMaxSize()) {
        when (preferences.viewType) {
            ViewType.List ->
                MediaList(
                    aspectRatio = type.aspectRatio,
                    items = items,
                    onItemClick = onItemClick,
                    itemIsActive = itemIsActive,
                    lazyListState = listState,
                    showBannerBackground = preferences.showBannerBackground,
                    includeOverview = preferences.includeOverview,
                    blur = preferences.bannerBlur,
                    posterElevation = preferences.posterElevation,
                    posterRadius = preferences.posterRadius,
                    multiSelectState = multiSelectState,
                    qualityProfiles = qualityProfiles,
                    tags = tags,
                    modifier = contentModifier,
                )

            ViewType.Grid ->
                PosterGrid(
                    aspectRatio = type.aspectRatio,
                    items = items,
                    onItemClick = onItemClick,
                    itemIsActive = itemIsActive,
                    lazyGridState = gridState,
                    showFullDetails = preferences.showFullDetails,
                    showOverlay = preferences.showOverlay,
                    gridDensity = preferences.gridDensity,
                    gridSpacing = preferences.gridSpacing,
                    posterElevation = preferences.posterElevation,
                    posterRadius = preferences.posterRadius,
                    multiSelectState = multiSelectState,
                    modifier = contentModifier,
                )
        }

        if (isSortByTitle && items.isNotEmpty()) {
            AlphabetFastScroller(
                alphabet = alphabet,
                onLetterSelected = { selectedLetter ->
                    val targetIndex = FastScrollUtils.findTargetIndex(
                        selectedLetter = selectedLetter,
                        letterIndexMap = letterIndexMap,
                        alphabet = alphabet,
                        itemCount = items.size,
                    )

                    coroutineScope.launch {
                        when (preferences.viewType) {
                            ViewType.List -> listState.scrollToItem(targetIndex)
                            ViewType.Grid -> gridState.scrollToItem(targetIndex)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 12.dp, bottom = 12.dp + bottomPadding, end = 2.dp)
                    .align(Alignment.CenterEnd),
            )
        }
    }
}
