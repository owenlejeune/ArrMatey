package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSubtitleLanguage
import com.dnfapps.arrmatey.bazarr.api.model.ProviderStatus
import com.dnfapps.arrmatey.bazarr.api.model.WantedEpisode
import com.dnfapps.arrmatey.bazarr.api.model.WantedMovie
import com.dnfapps.arrmatey.bazarr.state.BazarrMediaTarget
import com.dnfapps.arrmatey.bazarr.state.BazarrSection
import com.dnfapps.arrmatey.bazarr.state.ProvidersUiState
import com.dnfapps.arrmatey.bazarr.viewmodel.BazarrViewModel
import com.dnfapps.arrmatey.client.paging.PagedData
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.NoInstanceView
import com.dnfapps.arrmatey.ui.components.bazarr.BazarrSubtitleSearchSheet
import com.dnfapps.arrmatey.ui.components.bazarr.SubtitleLanguageChip
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.arr.viewmodel.InstancesViewModel
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BazarrScreen(
    viewModel: BazarrViewModel,
    wideRailIsVisible: Boolean,
    instancesViewModel: InstancesViewModel = koinInjectParams(InstanceType.Bazarr)
) {
    val instancesState by instancesViewModel.instancesState.collectAsStateWithLifecycle()
    val section by viewModel.selectedSection.collectAsStateWithLifecycle()
    val episodes by viewModel.wantedEpisodesState.collectAsStateWithLifecycle()
    val movies by viewModel.wantedMoviesState.collectAsStateWithLifecycle()
    val providers by viewModel.providersState.collectAsStateWithLifecycle()

    var searchTarget by remember { mutableStateOf<BazarrMediaTarget?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(mokoString(MR.strings.bazarr)) },
                navigationIcon = {
                    if (!wideRailIsVisible) {
                        NavigationDrawerButton()
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        if (instancesState.selectedInstance == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                NoInstanceView(InstanceType.Bazarr)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                SecondaryTabRow(selectedTabIndex = section.ordinal) {
                    Tab(
                        selected = section == BazarrSection.Episodes,
                        onClick = { viewModel.selectSection(BazarrSection.Episodes) },
                        text = { Text(sectionLabel(mokoString(MR.strings.bazarr_wanted_episodes), episodes.totalItemCount)) }
                    )
                    Tab(
                        selected = section == BazarrSection.Movies,
                        onClick = { viewModel.selectSection(BazarrSection.Movies) },
                        text = { Text(sectionLabel(mokoString(MR.strings.bazarr_wanted_movies), movies.totalItemCount)) }
                    )
                    Tab(
                        selected = section == BazarrSection.Providers,
                        onClick = { viewModel.selectSection(BazarrSection.Providers) },
                        text = { Text(mokoString(MR.strings.bazarr_providers)) }
                    )
                }

                when (section) {
                    BazarrSection.Episodes -> WantedEpisodesContent(
                        pagedData = episodes,
                        onLoadMore = viewModel::loadMoreEpisodes,
                        onRefresh = viewModel::refresh,
                        onSearch = { searchTarget = BazarrMediaTarget.Episode(it.sonarrSeriesId, it.sonarrEpisodeId) }
                    )

                    BazarrSection.Movies -> WantedMoviesContent(
                        pagedData = movies,
                        onLoadMore = viewModel::loadMoreMovies,
                        onRefresh = viewModel::refresh,
                        onSearch = { searchTarget = BazarrMediaTarget.Movie(it.radarrId) }
                    )

                    BazarrSection.Providers -> ProvidersContent(
                        state = providers,
                        onRefresh = viewModel::loadProviders,
                        onReset = viewModel::resetProviders
                    )
                }
            }
        }
    }

    searchTarget?.let { target ->
        BazarrSubtitleSearchSheet(
            target = target,
            onDismiss = { searchTarget = null }
        )
    }
}

private fun sectionLabel(base: String, count: Int): String =
    if (count > 0) "$base ($count)" else base

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WantedEpisodesContent(
    pagedData: PagedData<WantedEpisode>,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    onSearch: (WantedEpisode) -> Unit
) {
    PagedListContainer(
        isLoading = pagedData.isLoading && pagedData.items.isEmpty(),
        isEmpty = pagedData.isEmpty,
        emptyMessage = mokoString(MR.strings.bazarr_no_wanted_episodes),
        isRefreshing = pagedData.isLoading && pagedData.items.isNotEmpty(),
        onRefresh = onRefresh
    ) {
        WantedList(
            items = pagedData.items,
            hasMore = pagedData.hasMore,
            isLoadingMore = pagedData.isLoadingMore,
            onLoadMore = onLoadMore,
            key = { it.sonarrEpisodeId },
            title = { it.seriesTitle },
            subtitle = { "${it.episodeNumber} · ${it.episodeTitle}" },
            missing = { it.missingSubtitles },
            onSearch = onSearch
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WantedMoviesContent(
    pagedData: PagedData<WantedMovie>,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    onSearch: (WantedMovie) -> Unit
) {
    PagedListContainer(
        isLoading = pagedData.isLoading && pagedData.items.isEmpty(),
        isEmpty = pagedData.isEmpty,
        emptyMessage = mokoString(MR.strings.bazarr_no_wanted_movies),
        isRefreshing = pagedData.isLoading && pagedData.items.isNotEmpty(),
        onRefresh = onRefresh
    ) {
        WantedList(
            items = pagedData.items,
            hasMore = pagedData.hasMore,
            isLoadingMore = pagedData.isLoadingMore,
            onLoadMore = onLoadMore,
            key = { it.radarrId },
            title = { it.title },
            subtitle = { null },
            missing = { it.missingSubtitles },
            onSearch = onSearch
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> WantedList(
    items: List<T>,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    key: (T) -> Any,
    title: (T) -> String,
    subtitle: (T) -> String?,
    missing: (T) -> List<BazarrSubtitleLanguage>,
    onSearch: (T) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState, items.size, hasMore) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisible ->
                if (hasMore && !isLoadingMore && lastVisible != null && lastVisible >= items.size - 3) {
                    onLoadMore()
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { key(it) }) { item ->
            Surface(
                tonalElevation = 2.dp,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title(item), fontWeight = FontWeight.SemiBold)
                        subtitle(item)?.let {
                            Text(it, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(6.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            missing(item).forEach { lang ->
                                SubtitleLanguageChip(lang)
                            }
                        }
                    }
                    OutlinedButton(onClick = { onSearch(item) }) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(mokoString(MR.strings.bazarr_search_subtitles))
                    }
                }
            }
        }

        if (isLoadingMore) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProvidersContent(
    state: ProvidersUiState,
    onRefresh: () -> Unit,
    onReset: () -> Unit
) {
    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onReset) {
                    Text(mokoString(MR.strings.bazarr_reset_providers))
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.providers, key = { it.name }) { provider ->
                    ProviderRow(provider)
                }
            }
        }
    }
}

@Composable
private fun ProviderRow(provider: ProviderStatus) {
    val status = provider.status
    val retry = provider.retry
    val healthy = status.isNullOrBlank() || status.equals("good", ignoreCase = true)
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (healthy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                shape = CircleShape,
                modifier = Modifier.size(10.dp)
            ) {}
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(provider.name, fontWeight = FontWeight.SemiBold)
                if (!status.isNullOrBlank()) {
                    Text(status, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (!retry.isNullOrBlank() && !retry.equals("now", ignoreCase = true)) {
                Text(retry, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PagedListContainer(
    isLoading: Boolean,
    isEmpty: Boolean,
    emptyMessage: String,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    content: @Composable () -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.size(56.dp))
            isEmpty -> Text(emptyMessage, style = MaterialTheme.typography.bodyLarge)
            else -> content()
        }
    }
}
