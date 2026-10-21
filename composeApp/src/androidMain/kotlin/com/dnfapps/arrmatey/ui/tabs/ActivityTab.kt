package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.WantedMediaItem
import com.dnfapps.arrmatey.arr.state.ActivityTabSegment
import com.dnfapps.arrmatey.arr.viewmodel.ActivityQueueViewModel
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.navigation.ActivityScreen
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.toAlbumRelease
import com.dnfapps.arrmatey.navigation.toAudiobookRelease
import com.dnfapps.arrmatey.navigation.toBookRelease
import com.dnfapps.arrmatey.navigation.toComicReleases
import com.dnfapps.arrmatey.navigation.toMovieReleases
import com.dnfapps.arrmatey.navigation.toSeriesRelease
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ArrAppBarWithSearch
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.components.navigation.forwardSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.mediaNavEntries
import com.dnfapps.arrmatey.ui.components.navigation.popSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.predictivePopSlideTransform
import com.dnfapps.arrmatey.ui.menu.ActivityFilterMenu
import com.dnfapps.arrmatey.ui.sheets.ActivityCardCustomizationSheet
import com.dnfapps.arrmatey.ui.sheets.ConfirmDeleteItemSheet
import com.dnfapps.arrmatey.ui.sheets.DirectoryManualImportSheet
import com.dnfapps.arrmatey.ui.sheets.QueueItemInfoSheet
import com.dnfapps.arrmatey.ui.tabs.activity.ActiveQueueTabContent
import com.dnfapps.arrmatey.ui.tabs.activity.BlocklistTabContent
import com.dnfapps.arrmatey.ui.tabs.activity.DownloadedTabContent
import com.dnfapps.arrmatey.ui.tabs.activity.HistoryTabContent
import com.dnfapps.arrmatey.ui.tabs.activity.WantedTabContent
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ActivityTab(
    wideRailIsVisible: Boolean,
    onBack: (() -> Unit)? = null,
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigator<NavKey> = navigationManager.activity,
) {
    val isExpanded = wideRailIsVisible

    NavDisplay(
        backStack = navigation.backStack,
        onBack = { navigation.popBackStack() },
        transitionSpec = { forwardSlideTransform() },
        popTransitionSpec = { popSlideTransform() },
        predictivePopTransitionSpec = { _ -> predictivePopSlideTransform() },
        entryProvider =
        entryProvider {
            entry<ActivityScreen.Home> {
                ActivityHomeScreen(
                    wideRailIsVisible = wideRailIsVisible,
                    onBack = onBack,
                    onNavigateToInteractiveSearch = { item ->
                        item.id?.let { id ->
                            when (item.instanceType) {
                                InstanceType.Radarr -> {
                                    navigation.toMovieReleases(
                                        movieId = id,
                                        instanceId = item.instanceId,
                                    )
                                }

                                InstanceType.Sonarr -> {
                                    navigation.toSeriesRelease(
                                        episodeId = id,
                                        instanceId = item.instanceId,
                                    )
                                }

                                InstanceType.Lidarr -> {
                                    navigation.toAlbumRelease(
                                        albumId = id,
                                        instanceId = item.instanceId,
                                    )
                                }

                                InstanceType.Bookshelf,
                                InstanceType.Chaptarr,
                                -> {
                                    navigation.toBookRelease(
                                        bookId = id,
                                        instanceId = item.instanceId,
                                    )
                                }

                                InstanceType.Listenarr -> {
                                    navigation.toAudiobookRelease(
                                        audiobookId = id,
                                        query = item.title ?: "",
                                        instanceId = item.instanceId,
                                    )
                                }

                                InstanceType.Kapowarr -> {
                                    navigation.toComicReleases(
                                        mediaId = id,
                                        isVolume = true,
                                        instanceId = item.instanceId,
                                    )
                                }
                                else -> {}
                            }
                        }
                    },
                )
            }
            mediaNavEntries(
                navigation = navigation,
                isExpanded = isExpanded,
                wideRailIsVisible = wideRailIsVisible,
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActivityHomeScreen(
    wideRailIsVisible: Boolean,
    onNavigateToInteractiveSearch: (WantedMediaItem) -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: ActivityQueueViewModel = koinViewModel(),
    preferences: PreferencesStore = koinInject(),
) {
    val queueItems by viewModel.queueItems.collectAsStateWithLifecycle()
    val historyItems by viewModel.historyItems.collectAsStateWithLifecycle()
    val downloadedItems by viewModel.downloadedItems.collectAsStateWithLifecycle()
    val blocklistItems by viewModel.blocklistItems.collectAsStateWithLifecycle()
    val instances by viewModel.instances.collectAsStateWithLifecycle()
    val uiState by viewModel.activityQueueUiState.collectAsStateWithLifecycle()
    val removeItemStatus by viewModel.removeItemState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isPolling.collectAsStateWithLifecycle()
    val hasLoaded by viewModel.hasLoaded.collectAsStateWithLifecycle()
    val isHistoryLoading by viewModel.isHistoryLoading.collectAsStateWithLifecycle()
    val hasHistoryLoaded by viewModel.hasHistoryLoaded.collectAsStateWithLifecycle()
    val isDownloadedLoading by viewModel.isDownloadedLoading.collectAsStateWithLifecycle()
    val hasDownloadedLoaded by viewModel.hasDownloadedLoaded.collectAsStateWithLifecycle()
    val isBlocklistLoading by viewModel.isBlocklistLoading.collectAsStateWithLifecycle()
    val hasBlocklistLoaded by viewModel.hasBlocklistLoaded.collectAsStateWithLifecycle()
    val missingItems by viewModel.missingItems.collectAsStateWithLifecycle()
    val cutoffUnmetItems by viewModel.cutoffUnmetItems.collectAsStateWithLifecycle()
    val isMissingLoading by viewModel.isMissingLoading.collectAsStateWithLifecycle()
    val hasMissingLoaded by viewModel.hasMissingLoaded.collectAsStateWithLifecycle()
    val isCutoffLoading by viewModel.isCutoffLoading.collectAsStateWithLifecycle()
    val hasCutoffLoaded by viewModel.hasCutoffLoaded.collectAsStateWithLifecycle()
    val useColoredCards by preferences.useColoredActivityCards.collectAsStateWithLifecycle(false)

    val historyListState = rememberLazyListState()
    val downloadedListState = rememberLazyListState()
    val blocklistListState = rememberLazyListState()
    val missingListState = rememberLazyListState()
    val cutoffListState = rememberLazyListState()

    LaunchedEffect(uiState.historyStateFilter, uiState.historyInstanceId) {
        historyListState.scrollToItem(0)
    }

    LaunchedEffect(uiState.downloadedInstanceId) {
        downloadedListState.scrollToItem(0)
    }

    LaunchedEffect(uiState.blocklistInstanceId) {
        blocklistListState.scrollToItem(0)
    }

    LaunchedEffect(uiState.missingInstanceId) {
        missingListState.scrollToItem(0)
    }

    LaunchedEffect(uiState.cutoffInstanceId) {
        cutoffListState.scrollToItem(0)
    }

    var showConfirmRemove by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<QueueItem?>(null) }
    var showActivityCustomizationSheet by remember { mutableStateOf(false) }
    var showManualImportSheet by remember { mutableStateOf(false) }

    val textFieldState = rememberTextFieldState()

    LaunchedEffect(textFieldState.text) {
        viewModel.setSearchQuery(textFieldState.text.toString())
    }

    LaunchedEffect(removeItemStatus) {
        if (removeItemStatus is OperationStatus.Success) {
            selectedItem = null
            showConfirmRemove = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            val searchPlaceholder =
                when (uiState.selectedTab) {
                    ActivityTabSegment.Activity -> mokoString(MR.strings.activity)
                    ActivityTabSegment.History -> mokoString(MR.strings.history)
                    ActivityTabSegment.Downloaded -> mokoString(MR.strings.recently_downloaded)
                    ActivityTabSegment.Blocklist -> mokoString(MR.strings.blocklist)
                    ActivityTabSegment.Missing -> mokoString(MR.strings.missing)
                    ActivityTabSegment.CutoffUnmet -> mokoString(MR.strings.cutoff_unmet)
                }

            ArrAppBarWithSearch(
                textFieldState = textFieldState,
                searchPlaceholder = searchPlaceholder,
                navigationIcon = {
                    if (onBack != null) {
                        BackButton(onClick = onBack)
                    } else if (!wideRailIsVisible) {
                        NavigationDrawerButton()
                    }
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
                        selectedBlocklistInstanceId = uiState.blocklistInstanceId,
                        onBlocklistInstanceChange = { viewModel.setBlocklistInstanceId(it) },
                        selectedMissingInstanceId = uiState.missingInstanceId,
                        onMissingInstanceChange = { viewModel.setMissingInstanceId(it) },
                        selectedCutoffInstanceId = uiState.cutoffInstanceId,
                        onCutoffInstanceChange = { viewModel.setCutoffInstanceId(it) },
                        onOpenViewCustomization = { showActivityCustomizationSheet = true },
                    )
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
                            onRefresh = { viewModel.refreshActivity() },
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
                            onRefresh = { viewModel.refreshHistory() },
                        )
                    }

                    ActivityTabSegment.Downloaded -> {
                        DownloadedTabContent(
                            downloadedItems = downloadedItems,
                            instances = instances,
                            isDownloadedLoading = isDownloadedLoading,
                            hasDownloadedLoaded = hasDownloadedLoaded,
                            listState = downloadedListState,
                            onRefresh = { viewModel.refreshDownloaded() },
                        )
                    }

                    ActivityTabSegment.Blocklist -> {
                        BlocklistTabContent(
                            blocklistItems = blocklistItems,
                            instances = instances,
                            isBlocklistLoading = isBlocklistLoading,
                            hasBlocklistLoaded = hasBlocklistLoaded,
                            listState = blocklistListState,
                            onRefresh = { viewModel.refreshBlocklist() },
                            onDeleteItem = { viewModel.deleteBlocklistItem(it) },
                        )
                    }

                    ActivityTabSegment.Missing -> {
                        val selectedInstance = instances.firstOrNull { it.id == uiState.missingInstanceId }
                        val isManualImportEnabled = selectedInstance?.type !in listOf(InstanceType.Kapowarr, InstanceType.Listenarr)
                        WantedTabContent(
                            items = missingItems,
                            instances = instances,
                            isLoading = isMissingLoading,
                            hasLoaded = hasMissingLoaded,
                            listState = missingListState,
                            onRefresh = { viewModel.refreshMissing() },
                            onSearchAll = { viewModel.searchAllMissing() },
                            onManualImport = { showManualImportSheet = true },
                            onAutomaticSearch = { viewModel.executeAutomaticSearch(it) },
                            onInteractiveSearch = onNavigateToInteractiveSearch,
                            isManualImportEnabled = isManualImportEnabled,
                        )
                    }

                    ActivityTabSegment.CutoffUnmet -> {
                        val selectedInstance = instances.firstOrNull { it.id == uiState.cutoffInstanceId }
                        val isManualImportEnabled = selectedInstance?.type !in listOf(InstanceType.Kapowarr, InstanceType.Listenarr)
                        WantedTabContent(
                            items = cutoffUnmetItems,
                            instances = instances,
                            isLoading = isCutoffLoading,
                            hasLoaded = hasCutoffLoaded,
                            listState = cutoffListState,
                            onRefresh = { viewModel.refreshCutoff() },
                            onSearchAll = { viewModel.searchAllMissing() },
                            onManualImport = { showManualImportSheet = true },
                            onAutomaticSearch = { viewModel.executeAutomaticSearch(it) },
                            onInteractiveSearch = onNavigateToInteractiveSearch,
                            isManualImportEnabled = isManualImportEnabled,
                        )
                    }
                }

                if (!showConfirmRemove) {
                    selectedItem?.let { item ->
                        QueueItemInfoSheet(
                            item = item,
                            onDismiss = { selectedItem = null },
                            onRemove = { showConfirmRemove = true },
                        )
                    }
                }

                if (showConfirmRemove && selectedItem != null) {
                    ConfirmDeleteItemSheet(
                        onDismiss = {
                            showConfirmRemove = false
                            selectedItem = null
                        },
                        deleteInProgress = removeItemStatus is OperationStatus.InProgress,
                        onDelete = { clientRemove, blocklist, skipRedownload ->
                            viewModel.removeQueueItem(selectedItem!!, clientRemove, blocklist, skipRedownload)
                        },
                    )
                }

                if (showActivityCustomizationSheet) {
                    ActivityCardCustomizationSheet(
                        useColoredActivityCards = useColoredCards,
                        onToggleUseColoredActivityCards = { preferences.toggleUseColoredActivityCards() },
                        onDismissRequest = { showActivityCustomizationSheet = false },
                    )
                }

                if (showManualImportSheet) {
                    val supportedInstances = instances.filter { it.supportsManualImport }
                    DirectoryManualImportSheet(
                        instances = supportedInstances,
                        initialInstanceId = (uiState.missingInstanceId ?: uiState.cutoffInstanceId ?: uiState.instanceId)
                            ?.takeIf { id -> supportedInstances.any { it.id == id } },
                        onDismiss = { showManualImportSheet = false },
                        onSuccess = { viewModel.refresh() },
                    )
                }
            }
        }
    }
}
