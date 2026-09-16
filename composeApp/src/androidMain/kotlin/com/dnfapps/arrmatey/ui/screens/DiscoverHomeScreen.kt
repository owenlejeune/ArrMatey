package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.discover.model.DiscoverCategory
import com.dnfapps.arrmatey.discover.model.SearchResult
import com.dnfapps.arrmatey.discover.viewmodel.DiscoverViewModel
import com.dnfapps.arrmatey.entensions.isExpanded
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ArrAppBarWithSearch
import com.dnfapps.arrmatey.ui.components.DiscoverSection
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.tabs.DiscoverSearchOverlay
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DiscoverHomeScreen(
    viewModel: DiscoverViewModel,
    wideRailIsVisible: Boolean,
    onSeeMore: (DiscoverCategory) -> Unit,
    onItemClick: (SearchResult) -> Unit,
) {
    val trendingState by viewModel.trendingState.collectAsStateWithLifecycle()
    val moviesState by viewModel.moviesState.collectAsStateWithLifecycle()
    val tvState by viewModel.tvState.collectAsStateWithLifecycle()
    val upcomingMoviesState by viewModel.upcomingMoviesState.collectAsStateWithLifecycle()
    val upcomingTvState by viewModel.upcomingTvState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val searchShowBanners by viewModel.searchShowBanners.collectAsStateWithLifecycle()
    val searchShowInstanceIndicatorShadow by viewModel.searchShowInstanceIndicatorShadow.collectAsStateWithLifecycle()
    val isInitialLoading by viewModel.isInitialLoading.collectAsStateWithLifecycle()

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
                },
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
        ) {
            if (searchBarState.isExpanded()) {
                DiscoverSearchOverlay(
                    items = searchState,
                    isLoading = isSearching,
                    onItemClick = onItemClick,
                    showBanners = searchShowBanners,
                    showInstanceIndicatorShadow = searchShowInstanceIndicatorShadow,
                )
            } else if (isInitialLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator(modifier = Modifier.size(96.dp))
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        DiscoverSection(
                            title = MR.strings.trending,
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            data = trendingState,
                            onItemClick = { onItemClick(SearchResult.SeerrMediaResult(it)) },
                            onLoadMore = { viewModel.loadNextTrendingPage() },
                            onSeeMore = { onSeeMore(DiscoverCategory.TRENDING) },
                        )

                        DiscoverSection(
                            title = MR.strings.popular_movies,
                            icon = Icons.Default.Movie,
                            data = moviesState,
                            onItemClick = { onItemClick(SearchResult.SeerrMediaResult(it)) },
                            onLoadMore = { viewModel.loadNextMoviesPage() },
                            onSeeMore = { onSeeMore(DiscoverCategory.POPULAR_MOVIES) },
                        )

                        DiscoverSection(
                            title = MR.strings.upcoming_movies,
                            icon = Icons.Default.Event,
                            data = upcomingMoviesState,
                            onItemClick = { onItemClick(SearchResult.SeerrMediaResult(it)) },
                            onLoadMore = { viewModel.loadNextUpcomingMoviesPage() },
                            onSeeMore = { onSeeMore(DiscoverCategory.UPCOMING_MOVIES) },
                        )

                        DiscoverSection(
                            title = MR.strings.popular_series,
                            icon = Icons.Default.Tv,
                            data = tvState,
                            onItemClick = { onItemClick(SearchResult.SeerrMediaResult(it)) },
                            onLoadMore = { viewModel.loadNextTvPage() },
                            onSeeMore = { onSeeMore(DiscoverCategory.POPULAR_SERIES) },
                        )

                        DiscoverSection(
                            title = MR.strings.upcoming_series,
                            icon = Icons.Default.Event,
                            data = upcomingTvState,
                            onItemClick = { onItemClick(SearchResult.SeerrMediaResult(it)) },
                            onLoadMore = { viewModel.loadNextUpcomingTvPage() },
                            onSeeMore = { onSeeMore(DiscoverCategory.UPCOMING_SERIES) },
                        )

                        Spacer(modifier = Modifier.height(0.dp))
                    }
                }
            }
        }
    }
}
