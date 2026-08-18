package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.entensions.Bullet
import com.dnfapps.arrmatey.extensions.formatMinutesAsRuntime
import com.dnfapps.arrmatey.model.SeasonWrapper
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun SeasonHeader(
    season: SeasonWrapper,
    modifier: Modifier = Modifier,
    seriesId: Long? = null,
    onPerformAutomaticSearch: (Int) -> Unit = {},
    searchInProgress: (Int) -> Boolean = { false },
    onDeleteSeason: () -> Unit = {},
    deleteInProgress: Boolean = false,
    onNavigateToSeriesRelease: (Long?, Int) -> Unit = { _, _ -> }
) {
    val tbaLabel = mokoString(MR.strings.tba)
    val year = remember(season.episodes) {
        season.episodes.mapNotNull { it.airDateUtc }.minOrNull()
            ?.toLocalDateTime(TimeZone.UTC)?.date?.year?.toString()
            ?: season.episodes.mapNotNull { it.airDate?.year }.minOrNull()?.toString()
            ?: tbaLabel
    }

    val runtime = remember(season.episodes) {
        val items = season.episodes.mapNotNull { it.arrEpisode?.runtime?.takeIf { r -> r > 0 } }
        if (items.isEmpty()) null
        else items.sorted()[items.size / 2].formatMinutesAsRuntime()
    }

    val sizeOnDisk = season.arrSeason?.statistics?.sizeOnDisk?.bytesAsFileSizeString()

    val seasonInfo = listOfNotNull(year, runtime, sizeOnDisk)
    val infoString = seasonInfo.joinToString(Bullet)
    if (infoString.isNotBlank()) {
        Text(
            text = infoString,
            fontSize = 16.sp,
            modifier = modifier
        )
    }

    if (seriesId != null && season.arrSeason != null) {
        ReleaseDownloadButtons(
            onInteractiveClicked = {
                onNavigateToSeriesRelease(seriesId, season.seasonNumber)
            },
            onAutomaticClicked = {
                onPerformAutomaticSearch(season.seasonNumber)
            },
            automaticSearchInProgress = searchInProgress(season.seasonNumber),
            modifier = Modifier.fillMaxWidth(),
            automaticSearchEnabled = season.episodes.any { it.isMonitored },
            deleteInProgress = deleteInProgress,
            onDelete = onDeleteSeason,
        )
    }
}