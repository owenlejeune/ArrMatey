package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ReleaseParams
import com.dnfapps.arrmatey.compose.utils.ReleaseFilterBy
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.ArrScreen
import com.dnfapps.arrmatey.navigation.LocalArrNavigator
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.ui.screens.ArrLibraryScreen
import com.dnfapps.arrmatey.ui.screens.ArrSearchScreen
import com.dnfapps.arrmatey.ui.screens.AudiobookFilesScreen
import com.dnfapps.arrmatey.ui.screens.AuthorFilesScreen
import com.dnfapps.arrmatey.ui.screens.BookDetailsScreen
import com.dnfapps.arrmatey.ui.screens.EpisodeDetailsScreen
import com.dnfapps.arrmatey.ui.screens.InteractiveSearchScreen
import com.dnfapps.arrmatey.ui.screens.MediaDetailsScreen
import com.dnfapps.arrmatey.ui.screens.MediaPreviewScreen
import com.dnfapps.arrmatey.ui.screens.MovieFilesScreen
import org.koin.compose.koinInject

@Composable
fun ArrTab(
    type: InstanceType,
    windowSizeClass: WindowSizeClass,
    wideRailIsVisible: Boolean,
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigator<ArrScreen> = navigationManager.arr(type)
) {
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    val baseIndex = navigation.backStack.indexOfLast { it is ArrScreen.Library || it is ArrScreen.Search }.coerceAtLeast(0)
    val baseScreen = navigation.backStack[baseIndex]

    val detailBackStack = navigation.backStack.filterIndexed { index, _ -> index > baseIndex }
    val showDetails = isExpanded && detailBackStack.isNotEmpty()

    val detailsWeight by animateFloatAsState(
        targetValue = if (showDetails) 1f else 0.001f,
        label = "DetailsWeight"
    )

    CompositionLocalProvider(LocalArrNavigator provides navigation) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                NavDisplay(
                    backStack = if (showDetails) listOf(baseScreen) else navigation.backStack,
                    onBack = { navigation.popBackStack() },
                    entryProvider = arrEntryProvider(type, isExpanded, wideRailIsVisible)
                )
            }

            val lastValidDetailBackStack = remember { mutableStateOf<List<ArrScreen>>(emptyList()) }
            if (detailBackStack.isNotEmpty()) {
                lastValidDetailBackStack.value = detailBackStack
            }

            AnimatedVisibility(
                visible = showDetails,
                enter = slideInHorizontally { it },
                exit = slideOutHorizontally { it },
                modifier = Modifier.weight(detailsWeight)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (lastValidDetailBackStack.value.isNotEmpty()) {
                        NavDisplay(
                            backStack = lastValidDetailBackStack.value,
                            onBack = { navigation.popBackStack() },
                            entryProvider = arrEntryProvider(type, isExpanded, wideRailIsVisible)
                        )
                    }
                }
            }
        }
    }
}

private fun arrEntryProvider(type: InstanceType, isExpanded: Boolean, wideRailIsVisible: Boolean) = entryProvider {
    entry<ArrScreen.Library> {
        ArrLibraryScreen(type, isExpanded, wideRailIsVisible)
    }
    entry<ArrScreen.Details> { details ->
        MediaDetailsScreen(details.id, type, isExpanded)
    }
    entry<ArrScreen.Search> { search ->
        ArrSearchScreen(search.query, type)
    }
    entry<ArrScreen.Preview<ArrMedia>> { preview ->
        MediaPreviewScreen(preview.item, type, isExpanded = isExpanded)
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
            mediaId = params.albumId
        )
        InteractiveSearchScreen(type, releaseParams)
    }
    entry<ArrScreen.BookRelease> { params ->
        val releaseParams = ReleaseParams.Book(
            mediaId = params.bookId
        )
        InteractiveSearchScreen(type, releaseParams)
    }
    entry<ArrScreen.MovieFiles> { params ->
        MovieFilesScreen(movie = params.movie)
    }
    entry<ArrScreen.AuthorFiles> { params ->
        AuthorFilesScreen(author = params.author)
    }
    entry<ArrScreen.EpisodeDetails> { params ->
        EpisodeDetailsScreen(params.series, params.episode)
    }
    entry<ArrScreen.BookDetails> { params ->
        BookDetailsScreen(params.book, params.author)
    }
    entry<ArrScreen.AudiobookFiles> { params ->
        AudiobookFilesScreen(audiobook = params.audiobook)
    }
    entry<ArrScreen.AudiobookRelease> { params ->
        val releaseParams = ReleaseParams.Audiobook(
            mediaId = params.audiobookId,
            query = params.query
        )
        InteractiveSearchScreen(type, releaseParams)
    }
}
