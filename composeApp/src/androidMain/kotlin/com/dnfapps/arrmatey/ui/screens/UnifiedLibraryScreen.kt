package com.dnfapps.arrmatey.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.state.ArrLibrary
import com.dnfapps.arrmatey.arr.viewmodel.ActivityQueueViewModel
import com.dnfapps.arrmatey.arr.viewmodel.UnifiedLibraryViewModel
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.navigationManager
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ArrAppBarWithSearch
import com.dnfapps.arrmatey.ui.components.ErrorView
import com.dnfapps.arrmatey.ui.components.MediaView
import com.dnfapps.arrmatey.ui.components.NoInstanceView
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.menu.LibraryFilterMenu
import com.dnfapps.arrmatey.ui.sheets.ArrViewCustomizationSheet
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.networking.OperationStatus
import com.skydoves.flexible.bottomsheet.material3.FlexibleBottomSheet
import com.skydoves.flexible.core.FlexibleSheetSize
import com.skydoves.flexible.core.FlexibleSheetValue
import com.skydoves.flexible.core.rememberFlexibleBottomSheetState
import dev.icerock.moko.resources.compose.painterResource
import org.koin.compose.koinInject

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UnifiedLibraryScreen(
    isExpanded: Boolean = false,
    wideRailIsVisible: Boolean = false,
    onNavigateToSearch: (String, InstanceType) -> Unit,
    onNavigateToDetails: (ArrMedia, InstanceType) -> Unit,
    unifiedLibraryViewModel: UnifiedLibraryViewModel = koinInject(),
    activityQueueViewModel: ActivityQueueViewModel = koinInject()
) {
    val context = LocalContext.current
    val navigationManager = navigationManager

    val arrInstances by unifiedLibraryViewModel.arrInstances.collectAsStateWithLifecycle()
    val selectedInstance by unifiedLibraryViewModel.selectedInstance.collectAsStateWithLifecycle()
    val offlineInstanceIds by unifiedLibraryViewModel.offlineInstanceIds.collectAsStateWithLifecycle()
    val uiState by unifiedLibraryViewModel.currentLibraryState.collectAsStateWithLifecycle()
    val instanceData by unifiedLibraryViewModel.instanceData.collectAsStateWithLifecycle()
    val preferences by unifiedLibraryViewModel.preferences.collectAsStateWithLifecycle()
    val queueItems by activityQueueViewModel.queueItems.collectAsStateWithLifecycle()

    val isInSelectionMode by unifiedLibraryViewModel.selectionState.isInSelectionMode.collectAsStateWithLifecycle()
    val selectionCount by unifiedLibraryViewModel.selectionState.selectionCount.collectAsStateWithLifecycle()
    val selectedItem by unifiedLibraryViewModel.selectedItem.collectAsStateWithLifecycle()

    val errorMessage by unifiedLibraryViewModel.errorMessage.collectAsStateWithLifecycle()
    val deleteStatus by unifiedLibraryViewModel.deleteStatus.collectAsStateWithLifecycle()
    val editStatus by unifiedLibraryViewModel.editItemStatus.collectAsStateWithLifecycle()
    val lastSearchResult by unifiedLibraryViewModel.lastSearchResult.collectAsStateWithLifecycle()
    val hasBazarr by unifiedLibraryViewModel.hasBazarr.collectAsStateWithLifecycle()

    var showViewCustomizationSheet by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<ArrMedia?>(null) }
    var showEditSheet by remember { mutableStateOf<ArrMedia?>(null) }
    var moveFilesItem by remember { mutableStateOf<ArrMedia?>(null) }
    var confirmBulkDelete by remember { mutableStateOf(false) }
    var showMonitorOptionsSheet by remember { mutableStateOf(false) }

    LaunchedEffect(errorMessage) {
        errorMessage?.takeUnless { it.isEmpty() }?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        unifiedLibraryViewModel.resetErrorMessage()
    }

    val searchQueuedMessage = mokoString(MR.strings.search_queued)
    val searchErrorMessage = mokoString(MR.strings.search_error)
    LaunchedEffect(lastSearchResult) {
        when (lastSearchResult) {
            true -> {
                Toast.makeText(context, searchQueuedMessage, Toast.LENGTH_SHORT).show()
            }
            false -> {
                Toast.makeText(context, searchErrorMessage, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    LaunchedEffect(deleteStatus) {
        if (deleteStatus is OperationStatus.Success) {
            unifiedLibraryViewModel.resetDeleteStatus()
            confirmDelete = null
        }
    }

    LaunchedEffect(editStatus) {
        if (editStatus is OperationStatus.Success) {
            unifiedLibraryViewModel.resetEditItemStatus()
            showEditSheet = null
            unifiedLibraryViewModel.exitSelectionMode()
        }
    }

    val textFieldState = rememberTextFieldState()

    LaunchedEffect(textFieldState.text) {
        unifiedLibraryViewModel.updateSearchQuery(textFieldState.text.toString())
    }

    if (arrInstances.isEmpty() || selectedInstance == null) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                ArrAppBarWithSearch(
                    textFieldState = rememberTextFieldState(),
                    textFieldEnabled = false,
                    searchPlaceholder = mokoString(MR.strings.library),
                    navigationIcon = {
                        if (!wideRailIsVisible) {
                            NavigationDrawerButton()
                        }
                    }
                )
            },
            contentWindowInsets = WindowInsets.statusBars
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                NoInstanceView(InstanceType.Sonarr)
            }
        }
    } else {
        val currentInstance = selectedInstance!!
        val currentType = currentInstance.type

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                AnimatedVisibility(
                    visible = !isExpanded && !isInSelectionMode,
                    enter = scaleIn(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)),
                    exit = scaleOut(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200))
                ) {
                    FloatingActionButton(
                        onClick = { onNavigateToSearch("", currentType) }
                    ) {
                        Icon(Icons.Default.Add, null)
                    }
                }
            },
            topBar = {
                AnimatedContent(
                    targetState = isInSelectionMode,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(200, delayMillis = 50)) +
                            slideInVertically(animationSpec = tween(200, delayMillis = 50)) { -it / 2 })
                            .togetherWith(
                                fadeOut(animationSpec = tween(150)) +
                                    slideOutVertically(animationSpec = tween(150)) { -it / 2 }
                            )
                    },
                    label = "SelectionTopBarAnimation"
                ) { inSelection ->
                    if (inSelection) {
                        SelectionTopBar(
                            count = selectionCount,
                            onClose = { unifiedLibraryViewModel.exitSelectionMode() },
                            onSelectAll = {
                                if (unifiedLibraryViewModel.areAllItemsSelected()) {
                                    unifiedLibraryViewModel.clearSelection()
                                } else {
                                    unifiedLibraryViewModel.selectAllItems()
                                }
                            },
                            isAllSelected = unifiedLibraryViewModel.areAllItemsSelected()
                        )
                    } else {
                        ArrAppBarWithSearch(
                            textFieldState = textFieldState,
                            textFieldEnabled = true,
                            searchPlaceholder = mokoString(
                                MR.strings.search_placeholder,
                                currentInstance.label
                            ),
                            trailingIcon = {
                                Image(
                                    painter = painterResource(currentType.icon),
                                    contentDescription = mokoString(currentType.resource),
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            navigationIcon = {
                                if (!wideRailIsVisible) {
                                    NavigationDrawerButton()
                                }
                            },
                            actions = {
                                LibraryFilterMenu(
                                    type = currentType,
                                    filterBy = preferences.filterBy,
                                    onFilterByChanged = { unifiedLibraryViewModel.updateFilterBy(it) },
                                    customFilters = instanceData?.customFilters ?: emptyList(),
                                    selectedCustomFilterId = preferences.customFilterId,
                                    onCustomFilterChanged = { unifiedLibraryViewModel.updateCustomFilter(it) },
                                    sortBy = preferences.sortBy,
                                    onSortByChanged = { unifiedLibraryViewModel.updateSortBy(it) },
                                    sortOrder = preferences.sortOrder,
                                    onSortOrderChanged = { unifiedLibraryViewModel.updateSortOrder(it) },
                                    onOpenViewCustomization = { showViewCustomizationSheet = true }
                                )
                            }
                        )
                    }
                }
            },
            contentWindowInsets = WindowInsets.statusBars
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                AnimatedVisibility(
                    visible = arrInstances.size > 1 && !isInSelectionMode,
                    enter = expandVertically(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)),
                    exit = shrinkVertically(animationSpec = tween(150)) + fadeOut(animationSpec = tween(150))
                ) {
                    val selectedIndex = arrInstances.indexOfFirst { it.id == currentInstance.id }.coerceAtLeast(0)
                    SecondaryScrollableTabRow(
                        selectedTabIndex = selectedIndex,
                        edgePadding = 16.dp,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        divider = {}
                    ) {
                        arrInstances.forEach { tabInstance ->
                            val isOffline = tabInstance.id in offlineInstanceIds
                            Tab(
                                selected = tabInstance.id == currentInstance.id,
                                onClick = { unifiedLibraryViewModel.selectInstance(tabInstance) },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(tabInstance.type.icon),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = tabInstance.label,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (isOffline) {
                                            Icon(
                                                imageVector = Icons.Default.WifiOff,
                                                contentDescription = mokoString(MR.strings.offline),
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when (val state = uiState) {
                        is ArrLibrary.Initial -> {
                            NoInstanceView(currentType)
                        }

                        is ArrLibrary.Loading -> {
                            LoadingIndicator(
                                modifier = Modifier.size(96.dp)
                            )
                        }

                        is ArrLibrary.Error -> {
                            ErrorView(
                                errorType = state.type,
                                message = state.message,
                                onOpenSettings = {
                                    navigationManager.openEditInstanceScreen(currentInstance.id)
                                },
                                onRetry = {
                                    unifiedLibraryViewModel.refreshSelected()
                                }
                            )
                        }

                        is ArrLibrary.Success -> {
                            PullToRefreshBox(
                                isRefreshing = false,
                                onRefresh = {
                                    unifiedLibraryViewModel.refreshSelected()
                                },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val items = state.items
                                if (items.isEmpty() && textFieldState.text.isEmpty()) {
                                    EmptyLibraryView(modifier = Modifier.align(Alignment.Center))
                                } else if (items.isNotEmpty()) {
                                    MediaView(
                                        type = currentType,
                                        items = items,
                                        onItemClick = {
                                            onNavigateToDetails(it, currentType)
                                        },
                                        preferences = preferences,
                                        itemIsActive = { item ->
                                            queueItems.any { it.mediaId == item.id }
                                        },
                                        multiSelectState = unifiedLibraryViewModel.selectionState
                                    )
                                } else {
                                    EmptySearchResultsView(currentType, textFieldState.text.toString()) {
                                        onNavigateToSearch(textFieldState.text.toString(), currentType)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showViewCustomizationSheet) {
                ArrViewCustomizationSheet(
                    onDismissRequest = { showViewCustomizationSheet = false },
                    preferences = preferences,
                    type = currentType,
                    onViewTypeChanged = { unifiedLibraryViewModel.updateViewType(it) },
                    onShowFullDetailsChanged = { unifiedLibraryViewModel.updateShowFullDetails(it) },
                    onShowOverlayChanged = { unifiedLibraryViewModel.updateShowOverlay(it) },
                    onShowBannerBackgroundChanged = { unifiedLibraryViewModel.updateShowBannerBackground(it) },
                    onIncludeOverviewChanged = { unifiedLibraryViewModel.updateIncludeOverview(it) },
                    onBannerBlurChanged = { unifiedLibraryViewModel.updateBannerBlur(it) },
                    onGridDensityChanged = { unifiedLibraryViewModel.updateGridDensity(it) },
                    onGridSpacingChanged = { unifiedLibraryViewModel.updateGridSpacing(it) },
                    onPosterElevationChanged = { unifiedLibraryViewModel.updatePosterElevation(it) },
                    onPosterRadiusChanged = { unifiedLibraryViewModel.updatePosterRadius(it) },
                    onApplyGloballyChanged = { unifiedLibraryViewModel.updateApplyGlobally(it) }
                )
            }

            if (isInSelectionMode) {
                FlexibleBottomSheet(
                    onDismissRequest = { unifiedLibraryViewModel.exitSelectionMode() },
                    sheetState = rememberFlexibleBottomSheetState(
                        isModal = false,
                        initialValue = FlexibleSheetValue.IntermediatelyExpanded,
                        flexibleSheetSize = FlexibleSheetSize(
                            fullyExpanded = FlexibleSheetSize.WrapContent,
                            intermediatelyExpanded = 0.15f,
                            slightlyExpanded = 0.15f
                        )
                    ),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    SelectionBottomBar(
                        count = selectionCount,
                        type = currentType,
                        hasBazarr = hasBazarr,
                        isMonitored = selectedItem?.monitored == true,
                        onEdit = {
                            showEditSheet = selectedItem
                        },
                        onToggleMonitor = { unifiedLibraryViewModel.toggleMonitoringForSelected() },
                        onRefresh = { unifiedLibraryViewModel.refreshSelectedItems() },
                        onSearchMonitored = { unifiedLibraryViewModel.performAutomaticLookupSelected() },
                        onSearchSubtitles = { unifiedLibraryViewModel.performSubtitleSearchSelected() },
                        onUpdateMonitoring = { showMonitorOptionsSheet = true },
                        onDelete = { confirmBulkDelete = true }
                    )
                }
            }

            confirmDelete?.let { item ->
                ConfirmDeleteAlert(
                    deleteInProgress = deleteStatus is OperationStatus.InProgress,
                    initialAddExclusion = preferences.deleteAddExclusion,
                    initialDeleteFiles = preferences.deleteDeleteFiles,
                    onDismiss = { confirmDelete = null },
                    onDelete = { deleteFiles, addExclusion ->
                        unifiedLibraryViewModel.deleteMedia(item, deleteFiles, addExclusion)
                    }
                )
            }

            showEditSheet?.let { item ->
                val data = instanceData ?: return@let
                EditMediaSheet(
                    item = item,
                    qualityProfiles = data.qualityProfiles,
                    rootFolders = data.rootFolders,
                    tags = data.tags,
                    editInProgress = editStatus is OperationStatus.InProgress,
                    onEditItem = {
                        if (item.rootFolderPath != it.rootFolderPath) {
                            moveFilesItem = it
                        } else {
                            unifiedLibraryViewModel.editItem(it)
                        }
                    },
                    onDismiss = { showEditSheet = null }
                )
            }

            moveFilesItem?.let { item ->
                AlertDialog(
                    onDismissRequest = { moveFilesItem = null },
                    title = {
                        Text(mokoString(MR.strings.move_files_confirm, item.rootFolderPath ?: ""))
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                unifiedLibraryViewModel.editItem(item, moveFiles = true)
                                moveFilesItem = null
                            }
                        ) {
                            Text(mokoString(MR.strings.yes))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                unifiedLibraryViewModel.editItem(item)
                                moveFilesItem = null
                            }
                        ) {
                            Text(mokoString(MR.strings.no))
                        }
                    }
                )
            }

            if (confirmBulkDelete) {
                ConfirmDeleteAlert(
                    deleteInProgress = false,
                    initialAddExclusion = preferences.deleteAddExclusion,
                    initialDeleteFiles = preferences.deleteDeleteFiles,
                    onDismiss = { confirmBulkDelete = false },
                    onDelete = { deleteFiles, addExclusion ->
                        unifiedLibraryViewModel.deleteSelected(deleteFiles, addExclusion)
                        confirmBulkDelete = false
                    }
                )
            }

            if (showMonitorOptionsSheet) {
                MonitorOptionsSheet(
                    type = currentType,
                    onDismissRequest = { showMonitorOptionsSheet = false },
                    onOptionSelected = {
                        // Monitor option selected
                        showMonitorOptionsSheet = false
                    }
                )
            }
        }
    }
}
