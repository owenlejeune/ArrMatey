package com.dnfapps.arrmatey.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.bazarr.api.model.BazarrMediaType
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.mediaserver.model.MediaType
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem
import com.dnfapps.arrmatey.mediaserver.usecase.ResolveSonarrEpisodeUseCase
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * A generic navigator that manages a reactive backstack of screens.
 */
@Stable
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
open class BaseNavigator<T : NavKey>(
    initialScreen: T,
) : Navigator<T> {
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
class LibraryTabNavigator : BaseNavigator<NavKey>(ArrScreen.Library)

class SeriesTabNavigator : BaseNavigator<NavKey>(ArrScreen.Library)

class MoviesTabNavigator : BaseNavigator<NavKey>(ArrScreen.Library)

class MusicTabNavigator : BaseNavigator<NavKey>(ArrScreen.Library)

class RequestsTabNavigator : BaseNavigator<NavKey>(SeerrScreen.Home)

class DiscoverTabNavigator : BaseNavigator<NavKey>(DiscoverScreen.Home)

class CalendarTabNavigator : BaseNavigator<NavKey>(CalendarScreen.Home)

class ActivityTabNavigator : BaseNavigator<NavKey>(ActivityScreen.Home)

class BooksTabNavigator : BaseNavigator<NavKey>(ArrScreen.Library)

class AudiobooksTabNavigator : BaseNavigator<NavKey>(ArrScreen.Library)
class ComicsTabNavigator : BaseNavigator<NavKey>(ArrScreen.Library)

class SettingsTabNavigator : BaseNavigator<SettingsScreen>(SettingsScreen.Landing)

class DashboardTabNavigator : BaseNavigator<NavKey>(DashboardScreen.Main)

class BazarrTabNavigator : BaseNavigator<BazarrScreen>(BazarrScreen.Library)

class TracearrTabNavigator : BaseNavigator<NavKey>(TracearrScreen.Main)

class MediaServerLibraryTabNavigator : BaseNavigator<NavKey>(MediaServerLibraryScreen.Library)

/**
 * Shared media navigation extensions.
 */
@Suppress("UNCHECKED_CAST")
private fun Navigator<*>.nav(): Navigator<NavKey> = this as Navigator<NavKey>

fun Navigator<*>.toLibrary() = nav().navigateTo(ArrScreen.Library)

fun Navigator<*>.toComicReleases(
    mediaId: Long,
    isVolume: Boolean,
    instanceId: Long? = null,
) = nav().navigateTo(MediaScreen.ComicReleases(mediaId, isVolume, instanceId))

fun Navigator<*>.toHome() = nav().navigateTo(SeerrScreen.Home)

fun Navigator<*>.toDiscover() = nav().navigateTo(DiscoverScreen.Home)

fun Navigator<*>.toArrDetailsOrPreview(
    item: ArrMedia,
    type: InstanceType? = null,
    instanceId: Long? = null,
) {
    val isAdded = item.id != null && item.id != 0L
    if (!isAdded) {
        if (item is ArrMovie || item is ArrSeries) {
            val tmdbId = (item as? ArrMovie)?.tmdbId?.takeIf { it > 0 }
                ?: (item as? ArrSeries)?.tmdbId?.takeIf { it > 0 }
            val tvdbId = (item as? ArrSeries)?.tvdbId?.takeIf { it > 0 }
            toDetails(id = null, tmdbId = tmdbId, tvdbId = tvdbId, type = type, instanceId = instanceId)
        } else {
            toPreview(item, type)
        }
    } else {
        val tmdbId = (item as? ArrMovie)?.tmdbId?.takeIf { it > 0 }
            ?: (item as? ArrSeries)?.tmdbId?.takeIf { it > 0 }
        val tvdbId = (item as? ArrSeries)?.tvdbId?.takeIf { it > 0 }
        toDetails(id = item.id, tmdbId = tmdbId, tvdbId = tvdbId, type = type, instanceId = instanceId)
    }
}

fun Navigator<*>.toDetails(
    id: Long? = null,
    tmdbId: Long? = null,
    tvdbId: Long? = null,
    requestType: RequestType? = null,
    type: InstanceType? = null,
    instanceId: Long? = null,
    episodeId: Long? = null,
    mediaServerItemId: String? = null,
    mediaServerType: MediaServerType? = null,
    initialTitle: String? = null,
    initialYear: Int? = null,
) = nav().navigateTo(
    MediaScreen.Details(
        id = id?.takeIf { it > 0 },
        tmdbId = tmdbId,
        tvdbId = tvdbId,
        requestType = requestType,
        type = type,
        instanceId = instanceId,
        episodeId = episodeId,
        mediaServerItemId = mediaServerItemId,
        mediaServerType = mediaServerType,
        initialTitle = initialTitle,
        initialYear = initialYear,
    ),
)

fun Navigator<*>.replaceDetails(
    id: Long? = null,
    tmdbId: Long? = null,
    tvdbId: Long? = null,
    requestType: RequestType? = null,
    type: InstanceType? = null,
    instanceId: Long? = null,
    episodeId: Long? = null,
    mediaServerItemId: String? = null,
    mediaServerType: MediaServerType? = null,
    initialTitle: String? = null,
    initialYear: Int? = null,
) = nav().replaceCurrent(
    MediaScreen.Details(
        id = id?.takeIf { it > 0 },
        tmdbId = tmdbId,
        tvdbId = tvdbId,
        requestType = requestType,
        type = type,
        instanceId = instanceId,
        episodeId = episodeId,
        mediaServerItemId = mediaServerItemId,
        mediaServerType = mediaServerType,
        initialTitle = initialTitle,
        initialYear = initialYear,
    ),
)

fun Navigator<*>.toMediaDetails(
    media: ArrMedia,
    type: InstanceType? = null,
    instanceId: Long? = null,
) {
    val tmdbId =
        when (media) {
            is ArrMovie -> media.tmdbId.takeIf { it > 0 }
            is ArrSeries -> media.tmdbId?.takeIf { it > 0 }
            else -> null
        }
    val tvdbId = (media as? ArrSeries)?.tvdbId?.takeIf { it > 0 }
    toDetails(
        id = media.id?.takeIf { it > 0 },
        tmdbId = tmdbId,
        tvdbId = tvdbId,
        type = type,
        instanceId = instanceId,
    )
}

fun Navigator<*>.toMediaDetails(item: UnifiedMediaItem) {
    val reqType = when (item.type) {
        MediaType.MOVIE -> RequestType.Movie
        MediaType.SHOW, MediaType.SEASON, MediaType.EPISODE -> RequestType.Tv
        else -> null
    }
    val instanceType = when (item.type) {
        MediaType.MOVIE -> InstanceType.Radarr
        MediaType.SHOW, MediaType.SEASON, MediaType.EPISODE -> InstanceType.Sonarr
        MediaType.TRACK, MediaType.ALBUM -> InstanceType.Lidarr
        else -> null
    }
    toDetails(
        id = null,
        tmdbId = item.tmdbId,
        tvdbId = item.tvdbId,
        requestType = reqType,
        type = instanceType,
        mediaServerItemId = item.id,
        mediaServerType = item.serverType,
        initialTitle = item.title,
        initialYear = item.year,
    )
}

private var isResolvingMediaServerItem = false

fun Navigator<*>.navigateToMediaServerItem(
    item: UnifiedMediaItem,
    scope: CoroutineScope,
    resolveEpisodeUseCase: ResolveSonarrEpisodeUseCase,
) {
    if (item.type == MediaType.EPISODE) {
        if (isResolvingMediaServerItem) return
        isResolvingMediaServerItem = true
        scope.launch {
            try {
                val match = resolveEpisodeUseCase(item)
                if (match != null) {
                    toDetails(
                        id = match.series.id,
                        tmdbId = match.series.tmdbId?.takeIf { it > 0 },
                        tvdbId = match.series.tvdbId.takeIf { it > 0 },
                        type = InstanceType.Sonarr,
                        requestType = RequestType.Tv,
                        instanceId = match.instanceId,
                        initialTitle = match.series.title,
                        initialYear = match.series.year,
                    )
                    toEpisodeDetails(
                        series = match.series,
                        episode = match.episode,
                        instanceId = match.instanceId,
                    )
                } else {
                    toMediaDetails(item)
                }
            } finally {
                isResolvingMediaServerItem = false
            }
        }
    } else {
        toMediaDetails(item)
    }
}

fun <T> Navigator<*>.toPreview(
    item: T,
    type: InstanceType? = null,
) = nav().navigateTo(MediaScreen.Preview(item, type))

fun Navigator<*>.toSearch(
    query: String = "",
    type: InstanceType? = null,
    instanceId: Long? = null,
) = nav().navigateTo(MediaScreen.Search(query, type, instanceId))

fun Navigator<*>.toGlobalSearch(query: String = "") = nav().navigateTo(MediaScreen.GlobalSearch(query))

fun Navigator<*>.toMovieReleases(
    movieId: Long,
    instanceId: Long? = null,
) = nav().navigateTo(MediaScreen.MovieReleases(movieId, instanceId))

fun Navigator<*>.toEpisodeDetails(
    series: ArrSeries,
    episode: Episode,
    instanceId: Long? = null,
) = nav().navigateTo(MediaScreen.EpisodeDetails(series, episode, instanceId))

fun Navigator<*>.toBookDetails(
    author: Author,
    book: Book,
    instanceId: Long? = null,
) = nav().navigateTo(MediaScreen.BookDetails(author, book, instanceId))

fun Navigator<*>.toSeriesRelease(
    seriesId: Long? = null,
    seasonNumber: Int? = null,
    episodeId: Long? = null,
    instanceId: Long? = null,
) = nav().navigateTo(MediaScreen.SeriesRelease(seriesId, seasonNumber, episodeId, instanceId))

fun Navigator<*>.toAlbumRelease(
    albumId: Long,
    artistId: Long? = null,
    instanceId: Long? = null,
) = nav().navigateTo(MediaScreen.AlbumRelease(albumId, artistId, instanceId))

fun Navigator<*>.toBookRelease(
    bookId: Long,
    instanceId: Long? = null,
) = nav().navigateTo(MediaScreen.BookRelease(bookId, instanceId))

fun Navigator<*>.toAudiobookRelease(
    audiobookId: Long?,
    query: String,
    instanceId: Long? = null,
) = nav().navigateTo(MediaScreen.AudiobookRelease(audiobookId, query, instanceId))

fun Navigator<*>.toPersonDetails(personId: Long) = nav().navigateTo(MediaScreen.PersonDetails(personId))

fun Navigator<*>.toPersonWebView(url: String) = nav().navigateTo(MediaScreen.PersonWebView(url))

fun Navigator<*>.toTracearrUser(userRef: String) = nav().navigateTo(MediaScreen.TracearrUser(userRef))

/**
 * Domain-specific navigation extensions for Settings feature set.
 */
fun Navigator<SettingsScreen>.toLanding() = navigateTo(SettingsScreen.Landing)

fun Navigator<SettingsScreen>.toAddInstance(type: InstanceType = InstanceType.Sonarr) {
    if (backStack.lastOrNull() != SettingsScreen.Services) {
        navigateTo(SettingsScreen.Services)
    }
    navigateTo(SettingsScreen.AddInstance(type))
}

fun Navigator<SettingsScreen>.toEditInstance(id: Long) {
    if (backStack.lastOrNull() !is SettingsScreen.ArrDashboard) {
        navigateTo(SettingsScreen.Services)
    }
    navigateTo(SettingsScreen.EditInstance(id))
}

fun Navigator<SettingsScreen>.toDev() = navigateTo(SettingsScreen.Dev)

fun Navigator<SettingsScreen>.toOnboarding() = navigateTo(SettingsScreen.Onboarding)

fun Navigator<SettingsScreen>.toTabPreferences() {
    if (backStack.lastOrNull() != SettingsScreen.UserInterface) {
        navigateTo(SettingsScreen.UserInterface)
    }
    navigateTo(SettingsScreen.TabPreferences)
}

fun Navigator<SettingsScreen>.toShortcutsPreferences() {
    if (backStack.lastOrNull() != SettingsScreen.UserInterface) {
        navigateTo(SettingsScreen.UserInterface)
    }
    navigateTo(SettingsScreen.ShortcutPreferences)
}

fun Navigator<SettingsScreen>.toArrDashboard(id: Long) {
    if (backStack.lastOrNull() != SettingsScreen.Services) {
        navigateTo(SettingsScreen.Services)
    }
    navigateTo(SettingsScreen.ArrDashboard(id))
}

fun Navigator<SettingsScreen>.toAddDownloadClient() {
    if (backStack.lastOrNull() != SettingsScreen.Services) {
        navigateTo(SettingsScreen.Services)
    }
    navigateTo(SettingsScreen.AddDownloadClient)
}

fun Navigator<SettingsScreen>.toDownloadClientDashboard(id: Long) {
    if (backStack.lastOrNull() != SettingsScreen.Services) {
        navigateTo(SettingsScreen.Services)
    }
    navigateTo(SettingsScreen.DownloadClientDashboard(id))
}

fun Navigator<SettingsScreen>.toEditDownloadClient(id: Long) {
    if (backStack.lastOrNull() != SettingsScreen.Services) {
        navigateTo(SettingsScreen.Services)
    }
    navigateTo(SettingsScreen.EditDownloadClient(id))
}

fun Navigator<SettingsScreen>.toAddCustomWebpage() {
    if (backStack.lastOrNull() != SettingsScreen.Services) {
        navigateTo(SettingsScreen.Services)
    }
    navigateTo(SettingsScreen.AddCustomWebpage)
}

fun Navigator<SettingsScreen>.toEditCustomWebpage(id: Long) {
    if (backStack.lastOrNull() != SettingsScreen.Services) {
        navigateTo(SettingsScreen.Services)
    }
    navigateTo(SettingsScreen.EditCustomWebpage(id))
}

fun Navigator<SettingsScreen>.toAddWolDevice() {
    if (backStack.lastOrNull() != SettingsScreen.Services) {
        navigateTo(SettingsScreen.Services)
    }
    navigateTo(SettingsScreen.AddWolDevice)
}

fun Navigator<SettingsScreen>.toEditWolDevice(id: Long) {
    if (backStack.lastOrNull() != SettingsScreen.Services) {
        navigateTo(SettingsScreen.Services)
    }
    navigateTo(SettingsScreen.EditWolDevice(id))
}

fun Navigator<SettingsScreen>.toServices() = navigateTo(SettingsScreen.Services)

fun Navigator<SettingsScreen>.toUserInterface() = navigateTo(SettingsScreen.UserInterface)

fun Navigator<SettingsScreen>.toIntegrations() = navigateTo(SettingsScreen.Integrations)

fun Navigator<SettingsScreen>.toBackupRestore() = navigateTo(SettingsScreen.BackupRestore)

fun Navigator<SettingsScreen>.onInstanceTap(
    id: Long,
    type: InstanceType,
) = toArrDashboard(id)

/**
 * Domain-specific navigation extensions for Dashboard feature set.
 */
fun Navigator<*>.toMain() = nav().navigateTo(DashboardScreen.Main)

@JvmName("toDashboardArrDashboard")
fun Navigator<*>.toArrDashboard(id: Long) = nav().navigateTo(DashboardScreen.ArrDashboard(id))

fun Navigator<*>.openArrDashboard(id: Long) = nav().navigateTo(DashboardScreen.ArrDashboard(id))

/**
 * Domain-specific navigation extensions for Bazarr feature set.
 */
@JvmName("toBazarrLibrary")
fun Navigator<BazarrScreen>.toLibrary() = navigateTo(BazarrScreen.Library)

fun Navigator<BazarrScreen>.toDetails(
    id: Long,
    type: BazarrMediaType,
) = navigateTo(BazarrScreen.Details(id, type))

fun Navigator<BazarrScreen>.openDetails(
    id: Long,
    type: BazarrMediaType,
) = toDetails(id, type)
