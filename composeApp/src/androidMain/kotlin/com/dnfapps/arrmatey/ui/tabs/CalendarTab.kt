package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.EpisodeGroup
import com.dnfapps.arrmatey.arr.state.CalendarViewMode
import com.dnfapps.arrmatey.arr.viewmodel.CalendarViewModel
import com.dnfapps.arrmatey.navigation.CalendarScreen
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.toBookDetails
import com.dnfapps.arrmatey.navigation.toDetails
import com.dnfapps.arrmatey.navigation.toEpisodeDetails
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.calendar.CalendarListView
import com.dnfapps.arrmatey.ui.calendar.CalendarMonthView
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.components.navigation.forwardSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.mediaNavEntries
import com.dnfapps.arrmatey.ui.components.navigation.popSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.predictivePopSlideTransform
import com.dnfapps.arrmatey.ui.menu.CalendarFilterMenu
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarTab(
    windowSizeClass: WindowSizeClass,
    wideRailIsVisible: Boolean,
    viewModel: CalendarViewModel = koinInject(),
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigator<NavKey> = navigationManager.calendar,
) {
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    NavDisplay(
        backStack = navigation.backStack,
        onBack = { navigation.popBackStack() },
        transitionSpec = { forwardSlideTransform() },
        popTransitionSpec = { popSlideTransform() },
        predictivePopTransitionSpec = { _ -> predictivePopSlideTransform() },
        entryProvider = entryProvider {
            entry<CalendarScreen.Home> {
                CalendarHomeScreen(
                    viewModel = viewModel,
                    wideRailIsVisible = wideRailIsVisible,
                    isExpanded = isExpanded,
                    onItemClick = { item, instanceId ->
                        when (item) {
                            is ArrMovie -> navigation.toDetails(
                                id = item.id,
                                tmdbId = item.tmdbId,
                                type = item.associatedType,
                                instanceId = instanceId
                            )

                            is EpisodeGroup -> navigation.toDetails(
                                id = item.first.seriesId,
                                type = item.associatedType,
                                instanceId = instanceId
                            )

                            is Episode -> {
                                item.series?.let { series ->
                                    navigation.toDetails(
                                        id = series.id,
                                        tmdbId = series.tmdbId,
                                        type = item.associatedType,
                                        instanceId = instanceId
                                    )
                                    navigation.toEpisodeDetails(series, item)
                                }
                            }

                            is ArrAlbum -> navigation.toDetails(
                                id = item.id,
                                type = item.associatedType,
                                instanceId = instanceId
                            )

                            is Book -> {
                                item.author?.let { author ->
                                    navigation.toDetails(
                                        id = author.id,
                                        type = item.associatedType,
                                        instanceId = instanceId
                                    )
                                    navigation.toBookDetails(author, item)
                                }
                            }

                            is Audiobook -> navigation.toDetails(
                                id = item.id,
                                type = item.associatedType,
                                instanceId = instanceId
                            )
                        }
                    }
                )
            }
            mediaNavEntries(navigation = navigation, isExpanded = isExpanded)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarHomeScreen(
    viewModel: CalendarViewModel,
    wideRailIsVisible: Boolean,
    isExpanded: Boolean,
    onItemClick: (CalendarItem, Long?) -> Unit
) {
    val calendarState by viewModel.calendarState.collectAsStateWithLifecycle()
    val instances by viewModel.instances.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(mokoString(MR.strings.schedule)) },
                navigationIcon = {
                    if (!wideRailIsVisible) {
                        NavigationDrawerButton()
                    }
                },
                actions = {
                    if (!isExpanded) {
                        IconButton(onClick = {
                            viewModel.toggleViewMode()
                        }) {
                            Icon(
                                imageVector = when (calendarState.filterState.viewMode) {
                                    CalendarViewMode.List -> Icons.Default.CalendarMonth
                                    CalendarViewMode.Month -> Icons.Default.CalendarViewDay
                                },
                                contentDescription = null
                            )
                        }
                    }

                    CalendarFilterMenu(
                        filterState = calendarState.filterState,
                        onContentFilterChanged = { viewModel.setContentFilter(it) },
                        onToggleFilterMonitored = { viewModel.toggleShowMonitoredOnly() },
                        onToggleFilterPremiersOnly = { viewModel.toggleShowPremiersOnly() },
                        onToggleFilterFinalesOnly = { viewModel.toggleShowFinalesOnly() },
                    )
                }
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            isRefreshing = calendarState.isLoading,
            onRefresh = { viewModel.load() }
        ) {
            if (isExpanded) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        CalendarMonthView(
                            state = calendarState,
                            instances = instances,
                            onItemClick = onItemClick,
                            onLoadMore = { viewModel.loadMore() }
                        )
                    }
                    VerticalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        CalendarListView(
                            state = calendarState,
                            instances = instances,
                            onItemClick = onItemClick,
                            onLoadMore = { viewModel.loadMore() }
                        )
                    }
                }
            } else {
                when (calendarState.filterState.viewMode) {
                    CalendarViewMode.List -> {
                        CalendarListView(
                            state = calendarState,
                            instances = instances,
                            onItemClick = onItemClick,
                            onLoadMore = { viewModel.loadMore() }
                        )
                    }

                    CalendarViewMode.Month -> {
                        CalendarMonthView(
                            state = calendarState,
                            instances = instances,
                            onItemClick = onItemClick,
                            onLoadMore = { viewModel.loadMore() }
                        )
                    }
                }
            }
        }
    }
}
