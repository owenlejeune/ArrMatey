package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.state.DownloadClientCommandState
import com.dnfapps.arrmatey.downloadclient.viewmodel.DownloadClientsViewModel
import com.dnfapps.arrmatey.downloadclient.viewmodel.DownloadQueueViewModel
import com.dnfapps.arrmatey.entensions.showErrorImmediately
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ArrAppBarWithSearch
import com.dnfapps.arrmatey.ui.components.downloads.DeleteDownloadDialog
import com.dnfapps.arrmatey.ui.components.downloads.DownloadSelectionBottomBar
import com.dnfapps.arrmatey.ui.components.downloads.DownloadSelectionTopBar
import com.dnfapps.arrmatey.ui.components.downloads.DownloadTransferSpeedChips
import com.dnfapps.arrmatey.ui.components.downloads.NoDownloadClientsView
import com.dnfapps.arrmatey.ui.components.downloads.TorrentActionsCard
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.helpers.LocalFloatingBarBottomPadding
import com.dnfapps.arrmatey.ui.menu.DownloadQueueFilterMenu
import com.dnfapps.arrmatey.ui.theme.ArrRed
import com.dnfapps.arrmatey.utils.mokoPlural
import com.dnfapps.arrmatey.utils.mokoString
import com.skydoves.flexible.bottomsheet.material3.FlexibleBottomSheet
import com.skydoves.flexible.core.FlexibleSheetSize
import com.skydoves.flexible.core.FlexibleSheetValue
import com.skydoves.flexible.core.rememberFlexibleBottomSheetState
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DownloadsTab(
    wideRailIsVisible: Boolean,
    viewModel: DownloadQueueViewModel = koinViewModel(),
    clientsViewModel: DownloadClientsViewModel = koinViewModel(),
    navigationManager: NavigationManager = koinInject(),
) {
    val queueState by viewModel.downloadQueueState.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val commandState by viewModel.commandState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val hasLoaded by viewModel.hasLoaded.collectAsStateWithLifecycle()
    val sortState by viewModel.sortState.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val downloadClientState by clientsViewModel.downloadClientsState.collectAsStateWithLifecycle()

    val isInSelectionMode by viewModel.selectionState.isInSelectionMode.collectAsStateWithLifecycle()
    val selectionCount by viewModel.selectionState.selectionCount.collectAsStateWithLifecycle()
    val selectedItems by viewModel.selectionState.selectedItems.collectAsStateWithLifecycle()

    var deleteTarget by remember { mutableStateOf<DownloadItem?>(null) }
    var bulkDeleteTarget by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val genericErrorMessage = mokoString(MR.strings.error)

    val textFieldState = rememberTextFieldState()

    val availableTags =
        remember(queueState.queueItems) {
            queueState.queueItems
                .flatMap { it.tags }
                .distinct()
                .sorted()
        }

    LaunchedEffect(textFieldState.text) {
        viewModel.updateSearchQuery(textFieldState.text.toString())
    }

    LaunchedEffect(commandState) {
        when (val state = commandState) {
            is DownloadClientCommandState.Success -> {
                deleteTarget = null
                bulkDeleteTarget = false
                viewModel.resetCommandState()
            }

            is DownloadClientCommandState.Error -> {
                deleteTarget = null
                bulkDeleteTarget = false
                snackbarHostState.showErrorImmediately(
                    message = state.message ?: genericErrorMessage,
                )
                viewModel.resetCommandState()
            }

            else -> {}
        }
    }

    val listState = rememberLazyListState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (isInSelectionMode) {
                DownloadSelectionTopBar(
                    count = selectionCount,
                    onClose = { viewModel.exitSelectionMode() },
                    onSelectAll = {
                        if (viewModel.areAllItemsSelected()) {
                            viewModel.clearSelection()
                        } else {
                            viewModel.selectAllItems()
                        }
                    },
                    isAllSelected = viewModel.areAllItemsSelected(),
                )
            } else {
                val count = queueState.queueItems.size
                val placeholderLabel = mokoPlural(MR.plurals.search_downloads, count)

                ArrAppBarWithSearch(
                    textFieldState = textFieldState,
                    searchPlaceholder = placeholderLabel,
                    navigationIcon = {
                        if (!wideRailIsVisible) {
                            NavigationDrawerButton()
                        }
                    },
                    actions = {
                        DownloadQueueFilterMenu(
                            filterState = filterState,
                            sortBy = sortState.sortBy,
                            onSortByChanged = { viewModel.updateSortBy(it) },
                            sortOrder = sortState.sortOrder,
                            onSortOrderChanged = { viewModel.updateSortOrder(it) },
                            availableTags = availableTags,
                            onToggleStatus = viewModel::toggleStatusFilter,
                            onToggleTag = viewModel::toggleTagFilter,
                            onUpdateActiveOnly = viewModel::updateActiveOnly,
                            onUpdateCompletedOnly = viewModel::updateCompletedOnly,
                            onUpdateExcludeStatuses = viewModel::updateExcludeStatuses,
                            onUpdateExcludeTags = viewModel::updateExcludeTags,
                            onClearFilters = viewModel::clearFilters,
                        )
                    },
                )
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
            if (downloadClientState.downloadClients.isEmpty()) {
                NoDownloadClientsView(
                    onAddDownloadClientClick = {
                        navigationManager.openNewDownloadClientScreen()
                    },
                )
            } else if (!hasLoaded || (queueState.queueItems.isEmpty() && isRefreshing)) {
                LoadingIndicator(
                    modifier = Modifier.size(96.dp),
                )
            } else {
                PullToRefreshBox(
                    modifier = Modifier.fillMaxSize(),
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    contentAlignment = Alignment.Center,
                ) {
                    Column {
                        DownloadTransferSpeedChips(
                            downloadClients = downloadClientState.downloadClients,
                            transferInfo = queueState.transferInfo,
                            selectedClientIds = filterState.clientIds,
                            onToggleClientIdFilter = viewModel::toggleClientIdFilter,
                        )

                        if (queueState.queueItems.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                if (errorMessage != null) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = ArrRed,
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = errorMessage!!,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = ArrRed,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 32.dp),
                                    )
                                } else if (isRefreshing) {
                                    LoadingIndicator(
                                        modifier = Modifier.size(64.dp),
                                    )
                                } else {
                                    Text(text = mokoString(MR.strings.no_activity))
                                }
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding =
                                    PaddingValues(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = 16.dp,
                                        bottom = 16.dp + LocalFloatingBarBottomPadding.current,
                                    ),
                                state = listState,
                            ) {
                                items(
                                    items = queueState.queueItems,
                                    key = { "${it.client.id}:${it.id}" },
                                ) { item ->
                                    val isSelected = selectedItems.contains(item.id)
                                    TorrentActionsCard(
                                        item = item,
                                        showClientInfo = downloadClientState.downloadClients.size > 1,
                                        isInSelectionMode = isInSelectionMode,
                                        isSelected = isSelected,
                                        onPause = { viewModel.pauseDownload(item.id) },
                                        onResume = { viewModel.resumeDownload(item.id) },
                                        onDelete = { deleteTarget = item },
                                        onLongClick = {
                                            viewModel.toggleItemSelection(item.id)
                                            viewModel.enterSelectionMode()
                                        },
                                        onClick = {
                                            if (isInSelectionMode) {
                                                viewModel.toggleItemSelection(item.id)
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isInSelectionMode) {
            FlexibleBottomSheet(
                onDismissRequest = { viewModel.exitSelectionMode() },
                sheetState =
                    rememberFlexibleBottomSheetState(
                        isModal = false,
                        initialValue = FlexibleSheetValue.IntermediatelyExpanded,
                        flexibleSheetSize =
                            FlexibleSheetSize(
                                fullyExpanded = FlexibleSheetSize.WrapContent,
                                intermediatelyExpanded = 0.15f,
                                slightlyExpanded = 0.15f,
                            ),
                    ),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                DownloadSelectionBottomBar(
                    onPause = { viewModel.pauseSelected() },
                    onResume = { viewModel.resumeSelected() },
                    onDelete = { bulkDeleteTarget = true },
                )
            }
        }

        deleteTarget?.let { item ->
            DeleteDownloadDialog(
                commandState = commandState,
                onDismiss = {
                    deleteTarget = null
                },
                onConfirm = { deleteFiles ->
                    viewModel.deleteDownload(item.id, deleteFiles)
                },
            )
        }

        if (bulkDeleteTarget) {
            DeleteDownloadDialog(
                commandState = commandState,
                onDismiss = { bulkDeleteTarget = false },
                onConfirm = { deleteFiles ->
                    viewModel.deleteSelected(deleteFiles)
                },
            )
        }
    }
}
