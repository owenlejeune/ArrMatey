package com.dnfapps.arrmatey.ui.sheets

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import com.dnfapps.arrmatey.seerr.api.model.UserPermission
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.viewmodel.UnifiedMediaDetailsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private enum class SheetMode {
    Request,
    AddDirectly,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaRequestOrAddSheet(
    item: DiscoverResult,
    onDismiss: () -> Unit,
    viewModel: UnifiedMediaDetailsViewModel =
        koinViewModel(
            key = "request_add_${item.id}_${item.mediaType.name}",
            parameters = {
                parametersOf(
                    null,
                    item.id,
                    null,
                    if (item.mediaType == RequestType.Tv) InstanceType.Sonarr else InstanceType.Radarr,
                    item.mediaType,
                    null,
                )
            },
        ),
) {
    MediaRequestOrAddSheet(
        onDismiss = onDismiss,
        viewModel = viewModel,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaRequestOrAddSheet(
    onDismiss: () -> Unit,
    viewModel: UnifiedMediaDetailsViewModel,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val buttonState by viewModel.buttonState.collectAsStateWithLifecycle()
    val requestStatus by viewModel.requestStatus.collectAsStateWithLifecycle()
    val addItemStatus by viewModel.addItemStatus.collectAsStateWithLifecycle()
    val addSheetUiState by viewModel.addSheetUiState.collectAsStateWithLifecycle()
    val serviceDetails by viewModel.serviceDetails.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val qualityProfiles by viewModel.qualityProfiles.collectAsStateWithLifecycle()
    val rootFolders by viewModel.rootFolders.collectAsStateWithLifecycle()
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()

    val state = uiState as? UnifiedMediaDetailsUiState.Success
    val seerrMedia = state?.seerrMedia
    val arrMedia = state?.arrMedia
    val canAddDirectly = addSheetUiState.availableInstances.isNotEmpty()
    val canRequest = seerrMedia != null
    val mediaType =
        viewModel.resolvedRequestType
            ?: if (seerrMedia is TvDetails || arrMedia is ArrSeries) RequestType.Tv else RequestType.Movie
    val instanceType =
        viewModel.resolvedInstanceType
            ?: if (mediaType == RequestType.Tv) InstanceType.Sonarr else InstanceType.Radarr
    val isBusy = requestStatus is OperationStatus.InProgress || addItemStatus is OperationStatus.InProgress

    var showMode by remember {
        mutableStateOf(if (canAddDirectly) SheetMode.AddDirectly else SheetMode.Request)
    }
    var hasManuallySelected by remember { mutableStateOf(false) }

    LaunchedEffect(canAddDirectly) {
        if (canAddDirectly && !hasManuallySelected) {
            showMode = SheetMode.AddDirectly
        }
    }

    var is4k by remember { mutableStateOf(false) }

    val itemAddedSuccessfullyMessage = mokoString(MR.strings.item_added_successfully)
    val errorAddingItemMessage = mokoString(MR.strings.error_adding_item)
    val successMessage = mokoString(MR.strings.success)

    LaunchedEffect(requestStatus) {
        when (requestStatus) {
            is OperationStatus.Success -> {
                Toast.makeText(context, (requestStatus as OperationStatus.Success).message ?: successMessage, Toast.LENGTH_SHORT).show()
                onDismiss()
            }
            is OperationStatus.Error -> {
                (requestStatus as OperationStatus.Error).message?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            }
            else -> {}
        }
    }

    LaunchedEffect(addItemStatus) {
        when (addItemStatus) {
            is OperationStatus.Success -> {
                Toast.makeText(context, itemAddedSuccessfullyMessage, Toast.LENGTH_SHORT).show()
                onDismiss()
            }
            is OperationStatus.Error -> {
                Toast.makeText(context, errorAddingItemMessage, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    // Request state
    var selectedProfileId by remember { mutableStateOf<Long?>(null) }
    var selectedRootFolder by remember { mutableStateOf<String?>(null) }
    var selectedUserId by remember { mutableStateOf<Long?>(null) }
    var selectedSeasons by remember {
        mutableStateOf(
            if (seerrMedia is TvDetails) {
                seerrMedia.seasons.map { it.seasonNumber }.toSet()
            } else {
                emptySet()
            },
        )
    }

    LaunchedEffect(serviceDetails, currentUser) {
        if (selectedProfileId == null) {
            selectedProfileId = serviceDetails?.server?.activeProfileId?.toLong()
        }
        if (selectedRootFolder == null) {
            selectedRootFolder = serviceDetails?.server?.activeDirectory
        }
        if (selectedUserId == null) {
            selectedUserId = currentUser?.id
        }
    }

    LaunchedEffect(seerrMedia) {
        if (seerrMedia is TvDetails && selectedSeasons.isEmpty()) {
            selectedSeasons = seerrMedia.seasons.map { it.seasonNumber }.toSet()
        }
    }

    val isAdmin = currentUser?.hasPermission(UserPermission.ADMIN) == true

    // Add Movie state
    val targetInstanceId = addSheetUiState.targetInstance?.id
    val effectiveQualityProfiles = addSheetUiState.qualityProfiles.ifEmpty { qualityProfiles }
    val effectiveRootFolders = addSheetUiState.rootFolders.ifEmpty { rootFolders }
    val effectiveTags = addSheetUiState.tags.ifEmpty { tags }

    var movieMonitored by remember(preferences.addMovieMonitored, targetInstanceId) { mutableStateOf(preferences.addMovieMonitored) }
    var movieMinimumAvailability by remember(preferences.addMovieMinimumAvailability, targetInstanceId) {
        mutableStateOf(preferences.addMovieMinimumAvailability)
    }
    var movieQualityProfile by remember(effectiveQualityProfiles, preferences.addQualityProfileId, targetInstanceId) {
        mutableStateOf(
            effectiveQualityProfiles.firstOrNull { it.id == preferences.addQualityProfileId }
                ?: effectiveQualityProfiles.firstOrNull(),
        )
    }
    var movieRootFolder by remember(effectiveRootFolders, preferences.addRootFolderPath, targetInstanceId) {
        mutableStateOf(
            effectiveRootFolders.firstOrNull { it.path == preferences.addRootFolderPath }
                ?: effectiveRootFolders.firstOrNull(),
        )
    }
    val movieTags = remember(targetInstanceId) { mutableStateListOf<Int>() }
    var movieSearchOnAdd by remember(preferences.addSearchOnAdd, targetInstanceId) { mutableStateOf(preferences.addSearchOnAdd) }

    // Add Series state
    var seriesMonitor by remember(preferences.addSeriesMonitor, targetInstanceId) { mutableStateOf(preferences.addSeriesMonitor) }
    var seriesQualityProfile by remember(effectiveQualityProfiles, preferences.addQualityProfileId, targetInstanceId) {
        mutableStateOf(
            effectiveQualityProfiles.firstOrNull { it.id == preferences.addQualityProfileId }
                ?: effectiveQualityProfiles.firstOrNull(),
        )
    }
    var seriesType by remember(preferences.addSeriesType, targetInstanceId) { mutableStateOf(preferences.addSeriesType) }
    var seriesSeasonFolders by remember(preferences.addSeriesSeasonFolder, targetInstanceId) {
        mutableStateOf(preferences.addSeriesSeasonFolder)
    }
    var seriesRootFolder by remember(effectiveRootFolders, preferences.addRootFolderPath, targetInstanceId) {
        mutableStateOf(
            effectiveRootFolders.firstOrNull { it.path == preferences.addRootFolderPath }
                ?: effectiveRootFolders.firstOrNull(),
        )
    }
    val seriesTags = remember(targetInstanceId) { mutableStateListOf<Int>() }
    var seriesSearchOnAdd by remember(preferences.addSearchOnAdd, targetInstanceId) { mutableStateOf(preferences.addSearchOnAdd) }

    val isActionEnabled =
        if (showMode == SheetMode.Request) {
            !isBusy && seerrMedia != null && (seerrMedia !is TvDetails || selectedSeasons.isNotEmpty())
        } else {
            when (arrMedia) {
                is ArrSeries -> !isBusy && seriesQualityProfile != null && seriesRootFolder != null
                is ArrMovie -> !isBusy && movieQualityProfile != null && movieRootFolder != null
                else -> false
            }
        }

    ModalBottomSheet(
        onDismissRequest = {
            if (!isBusy) {
                onDismiss()
            }
        },
        sheetState =
            rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { !isBusy },
            ),
    ) {
        if (state == null || (seerrMedia == null && arrMedia == null)) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            val displayTitle = seerrMedia?.displayTitle ?: arrMedia?.title ?: ""
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp)
                        .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Static Header: Title and Selector
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(
                            text =
                                mokoString(
                                    if (mediaType == RequestType.Tv) MR.strings.type_series else MR.strings.type_movie,
                                ).uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    if (canAddDirectly && canRequest) {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            SegmentedButton(
                                selected = showMode == SheetMode.AddDirectly,
                                onClick = {
                                    showMode = SheetMode.AddDirectly
                                    hasManuallySelected = true
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                                label = { Text(mokoString(MR.strings.add_to_arr, instanceType.name)) },
                                enabled = !isBusy,
                            )
                            SegmentedButton(
                                selected = showMode == SheetMode.Request,
                                onClick = {
                                    showMode = SheetMode.Request
                                    hasManuallySelected = true
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                                label = { Text(mokoString(MR.strings.request)) },
                                enabled = !isBusy,
                            )
                        }
                    }
                }

                // Middle Configuration (Crossfaded & Scrollable)
                Crossfade(
                    targetState = showMode,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                    label = "RequestOrAddContentCrossfade",
                ) { mode ->
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        if (mode == SheetMode.Request) {
                            if (seerrMedia != null) {
                                SeerrRequestConfigurationContent(
                                    details = seerrMedia,
                                    serviceDetails = serviceDetails,
                                    isAdmin = isAdmin,
                                    users = users,
                                    selectedProfileId = selectedProfileId,
                                    onSelectedProfileIdChange = { selectedProfileId = it },
                                    selectedRootFolder = selectedRootFolder,
                                    onSelectedRootFolderChange = { selectedRootFolder = it },
                                    selectedUserId = selectedUserId,
                                    onSelectedUserIdChange = { selectedUserId = it },
                                    selectedSeasons = selectedSeasons,
                                    onSelectedSeasonsChange = { selectedSeasons = it },
                                    canRequest4k = buttonState.showRequest4kButton,
                                    is4k = is4k,
                                    onIs4kChange = { is4k = it },
                                    enabled = !isBusy,
                                )
                            }
                        } else {
                            if (arrMedia == null) {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                when (arrMedia) {
                                    is ArrSeries ->
                                        SeriesAddConfigurationContent(
                                            instances = addSheetUiState.availableInstances,
                                            selectedInstance = addSheetUiState.targetInstance,
                                            onInstanceSelected = { viewModel.setAddSheetTargetInstance(it) },
                                            qualityProfiles = effectiveQualityProfiles,
                                            qualityProfile = seriesQualityProfile,
                                            onQualityProfileChange = { seriesQualityProfile = it },
                                            rootFolders = effectiveRootFolders,
                                            rootFolder = seriesRootFolder,
                                            onRootFolderChange = { seriesRootFolder = it },
                                            tags = effectiveTags,
                                            selectedTags = seriesTags,
                                            monitor = seriesMonitor,
                                            onMonitorChange = { seriesMonitor = it },
                                            seriesType = seriesType,
                                            onSeriesTypeChange = { seriesType = it },
                                            seasonFolders = seriesSeasonFolders,
                                            onSeasonFoldersChange = { seriesSeasonFolders = it },
                                            searchOnAdd = seriesSearchOnAdd,
                                            onSearchOnAddChange = { seriesSearchOnAdd = it },
                                            enabled = !isBusy,
                                        )

                                    is ArrMovie ->
                                        MovieAddConfigurationContent(
                                            instances = addSheetUiState.availableInstances,
                                            selectedInstance = addSheetUiState.targetInstance,
                                            onInstanceSelected = { viewModel.setAddSheetTargetInstance(it) },
                                            qualityProfiles = effectiveQualityProfiles,
                                            qualityProfile = movieQualityProfile,
                                            onQualityProfileChange = { movieQualityProfile = it },
                                            rootFolders = effectiveRootFolders,
                                            rootFolder = movieRootFolder,
                                            onRootFolderChange = { movieRootFolder = it },
                                            tags = effectiveTags,
                                            selectedTags = movieTags,
                                            monitored = movieMonitored,
                                            onMonitoredChange = { movieMonitored = it },
                                            minimumAvailability = movieMinimumAvailability,
                                            onMinimumAvailabilityChange = { movieMinimumAvailability = it },
                                            searchOnAdd = movieSearchOnAdd,
                                            onSearchOnAddChange = { movieSearchOnAdd = it },
                                            enabled = !isBusy,
                                        )

                                    else -> {}
                                }
                            }
                        }
                    }
                }

                // Static Footer: Action Buttons
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isBusy,
                    ) {
                        Text(mokoString(MR.strings.cancel))
                    }

                    Button(
                        onClick = {
                            if (showMode == SheetMode.Request) {
                                val seasons = if (seerrMedia is TvDetails) selectedSeasons.toList() else null
                                viewModel.submitRequest(
                                    profileId = selectedProfileId,
                                    rootFolder = selectedRootFolder,
                                    languageProfileId = null,
                                    seasons = seasons,
                                    is4k = is4k,
                                    userId = selectedUserId,
                                )
                            } else {
                                when (arrMedia) {
                                    is ArrSeries -> {
                                        val qp = seriesQualityProfile
                                        val rf = seriesRootFolder
                                        if (qp != null && rf != null) {
                                            viewModel.updatePreferences(
                                                preferences.copy(
                                                    addSeriesMonitor = seriesMonitor,
                                                    addQualityProfileId = qp.id,
                                                    addSeriesType = seriesType,
                                                    addSeriesSeasonFolder = seriesSeasonFolders,
                                                    addRootFolderPath = rf.path,
                                                    addSearchOnAdd = seriesSearchOnAdd,
                                                ),
                                            )
                                            val newItem =
                                                arrMedia.copyForCreation(
                                                    monitor = seriesMonitor,
                                                    qualityProfileId = qp.id,
                                                    seriesType = seriesType,
                                                    seasonFolder = seriesSeasonFolders,
                                                    rootFolderPath = rf.path,
                                                    tags = seriesTags,
                                                )
                                            viewModel.smartAdd(newItem, seriesSearchOnAdd, addSheetUiState.targetInstance?.id)
                                        }
                                    }

                                    is ArrMovie -> {
                                        val qp = movieQualityProfile
                                        val rf = movieRootFolder
                                        if (qp != null && rf != null) {
                                            viewModel.updatePreferences(
                                                preferences.copy(
                                                    addMovieMonitored = movieMonitored,
                                                    addMovieMinimumAvailability = movieMinimumAvailability,
                                                    addQualityProfileId = qp.id,
                                                    addRootFolderPath = rf.path,
                                                    addSearchOnAdd = movieSearchOnAdd,
                                                ),
                                            )
                                            val newItem =
                                                arrMedia.copyForCreation(
                                                    monitored = movieMonitored,
                                                    minimumAvailability = movieMinimumAvailability,
                                                    qualityProfileId = qp.id,
                                                    rootFolderPath = rf.path,
                                                    tags = movieTags,
                                                )
                                            viewModel.smartAdd(newItem, movieSearchOnAdd, addSheetUiState.targetInstance?.id)
                                        }
                                    }

                                    else -> {}
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isActionEnabled,
                    ) {
                        if (isBusy) {
                            CircularProgressIndicator(Modifier.size(24.dp))
                        } else {
                            Crossfade(
                                targetState = showMode to is4k,
                                label = "ActionButtonCrossfade",
                            ) { (targetMode, is4kMode) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    if (targetMode == SheetMode.Request) {
                                        Text(
                                            if (is4kMode) {
                                                mokoString(MR.strings.request_in_4k)
                                            } else {
                                                mokoString(MR.strings.request)
                                            },
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(mokoString(MR.strings.save))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
