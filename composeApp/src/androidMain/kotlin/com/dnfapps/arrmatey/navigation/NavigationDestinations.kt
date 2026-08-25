package com.dnfapps.arrmatey.navigation

import androidx.navigation3.runtime.NavKey
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.bazarr.api.model.BazarrMediaType
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.seerr.api.model.RequestType

sealed interface MediaScreen : NavKey {
    data class Details(
        val id: Long? = null,
        val tmdbId: Long? = null,
        val tvdbId: Long? = null,
        val requestType: RequestType? = null,
        val type: InstanceType? = null,
        val instanceId: Long? = null
    ): MediaScreen
    data class Preview<T>(val item: T): MediaScreen
    data class Search(val query: String = "", val type: InstanceType? = null, val instanceId: Long? = null): MediaScreen
    data class MovieReleases(val movieId: Long): MediaScreen
    data class MovieFiles(val movie: ArrMovie): MediaScreen
    data class AuthorFiles(val author: Author): MediaScreen
    data class AudiobookFiles(val audiobook: Audiobook): MediaScreen
    data class EpisodeDetails(val series: ArrSeries, val episode: Episode): MediaScreen
    data class BookDetails(val author: Author, val book: Book): MediaScreen
    data class SeriesRelease(val seriesId: Long? = null, val seasonNumber: Int? = null, val episodeId: Long? = null): MediaScreen
    data class AlbumRelease(val albumId: Long, val artistId: Long? = null): MediaScreen
    data class BookRelease(val bookId: Long): MediaScreen
    data class AudiobookRelease(val audiobookId: Long?, val query: String): MediaScreen
    data class PersonDetails(val personId: Long): MediaScreen
}

sealed interface ArrScreen : NavKey {
    data object Library: ArrScreen
}

sealed interface SeerrScreen: NavKey {
    data object Home: SeerrScreen
}

sealed interface DiscoverScreen: NavKey {
    data object Home: DiscoverScreen
}

sealed interface CalendarScreen: NavKey {
    data object Home: CalendarScreen
}

sealed interface SettingsScreen : NavKey {
    data object Landing : SettingsScreen
    data class AddInstance(val type: InstanceType = InstanceType.Sonarr) : SettingsScreen
    data class EditInstance(val id: Long): SettingsScreen
    data object Dev: SettingsScreen
    data object TabPreferences: SettingsScreen
    data object ShortcutPreferences: SettingsScreen
    data class ArrDashboard(val id: Long): SettingsScreen
    data object AddDownloadClient: SettingsScreen
    data class EditDownloadClient(val id: Long): SettingsScreen
    data object AddCustomWebpage : SettingsScreen
    data class EditCustomWebpage(val id: Long) : SettingsScreen
}

sealed interface DashboardScreen: NavKey {
    data object Main: DashboardScreen
    data class ArrDashboard(val id: Long): DashboardScreen
}

sealed interface BazarrScreen: NavKey {
    data object Library: BazarrScreen
    data class Details(val id: Long, val type: BazarrMediaType): BazarrScreen
}