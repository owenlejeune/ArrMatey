package com.dnfapps.arrmatey.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import kotlin.jvm.JvmName
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.bazarr.api.model.BazarrMediaType
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.ui.screens.SettingsScreen

/**
 * A generic navigator that manages a reactive backstack of screens.
 */
interface Navigator<T : NavKey> {
    val backStack: SnapshotStateList<T>
    fun navigateTo(screen: T)
    fun popBackStack()
    fun replaceCurrent(screen: T)
    fun replaceBackStack(newStack: List<T>)
    fun clearAndStartWith(screen: T)
    fun popToRoot()
}

/**
 * Base implementation of [Navigator].
 */
open class BaseNavigator<T : NavKey>(initialScreen: T) : Navigator<T> {
    override val backStack = mutableStateListOf(initialScreen)

    override fun navigateTo(screen: T) {
        backStack.add(screen)
    }

    override fun popBackStack() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    override fun replaceCurrent(screen: T) {
        if (backStack.isNotEmpty()) {
            backStack.removeAt(backStack.size - 1)
        }
        backStack.add(screen)
    }

    override fun replaceBackStack(newStack: List<T>) {
        backStack.clear()
        backStack.addAll(newStack)
    }

    override fun clearAndStartWith(screen: T) {
        backStack.clear()
        backStack.add(screen)
    }

    override fun popToRoot() {
        if (backStack.size > 1) {
            val root = backStack.first()
            backStack.clear()
            backStack.add(root)
        }
    }
}

// Marker classes for type-safe DI
class SeriesTabNavigator : BaseNavigator<ArrScreen>(ArrScreen.Library)
class MoviesTabNavigator : BaseNavigator<ArrScreen>(ArrScreen.Library)
class MusicTabNavigator : BaseNavigator<ArrScreen>(ArrScreen.Library)
class RequestsTabNavigator : BaseNavigator<SeerrScreen>(SeerrScreen.Home)
class DiscoverTabNavigator : BaseNavigator<DiscoverScreen>(DiscoverScreen.Home)
class BooksTabNavigator : BaseNavigator<ArrScreen>(ArrScreen.Library)
class AudiobooksTabNavigator : BaseNavigator<ArrScreen>(ArrScreen.Library)
class SettingsTabNavigator : BaseNavigator<SettingsScreen>(SettingsScreen.Landing)
class DashboardTabNavigator : BaseNavigator<DashboardScreen>(DashboardScreen.Main)
class BazarrTabNavigator : BaseNavigator<BazarrScreen>(BazarrScreen.Library)

/**
 * Domain-specific navigation extensions for Arr feature set.
 */
fun Navigator<ArrScreen>.toLibrary() = navigateTo(ArrScreen.Library)
fun Navigator<ArrScreen>.toDetails(
    id: Long? = null,
    tmdbId: Long? = null,
    tvdbId: Long? = null,
    type: InstanceType? = null
) = navigateTo(ArrScreen.Details(id, tmdbId, tvdbId, type))
fun <T> Navigator<ArrScreen>.toPreview(item: T) = navigateTo(ArrScreen.Preview(item))
fun Navigator<ArrScreen>.toSearch(query: String = "") = navigateTo(ArrScreen.Search(query))
fun Navigator<ArrScreen>.toMovieReleases(movieId: Long) = navigateTo(ArrScreen.MovieReleases(movieId))
fun Navigator<ArrScreen>.toMovieFiles(movie: ArrMovie) = navigateTo(ArrScreen.MovieFiles(movie))
fun Navigator<ArrScreen>.toAuthorFiles(author: Author) = navigateTo(ArrScreen.AuthorFiles(author))
fun Navigator<ArrScreen>.toAudiobookFiles(audiobook: Audiobook) = navigateTo(ArrScreen.AudiobookFiles(audiobook))
fun Navigator<ArrScreen>.toEpisodeDetails(series: ArrSeries, episode: Episode) = navigateTo(ArrScreen.EpisodeDetails(series, episode))
fun Navigator<ArrScreen>.toBookDetails(author: Author, book: Book) = navigateTo(ArrScreen.BookDetails(author, book))
fun Navigator<ArrScreen>.toSeriesRelease(seriesId: Long? = null, seasonNumber: Int? = null, episodeId: Long? = null) = navigateTo(ArrScreen.SeriesRelease(seriesId, seasonNumber, episodeId))
fun Navigator<ArrScreen>.toAlbumRelease(albumId: Long, artistId: Long? = null) = navigateTo(ArrScreen.AlbumRelease(albumId, artistId))
fun Navigator<ArrScreen>.toBookRelease(bookId: Long) = navigateTo(ArrScreen.BookRelease(bookId))
fun Navigator<ArrScreen>.toAudiobookRelease(audiobookId: Long?, query: String) = navigateTo(ArrScreen.AudiobookRelease(audiobookId, query))
fun Navigator<ArrScreen>.toPersonDetails(personId: Long) = navigateTo(ArrScreen.PersonDetails(personId))

/**
 * Domain-specific navigation extensions for Seerr feature set.
 */
fun Navigator<SeerrScreen>.toHome() = navigateTo(SeerrScreen.Home)
@JvmName("toSeerrDetails")
fun Navigator<SeerrScreen>.toDetails(tmdbId: Long, requestType: RequestType) = navigateTo(SeerrScreen.Details(tmdbId, requestType))
@JvmName("toSeerrPersonDetails")
fun Navigator<SeerrScreen>.toPersonDetails(personId: Long) = navigateTo(SeerrScreen.PersonDetails(personId))
@JvmName("toSeerrMovieReleases")
fun Navigator<SeerrScreen>.toMovieReleases(movieId: Long) = navigateTo(SeerrScreen.MovieReleases(movieId))
@JvmName("toSeerrMovieFiles")
fun Navigator<SeerrScreen>.toMovieFiles(movie: ArrMovie) = navigateTo(SeerrScreen.MovieFiles(movie))
@JvmName("toSeerrAuthorFiles")
fun Navigator<SeerrScreen>.toAuthorFiles(author: Author) = navigateTo(SeerrScreen.AuthorFiles(author))
@JvmName("toSeerrAudiobookFiles")
fun Navigator<SeerrScreen>.toAudiobookFiles(audiobook: Audiobook) = navigateTo(SeerrScreen.AudiobookFiles(audiobook))
@JvmName("toSeerrEpisodeDetails")
fun Navigator<SeerrScreen>.toEpisodeDetails(series: ArrSeries, episode: Episode) = navigateTo(SeerrScreen.EpisodeDetails(series, episode))
@JvmName("toSeerrBookDetails")
fun Navigator<SeerrScreen>.toBookDetails(author: Author, book: Book) = navigateTo(SeerrScreen.BookDetails(author, book))
@JvmName("toSeerrSeriesRelease")
fun Navigator<SeerrScreen>.toSeriesRelease(seriesId: Long? = null, seasonNumber: Int? = null, episodeId: Long? = null) = navigateTo(SeerrScreen.SeriesRelease(seriesId, seasonNumber, episodeId))
@JvmName("toSeerrAlbumRelease")
fun Navigator<SeerrScreen>.toAlbumRelease(albumId: Long, artistId: Long? = null) = navigateTo(SeerrScreen.AlbumRelease(albumId, artistId))
@JvmName("toSeerrBookRelease")
fun Navigator<SeerrScreen>.toBookRelease(bookId: Long) = navigateTo(SeerrScreen.BookRelease(bookId))
@JvmName("toSeerrAudiobookRelease")
fun Navigator<SeerrScreen>.toAudiobookRelease(audiobookId: Long?, query: String) = navigateTo(SeerrScreen.AudiobookRelease(audiobookId, query))

/**
 * Domain-specific navigation extensions for Discover tab
 */
fun Navigator<DiscoverScreen>.toDiscover() = navigateTo(DiscoverScreen.Home)
@JvmName("toDiscoverDetails")
fun Navigator<DiscoverScreen>.toDetails(tmdbId: Long, requestType: RequestType) = navigateTo(DiscoverScreen.Details(tmdbId, requestType))
@JvmName("toDiscoverPersonDetails")
fun Navigator<DiscoverScreen>.toPersonDetails(personId: Long) = navigateTo(DiscoverScreen.PersonDetails(personId))
@JvmName("toDiscoverMovieReleases")
fun Navigator<DiscoverScreen>.toMovieReleases(movieId: Long) = navigateTo(DiscoverScreen.MovieReleases(movieId))
@JvmName("toDiscoverMovieFiles")
fun Navigator<DiscoverScreen>.toMovieFiles(movie: ArrMovie) = navigateTo(DiscoverScreen.MovieFiles(movie))
@JvmName("toDiscoverAuthorFiles")
fun Navigator<DiscoverScreen>.toAuthorFiles(author: Author) = navigateTo(DiscoverScreen.AuthorFiles(author))
@JvmName("toDiscoverAudiobookFiles")
fun Navigator<DiscoverScreen>.toAudiobookFiles(audiobook: Audiobook) = navigateTo(DiscoverScreen.AudiobookFiles(audiobook))
@JvmName("toDiscoverEpisodeDetails")
fun Navigator<DiscoverScreen>.toEpisodeDetails(series: ArrSeries, episode: Episode) = navigateTo(DiscoverScreen.EpisodeDetails(series, episode))
@JvmName("toDiscoverBookDetails")
fun Navigator<DiscoverScreen>.toBookDetails(author: Author, book: Book) = navigateTo(DiscoverScreen.BookDetails(author, book))
@JvmName("toDiscoverSeriesRelease")
fun Navigator<DiscoverScreen>.toSeriesRelease(seriesId: Long? = null, seasonNumber: Int? = null, episodeId: Long? = null) = navigateTo(DiscoverScreen.SeriesRelease(seriesId, seasonNumber, episodeId))
@JvmName("toDiscoverAlbumRelease")
fun Navigator<DiscoverScreen>.toAlbumRelease(albumId: Long, artistId: Long? = null) = navigateTo(DiscoverScreen.AlbumRelease(albumId, artistId))
@JvmName("toDiscoverBookRelease")
fun Navigator<DiscoverScreen>.toBookRelease(bookId: Long) = navigateTo(DiscoverScreen.BookRelease(bookId))
@JvmName("toDiscoverAudiobookRelease")
fun Navigator<DiscoverScreen>.toAudiobookRelease(audiobookId: Long?, query: String) = navigateTo(DiscoverScreen.AudiobookRelease(audiobookId, query))

/**
 * Domain-specific navigation extensions for Settings feature set.
 */
fun Navigator<SettingsScreen>.toLanding() = navigateTo(SettingsScreen.Landing)
fun Navigator<SettingsScreen>.toAddInstance(type: InstanceType = InstanceType.Sonarr) = navigateTo(SettingsScreen.AddInstance(type))
fun Navigator<SettingsScreen>.toEditInstance(id: Long) = navigateTo(SettingsScreen.EditInstance(id))
fun Navigator<SettingsScreen>.toDev() = navigateTo(SettingsScreen.Dev)
fun Navigator<SettingsScreen>.toTabPreferences() = navigateTo(SettingsScreen.TabPreferences)
fun Navigator<SettingsScreen>.toShortcutsPreferences() = navigateTo(SettingsScreen.ShortcutPreferences)
fun Navigator<SettingsScreen>.toArrDashboard(id: Long) = navigateTo(SettingsScreen.ArrDashboard(id))
fun Navigator<SettingsScreen>.toAddDownloadClient() = navigateTo(SettingsScreen.AddDownloadClient)
fun Navigator<SettingsScreen>.toEditDownloadClient(id: Long) = navigateTo(SettingsScreen.EditDownloadClient(id))
fun Navigator<SettingsScreen>.toAddCustomWebpage() = navigateTo(SettingsScreen.AddCustomWebpage)
fun Navigator<SettingsScreen>.toEditCustomWebpage(id: Long) = navigateTo(SettingsScreen.EditCustomWebpage(id))
fun Navigator<SettingsScreen>.onInstanceTap(id: Long, type: InstanceType) =
    when (type) {
        InstanceType.Sonarr,
        InstanceType.Radarr,
        InstanceType.Lidarr,
        InstanceType.Booksehelf,
        InstanceType.Listenarr -> toArrDashboard(id)
        else -> toEditInstance(id)
    }

fun Navigator<DashboardScreen>.openArrDashboard(id: Long) = navigateTo(DashboardScreen.ArrDashboard(id))

fun Navigator<BazarrScreen>.openDetails(id: Long, type: BazarrMediaType) = navigateTo(BazarrScreen.Details(id, type))
