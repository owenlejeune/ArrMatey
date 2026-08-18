package com.dnfapps.arrmatey.ui.components

import androidx.compose.runtime.Composable
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.compose.utils.formatWithCommas
import com.dnfapps.arrmatey.model.InfoItem
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.model.toInfoList
import com.dnfapps.arrmatey.seerr.api.model.MovieDetails
import com.dnfapps.arrmatey.seerr.api.model.PersonDetails
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.screens.artistInfo
import com.dnfapps.arrmatey.ui.screens.audiobookInfo
import com.dnfapps.arrmatey.ui.screens.authorInfo
import com.dnfapps.arrmatey.ui.screens.movieInfo
import com.dnfapps.arrmatey.ui.screens.seriesInfo
import com.dnfapps.arrmatey.utils.format
import dev.icerock.moko.resources.compose.stringResource as mokoString

@Composable
fun buildUnifiedInfoItems(
    state: UnifiedMediaDetailsUiState.Success,
    qualityProfiles: List<QualityProfile>,
    tags: List<Tag>
): List<InfoItem> = buildList {
    val arrMedia = state.arrMedia
    if (arrMedia != null && state.hasArrId) {
        val arrMap = when (arrMedia) {
            is ArrSeries -> seriesInfo(arrMedia, qualityProfiles, tags)
            is ArrMovie -> movieInfo(arrMedia, qualityProfiles, tags)
            is Arrtist -> artistInfo(arrMedia, qualityProfiles, tags)
            is Author -> authorInfo(arrMedia, qualityProfiles, tags)
            is Audiobook -> audiobookInfo(arrMedia)
            else -> emptyMap()
        }.toInfoList()
        addAll(arrMap)
    }

    val seerrMedia = state.seerrMedia
    if (seerrMedia != null && seerrMedia !is PersonDetails) {
        val statusLabel = mokoString(MR.strings.status)
        add(InfoItem(statusLabel, seerrMedia.status))

        (seerrMedia as? MovieDetails)?.let { movie ->
            movie.releaseDate?.format("MMM dd, yyyy")?.let { releaseDate ->
                add(InfoItem(mokoString(MR.strings.release_date), releaseDate))
            }
            if (movie.revenue > 0L) {
                add(InfoItem(mokoString(MR.strings.revenue), movie.revenue.formatWithCommas()))
            }
            if (movie.budget > 0L) {
                add(InfoItem(mokoString(MR.strings.budget), movie.budget.formatWithCommas()))
            }
        }

        val countriesText = seerrMedia.productionCountries.joinToString("\n") { it.name }
        if (countriesText.isNotEmpty()) {
            add(InfoItem(mokoString(MR.strings.production_countries), countriesText))
        }
        val studiosText = seerrMedia.productionCompanies.joinToString("\n") { it.name }
        if (studiosText.isNotEmpty()) {
            add(InfoItem(mokoString(MR.strings.studios), studiosText))
        }
    }
}
