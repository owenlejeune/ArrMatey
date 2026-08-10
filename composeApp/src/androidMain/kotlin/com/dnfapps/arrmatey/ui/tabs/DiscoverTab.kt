package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.dnfapps.arrmatey.client.paging.PagingState
import com.dnfapps.arrmatey.navigation.DiscoverScreen
import com.dnfapps.arrmatey.navigation.LocalDiscoverNavigator
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.toDetails
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.viewmodel.TrendingViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.PosterItem
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.components.navigation.forwardSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.popSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.predictivePopSlideTransform
import com.dnfapps.arrmatey.ui.screens.SeerrDetailsScreen
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
                    SeerrDetailsScreen(details.tmdbId, details.requestType, onBack = { navigation.popBackStack() })
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
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mokoString(MR.strings.discover)) },
                navigationIcon = {
                    if (!wideRailIsVisible) {
                        NavigationDrawerButton()
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                TrendingSection(
                    state = trendingState,
                    onItemClick = onItemClick,
                    onLoadMore = { viewModel.loadNextPage() }
                )
            }
        }
    }
}

@Composable
private fun TrendingSection(
    state: PagingState<DiscoverResult>,
    onItemClick: (DiscoverResult) -> Unit,
    onLoadMore: () -> Unit
) {
    val lazyListState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItemsCount = lazyListState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItemIndex >= totalItemsCount - 5 && totalItemsCount > 0
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.TrendingUp, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = mokoString(MR.strings.trending),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        val items = when (state) {
            is PagingState.Success -> state.items
            is PagingState.LoadingMore -> state.items
            else -> emptyList()
        }

        if (state is PagingState.Loading) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (items.isNotEmpty()) {
            LazyRow(
                state = lazyListState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(items) { item ->
                    PosterItem(
                        item = item,
                        modifier = Modifier.width(120.dp),
                        onItemClick = { onItemClick(item) }
                    )
                }

                if (state is PagingState.LoadingMore) {
                    item {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    }
                }
            }
        } else if (state is PagingState.Error) {
            Text(
                text = state.message,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
