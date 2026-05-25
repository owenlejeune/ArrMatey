package com.dnfapps.arrmatey.navigation

import androidx.navigation3.runtime.NavKey
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.seerr.api.model.RequestType

sealed interface HomeTab : NavKey {
    data object SeriesTab : HomeTab
    data object MoviesTab: HomeTab
    data object SettingsTab : HomeTab
}

sealed interface ArrScreen : NavKey {
    data object Library: ArrScreen
    data class Details(val id: Long): ArrScreen
    data class Preview<T>(val item: T): ArrScreen
    data class Search(val query: String = ""): ArrScreen
    data class MovieReleases(val movieId: Long): ArrScreen
    data class MovieFiles(val movie: ArrMovie): ArrScreen
    data class AuthorFiles(val author: Author): ArrScreen
    data class AudiobookFiles(val audiobook: Audiobook): ArrScreen
    data class EpisodeDetails(val series: ArrSeries, val episode: Episode): ArrScreen
    data class BookDetails(val author: Author, val book: Book): ArrScreen
    data class SeriesRelease(val seriesId: Long? = null, val seasonNumber: Int? = null, val episodeId: Long? = null): ArrScreen
    data class AlbumRelease(val albumId: Long, val artistId: Long? = null): ArrScreen
    data class BookRelease(val bookId: Long): ArrScreen
    data class AudiobookRelease(val audiobookId: Long?, val query: String): ArrScreen
}

sealed interface SeerrScreen: NavKey {
    data object Home: SeerrScreen
    data class Details(
        val tmdbId: Long,
        val requestType: RequestType
    ): SeerrScreen
}

sealed interface SettingsScreen : NavKey {
    data object Landing : SettingsScreen
    data class AddInstance(val type: InstanceType = InstanceType.Sonarr) : SettingsScreen
    data class EditInstance(val id: Long): SettingsScreen
    data object Dev: SettingsScreen
    data object TabPreferences: SettingsScreen
    data class ArrDashboard(val id: Long): SettingsScreen
    data object AddDownloadClient: SettingsScreen
    data class EditDownloadClient(val id: Long): SettingsScreen
    data object AddCustomWebpage : SettingsScreen
    data class EditCustomWebpage(val id: Long) : SettingsScreen
}