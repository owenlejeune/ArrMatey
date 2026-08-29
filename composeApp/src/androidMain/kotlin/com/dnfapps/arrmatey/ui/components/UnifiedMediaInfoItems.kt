package com.dnfapps.arrmatey.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLocale
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.ArtistMonitorType
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.AuthorMonitorType
import com.dnfapps.arrmatey.arr.api.model.MonitorNewItems
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.compose.utils.formatWithCommas
import com.dnfapps.arrmatey.entensions.BULLET
import com.dnfapps.arrmatey.model.InfoItem
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.seerr.api.model.MovieDetails
import com.dnfapps.arrmatey.seerr.api.model.PersonDetails
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString
import kotlin.time.ExperimentalTime

@Composable
fun buildArrInfoItems(
    state: UnifiedMediaDetailsUiState.Success,
    qualityProfiles: List<QualityProfile>,
    tags: List<Tag>,
    onEditPath: () -> Unit,
): List<InfoItem> =
    buildList {
        val arrMedia = state.arrMedia
        if (arrMedia != null && state.hasArrId) {
            val arrItems =
                when (arrMedia) {
                    is ArrSeries -> seriesInfo(arrMedia, qualityProfiles, tags, onEditPath)
                    is ArrMovie -> movieInfo(arrMedia, qualityProfiles, tags, onEditPath)
                    is Arrtist -> artistInfo(arrMedia, qualityProfiles, tags, onEditPath)
                    is Author -> authorInfo(arrMedia, qualityProfiles, tags, onEditPath)
                    is Audiobook -> audiobookInfo(arrMedia, onEditPath)
                    else -> emptyList()
                }
            addAll(arrItems)
        }
    }

@Composable
fun buildSeerrInfoItems(state: UnifiedMediaDetailsUiState.Success): List<InfoItem> =
    buildList {
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

@Composable
fun seriesInfo(
    series: ArrSeries,
    qualityProfiles: List<QualityProfile>,
    tags: List<Tag>,
    onEditPath: () -> Unit,
): List<InfoItem> {
    val qualityProfile = qualityProfiles.firstOrNull { it.id == series.qualityProfileId }
    val tagsLabel = series.formatTags(tags) ?: mokoString(MR.strings.none)

    val unknown = mokoString(MR.strings.unknown)
    val monitorLabel =
        if (series.monitorNewItems == MonitorNewItems.All) {
            mokoString(MR.strings.monitored)
        } else {
            mokoString(MR.strings.unmonitored)
        }

    val seasonFolderLabel =
        if (series.seasonFolder) {
            mokoString(MR.strings.yes)
        } else {
            mokoString(MR.strings.no)
        }

    val diskSize = series.fileSize.bytesAsFileSizeString()

    return listOf(
        InfoItem(mokoString(MR.strings.status), mokoString(series.status.resource)),
        InfoItem(mokoString(MR.strings.series_type), series.seriesType.name),
        InfoItem(mokoString(MR.strings.size_on_disk), diskSize),
        InfoItem(mokoString(MR.strings.root_folder), (series.rootFolderPath ?: unknown), onClick = onEditPath),
        InfoItem(mokoString(MR.strings.path), (series.path ?: unknown), onClick = onEditPath),
        InfoItem(mokoString(MR.strings.new_seasons), monitorLabel),
        InfoItem(mokoString(MR.strings.season_folders), seasonFolderLabel),
        InfoItem(mokoString(MR.strings.quality_profile), (qualityProfile?.name ?: unknown)),
        InfoItem(mokoString(MR.strings.tags), tagsLabel),
    )
}

@OptIn(ExperimentalTime::class)
@Composable
fun movieInfo(
    movie: ArrMovie,
    qualityProfiles: List<QualityProfile>,
    tags: List<Tag>,
    onEditPath: () -> Unit,
): List<InfoItem> {
    val qualityProfile = qualityProfiles.firstOrNull { it.id == movie.qualityProfileId }
    val tagsLabel = movie.formatTags(tags) ?: mokoString(MR.strings.none)

    val unknown = mokoString(MR.strings.unknown)

    val rootFolderPathValue =
        movie.rootFolderPath.takeUnless { it.isBlank() }
            ?: mokoString(MR.strings.unknown)

    return buildList {
        add(InfoItem(mokoString(MR.strings.status), mokoString(movie.status.resource)))
        add(InfoItem(mokoString(MR.strings.minimum_availability), movie.minimumAvailability.name))
        add(InfoItem(mokoString(MR.strings.root_folder), rootFolderPathValue, onClick = onEditPath))
        add(InfoItem(mokoString(MR.strings.path), (movie.path ?: unknown), onClick = onEditPath))
        movie.inCinemas?.format("MMM d, yyyy")?.let {
            add(InfoItem(mokoString(MR.strings.in_cinemas), it))
        }
        movie.physicalRelease?.format("MMM d, yyyy")?.let {
            add(InfoItem(mokoString(MR.strings.physical_release), it))
        }
        movie.digitalRelease?.format("MMM d, yyyy")?.let {
            add(InfoItem(mokoString(MR.strings.digital_release), it))
        }
        add(InfoItem(mokoString(MR.strings.quality_profile), (qualityProfile?.name ?: unknown)))
        add(InfoItem(mokoString(MR.strings.tags), tagsLabel))
    }
}

@Composable
fun artistInfo(
    artist: Arrtist,
    qualityProfiles: List<QualityProfile>,
    tags: List<Tag>,
    onEditPath: () -> Unit,
): List<InfoItem> {
    val qualityProfile = qualityProfiles.firstOrNull { it.id == artist.qualityProfileId }
    val tagsLabel = artist.formatTags(tags) ?: mokoString(MR.strings.none)

    val unknown = mokoString(MR.strings.unknown)
    val monitorLabel =
        if (artist.monitorNewItems == ArtistMonitorType.All) {
            mokoString(MR.strings.monitored)
        } else {
            mokoString(MR.strings.unmonitored)
        }

    val rootFolderPathValue =
        artist.rootFolderPath?.takeUnless { it.isBlank() }
            ?: mokoString(MR.strings.unknown)

    val diskSize = artist.fileSize.bytesAsFileSizeString()

    return buildList {
        add(InfoItem(mokoString(MR.strings.status), mokoString(artist.status.resource)))
        add(InfoItem(mokoString(MR.strings.size_on_disk), diskSize))
        add(InfoItem(mokoString(MR.strings.root_folder), rootFolderPathValue, onClick = onEditPath))
        add(InfoItem(mokoString(MR.strings.path), (artist.path ?: unknown), onClick = onEditPath))
        add(InfoItem(mokoString(MR.strings.new_albums), monitorLabel))
        add(InfoItem(mokoString(MR.strings.quality_profile), (qualityProfile?.name ?: unknown)))
        add(InfoItem(mokoString(MR.strings.tags), tagsLabel))
    }
}

@Composable
fun authorInfo(
    author: Author,
    qualityProfiles: List<QualityProfile>,
    tags: List<Tag>,
    onEditPath: () -> Unit,
): List<InfoItem> {
    val qualityProfile = qualityProfiles.firstOrNull { it.id == author.qualityProfileId }
    val tagsLabel = author.formatTags(tags) ?: mokoString(MR.strings.none)

    val unknown = mokoString(MR.strings.unknown)
    val monitorLabel =
        if (author.monitorNewItems == AuthorMonitorType.All) {
            mokoString(MR.strings.monitored)
        } else {
            mokoString(MR.strings.unmonitored)
        }

    val rootFolderPathValue =
        author.rootFolderPath?.takeUnless { it.isBlank() }
            ?: mokoString(MR.strings.unknown)

    val diskSize = author.fileSize.bytesAsFileSizeString()

    return buildList {
        add(InfoItem(mokoString(MR.strings.status), mokoString(author.status.resource)))
        add(InfoItem(mokoString(MR.strings.size_on_disk), diskSize))
        add(InfoItem(mokoString(MR.strings.root_folder), rootFolderPathValue, onClick = onEditPath))
        add(InfoItem(mokoString(MR.strings.path), (author.path ?: unknown), onClick = onEditPath))
        add(InfoItem(mokoString(MR.strings.new_books), monitorLabel))
        add(InfoItem(mokoString(MR.strings.quality_profile), (qualityProfile?.name ?: unknown)))
        add(InfoItem(mokoString(MR.strings.tags), tagsLabel))
    }
}

@Composable
fun audiobookInfo(
    audiobook: Audiobook,
    onEditPath: () -> Unit,
): List<InfoItem> {
    val unknown = mokoString(MR.strings.unknown)

    val diskSize = audiobook.fileSize.bytesAsFileSizeString()

    val authorString = audiobook.authors.takeUnless { it.isEmpty() }?.joinToString(BULLET) ?: unknown
    val narratorsString = audiobook.narrators.takeUnless { it.isEmpty() }?.joinToString(BULLET) ?: unknown

    return buildList {
        add(InfoItem(mokoString(MR.strings.audiobook_info_authors), authorString))
        add(InfoItem(mokoString(MR.strings.audiobook_info_narrators), narratorsString))
        add(InfoItem(mokoString(MR.strings.publisher), (audiobook.publisher ?: unknown)))
        audiobook.language?.let { language ->
            add(
                InfoItem(
                    mokoString(MR.strings.language),
                    language.replaceFirstChar { if (it.isLowerCase()) it.titlecase(LocalLocale.current.platformLocale) else it.toString() },
                ),
            )
        }
        add(InfoItem(mokoString(MR.strings.size_on_disk), diskSize))
        add(InfoItem(mokoString(MR.strings.path), (audiobook.path ?: unknown), onClick = onEditPath))
    }
}
