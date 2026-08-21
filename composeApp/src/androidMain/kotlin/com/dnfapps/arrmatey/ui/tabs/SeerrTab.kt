package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.dnfapps.arrmatey.compose.utils.ReleaseFilterBy
import com.dnfapps.arrmatey.arr.api.model.ReleaseParams
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.SeerrScreen
import com.dnfapps.arrmatey.navigation.toAlbumRelease
import com.dnfapps.arrmatey.navigation.toAudiobookFiles
import com.dnfapps.arrmatey.navigation.toAudiobookRelease
import com.dnfapps.arrmatey.navigation.toAuthorFiles
import com.dnfapps.arrmatey.navigation.toBookDetails
import com.dnfapps.arrmatey.navigation.toBookRelease
import com.dnfapps.arrmatey.navigation.toDetails
import com.dnfapps.arrmatey.navigation.toEpisodeDetails
import com.dnfapps.arrmatey.navigation.toMovieFiles
import com.dnfapps.arrmatey.navigation.toMovieReleases
import com.dnfapps.arrmatey.navigation.toPersonDetails
import com.dnfapps.arrmatey.navigation.toSeriesRelease
import com.dnfapps.arrmatey.seerr.viewmodel.RequestsViewModel
import com.dnfapps.arrmatey.ui.components.navigation.forwardSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.popSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.predictivePopSlideTransform
import com.dnfapps.arrmatey.ui.screens.AudiobookFilesScreen
import com.dnfapps.arrmatey.ui.screens.AuthorFilesScreen
import com.dnfapps.arrmatey.ui.screens.BookDetailsScreen
import com.dnfapps.arrmatey.ui.screens.EpisodeDetailsScreen
import com.dnfapps.arrmatey.ui.screens.InteractiveSearchScreen
import com.dnfapps.arrmatey.ui.screens.MovieFilesScreen
import com.dnfapps.arrmatey.ui.screens.RequestsScreen
import com.dnfapps.arrmatey.ui.screens.SeerrPersonDetailsScreen
import com.dnfapps.arrmatey.ui.screens.UnifiedMediaDetailsScreen
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SeerrTab(
    windowSizeClass: WindowSizeClass,
    wideRailIsVisible: Boolean,
    viewModel: RequestsViewModel = koinInject(),
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigator<SeerrScreen> = navigationManager.requests
) {
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    NavDisplay(
        backStack = navigation.backStack,
        onBack = { navigation.popBackStack() },
        transitionSpec = { forwardSlideTransform() },
        popTransitionSpec = { popSlideTransform() },
        predictivePopTransitionSpec = { _ -> predictivePopSlideTransform() },
        entryProvider = entryProvider {
            entry<SeerrScreen.Home> {
                RequestsScreen(
                    viewModel = viewModel,
                    isExpanded = isExpanded,
                    wideRailIsVisible = wideRailIsVisible,
                    onNavigateToDetails = { tmdbId, type ->
                        navigation.toDetails(tmdbId, type)
                    }
                )
            }
            entry<SeerrScreen.Details> { details ->
                UnifiedMediaDetailsScreen(
                    tmdbId = details.tmdbId,
                    requestType = details.requestType,
                    isExpanded = isExpanded,
                    onBack = { navigation.popBackStack() },
                    onNavigateToEpisodeDetails = { series, episode -> navigation.toEpisodeDetails(series, episode) },
                    onNavigateToSeriesRelease = { seriesId, seasonNumber -> navigation.toSeriesRelease(seriesId, seasonNumber) },
                    onNavigateToMovieFiles = { navigation.toMovieFiles(it) },
                    onNavigateToMovieReleases = { navigation.toMovieReleases(it) },
                    onNavigateToAuthorFiles = { navigation.toAuthorFiles(it) },
                    onNavigateToBookDetails = { author, book -> navigation.toBookDetails(author, book) },
                    onNavigateToBookRelease = { navigation.toBookRelease(it) },
                    onNavigateToAudiobookFiles = { navigation.toAudiobookFiles(it) },
                    onNavigateToAudiobookRelease = { id, query -> navigation.toAudiobookRelease(id, query ?: "") },
                    onNavigateToAlbumRelease = { artistId, albumId -> navigation.toAlbumRelease(albumId, artistId) },
                    onPersonClick = { navigation.toPersonDetails(it) }
                )
            }
            entry<SeerrScreen.MovieReleases> { params ->
                val releaseParams = ReleaseParams.Movie(params.movieId)
                InteractiveSearchScreen(
                    instanceType = InstanceType.Radarr,
                    releaseParams = releaseParams,
                    onBack = { navigation.popBackStack() }
                )
            }
            entry<SeerrScreen.SeriesRelease> { params ->
                val releaseParams = ReleaseParams.Series(
                    params.seriesId,
                    params.seasonNumber,
                    params.episodeId
                )
                InteractiveSearchScreen(
                    instanceType = InstanceType.Sonarr,
                    releaseParams = releaseParams,
                    defaultFilter = if (params.episodeId != null) {
                        ReleaseFilterBy.SingleEpisode
                    } else ReleaseFilterBy.SeasonPack,
                    onBack = { navigation.popBackStack() }
                )
            }
            entry<SeerrScreen.AlbumRelease> { params ->
                val releaseParams = ReleaseParams.Album(
                    artistId = params.artistId,
                    mediaId = params.albumId
                )
                InteractiveSearchScreen(
                    instanceType = InstanceType.Lidarr,
                    releaseParams = releaseParams,
                    onBack = { navigation.popBackStack() }
                )
            }
            entry<SeerrScreen.BookRelease> { params ->
                val releaseParams = ReleaseParams.Book(
                    mediaId = params.bookId
                )
                InteractiveSearchScreen(
                    instanceType = InstanceType.Booksehelf,
                    releaseParams = releaseParams,
                    onBack = { navigation.popBackStack() }
                )
            }
            entry<SeerrScreen.MovieFiles> { params ->
                MovieFilesScreen(
                    movie = params.movie,
                    onBack = { navigation.popBackStack() }
                )
            }
            entry<SeerrScreen.AuthorFiles> { params ->
                AuthorFilesScreen(
                    author = params.author,
                    onBack = { navigation.popBackStack() }
                )
            }
            entry<SeerrScreen.EpisodeDetails> { params ->
                EpisodeDetailsScreen(
                    series = params.series,
                    episode = params.episode,
                    onBack = { navigation.popBackStack() },
                    onNavigateToSeriesRelease = { episodeId ->
                        navigation.toSeriesRelease(episodeId = episodeId)
                    }
                )
            }
            entry<SeerrScreen.BookDetails> { params ->
                BookDetailsScreen(
                    book = params.book,
                    author = params.author,
                    onBack = { navigation.popBackStack() },
                    onNavigateToBookRelease = { bookId ->
                        navigation.toBookRelease(bookId = bookId)
                    }
                )
            }
            entry<SeerrScreen.AudiobookFiles> { params ->
                AudiobookFilesScreen(
                    audiobook = params.audiobook,
                    onBack = { navigation.popBackStack() }
                )
            }
            entry<SeerrScreen.AudiobookRelease> { params ->
                val releaseParams = ReleaseParams.Audiobook(
                    mediaId = params.audiobookId,
                    query = params.query
                )
                InteractiveSearchScreen(
                    instanceType = InstanceType.Listenarr,
                    releaseParams = releaseParams,
                    onBack = { navigation.popBackStack() }
                )
            }
            entry<SeerrScreen.PersonDetails> { details ->
                SeerrPersonDetailsScreen(
                    personId = details.personId,
                    onBack = { navigation.popBackStack() },
                    onMediaClick = { tmdbId, type ->
                        navigation.toDetails(tmdbId, type)
                    }
                )
            }
        }
    )
}