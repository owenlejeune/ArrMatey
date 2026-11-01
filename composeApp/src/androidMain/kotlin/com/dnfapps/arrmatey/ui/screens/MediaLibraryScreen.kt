package com.dnfapps.arrmatey.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem
import com.dnfapps.arrmatey.mediaserver.viewmodel.MediaServerLibraryViewModel
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ArrAppBarWithSearch
import com.dnfapps.arrmatey.ui.components.mediaserver.MediaServerEmptyConfigView
import com.dnfapps.arrmatey.ui.components.mediaserver.MediaServerFilterSortMenu
import com.dnfapps.arrmatey.ui.components.mediaserver.MediaServerMediaView
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.sheets.ArrViewCustomizationSheet
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediaLibraryScreen(
    isExpanded: Boolean = false,
    wideRailIsVisible: Boolean = false,
    onNavigateToDetails: (UnifiedMediaItem) -> Unit,
    viewModel: MediaServerLibraryViewModel = koinViewModel(),
) {
    val configuredServers by viewModel.configuredServers.collectAsStateWithLifecycle()
    val selectedServer by viewModel.selectedServer.collectAsStateWithLifecycle()
    val libraries by viewModel.libraries.collectAsStateWithLifecycle()
    val selectedLibrary by viewModel.selectedLibrary.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val filteredItems by viewModel.filteredItems.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val watchedFilter by viewModel.watchedFilter.collectAsStateWithLifecycle()

    var showViewCustomizationSheet by remember { mutableStateOf(false) }

    val textFieldState = rememberTextFieldState()

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }
            .collect { query ->
                viewModel.setSearchQuery(query)
            }
    }

    if (configuredServers.isEmpty()) {
        Scaffold(
            topBar = {
                ArrAppBarWithSearch(
                    textFieldState = textFieldState,
                    textFieldEnabled = false,
                    searchPlaceholder = mokoString(MR.strings.media_library),
                    navigationIcon = {
                        if (!wideRailIsVisible) {
                            NavigationDrawerButton()
                        }
                    },
                )
            },
            contentWindowInsets = WindowInsets.statusBars,
        ) { paddingValues ->
            MediaServerEmptyConfigView(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
            )
        }
        return
    }

    val currentServer = selectedServer ?: configuredServers.firstOrNull() ?: return

    Scaffold(
        topBar = {
            ArrAppBarWithSearch(
                textFieldState = textFieldState,
                textFieldEnabled = true,
                searchPlaceholder = mokoString(MR.strings.search_placeholder, currentServer.label),
                navigationIcon = {
                    if (!wideRailIsVisible) {
                        NavigationDrawerButton()
                    }
                },
                actions = {
                    MediaServerFilterSortMenu(
                        sortBy = preferences.sortBy,
                        onSortByChanged = { viewModel.updateSortBy(it) },
                        sortOrder = preferences.sortOrder,
                        onSortOrderChanged = { viewModel.updateSortOrder(it) },
                        watchedFilter = watchedFilter,
                        onWatchedFilterChanged = { viewModel.setWatchedFilter(it) },
                        onOpenViewCustomization = { showViewCustomizationSheet = true },
                    )
                },
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            // Media Server Switcher Tabs (if more than 1 server configured)
            AnimatedVisibility(
                visible = configuredServers.size > 1,
                enter = expandVertically(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)),
                exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200)),
            ) {
                val selectedIndex = configuredServers.indexOfFirst { it.id == currentServer.id }.coerceAtLeast(0)
                SecondaryScrollableTabRow(
                    selectedTabIndex = selectedIndex,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    divider = {},
                ) {
                    configuredServers.forEach { server ->
                        Tab(
                            selected = server.id == currentServer.id,
                            onClick = {
                                textFieldState.setTextAndPlaceCursorAtEnd("")
                                viewModel.selectServer(server)
                            },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(server.serverType.tabIcon),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = server.serverType.brandColor,
                                    )
                                    Text(
                                        text = server.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            },
                        )
                    }
                }
            }

            // Library Section Chips Row
            if (libraries.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    item {
                        FilterChip(
                            selected = selectedLibrary == null,
                            onClick = { viewModel.selectLibrary(null) },
                            label = { Text(mokoString(MR.strings.all)) },
                        )
                    }
                    items(libraries, key = { it.id }) { lib ->
                        FilterChip(
                            selected = selectedLibrary?.id == lib.id,
                            onClick = { viewModel.selectLibrary(lib) },
                            label = { Text(lib.title) },
                        )
                    }
                }
            }

            // Content Area with PullToRefresh
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                MediaServerMediaView(
                    items = filteredItems,
                    preferences = preferences,
                    onItemClick = onNavigateToDetails,
                    onToggleWatched = { viewModel.toggleWatched(it) },
                )
            }
        }

        if (showViewCustomizationSheet) {
            ArrViewCustomizationSheet(
                onDismissRequest = { showViewCustomizationSheet = false },
                type = InstanceType.Radarr,
                preferences = preferences,
                onViewTypeChanged = { viewModel.updateViewType(it) },
                onShowFullDetailsChanged = { viewModel.updateShowFullDetails(it) },
                onShowOverlayChanged = { viewModel.updateShowOverlay(it) },
                onShowBannerBackgroundChanged = { viewModel.updateShowBannerBackground(it) },
                onIncludeOverviewChanged = { viewModel.updateIncludeOverview(it) },
                onBannerBlurChanged = { viewModel.updateBannerBlur(it) },
                onGridDensityChanged = { viewModel.updateGridDensity(it) },
                onGridSpacingChanged = { viewModel.updateGridSpacing(it) },
                onPosterElevationChanged = { viewModel.updatePosterElevation(it) },
                onPosterRadiusChanged = { viewModel.updatePosterRadius(it) },
                onApplyGloballyChanged = { viewModel.updateApplyGlobally(it) },
            )
        }
    }
}
