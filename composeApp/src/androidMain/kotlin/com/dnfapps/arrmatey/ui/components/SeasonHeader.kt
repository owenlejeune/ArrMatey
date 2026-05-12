package com.dnfapps.arrmatey.ui.components

import com.dnfapps.arrmatey.shared.MR
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.Season
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.entensions.Bullet
import com.dnfapps.arrmatey.extensions.formatMinutesAsRuntime
import com.dnfapps.arrmatey.navigation.ArrScreen
import com.dnfapps.arrmatey.navigation.Navigation
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun SeasonHeader(
    seriesId: Long?,
    season: Season,
    episodes: List<Episode>,
    onPerformAutomaticSearch: (Int) -> Unit,
    searchInProgress: (Int) -> Boolean,
    onDeleteSeason: () -> Unit,
    deleteInProgress: Boolean,
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<ArrScreen> = navigationManager.series()
) {
    val tbaLabel = mokoString(MR.strings.tba)
    val year = remember(episodes) {
        episodes.mapNotNull { it.airDateUtc }.minOrNull()
            ?.toLocalDateTime(TimeZone.UTC)?.date?.year?.toString()
            ?: tbaLabel
    }

    val runtime = remember(episodes) {
        val items = episodes.mapNotNull { it.runtime?.takeIf { r -> r > 0 } }
        if (items.isEmpty()) null
        else items.sorted()[items.size / 2].formatMinutesAsRuntime()
    }

    val seasonInfo = listOfNotNull(
        year, runtime, season.statistics?.sizeOnDisk?.bytesAsFileSizeString()
    )
    val infoString = seasonInfo.joinToString(Bullet)
    Text(
        text = infoString,
        fontSize = 16.sp
    )

    ReleaseDownloadButtons(
        onInteractiveClicked = {
            seriesId?.let { seriesId ->
                val destination = ArrScreen.SeriesRelease(
                    seriesId = seriesId,
                    seasonNumber = season.seasonNumber
                )
                navigation.navigateTo(destination)
            }
        },
        onAutomaticClicked = {
            onPerformAutomaticSearch(season.seasonNumber)
        },
        automaticSearchInProgress = searchInProgress(season.seasonNumber),
        modifier = Modifier.fillMaxWidth(),
        automaticSearchEnabled = episodes.any { it.monitored },
        deleteInProgress = deleteInProgress,
        onDelete = onDeleteSeason,
    )
}