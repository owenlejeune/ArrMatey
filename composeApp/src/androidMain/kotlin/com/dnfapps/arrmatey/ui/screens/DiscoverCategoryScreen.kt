package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.discover.model.DiscoverCategory
import com.dnfapps.arrmatey.discover.model.SearchResult
import com.dnfapps.arrmatey.discover.viewmodel.DiscoverViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.PosterItem
import com.dnfapps.arrmatey.ui.components.SearchResultItem
import com.dnfapps.arrmatey.ui.components.ViewTypeMenuButton
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.ui.helpers.LocalFloatingBarBottomPadding
import com.dnfapps.arrmatey.ui.helpers.MinColumnsAdaptiveGridCells
import com.dnfapps.arrmatey.ui.theme.ViewType
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DiscoverCategoryScreen(
    category: DiscoverCategory,
    viewModel: DiscoverViewModel,
    onBack: () -> Unit,
    onItemClick: (SearchResult) -> Unit,
) {
    val dataState by viewModel.getStateForCategory(category).collectAsStateWithLifecycle()
    val lazyGridState = rememberLazyGridState()
    val lazyListState = rememberLazyListState()
    var viewType by rememberSaveable { mutableStateOf(ViewType.Grid) }

    val shouldLoadMore by remember {
        derivedStateOf {
            if (viewType == ViewType.Grid) {
                val totalItemsCount = lazyGridState.layoutInfo.totalItemsCount
                val lastVisibleItemIndex =
                    lazyGridState.layoutInfo.visibleItemsInfo
                        .lastOrNull()
                        ?.index ?: 0
                lastVisibleItemIndex >= totalItemsCount - 5 && totalItemsCount > 0
            } else {
                val totalItemsCount = lazyListState.layoutInfo.totalItemsCount
                val lastVisibleItemIndex =
                    lazyListState.layoutInfo.visibleItemsInfo
                        .lastOrNull()
                        ?.index ?: 0
                lastVisibleItemIndex >= totalItemsCount - 5 && totalItemsCount > 0
            }
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadNextPageForCategory(category)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = mokoString(category.title)) },
                navigationIcon = {
                    BackButton(onClick = onBack)
                },
                actions = {
                    ViewTypeMenuButton(
                        viewType = viewType,
                        onViewTypeChanged = { viewType = it },
                    )
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
            val items = dataState.items
            if (dataState.isLoading && items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingIndicator(modifier = Modifier.size(96.dp))
                }
            } else if (items.isNotEmpty()) {
                if (viewType == ViewType.Grid) {
                    LazyVerticalGrid(
                        state = lazyGridState,
                        columns = MinColumnsAdaptiveGridCells(minSize = 120.dp, minColumns = 3),
                        contentPadding =
                            PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 16.dp + LocalFloatingBarBottomPadding.current,
                            ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(items) { item ->
                            PosterItem(
                                item = item,
                                onItemClick = { onItemClick(SearchResult.SeerrMediaResult(it)) },
                            )
                        }

                        if (dataState.isLoadingMore) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    LoadingIndicator()
                                }
                            }
                        } else if (dataState.loadMoreFailed) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    IconButton(
                                        onClick = { viewModel.loadNextPageForCategory(category) },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = mokoString(MR.strings.retry),
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = lazyListState,
                        contentPadding =
                            PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 16.dp + LocalFloatingBarBottomPadding.current,
                            ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(items) { item ->
                            SearchResultItem(
                                item = SearchResult.SeerrMediaResult(item),
                                onItemClick = onItemClick,
                            )
                        }

                        if (dataState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    LoadingIndicator()
                                }
                            }
                        } else if (dataState.loadMoreFailed) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    IconButton(
                                        onClick = { viewModel.loadNextPageForCategory(category) },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = mokoString(MR.strings.retry),
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
