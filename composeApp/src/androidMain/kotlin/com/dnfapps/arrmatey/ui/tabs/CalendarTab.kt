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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.arr.state.CalendarViewMode
import com.dnfapps.arrmatey.arr.usecase.ResolvedMediaDestination
import com.dnfapps.arrmatey.arr.viewmodel.CalendarViewModel
import com.dnfapps.arrmatey.navigation.CalendarScreen
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.toResolvedDestination
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.calendar.CalendarListView
import com.dnfapps.arrmatey.ui.calendar.CalendarMonthView
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.components.navigation.forwardSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.mediaNavEntries
import com.dnfapps.arrmatey.ui.components.navigation.popSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.predictivePopSlideTransform
import com.dnfapps.arrmatey.ui.dialogs.SelectInstanceDialog
import com.dnfapps.arrmatey.ui.menu.CalendarFilterMenu
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CalendarTab(
    windowSizeClass: WindowSizeClass,
    wideRailIsVisible: Boolean,
    viewModel: CalendarViewModel = koinInject(),
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigator<NavKey> = navigationManager.calendar
) {
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    NavDisplay(
        backStack = navigation.backStack,
        onBack = { navigation.popBackStack() },
        transitionSpec = { forwardSlideTransform() },
        popTransitionSpec = { popSlideTransform() },
        predictivePopTransitionSpec = { _ -> predictivePopSlideTransform() },
        entryProvider = entryProvider {
            entry<CalendarScreen.Calendar> {
                CalendarContentScreen(
                    windowSizeClass = windowSizeClass,
                    wideRailIsVisible = wideRailIsVisible,
                    viewModel = viewModel,
                    navigation = navigation
                )
            }
            mediaNavEntries(navigation = navigation, isExpanded = isExpanded)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarContentScreen(
    windowSizeClass: WindowSizeClass,
    wideRailIsVisible: Boolean,
    viewModel: CalendarViewModel,
    navigation: Navigator<NavKey>
) {
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    val calendarState by viewModel.calendarState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var pendingDestinations by remember { mutableStateOf<List<ResolvedMediaDestination>?>(null) }

    fun navigateToDestination(destination: ResolvedMediaDestination) {
        scope.launch {
            viewModel.selectInstance(destination.instance)
            navigation.toResolvedDestination(destination)
        }
    }

    fun handleItemClick(item: CalendarItem) {
        scope.launch {
            val destinations = viewModel.resolveDestination(item)
            if (destinations.size > 1) {
                pendingDestinations = destinations
            } else if (destinations.size == 1) {
                navigateToDestination(destinations.first())
            }
        }
    }

    if (pendingDestinations != null) {
        SelectInstanceDialog(
            destinations = pendingDestinations ?: emptyList(),
            onSelect = { dest ->
                pendingDestinations = null
                navigateToDestination(dest)
            },
            onDismiss = {
                pendingDestinations = null
            }
        )
    }

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
                            onLoadMore = { viewModel.loadMore() },
                            onItemClick = { handleItemClick(it) }
                        )
                    }
                    VerticalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        CalendarListView(
                            state = calendarState,
                            onLoadMore = { viewModel.loadMore() },
                            onItemClick = { handleItemClick(it) }
                        )
                    }
                }
            } else {
                when (calendarState.filterState.viewMode) {
                    CalendarViewMode.List -> {
                        CalendarListView(
                            state = calendarState,
                            onLoadMore = { viewModel.loadMore() },
                            onItemClick = { handleItemClick(it) }
                        )
                    }

                    CalendarViewMode.Month -> {
                        CalendarMonthView(
                            state = calendarState,
                            onLoadMore = { viewModel.loadMore() },
                            onItemClick = { handleItemClick(it) }
                        )
                    }
                }
            }
        }
    }
}