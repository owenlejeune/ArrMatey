package com.dnfapps.arrmatey.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.MockMedia
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.arr.state.ArrLibrary
import com.dnfapps.arrmatey.arr.viewmodel.ActivityQueueViewModel
import com.dnfapps.arrmatey.arr.viewmodel.ArrMediaViewModel
import com.dnfapps.arrmatey.arr.viewmodel.InstancesViewModel
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.arrNavigator
import com.dnfapps.arrmatey.navigation.navigationManager
import com.dnfapps.arrmatey.navigation.toDetails
import com.dnfapps.arrmatey.navigation.toSearch
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ArrAppBarWithSearch
import com.dnfapps.arrmatey.ui.components.ErrorView
import com.dnfapps.arrmatey.ui.components.InstancePicker
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.ui.components.MediaView
import com.dnfapps.arrmatey.ui.components.NoInstanceView
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.menu.LibraryFilterMenu
import com.dnfapps.arrmatey.ui.sheets.ArrViewCustomizationSheet
import com.dnfapps.arrmatey.ui.sheets.EditArtistSheet
import com.dnfapps.arrmatey.ui.sheets.EditAudiobookSheet
import com.dnfapps.arrmatey.ui.sheets.EditAuthorSheet
import com.dnfapps.arrmatey.ui.sheets.EditMovieSheet
import com.dnfapps.arrmatey.ui.sheets.EditSeriesSheet
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource
import org.koin.compose.koinInject

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArrLibraryScreen(
    type: InstanceType,
    isExpanded: Boolean = false,
    wideRailIsVisible: Boolean = false,
    arrMediaViewModel: ArrMediaViewModel = koinInjectParams(type),
    instancesViewModel: InstancesViewModel = koinInjectParams(type),
    activityQueueViewModel: ActivityQueueViewModel = koinInject(),
    globalPreferencesStore: PreferencesStore = koinInject(),
) {
    val context = LocalContext.current
    val navigation = arrNavigator
    val navigationManager = navigationManager

    val queueItems by activityQueueViewModel.queueItems.collectAsStateWithLifecycle()
    val uiState by arrMediaViewModel.uiState.collectAsStateWithLifecycle()
    val instancesState by instancesViewModel.instancesState.collectAsStateWithLifecycle()
    val instanceData by arrMediaViewModel.instanceData.collectAsStateWithLifecycle()
    val preferences by arrMediaViewModel.preferences.collectAsStateWithLifecycle()

    val hideInstancePicker by globalPreferencesStore.hideInstanceSwitcher.collectAsStateWithLifecycle(false)

    val errorMessage by arrMediaViewModel.errorMessage.collectAsStateWithLifecycle()
    val deleteStatus by arrMediaViewModel.deleteStatus.collectAsStateWithLifecycle()
    val editStatus by arrMediaViewModel.editItemStatus.collectAsStateWithLifecycle()
    val lastSearchResult by arrMediaViewModel.lastSearchResult.collectAsStateWithLifecycle()

    var showViewCustomizationSheet by remember { mutableStateOf(false) }
    var selectedItemForMenu by remember { mutableStateOf<ArrMedia?>(null) }
    var confirmDelete by remember { mutableStateOf<ArrMedia?>(null) }
    var showEditSheet by remember { mutableStateOf<ArrMedia?>(null) }
    var moveFilesItem by remember { mutableStateOf<ArrMedia?>(null) }

    LaunchedEffect(errorMessage) {
        errorMessage?.takeUnless { it.isEmpty() }?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        arrMediaViewModel.resetErrorMessage()
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
            arrMediaViewModel.resetDeleteStatus()
            selectedItemForMenu = null
            confirmDelete = null
        }
    }

    LaunchedEffect(editStatus) {
        if (editStatus is OperationStatus.Success) {
            arrMediaViewModel.resetEditItemStatus()
            showEditSheet = null
        }
    }

    val textFieldState = rememberTextFieldState()

    LaunchedEffect(textFieldState.text) {
        arrMediaViewModel.updateSearchQuery(textFieldState.text.toString())
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            if (!isExpanded) {
                instancesState.selectedInstance?.let {
                    FloatingActionButton(
                        onClick = { navigation.toSearch() }
                    ) {
                        Icon(Icons.Default.Add, null)
                    }
                }
            }
        },
        topBar = {
            ArrAppBarWithSearch(
                textFieldState = textFieldState,
                textFieldEnabled = instancesState.selectedInstance != null,
                searchPlaceholder = mokoString(MR.strings.search_placeholder, instancesState.selectedInstance?.label ?: ""),
                trailingIcon = {
                    Image(
                        painter = painterResource(type.icon),
                        contentDescription = mokoString(type.resource),
                        modifier = Modifier.size(24.dp)
                    )
                },
                navigationIcon = {
                    if (!wideRailIsVisible) {
                        NavigationDrawerButton()
                    }
                },
                actions = {
                    if (!hideInstancePicker || instancesState.instances.size > 1) {
                        InstancePicker(
                            type = type,
                            currentInstance = instancesState.selectedInstance,
                            typeInstances = instancesState.instances,
                            onInstanceSelected = { instancesViewModel.setInstanceActive(it) }
                        )
                    }
                    LibraryFilterMenu(
                        type = type,
                        filterBy = preferences.filterBy,
                        onFilterByChanged = { arrMediaViewModel.updateFilterBy(it) },
                        customFilters = instanceData?.customFilters ?: emptyList(),
                        selectedCustomFilterId = preferences.customFilterId,
                        onCustomFilterChanged = { arrMediaViewModel.updateCustomFilter(it) },
                        sortBy = preferences.sortBy,
                        onSortByChanged = { arrMediaViewModel.updateSortBy(it) },
                        sortOrder = preferences.sortOrder,
                        onSortOrderChanged = { arrMediaViewModel.updateSortOrder(it) },
                        onOpenViewCustomization = { showViewCustomizationSheet = true }
                    )
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
            if (instancesState.selectedInstance == null) {
                NoInstanceView(type)
            } else {
                when (val state = uiState) {
                    is ArrLibrary.Initial -> {
                        NoInstanceView(type)
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
                                instancesState.selectedInstance?.let {
                                    navigationManager.openEditInstanceScreen(it.id)
                                }
                            },
                            onRetry = {
                                arrMediaViewModel.refresh()
                            }
                        )
                    }

                    is ArrLibrary.Success -> {
                        PullToRefreshBox(
                            isRefreshing = false,
                            onRefresh = {
                                arrMediaViewModel.refresh()
                            },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val items = state.items
                            if (items.isEmpty() && textFieldState.text.isEmpty()) {
                                EmptyLibraryView(modifier = Modifier.align(Alignment.Center))
                            } else if (items.isNotEmpty()) {
                                MediaView(
                                    type = type,
                                    items = items,
                                    onItemClick = {
                                        it.id?.let { id ->
                                            navigation.toDetails(id)
                                        }
                                    },
                                    preferences = preferences,
                                    itemIsActive = { item ->
                                        queueItems.any { it.mediaId == item.id }
                                    },
                                    onItemLongClick = { item ->
                                        selectedItemForMenu = item
                                    }
                                )
                            } else {
                                EmptySearchResultsView(type, textFieldState.text.toString()) {
                                    navigation.toSearch(textFieldState.text.toString())
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
                type = type,
                onViewTypeChanged = { arrMediaViewModel.updateViewType(it) },
                onShowFullDetailsChanged = { arrMediaViewModel.updateShowFullDetails(it) },
                onShowOverlayChanged = { arrMediaViewModel.updateShowOverlay(it) },
                onShowBannerBackgroundChanged = { arrMediaViewModel.updateShowBannerBackground(it) },
                onIncludeOverviewChanged = { arrMediaViewModel.updateIncludeOverview(it) },
                onBannerBlurChanged = { arrMediaViewModel.updateBannerBlur(it) },
                onGridDensityChanged = { arrMediaViewModel.updateGridDensity(it) },
                onGridSpacingChanged = { arrMediaViewModel.updateGridSpacing(it) },
                onPosterElevationChanged = { arrMediaViewModel.updatePosterElevation(it) },
                onPosterRadiusChanged = { arrMediaViewModel.updatePosterRadius(it) },
                onApplyGloballyChanged = { arrMediaViewModel.updateApplyGlobally(it) }
            )
        }

        selectedItemForMenu?.let { item ->
            ArrItemContextMenu(
                item = item,
                type = type,
                onDismissRequest = { selectedItemForMenu = null },
                onSearchMonitored = { arrMediaViewModel.performAutomaticLookup(item) },
                onSearchSubtitles = { /* TODO */ },
                onEdit = { showEditSheet = item },
                onRefresh = { arrMediaViewModel.performRefresh(item) },
                onRemove = { confirmDelete = item },
                onToggleMonitored = { arrMediaViewModel.toggleMonitored(item) }
            )
        }

        confirmDelete?.let { item ->
            ConfirmDeleteAlert(
                deleteInProgress = deleteStatus is OperationStatus.InProgress,
                onDismiss = { confirmDelete = null },
                onDelete = { deleteFiles, addExclusion ->
                    arrMediaViewModel.deleteMedia(item, deleteFiles, addExclusion)
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
                        arrMediaViewModel.editItem(it)
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
                            arrMediaViewModel.editItem(item, moveFiles = true)
                            moveFilesItem = null
                        }
                    ) {
                        Text(mokoString(MR.strings.yes))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            arrMediaViewModel.editItem(item)
                            moveFilesItem = null
                        }
                    ) {
                        Text(mokoString(MR.strings.no))
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArrItemContextMenu(
    item: ArrMedia,
    type: InstanceType,
    onDismissRequest: () -> Unit,
    onSearchMonitored: () -> Unit,
    onSearchSubtitles: () -> Unit,
    onEdit: () -> Unit,
    onRefresh: () -> Unit,
    onRemove: () -> Unit,
    onToggleMonitored: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = item.title ?: "",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text(mokoString(if (item.monitored) MR.strings.unmonitored else MR.strings.monitored)) },
                leadingContent = {
                    Icon(
                        if (item.monitored) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        null
                    )
                },
                modifier = Modifier.clickable {
                    onToggleMonitored()
                    onDismissRequest()
                }
            )

            if (type.includeTopLevelAutomaticSearchOption) {
                ListItem(
                    headlineContent = { Text(mokoString(MR.strings.search_monitored)) },
                    leadingContent = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.clickable {
                        onSearchMonitored()
                        onDismissRequest()
                    }
                )
            }

            ListItem(
                headlineContent = { Text(mokoString(MR.strings.refresh)) },
                leadingContent = { Icon(Icons.Default.Refresh, null) },
                modifier = Modifier.clickable {
                    onRefresh()
                    onDismissRequest()
                }
            )

            ListItem(
                headlineContent = { Text(mokoString(MR.strings.edit)) },
                leadingContent = { Icon(Icons.Default.Edit, null) },
                modifier = Modifier.clickable {
                    onEdit()
                    onDismissRequest()
                }
            )

            ListItem(
                headlineContent = {
                    Text(
                        mokoString(MR.strings.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                },
                leadingContent = {
                    Icon(
                        Icons.Default.Delete,
                        null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                modifier = Modifier.clickable {
                    onRemove()
                    onDismissRequest()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmDeleteAlert(
    deleteInProgress: Boolean,
    onDismiss: () -> Unit,
    onDelete: (Boolean, Boolean) -> Unit
) {
    var addExclusion by remember { mutableStateOf(false) }
    var deleteFiles by remember { mutableStateOf(false) }
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            LabelledSwitch(
                label = mokoString(MR.strings.add_exclusion),
                sublabel = mokoString(MR.strings.add_exclusion_description),
                checked = addExclusion,
                onCheckedChange = { addExclusion = !addExclusion }
            )
            LabelledSwitch(
                label = mokoString(MR.strings.delete_files),
                sublabel = mokoString(MR.strings.delete_files_description),
                checked = deleteFiles,
                onCheckedChange = { deleteFiles = !deleteFiles }
            )
            Button(
                onClick = { onDelete(deleteFiles, addExclusion) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                enabled = !deleteInProgress
            ) {
                if (deleteInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null
                    )
                    Text(text = mokoString(MR.strings.delete))
                }
            }
        }
    }
}

@Composable
private fun EditMediaSheet(
    item: ArrMedia,
    qualityProfiles: List<QualityProfile>,
    rootFolders: List<RootFolder>,
    tags: List<Tag>,
    editInProgress: Boolean,
    onEditItem: (ArrMedia) -> Unit,
    onDismiss: () -> Unit
) {
    when (item) {
        is ArrMovie -> EditMovieSheet(
            item = item,
            qualityProfiles = qualityProfiles,
            rootFolders = rootFolders,
            tags = tags,
            editInProgress = editInProgress,
            onEditItem = onEditItem,
            onDismiss = onDismiss,
        )
        is ArrSeries -> EditSeriesSheet(
            item = item,
            qualityProfiles = qualityProfiles,
            rootFolders = rootFolders,
            tags = tags,
            editInProgress = editInProgress,
            onEditItem = onEditItem,
            onDismiss = onDismiss
        )
        is Arrtist -> EditArtistSheet(
            item = item,
            qualityProfiles = qualityProfiles,
            rootFolders = rootFolders,
            tags = tags,
            editInProgress = editInProgress,
            onEditItem = onEditItem,
            onDismiss = onDismiss
        )
        is Author -> EditAuthorSheet(
            item = item,
            qualityProfiles = qualityProfiles,
            rootFolders = rootFolders,
            tags = tags,
            editInProgress = editInProgress,
            onEditItem = onEditItem,
            onDismiss = onDismiss
        )
        is Audiobook -> EditAudiobookSheet(
            item = item,
            qualityProfiles = qualityProfiles,
            rootFolders = rootFolders,
            editInProgress = editInProgress,
            onEditItem = onEditItem,
            onDismiss = onDismiss
        )
        is SearchAudiobook,
        is MockMedia -> {}
    }
}

@Composable
private fun EmptySearchResultsView(
    type: InstanceType,
    query: String,
    onShouldSearch: () -> Unit
) {
    val mediaType = when (type) {
        InstanceType.Sonarr -> mokoString(MR.strings.type_series)
        InstanceType.Radarr -> mokoString(MR.strings.type_movie)
        InstanceType.Lidarr -> mokoString(MR.strings.type_artist)
        InstanceType.Booksehelf -> mokoString(MR.strings.type_author)
        else -> mokoString(MR.strings.unknown)
    }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
    ) {
        Text(
            text = mokoString(MR.strings.no_query_results, query),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = buildAnnotatedString {
                append(mokoString(MR.strings.no_query_results_label))
                append(" ")
                withLink(
                    link = LinkAnnotation.Clickable(tag = "new_entry") {
                        onShouldSearch()
                    }
                ) {
                    withStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )) {
                        append(mokoString(MR.strings.no_query_results_link, mediaType))
                    }
                }
            }
        )
    }
}

@Composable
private fun EmptyLibraryView(
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.VideoLibrary,
            contentDescription = null,
            modifier = Modifier.size(128.dp)
        )
        Text(
            text = mokoString(MR.strings.empty_library),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = mokoString(MR.strings.empty_library_message)
        )
    }
}
