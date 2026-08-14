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
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.dnfapps.arrmatey.client.paging.PagedData
import com.dnfapps.arrmatey.entensions.isExpanded
import com.dnfapps.arrmatey.navigation.DiscoverScreen
import com.dnfapps.arrmatey.navigation.LocalDiscoverNavigator
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.toDetails
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.viewmodel.TrendingViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ArrAppBarWithSearch
import com.dnfapps.arrmatey.ui.components.DiscoverSection
import com.dnfapps.arrmatey.ui.components.PosterItem
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.components.navigation.forwardSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.popSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.predictivePopSlideTransform
import com.dnfapps.arrmatey.ui.screens.SeerrDetailsScreen
import com.dnfapps.arrmatey.ui.screens.SeerrPersonDetailsScreen
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
    CompositionLocalProvider(LocalDiscoverNavigator provides navigation) {
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
                    if (details.requestType == RequestType.Person) {
                        SeerrPersonDetailsScreen(details.tmdbId, onBack = { navigation.popBackStack() })
                    } else {
                        SeerrDetailsScreen(details.tmdbId, details.requestType, onBack = { navigation.popBackStack() })
                    }
                }
            }
        )
    }
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
