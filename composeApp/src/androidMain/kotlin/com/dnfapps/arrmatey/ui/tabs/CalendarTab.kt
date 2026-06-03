package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.dnfapps.arrmatey.arr.state.CalendarViewMode
import com.dnfapps.arrmatey.arr.viewmodel.CalendarViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.calendar.CalendarListView
import com.dnfapps.arrmatey.ui.calendar.CalendarMonthView
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.menu.CalendarFilterMenu
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarTab(
    windowSizeClass: WindowSizeClass,
    wideRailIsVisible: Boolean,
    viewModel: CalendarViewModel = koinInject()
) {
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
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
                        instances = instances,
                        filterState = calendarState.filterState,
                        onInstanceChanged = { viewModel.setFilterInstanceId(it) },
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
                            onLoadMore = { viewModel.loadMore() }
                        )
                    }
                    VerticalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        CalendarListView(
                            state = calendarState,
                            onLoadMore = { viewModel.loadMore() }
                        )
                    }
                }
            } else {
                when (calendarState.filterState.viewMode) {
                    CalendarViewMode.List -> {
                        CalendarListView(
                            state = calendarState,
                            onLoadMore = { viewModel.loadMore() }
                        )
                    }

                    CalendarViewMode.Month -> {
                        CalendarMonthView(
                            state = calendarState,
                            onLoadMore = { viewModel.loadMore() }
                        )
                    }
                }
            }
        }
    }
}