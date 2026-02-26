package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ReleaseParams
import com.dnfapps.arrmatey.arr.viewmodel.ArrMediaViewModel
import com.dnfapps.arrmatey.compose.utils.ReleaseFilterBy
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.ArrScreen
import com.dnfapps.arrmatey.navigation.Navigation
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.ui.screens.ArrLibraryScreen
import com.dnfapps.arrmatey.ui.screens.ArrSearchScreen
import com.dnfapps.arrmatey.ui.screens.EpisodeDetailsScreen
import com.dnfapps.arrmatey.ui.screens.InteractiveSearchScreen
import com.dnfapps.arrmatey.ui.screens.MediaDetailsScreen
import com.dnfapps.arrmatey.ui.screens.MediaPreviewScreen
import com.dnfapps.arrmatey.ui.screens.MovieFilesScreen
import com.dnfapps.arrmatey.utils.koinInjectParams
import org.koin.compose.koinInject

@Composable
fun ArrTab(
    type: InstanceType,
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<ArrScreen> = navigationManager.arr(type)
) {
    NavDisplay(
        backStack = navigation.backStack,
        onBack = { navigation.popBackStack() },
        entryProvider = entryProvider {
            entry<ArrScreen.Library> {
                ArrLibraryScreen(type)
            }
            entry<ArrScreen.Details> { details ->
                MediaDetailsScreen(details.id, type)
            }
            entry<ArrScreen.Search> { search ->
                ArrSearchScreen(search.query, type)
            }
            entry<ArrScreen.Preview<ArrMedia>> { preview ->
                MediaPreviewScreen(preview.item, type)
            }
            entry<ArrScreen.MovieReleases> { params ->
                val releaseParams = ReleaseParams.Movie(params.movieId)
                InteractiveSearchScreen(type, releaseParams)
            }
            entry<ArrScreen.SeriesRelease> { params ->
                val releaseParams = ReleaseParams.Series(
                    params.seriesId,
                    params.seasonNumber,
                    params.episodeId
                )
                InteractiveSearchScreen(
                    type,
                    releaseParams = releaseParams,
                    defaultFilter = if (params.episodeId != null) {
                        ReleaseFilterBy.SingleEpisode
                    } else ReleaseFilterBy.SeasonPack
                )
            }
            entry<ArrScreen.AlbumRelease> { params ->
                val releaseParams = ReleaseParams.Album(
                    artistId = params.artistId,
                    albumId = params.albumId
                )
                InteractiveSearchScreen(type, releaseParams)
            }
            entry<ArrScreen.MovieFiles> { params ->
                MovieFilesScreen(movie = params.movie)
            }
            entry<ArrScreen.EpisodeDetails> { params ->
                EpisodeDetailsScreen(params.series, params.episode)
            }
        }
    )
}