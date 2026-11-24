package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.state.ProwlarrTabSegment
import com.dnfapps.arrmatey.arr.viewmodel.ProwlarrIndexersViewModel
import com.dnfapps.arrmatey.arr.viewmodel.ProwlarrSearchViewModel
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.newest.NewestDownloadableItem
import com.dnfapps.arrmatey.newest.NewestDownloadableViewModel
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.NewestDownloadableList
import com.dnfapps.arrmatey.ui.components.appbar.FullScreenSearchAppBar
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.menu.IndexersSortMenu
import com.dnfapps.arrmatey.ui.screens.ProwlarrIndexersContent
import com.dnfapps.arrmatey.ui.screens.ProwlarrSearchContent
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProwlarrTab(
    wideRailIsVisible: Boolean,
    onBack: (() -> Unit)? = null,
    indexersViewModel: ProwlarrIndexersViewModel = koinViewModel(),
    searchViewModel: ProwlarrSearchViewModel = koinViewModel(),
    newestViewModel: NewestDownloadableViewModel = koinViewModel(),
    navigationManager: NavigationManager = koinInject(),
) {
    val textFieldState = rememberTextFieldState()

    LaunchedEffect(textFieldState.text) {
        searchViewModel.performSearch(textFieldState.text.toString())
    }

    val sortingState by indexersViewModel.indexerSortState.collectAsStateWithLifecycle()
    val moviesState by newestViewModel.moviesState.collectAsStateWithLifecycle()
    val tvState by newestViewModel.tvState.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableStateOf(ProwlarrTabSegment.Indexers) }

    val openDetails: (NewestDownloadableItem) -> Unit = { item ->
        item.match?.let { navigationManager.navigateToMediaDetails(tmdbId = it.id, requestType = it.mediaType) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            FullScreenSearchAppBar(
                textFieldState = textFieldState,
                searchPlaceholder = mokoString(MR.strings.prowlarr_search_hint),
                navigationIcon = {
                    if (onBack != null) {
                        BackButton(onClick = onBack)
                    } else if (!wideRailIsVisible) {
                        NavigationDrawerButton()
                    }
                },
                actions = {
                    if (selectedTab == ProwlarrTabSegment.Indexers) {
                        IndexersSortMenu(
                            sortBy = sortingState.sortBy,
                            onSortByChanged = { indexersViewModel.updateSortBy(it) },
                            sortOrder = sortingState.sortOrder,
                            onSortOrderChanged = { indexersViewModel.updateSortOrder(it) },
                        )
                    }
                },
            ) {
                ProwlarrSearchContent(
                    viewModel = searchViewModel,
                )
            }
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { paddingValues ->
        Column(
            modifier =
            Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            PrimaryTabRow(
                selectedTabIndex = ProwlarrTabSegment.entries.indexOf(selectedTab),
                modifier = Modifier.fillMaxWidth(),
            ) {
                ProwlarrTabSegment.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = mokoString(tab.resource),
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false,
                            )
                        },
                    )
                }
            }

            when (selectedTab) {
                ProwlarrTabSegment.Indexers -> {
                    ProwlarrIndexersContent(
                        viewModel = indexersViewModel,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                ProwlarrTabSegment.Movies -> {
                    NewestDownloadableList(
                        state = moviesState,
                        onItemClick = openDetails,
                        onRefresh = { newestViewModel.refresh() },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                ProwlarrTabSegment.Tv -> {
                    NewestDownloadableList(
                        state = tvState,
                        onItemClick = openDetails,
                        onRefresh = { newestViewModel.refresh() },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
