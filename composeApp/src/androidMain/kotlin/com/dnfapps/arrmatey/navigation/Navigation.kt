package com.dnfapps.arrmatey.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
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
class SeriesTabNavigator : BaseNavigator<NavKey>(ArrScreen.Library)
class MoviesTabNavigator : BaseNavigator<NavKey>(ArrScreen.Library)
class MusicTabNavigator : BaseNavigator<NavKey>(ArrScreen.Library)
class RequestsTabNavigator : BaseNavigator<NavKey>(SeerrScreen.Home)
class DiscoverTabNavigator : BaseNavigator<NavKey>(DiscoverScreen.Home)
class BooksTabNavigator : BaseNavigator<NavKey>(ArrScreen.Library)
class AudiobooksTabNavigator : BaseNavigator<NavKey>(ArrScreen.Library)
class SettingsTabNavigator : BaseNavigator<SettingsScreen>(SettingsScreen.Landing)
class DashboardTabNavigator : BaseNavigator<DashboardScreen>(DashboardScreen.Main)
class BazarrTabNavigator : BaseNavigator<BazarrScreen>(BazarrScreen.Library)

/**
 * Shared media navigation extensions.
 */
@Suppress("UNCHECKED_CAST")
private fun Navigator<*>.nav(): Navigator<NavKey> = this as Navigator<NavKey>

fun Navigator<*>.toLibrary() = nav().navigateTo(ArrScreen.Library)
fun Navigator<*>.toHome() = nav().navigateTo(SeerrScreen.Home)
fun Navigator<*>.toDiscover() = nav().navigateTo(DiscoverScreen.Home)

fun Navigator<*>.toDetails(
    id: Long? = null,
    tmdbId: Long? = null,
    tvdbId: Long? = null,
    requestType: RequestType? = null,
    type: InstanceType? = null
) = nav().navigateTo(MediaScreen.Details(id, tmdbId, tvdbId, requestType, type))

fun <T> Navigator<*>.toPreview(item: T) = nav().navigateTo(MediaScreen.Preview(item))
fun Navigator<*>.toSearch(query: String = "") = nav().navigateTo(MediaScreen.Search(query))
fun Navigator<*>.toMovieReleases(movieId: Long) = nav().navigateTo(MediaScreen.MovieReleases(movieId))
fun Navigator<*>.toMovieFiles(movie: ArrMovie) = nav().navigateTo(MediaScreen.MovieFiles(movie))
fun Navigator<*>.toAuthorFiles(author: Author) = nav().navigateTo(MediaScreen.AuthorFiles(author))
fun Navigator<*>.toAudiobookFiles(audiobook: Audiobook) = nav().navigateTo(MediaScreen.AudiobookFiles(audiobook))
fun Navigator<*>.toEpisodeDetails(series: ArrSeries, episode: Episode) = nav().navigateTo(MediaScreen.EpisodeDetails(series, episode))
fun Navigator<*>.toBookDetails(author: Author, book: Book) = nav().navigateTo(MediaScreen.BookDetails(author, book))
fun Navigator<*>.toSeriesRelease(seriesId: Long? = null, seasonNumber: Int? = null, episodeId: Long? = null) = nav().navigateTo(MediaScreen.SeriesRelease(seriesId, seasonNumber, episodeId))
fun Navigator<*>.toAlbumRelease(albumId: Long, artistId: Long? = null) = nav().navigateTo(MediaScreen.AlbumRelease(albumId, artistId))
fun Navigator<*>.toBookRelease(bookId: Long) = nav().navigateTo(MediaScreen.BookRelease(bookId))
fun Navigator<*>.toAudiobookRelease(audiobookId: Long?, query: String) = nav().navigateTo(MediaScreen.AudiobookRelease(audiobookId, query))
fun Navigator<*>.toPersonDetails(personId: Long) = nav().navigateTo(MediaScreen.PersonDetails(personId))

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
        InstanceType.Seerr,
        InstanceType.Bazarr,
        InstanceType.Prowlarr -> toEditInstance(id)
    }

/**
 * Domain-specific navigation extensions for Dashboard feature set.
 */
fun Navigator<DashboardScreen>.toMain() = navigateTo(DashboardScreen.Main)
@JvmName("toDashboardArrDashboard")
fun Navigator<DashboardScreen>.toArrDashboard(id: Long) = navigateTo(DashboardScreen.ArrDashboard(id))
fun Navigator<DashboardScreen>.openArrDashboard(id: Long) = toArrDashboard(id)

/**
 * Domain-specific navigation extensions for Bazarr feature set.
 */
@JvmName("toBazarrLibrary")
fun Navigator<BazarrScreen>.toLibrary() = navigateTo(BazarrScreen.Library)
fun Navigator<BazarrScreen>.toDetails(id: Long, type: BazarrMediaType) = navigateTo(BazarrScreen.Details(id, type))
fun Navigator<BazarrScreen>.openDetails(id: Long, type: BazarrMediaType) = toDetails(id, type)
