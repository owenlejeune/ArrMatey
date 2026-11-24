package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.newest.NewestDownloadableItem
import com.dnfapps.arrmatey.newest.NewestDownloadableState
import com.dnfapps.arrmatey.newest.NewestDownloadableViewModel
import com.dnfapps.arrmatey.newest.NewestFilter
import com.dnfapps.arrmatey.newest.ReleaseKind
import com.dnfapps.arrmatey.seerr.api.model.Genre
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.NewestDownloadableListItem
import com.dnfapps.arrmatey.ui.components.PlainReleaseCard
import com.dnfapps.arrmatey.ui.components.PosterItem
import com.dnfapps.arrmatey.ui.components.ViewTypeMenuButton
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.ui.helpers.LocalFloatingBarBottomPadding
import com.dnfapps.arrmatey.ui.helpers.MinColumnsAdaptiveGridCells
import com.dnfapps.arrmatey.ui.sheets.DiscoverCategoryFilterSheet
import com.dnfapps.arrmatey.ui.theme.ViewType
import com.dnfapps.arrmatey.utils.mokoString

/**
 * The See More screen behind a Discover "Movies from Prowlarr" or "TV from Prowlarr" row,
 * laid out like [DiscoverCategoryScreen]: a poster grid or a list, switched from the top bar.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NewestDownloadableGridScreen(
    kind: ReleaseKind,
    viewModel: NewestDownloadableViewModel,
    onBack: () -> Unit,
    onItemClick: (NewestDownloadableItem) -> Unit,
    genres: List<Genre> = emptyList(),
) {
    val state by viewModel.filteredStateFor(kind).collectAsStateWithLifecycle()
    val rawState by (if (kind == ReleaseKind.Movie) viewModel.moviesState else viewModel.tvState).collectAsStateWithLifecycle()
    val filterState by viewModel.filterFor(kind).collectAsStateWithLifecycle()
    val hasSeerr by viewModel.hasSeerr.collectAsStateWithLifecycle()
    var showFilterSheet by remember { mutableStateOf(false) }
    var viewType by rememberSaveable { mutableStateOf(ViewType.Grid) }
    val title = if (kind == ReleaseKind.Movie) MR.strings.movies_from_prowlarr else MR.strings.tv_from_prowlarr
    val bottomPadding = 16.dp + LocalFloatingBarBottomPadding.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = mokoString(title)) },
                navigationIcon = { BackButton(onClick = onBack) },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = mokoString(MR.strings.filter_and_sort),
                            tint = if (filterState != NewestFilter.DEFAULT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
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
            contentAlignment = Alignment.Center,
        ) {
            when (val current = state) {
                NewestDownloadableState.Initial, NewestDownloadableState.Loading -> {
                    LoadingIndicator(modifier = Modifier.size(96.dp))
                }
                is NewestDownloadableState.Error -> {
                    Text(text = current.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                }
                is NewestDownloadableState.Success -> {
                    if (current.items.isEmpty()) {
                        Text(
                            text = mokoString(MR.strings.newest_downloadable_empty),
                            modifier = Modifier.padding(16.dp),
                        )
                    } else if (viewType == ViewType.List) {
                        LazyColumn(
                            contentPadding = PaddingValues(top = 8.dp, bottom = bottomPadding),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(items = current.items, key = { it.key }) { item ->
                                NewestDownloadableListItem(
                                    item = item,
                                    onClick = { onItemClick(item) },
                                    showOverview = true,
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = MinColumnsAdaptiveGridCells(minSize = 120.dp, minColumns = 3),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = bottomPadding),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(
                                items = current.items,
                                key = { it.key },
                                contentType = { if (it.match != null) "poster" else "plain" },
                            ) { item ->
                                val match = item.match
                                if (match != null) {
                                    PosterItem(
                                        item = match,
                                        onItemClick = { onItemClick(item) },
                                    )
                                } else {
                                    PlainReleaseCard(
                                        item = item,
                                        modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        val rawItems = (rawState as? NewestDownloadableState.Success)?.items.orEmpty()
        DiscoverCategoryFilterSheet(
            filterState = filterState,
            // Seerr's genre list when available, otherwise the genres of the matched titles.
            rawItems = rawItems.mapNotNull { it.match },
            genres = if (hasSeerr) genres else emptyList(),
            sortOptions = NewestFilter.sortOptions(hasSeerr),
            onFilterStateChanged = { viewModel.updateFilter(kind, it) },
            onResetFilterState = { viewModel.resetFilter(kind) },
            onDismissRequest = { showFilterSheet = false },
        )
    }
}
