package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.state.ActivityTabSegment
import com.dnfapps.arrmatey.arr.viewmodel.ActivityQueueViewModel
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.menu.ActivityFilterMenu
import com.dnfapps.arrmatey.ui.sheets.ConfirmDeleteItemSheet
import com.dnfapps.arrmatey.ui.sheets.QueueItemInfoSheet
import com.dnfapps.arrmatey.ui.tabs.activity.ActiveQueueTabContent
import com.dnfapps.arrmatey.ui.tabs.activity.DownloadedTabContent
import com.dnfapps.arrmatey.ui.tabs.activity.HistoryTabContent
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActivityTab(
    wideRailIsVisible: Boolean,
    onBack: (() -> Unit)? = null,
    viewModel: ActivityQueueViewModel = koinViewModel(),
    preferences: PreferencesStore = koinInject(),
) {
    val queueItems by viewModel.queueItems.collectAsStateWithLifecycle()
    val historyItems by viewModel.historyItems.collectAsStateWithLifecycle()
    val downloadedItems by viewModel.downloadedItems.collectAsStateWithLifecycle()
    val instances by viewModel.instances.collectAsStateWithLifecycle()
    val uiState by viewModel.activityQueueUiState.collectAsStateWithLifecycle()
    val removeItemStatus by viewModel.removeItemState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isPolling.collectAsStateWithLifecycle()
    val hasLoaded by viewModel.hasLoaded.collectAsStateWithLifecycle()
    val isHistoryLoading by viewModel.isHistoryLoading.collectAsStateWithLifecycle()
    val hasHistoryLoaded by viewModel.hasHistoryLoaded.collectAsStateWithLifecycle()
    val isDownloadedLoading by viewModel.isDownloadedLoading.collectAsStateWithLifecycle()
    val hasDownloadedLoaded by viewModel.hasDownloadedLoaded.collectAsStateWithLifecycle()
    val useColoredCards by preferences.useColoredActivityCards.collectAsStateWithLifecycle(false)

    val historyListState = rememberLazyListState()
    val downloadedListState = rememberLazyListState()

    LaunchedEffect(uiState.historyStateFilter, uiState.historyInstanceId) {
        historyListState.scrollToItem(0)
    }

    LaunchedEffect(uiState.downloadedInstanceId) {
        downloadedListState.scrollToItem(0)
    }

    var showConfirmRemove by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<QueueItem?>(null) }

    LaunchedEffect(removeItemStatus) {
        if (removeItemStatus is OperationStatus.Success) {
            selectedItem = null
            showConfirmRemove = false
            viewModel.refresh()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(mokoString(MR.strings.activity))
                },
                actions = {
                    ActivityFilterMenu(
                        instances = instances,
                        selectedTab = uiState.selectedTab,
                        selectedInstanceId = uiState.instanceId,
                        onInstanceChange = { viewModel.setInstanceId(it) },
                        sortBy = uiState.sortBy,
                        onSortByChanged = { viewModel.setSortBy(it) },
                        sortOrder = uiState.sortOrder,
                        onSortOrderChanged = { viewModel.setSortOrder(it) },
                        selectedHistoryInstanceId = uiState.historyInstanceId,
                        onHistoryInstanceChange = { viewModel.setHistoryInstanceId(it) },
                        historyStateFilter = uiState.historyStateFilter,
                        onHistoryStateFilterChanged = { viewModel.setHistoryStateFilter(it) },
                        selectedDownloadedInstanceId = uiState.downloadedInstanceId,
                        onDownloadedInstanceChange = { viewModel.setDownloadedInstanceId(it) },
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        BackButton(onClick = onBack)
                    } else if (!wideRailIsVisible) {
                        NavigationDrawerButton()
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { paddingValues ->
        Column(
            modifier =
            Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            PrimaryScrollableTabRow(
                selectedTabIndex = ActivityTabSegment.entries.indexOf(uiState.selectedTab),
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                ActivityTabSegment.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.setSelectedTab(tab) },
                        text = {
                            Text(
                                text = mokoString(tab.resource),
                                fontWeight = if (uiState.selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false,
                            )
                        },
                    )
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                when (uiState.selectedTab) {
                    ActivityTabSegment.Activity -> {
                        ActiveQueueTabContent(
                            queueItems = queueItems,
                            instances = instances,
                            isLoading = isLoading,
                            hasLoaded = hasLoaded,
                            useColoredCards = useColoredCards,
                            onRefresh = { viewModel.refresh() },
                            onSelectItem = { selectedItem = it },
                        )
                    }

                    ActivityTabSegment.History -> {
                        HistoryTabContent(
                            historyItems = historyItems,
                            instances = instances,
                            isHistoryLoading = isHistoryLoading,
                            hasHistoryLoaded = hasHistoryLoaded,
                            listState = historyListState,
                            onRefresh = { viewModel.refresh() },
                        )
                    }

                    ActivityTabSegment.Downloaded -> {
                        DownloadedTabContent(
                            downloadedItems = downloadedItems,
                            instances = instances,
                            isDownloadedLoading = isDownloadedLoading,
                            hasDownloadedLoaded = hasDownloadedLoaded,
                            listState = downloadedListState,
                            onRefresh = { viewModel.refresh() },
                        )
                    }
                }

                selectedItem?.let { item ->
                    QueueItemInfoSheet(
                        item = item,
                        onDismiss = { selectedItem = null },
                        onRemove = { showConfirmRemove = true },
                    )
                }

                if (showConfirmRemove && selectedItem != null) {
                    ConfirmDeleteItemSheet(
                        onDismiss = { showConfirmRemove = false },
                        deleteInProgress = removeItemStatus is OperationStatus.InProgress,
                        onDelete = { clientRemove, blocklist, skipRedownload ->
                            viewModel.removeQueueItem(selectedItem!!, clientRemove, blocklist, skipRedownload)
                        },
                    )
                }
            }
        }
    }
}
