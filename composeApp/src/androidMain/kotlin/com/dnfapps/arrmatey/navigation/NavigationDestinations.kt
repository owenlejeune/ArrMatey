package com.dnfapps.arrmatey.navigation

import androidx.navigation3.runtime.NavKey
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.instances.model.InstanceType

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
    data class EpisodeDetails(val series: ArrSeries, val episode: Episode): ArrScreen
    data class SeriesRelease(val seriesId: Long? = null, val seasonNumber: Int? = null, val episodeId: Long? = null): ArrScreen
    data class AlbumRelease(val albumId: Long, val artistId: Long? = null): ArrScreen

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