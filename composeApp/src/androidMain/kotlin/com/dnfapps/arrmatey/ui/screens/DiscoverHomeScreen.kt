package com.dnfapps.arrmatey.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.discover.model.DiscoverCategory
import com.dnfapps.arrmatey.discover.model.SearchResult
import com.dnfapps.arrmatey.discover.viewmodel.DiscoverViewModel
import com.dnfapps.arrmatey.entensions.isExpanded
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ArrAppBarWithSearch
import com.dnfapps.arrmatey.ui.components.DiscoverSection
import com.dnfapps.arrmatey.ui.components.NoInstanceView
import com.dnfapps.arrmatey.ui.components.appbar.FloatingBarAction
import com.dnfapps.arrmatey.ui.components.appbar.ProvideFloatingBarAction
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.helpers.LocalFloatingBarBottomPadding
import com.dnfapps.arrmatey.ui.sheets.DiscoverSectionCustomizationSheet
import com.dnfapps.arrmatey.ui.tabs.DiscoverSearchOverlay
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DiscoverHomeScreen(
    viewModel: DiscoverViewModel,
    wideRailIsVisible: Boolean,
    onSeeMore: (DiscoverCategory) -> Unit,
    onItemClick: (SearchResult) -> Unit,
) {
    val globalPreferencesStore: PreferencesStore = koinInject()
    val useFloatingNavigationBar by globalPreferencesStore.useFloatingNavigationBar.collectAsStateWithLifecycle(false)

    val selectedInstance by viewModel.selectedInstance.collectAsStateWithLifecycle()
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
    val isInitialLoading by viewModel.isInitialLoading.collectAsStateWithLifecycle()
    val sectionPreferences by viewModel.discoverSectionPreferences.collectAsStateWithLifecycle()

    var showMenu by remember { mutableStateOf(false) }
    var showCustomizationSheet by remember { mutableStateOf(false) }

    val textFieldState = rememberTextFieldState(searchQuery)
    val searchBarState = rememberSearchBarState()
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(textFieldState.text) {
        viewModel.updateSearchQuery(textFieldState.text.toString())
    }

    val showFab = !wideRailIsVisible && selectedInstance != null && !searchBarState.isExpanded()

    ProvideFloatingBarAction(
        visible = useFloatingNavigationBar && showFab,
        action =
            FloatingBarAction(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = mokoString(MR.strings.search),
                    )
                },
                onClick = {
                    scope.launch {
                        focusRequester.requestFocus()
                        searchBarState.animateToExpanded()
                        focusRequester.requestFocus()
                    }
                },
            ),
    )

    Scaffold(
        topBar = {
            ArrAppBarWithSearch(
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                inputFieldModifier = Modifier.focusRequester(focusRequester),
                searchPlaceholder = mokoString(MR.strings.discover),
                navigationIcon = {
                    if (!wideRailIsVisible) {
                        NavigationDrawerButton()
                    }
                },
                actions = {
                    if (selectedInstance != null) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = null,
                                )
                            }
                            DropdownMenuPopup(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                            ) {
                                DropdownMenuGroup(
                                    shapes = MenuDefaults.groupShape(0, 1),
                                    interactionSource = remember { MutableInteractionSource() },
                                    containerColor = MenuDefaults.groupStandardContainerColor,
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(mokoString(MR.strings.reorganize_hide_sections)) },
                                        shapes = MenuDefaults.itemShape(0, 1),
                                        colors = MenuDefaults.itemColors(),
                                        onClick = {
                                            showMenu = false
                                            showCustomizationSheet = true
                                        },
                                        selected = false,
                                    )
                                }
                            }
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (!useFloatingNavigationBar) {
                AnimatedVisibility(
                    visible = showFab,
                    enter = scaleIn(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)),
                    exit = scaleOut(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200)),
                ) {
                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                focusRequester.requestFocus()
                                searchBarState.animateToExpanded()
                                focusRequester.requestFocus()
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = mokoString(MR.strings.search),
                        )
                    }
                }
            }
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
            if (selectedInstance == null) {
                NoInstanceView(InstanceType.Seerr)
            } else if (searchBarState.isExpanded()) {
                DiscoverSearchOverlay(
                    items = searchState,
                    isLoading = isSearching,
                    onItemClick = onItemClick,
                    showBanners = searchShowBanners,
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
                        sectionPreferences.visibleCategories.forEach { category ->
                            when (category) {
                                DiscoverCategory.TRENDING -> {
                                    DiscoverSection(
                                        title = MR.strings.trending,
                                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                                        data = trendingState,
                                        onItemClick = { onItemClick(SearchResult.SeerrMediaResult(it)) },
                                        onLoadMore = { viewModel.loadNextTrendingPage() },
                                        onSeeMore = { onSeeMore(DiscoverCategory.TRENDING) },
                                    )
                                }
                                DiscoverCategory.POPULAR_MOVIES -> {
                                    DiscoverSection(
                                        title = MR.strings.popular_movies,
                                        icon = Icons.Default.Movie,
                                        data = moviesState,
                                        onItemClick = { onItemClick(SearchResult.SeerrMediaResult(it)) },
                                        onLoadMore = { viewModel.loadNextMoviesPage() },
                                        onSeeMore = { onSeeMore(DiscoverCategory.POPULAR_MOVIES) },
                                    )
                                }
                                DiscoverCategory.POPULAR_SERIES -> {
                                    DiscoverSection(
                                        title = MR.strings.popular_series,
                                        icon = Icons.Default.Tv,
                                        data = tvState,
                                        onItemClick = { onItemClick(SearchResult.SeerrMediaResult(it)) },
                                        onLoadMore = { viewModel.loadNextTvPage() },
                                        onSeeMore = { onSeeMore(DiscoverCategory.POPULAR_SERIES) },
                                    )
                                }
                                DiscoverCategory.UPCOMING_MOVIES -> {
                                    DiscoverSection(
                                        title = MR.strings.upcoming_movies,
                                        icon = Icons.Default.Event,
                                        data = upcomingMoviesState,
                                        onItemClick = { onItemClick(SearchResult.SeerrMediaResult(it)) },
                                        onLoadMore = { viewModel.loadNextUpcomingMoviesPage() },
                                        onSeeMore = { onSeeMore(DiscoverCategory.UPCOMING_MOVIES) },
                                    )
                                }
                                DiscoverCategory.UPCOMING_SERIES -> {
                                    DiscoverSection(
                                        title = MR.strings.upcoming_series,
                                        icon = Icons.Default.Event,
                                        data = upcomingTvState,
                                        onItemClick = { onItemClick(SearchResult.SeerrMediaResult(it)) },
                                        onLoadMore = { viewModel.loadNextUpcomingTvPage() },
                                        onSeeMore = { onSeeMore(DiscoverCategory.UPCOMING_SERIES) },
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(LocalFloatingBarBottomPadding.current))
                    }
                }
            }
        }
    }

    if (showCustomizationSheet) {
        DiscoverSectionCustomizationSheet(
            preferences = sectionPreferences,
            onUpdatePreferences = { viewModel.updateDiscoverSectionPreferences(it) },
            onResetPreferences = { viewModel.resetDiscoverSectionPreferences() },
            onDismissRequest = { showCustomizationSheet = false },
        )
    }
}
