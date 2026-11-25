package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.dnfapps.arrmatey.arr.state.ProwlarrTabSegment
import com.dnfapps.arrmatey.arr.viewmodel.ProwlarrIndexersViewModel
import com.dnfapps.arrmatey.arr.viewmodel.ProwlarrSearchViewModel
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.ProwlarrScreen
import com.dnfapps.arrmatey.navigation.toDetails
import com.dnfapps.arrmatey.newest.NewestDownloadableItem
import com.dnfapps.arrmatey.newest.NewestDownloadableViewModel
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.NewestDownloadableList
import com.dnfapps.arrmatey.ui.components.appbar.FullScreenSearchAppBar
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.components.navigation.TwoPaneMasterDetailNavDisplay
import com.dnfapps.arrmatey.ui.components.navigation.mediaNavEntries
import com.dnfapps.arrmatey.ui.menu.IndexersSortMenu
import com.dnfapps.arrmatey.ui.screens.ProwlarrIndexersContent
import com.dnfapps.arrmatey.ui.screens.ProwlarrSearchContent
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * The Prowlarr tab with its own back stack, so a title's details open inside the tab and
 * Back returns to the list it was opened from.
 */
@Composable
fun ProwlarrTabHost(
    windowSizeClass: WindowSizeClass,
    wideRailIsVisible: Boolean,
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigator<NavKey> = navigationManager.navigatorFor(TabItem.Standard.PROWLARR),
) {
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    TwoPaneMasterDetailNavDisplay(
        navigation = navigation,
        isExpanded = isExpanded,
        wideRailIsVisible = wideRailIsVisible,
        isMasterScreen = { it is ProwlarrScreen.Home },
        entryProvider =
        entryProvider {
            entry<ProwlarrScreen.Home> {
                ProwlarrTab(
                    wideRailIsVisible = wideRailIsVisible,
                    onOpenDetails = { tmdbId, type -> navigation.toDetails(tmdbId = tmdbId, requestType = type) },
                    onOpenIndexers = { navigation.navigateTo(ProwlarrScreen.Indexers) },
                )
            }
            entry<ProwlarrScreen.Indexers> {
                ProwlarrIndexersPage(onBack = { navigation.popBackStack() })
            }
            mediaNavEntries(
                navigation = navigation,
                isExpanded = isExpanded,
                wideRailIsVisible = wideRailIsVisible,
            )
        },
    )
}

/**
 * Prowlarr's search bar over the newest releases: All, Movies and TV. The indexers open
 * from the icon in the top bar. [onOpenDetails] opens a matched title's details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProwlarrTab(
    wideRailIsVisible: Boolean,
    onOpenDetails: (Long, RequestType) -> Unit,
    onOpenIndexers: () -> Unit,
    onBack: (() -> Unit)? = null,
    searchViewModel: ProwlarrSearchViewModel = koinViewModel(),
    newestViewModel: NewestDownloadableViewModel = koinViewModel(),
) {
    val textFieldState = rememberTextFieldState()

    LaunchedEffect(textFieldState.text) {
        searchViewModel.performSearch(textFieldState.text.toString())
    }

    val allState by newestViewModel.allState.collectAsStateWithLifecycle()
    val moviesState by newestViewModel.moviesState.collectAsStateWithLifecycle()
    val tvState by newestViewModel.tvState.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableStateOf(ProwlarrTabSegment.All) }

    val openDetails: (NewestDownloadableItem) -> Unit = { item ->
        item.match?.let { onOpenDetails(it.id, it.mediaType) }
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
                    IconButton(onClick = onOpenIndexers) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = mokoString(MR.strings.indexers),
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

            NewestDownloadableList(
                state = when (selectedTab) {
                    ProwlarrTabSegment.All -> allState
                    ProwlarrTabSegment.Movies -> moviesState
                    ProwlarrTabSegment.Tv -> tvState
                },
                onItemClick = openDetails,
                onRefresh = { newestViewModel.refresh() },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** Prowlarr's indexers, opened from the Prowlarr tab's top bar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProwlarrIndexersPage(
    onBack: () -> Unit,
    indexersViewModel: ProwlarrIndexersViewModel = koinViewModel(),
) {
    val sortingState by indexersViewModel.indexerSortState.collectAsStateWithLifecycle()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = mokoString(MR.strings.indexers)) },
                navigationIcon = { BackButton(onClick = onBack) },
                actions = {
                    IndexersSortMenu(
                        sortBy = sortingState.sortBy,
                        onSortByChanged = { indexersViewModel.updateSortBy(it) },
                        sortOrder = sortingState.sortOrder,
                        onSortOrderChanged = { indexersViewModel.updateSortOrder(it) },
                    )
                },
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { paddingValues ->
        ProwlarrIndexersContent(
            viewModel = indexersViewModel,
            modifier =
            Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        )
    }
}
