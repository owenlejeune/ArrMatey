package com.dnfapps.arrmatey.ui.components.unifiedmedia.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.MockMedia
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.bazarr.state.BazarrMediaTarget
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.ui.components.AlbumsArea
import com.dnfapps.arrmatey.ui.components.AudiobookFileView
import com.dnfapps.arrmatey.ui.components.BooksArea
import com.dnfapps.arrmatey.ui.components.MediaActivitySection
import com.dnfapps.arrmatey.ui.components.MovieFileView
import com.dnfapps.arrmatey.ui.components.SeasonsArea
import com.dnfapps.arrmatey.ui.components.bazarr.BazarrSubtitlesSection

@Composable
fun SeasonsFilesTabContent(
    state: UnifiedMediaDetailsUiState.Success,
    automaticSearchIds: Set<Long>,
    deleteSeasonStatus: OperationStatus,
    deleteAlbumStatus: OperationStatus,
    onQueueItemClicked: (QueueItem) -> Unit,
    onToggleSeasonMonitor: (Int) -> Unit,
    onToggleEpisodeMonitor: (Episode) -> Unit,
    onEpisodeAutomaticSearch: (Long) -> Unit,
    onSeasonAutomaticSearch: (Int) -> Unit,
    onDeleteSeasonFiles: (Int) -> Unit,
    onNavigateToEpisodeDetails: (ArrSeries, Episode) -> Unit,
    onDeleteEpisodeFile: (Long) -> Unit,
    onNavigateToSeriesRelease: (seriesId: Long?, seasonNumber: Int?, episodeId: Long?) -> Unit,
    onPerformAutomaticLookup: () -> Unit,
    onDeleteMovieFile: () -> Unit,
    onNavigateToMovieFiles: (ArrMovie) -> Unit,
    onNavigateToMovieReleases: (Long) -> Unit,
    onToggleAlbumMonitor: (ArrAlbum) -> Unit,
    onEditAlbum: (ArrAlbum) -> Unit,
    onAlbumAutomaticSearch: (Long) -> Unit,
    onDeleteAlbumFiles: (Long) -> Unit,
    onNavigateToAlbumRelease: (Long, Long) -> Unit,
    onToggleBookMonitor: (Book) -> Unit,
    onToggleBookSeriesMonitor: (List<Book>) -> Unit,
    onBookAutomaticSearch: (Long) -> Unit,
    onNavigateToAuthorFiles: (Author) -> Unit,
    onNavigateToBookDetails: (Author, Book) -> Unit,
    onNavigateToBookRelease: (Long) -> Unit,
    onNavigateToAudiobookFiles: (Audiobook) -> Unit,
    onNavigateToAudiobookRelease: (Long?, String?) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        AnimatedVisibility(
            visible = state.queueItems.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            MediaActivitySection(
                queueItems = state.queueItems,
                onQueueItemClicked = onQueueItemClicked,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }

        if (state.seasons.isNotEmpty()) {
            val arrSeries = state.arrMedia as? ArrSeries
            SeasonsArea(
                seasons = state.seasons,
                seriesId = arrSeries?.id,
                modifier = Modifier.padding(horizontal = 24.dp),
                searchIds = automaticSearchIds,
                onToggleSeasonMonitor = onToggleSeasonMonitor,
                onToggleEpisodeMonitor = onToggleEpisodeMonitor,
                onEpisodeAutomaticSearch = onEpisodeAutomaticSearch,
                onSeasonAutomaticSearch = onSeasonAutomaticSearch,
                deleteSeasonFiles = onDeleteSeasonFiles,
                seasonDeleteInProgress = deleteSeasonStatus is OperationStatus.InProgress,
                onNavigateToEpisodeDetails = { episode ->
                    arrSeries?.let { series -> onNavigateToEpisodeDetails(series, episode) }
                },
                deleteEpisodeFile = onDeleteEpisodeFile,
                onNavigateToSeriesRelease = onNavigateToSeriesRelease,
                bazarrDetailsIntegration = state.bazarrDetailsIntegration,
            )
        }

        AnimatedVisibility(
            visible = state.hasArrId && state.arrMedia !is ArrSeries,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            when (val item = state.arrMedia) {
                is ArrMovie -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        MovieFileView(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            movie = item,
                            movieExtraFiles = state.extraFiles,
                            searchIds = automaticSearchIds,
                            onAutomaticSearch = onPerformAutomaticLookup,
                            onDeleteFile = onDeleteMovieFile,
                            onNavigateToMovieFiles = onNavigateToMovieFiles,
                            onNavigateToMovieReleases = onNavigateToMovieReleases,
                        )
                        item.id?.let { movieId ->
                            if (state.bazarrDetailsIntegration) {
                                BazarrSubtitlesSection(
                                    target = BazarrMediaTarget.Movie(movieId),
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                )
                            }
                        }
                    }
                }

                is Arrtist ->
                    AlbumsArea(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        artist = item,
                        albums = state.albums,
                        tracks = state.tracks,
                        trackFiles = state.trackFiles,
                        searchIds = automaticSearchIds,
                        onToggleAlbumMonitor = onToggleAlbumMonitor,
                        onEditAlbum = onEditAlbum,
                        onAlbumAutomaticSearch = onAlbumAutomaticSearch,
                        deleteAlbumFiles = onDeleteAlbumFiles,
                        albumDeleteInProgress = deleteAlbumStatus is OperationStatus.InProgress,
                        onNavigateToAlbumRelease = onNavigateToAlbumRelease,
                    )

                is Author ->
                    BooksArea(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        author = item,
                        series = state.bookSeries,
                        files = state.bookFiles,
                        books = state.books,
                        searchIds = automaticSearchIds,
                        onToggleMonitor = onToggleBookMonitor,
                        onToggleSeriesMonitor = onToggleBookSeriesMonitor,
                        onAutomaticSearch = onBookAutomaticSearch,
                        onNavigateToAuthorFiles = onNavigateToAuthorFiles,
                        onNavigateToBookDetails = onNavigateToBookDetails,
                        onNavigateToBookRelease = onNavigateToBookRelease,
                    )

                is Audiobook ->
                    AudiobookFileView(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        audiobook = item,
                        searchIds = automaticSearchIds,
                        onAutomaticSearch = { item.id?.let { onBookAutomaticSearch(it) } },
                        onNavigateToAudiobookFiles = onNavigateToAudiobookFiles,
                        onNavigateToAudiobookRelease = onNavigateToAudiobookRelease,
                    )

                is ArrSeries, is SearchAudiobook, is MockMedia, null -> {}
            }
        }
    }
}
