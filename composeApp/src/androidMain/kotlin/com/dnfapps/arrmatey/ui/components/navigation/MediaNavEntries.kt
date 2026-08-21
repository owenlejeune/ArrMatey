package com.dnfapps.arrmatey.ui.components.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ReleaseParams
import com.dnfapps.arrmatey.compose.utils.ReleaseFilterBy
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.navigation.MediaScreen
import com.dnfapps.arrmatey.navigation.Navigator
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
import com.dnfapps.arrmatey.navigation.toPreview
import com.dnfapps.arrmatey.navigation.toSeriesRelease
import com.dnfapps.arrmatey.ui.screens.ArrSearchScreen
import com.dnfapps.arrmatey.ui.screens.AudiobookFilesScreen
import com.dnfapps.arrmatey.ui.screens.AuthorFilesScreen
import com.dnfapps.arrmatey.ui.screens.BookDetailsScreen
import com.dnfapps.arrmatey.ui.screens.EpisodeDetailsScreen
import com.dnfapps.arrmatey.ui.screens.InteractiveSearchScreen
import com.dnfapps.arrmatey.ui.screens.MediaPreviewScreen
import com.dnfapps.arrmatey.ui.screens.MovieFilesScreen
import com.dnfapps.arrmatey.ui.screens.SeerrPersonDetailsScreen
import com.dnfapps.arrmatey.ui.screens.UnifiedMediaDetailsScreen

fun EntryProviderScope<NavKey>.mediaNavEntries(
    navigation: Navigator<*>,
    isExpanded: Boolean,
    defaultInstanceType: InstanceType? = null
) {
    entry<MediaScreen.Details> { details ->
        val resolvedInstanceType = details.type ?: details.requestType?.associatedInstanceType ?: defaultInstanceType

        UnifiedMediaDetailsScreen(
            arrId = details.id,
            tmdbId = details.tmdbId,
            tvdbId = details.tvdbId,
            requestType = details.requestType,
            instanceType = resolvedInstanceType,
            isExpanded = isExpanded,
            onBack = { navigation.popBackStack() },
            onNavigateToEpisodeDetails = { series, episode -> navigation.toEpisodeDetails(series, episode) },
            onNavigateToSeriesRelease = { seriesId, seasonNumber ->
                navigation.toSeriesRelease(
                    seriesId,
                    seasonNumber
                )
            },
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
    entry<MediaScreen.PersonDetails> { details ->
        SeerrPersonDetailsScreen(
            personId = details.personId,
            onBack = { navigation.popBackStack() },
            onMediaClick = { tmdbId, type ->
                navigation.toDetails(tmdbId = tmdbId, requestType = type)
            }
        )
    }
    entry<MediaScreen.MovieReleases> { params ->
        val releaseParams = ReleaseParams.Movie(params.movieId)
        InteractiveSearchScreen(
            instanceType = InstanceType.Radarr,
            releaseParams = releaseParams,
            onBack = { navigation.popBackStack() }
        )
    }
    entry<MediaScreen.SeriesRelease> { params ->
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
    entry<MediaScreen.AlbumRelease> { params ->
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
    entry<MediaScreen.BookRelease> { params ->
        val releaseParams = ReleaseParams.Book(
            mediaId = params.bookId
        )
        InteractiveSearchScreen(
            instanceType = InstanceType.Booksehelf,
            releaseParams = releaseParams,
            onBack = { navigation.popBackStack() }
        )
    }
    entry<MediaScreen.MovieFiles> { params ->
        MovieFilesScreen(
            movie = params.movie,
            onBack = { navigation.popBackStack() }
        )
    }
    entry<MediaScreen.AuthorFiles> { params ->
        AuthorFilesScreen(
            author = params.author,
            onBack = { navigation.popBackStack() }
        )
    }
    entry<MediaScreen.EpisodeDetails> { params ->
        EpisodeDetailsScreen(
            series = params.series,
            episode = params.episode,
            onBack = { navigation.popBackStack() },
            onNavigateToSeriesRelease = { episodeId ->
                navigation.toSeriesRelease(episodeId = episodeId)
            }
        )
    }
    entry<MediaScreen.BookDetails> { params ->
        BookDetailsScreen(
            book = params.book,
            author = params.author,
            onBack = { navigation.popBackStack() },
            onNavigateToBookRelease = { bookId ->
                navigation.toBookRelease(bookId = bookId)
            }
        )
    }
    entry<MediaScreen.AudiobookFiles> { params ->
        AudiobookFilesScreen(
            audiobook = params.audiobook,
            onBack = { navigation.popBackStack() }
        )
    }
    entry<MediaScreen.AudiobookRelease> { params ->
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
    entry<MediaScreen.Search> { search ->
        if (defaultInstanceType != null) {
            ArrSearchScreen(
                initialQuery = search.query,
                type = defaultInstanceType,
                onBack = { navigation.popBackStack() },
                onNavigateToDetails = { navigation.toDetails(it) },
                onNavigateToUnifiedDetails = { arrId, tmdbId, tvdbId, instanceType ->
                    navigation.toDetails(arrId, tmdbId, tvdbId, type = instanceType)
                },
                onNavigateToPreview = { navigation.toPreview(it) }
            )
        }
    }
    entry<MediaScreen.Preview<ArrMedia>> { preview ->
        if (defaultInstanceType != null) {
            MediaPreviewScreen(
                item = preview.item,
                type = defaultInstanceType,
                isExpanded = isExpanded,
                onBack = { navigation.popBackStack() },
                onItemAdded = { navigation.toDetails(it) }
            )
        }
    }
}
