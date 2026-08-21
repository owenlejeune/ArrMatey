package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.dnfapps.arrmatey.client.paging.PagedData
import com.dnfapps.arrmatey.compose.utils.ReleaseFilterBy
import com.dnfapps.arrmatey.entensions.isExpanded
import com.dnfapps.arrmatey.arr.api.model.ReleaseParams
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.DiscoverScreen
import com.dnfapps.arrmatey.navigation.NavigationManager
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
import com.dnfapps.arrmatey.navigation.toSeriesRelease
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.viewmodel.TrendingViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ArrAppBarWithSearch
import com.dnfapps.arrmatey.ui.components.DiscoverSection
import com.dnfapps.arrmatey.ui.components.PosterItem
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.components.navigation.forwardSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.popSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.predictivePopSlideTransform
import com.dnfapps.arrmatey.ui.screens.AudiobookFilesScreen
import com.dnfapps.arrmatey.ui.screens.AuthorFilesScreen
import com.dnfapps.arrmatey.ui.screens.BookDetailsScreen
import com.dnfapps.arrmatey.ui.screens.EpisodeDetailsScreen
import com.dnfapps.arrmatey.ui.screens.InteractiveSearchScreen
import com.dnfapps.arrmatey.ui.screens.MovieFilesScreen
import com.dnfapps.arrmatey.ui.screens.SeerrPersonDetailsScreen
import com.dnfapps.arrmatey.ui.screens.UnifiedMediaDetailsScreen
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DiscoverTab(
    windowSizeClass: WindowSizeClass,
    wideRailIsVisible: Boolean,
    viewModel: TrendingViewModel = koinInject(),
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigator<DiscoverScreen> = navigationManager.discover
) {
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    NavDisplay(
        backStack = navigation.backStack,
        onBack = { navigation.popBackStack() },
        transitionSpec = { forwardSlideTransform() },
        popTransitionSpec = { popSlideTransform() },
        predictivePopTransitionSpec = { _ -> predictivePopSlideTransform() },
        entryProvider = entryProvider {
            entry<DiscoverScreen.Home> {
                DiscoverHomeScreen(
                    viewModel = viewModel,
                    wideRailIsVisible = wideRailIsVisible,
                    onItemClick = { result ->
                        navigation.toDetails(result.id, result.mediaType)
                    }
                )
            }
            entry<DiscoverScreen.Details> { details ->
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
            entry<DiscoverScreen.MovieReleases> { params ->
                val releaseParams = ReleaseParams.Movie(params.movieId)
                InteractiveSearchScreen(
                    instanceType = InstanceType.Radarr,
                    releaseParams = releaseParams,
                    onBack = { navigation.popBackStack() }
                )
            }
            entry<DiscoverScreen.SeriesRelease> { params ->
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
            entry<DiscoverScreen.AlbumRelease> { params ->
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
            entry<DiscoverScreen.BookRelease> { params ->
                val releaseParams = ReleaseParams.Book(
                    mediaId = params.bookId
                )
                InteractiveSearchScreen(
                    instanceType = InstanceType.Booksehelf,
                    releaseParams = releaseParams,
                    onBack = { navigation.popBackStack() }
                )
            }
            entry<DiscoverScreen.MovieFiles> { params ->
                MovieFilesScreen(
                    movie = params.movie,
                    onBack = { navigation.popBackStack() }
                )
            }
            entry<DiscoverScreen.AuthorFiles> { params ->
                AuthorFilesScreen(
                    author = params.author,
                    onBack = { navigation.popBackStack() }
                )
            }
            entry<DiscoverScreen.EpisodeDetails> { params ->
                EpisodeDetailsScreen(
                    series = params.series,
                    episode = params.episode,
                    onBack = { navigation.popBackStack() },
                    onNavigateToSeriesRelease = { episodeId ->
                        navigation.toSeriesRelease(episodeId = episodeId)
                    }
                )
            }
            entry<DiscoverScreen.BookDetails> { params ->
                BookDetailsScreen(
                    book = params.book,
                    author = params.author,
                    onBack = { navigation.popBackStack() },
                    onNavigateToBookRelease = { bookId ->
                        navigation.toBookRelease(bookId = bookId)
                    }
                )
            }
            entry<DiscoverScreen.AudiobookFiles> { params ->
                AudiobookFilesScreen(
                    audiobook = params.audiobook,
                    onBack = { navigation.popBackStack() }
                )
            }
            entry<DiscoverScreen.AudiobookRelease> { params ->
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
            entry<DiscoverScreen.PersonDetails> { details ->
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiscoverHomeScreen(
    viewModel: TrendingViewModel,
    wideRailIsVisible: Boolean,
    onItemClick: (DiscoverResult) -> Unit
) {
    val trendingState by viewModel.trendingState.collectAsStateWithLifecycle()
    val moviesState by viewModel.moviesState.collectAsStateWithLifecycle()
    val tvState by viewModel.tvState.collectAsStateWithLifecycle()
    val upcomingMoviesState by viewModel.upcomingMoviesState.collectAsStateWithLifecycle()
    val upcomingTvState by viewModel.upcomingTvState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()

    val textFieldState = rememberTextFieldState(searchQuery)
    val searchBarState = rememberSearchBarState()

    LaunchedEffect(textFieldState.text) {
        viewModel.updateSearchQuery(textFieldState.text.toString())
    }

    Scaffold(
        topBar = {
            ArrAppBarWithSearch(
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                searchPlaceholder = mokoString(MR.strings.discover),
                navigationIcon = {
                    if (!wideRailIsVisible) {
                        NavigationDrawerButton()
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()) {
            if (searchBarState.isExpanded()) {
                DiscoverSearchOverlay(
                    data = searchState,
                    onItemClick = onItemClick,
                    onLoadMore = { viewModel.loadNextSearchPage() }
                )
            } else {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        DiscoverSection(
                            title = MR.strings.trending,
                            icon = Icons.Default.TrendingUp,
                            data = trendingState,
                            onItemClick = onItemClick,
                            onLoadMore = { viewModel.loadNextTrendingPage() }
                        )

                        DiscoverSection(
                            title = MR.strings.popular_movies,
                            icon = Icons.Default.Movie,
                            data = moviesState,
                            onItemClick = onItemClick,
                            onLoadMore = { viewModel.loadNextMoviesPage() }
                        )

                        DiscoverSection(
                            title = MR.strings.upcoming_movies,
                            icon = Icons.Default.Event,
                            data = upcomingMoviesState,
                            onItemClick = onItemClick,
                            onLoadMore = { viewModel.loadNextUpcomingMoviesPage() }
                        )

                        DiscoverSection(
                            title = MR.strings.popular_series,
                            icon = Icons.Default.Tv,
                            data = tvState,
                            onItemClick = onItemClick,
                            onLoadMore = { viewModel.loadNextTvPage() }
                        )

                        DiscoverSection(
                            title = MR.strings.upcoming_series,
                            icon = Icons.Default.Event,
                            data = upcomingTvState,
                            onItemClick = onItemClick,
                            onLoadMore = { viewModel.loadNextUpcomingTvPage() }
                        )

                        Spacer(modifier = Modifier.height(0.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoverSearchOverlay(
    data: PagedData<DiscoverResult>,
    onItemClick: (DiscoverResult) -> Unit,
    onLoadMore: () -> Unit
) {
    val lazyGridState = rememberLazyGridState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItemsCount = lazyGridState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = lazyGridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItemIndex >= totalItemsCount - 5 && totalItemsCount > 0
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    if (data.isLoading && data.items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (data.items.isNotEmpty()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 120.dp),
            state = lazyGridState,
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = data.items,
                key = { "${it.mediaType.name}_${it.id}" }
            ) { item ->
                PosterItem(
                    item = item,
                    onItemClick = { onItemClick(item) }
                )
            }

            if (data.isLoadingMore) {
                item {
                    Box(Modifier
                        .fillMaxWidth()
                        .padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    } else if (data.error != null) {
        Box(Modifier
            .fillMaxSize()
            .padding(16.dp), contentAlignment = Alignment.Center) {
            Text(text = data.error ?: "", color = MaterialTheme.colorScheme.error)
        }
    }
}
