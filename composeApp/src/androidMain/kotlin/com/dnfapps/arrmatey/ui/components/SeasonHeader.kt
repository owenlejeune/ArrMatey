package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dnfapps.arrmatey.model.SeasonWrapper

@Composable
fun SeasonHeader(
    season: SeasonWrapper,
    modifier: Modifier = Modifier,
    seriesId: Long? = null,
    onPerformAutomaticSearch: (Int) -> Unit = {},
    searchInProgress: (Int) -> Boolean = { false },
    onDeleteSeason: () -> Unit = {},
    deleteInProgress: Boolean = false,
    onNavigateToSeriesRelease: (Long?, Int) -> Unit = { _, _ -> },
) {
    if (seriesId != null && season.arrSeason != null) {
        ReleaseDownloadButtons(
            onInteractiveClicked = {
                onNavigateToSeriesRelease(seriesId, season.seasonNumber)
            },
            onAutomaticClicked = {
                onPerformAutomaticSearch(season.seasonNumber)
            },
            automaticSearchInProgress = searchInProgress(season.seasonNumber),
            modifier = modifier.fillMaxWidth(),
            automaticSearchEnabled = season.episodes.any { it.isMonitored },
            deleteInProgress = deleteInProgress,
            onDelete = onDeleteSeason,
        )
    }
}
