package com.dnfapps.arrmatey.ui.components.unifiedmedia.sheets

import androidx.compose.runtime.Composable
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.arr.api.model.toSearchAudiobook
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.model.AddSheetUiState
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.ui.sheets.AddArtistSheet
import com.dnfapps.arrmatey.ui.sheets.AddAudiobookSheet
import com.dnfapps.arrmatey.ui.sheets.AddAuthorSheet
import com.dnfapps.arrmatey.ui.sheets.AddMovieSheet
import com.dnfapps.arrmatey.ui.sheets.AddSeriesSheet

@Composable
fun AddMediaSheetsHost(
    visible: Boolean,
    state: UnifiedMediaDetailsUiState.Success,
    addSheetUiState: AddSheetUiState,
    qualityProfiles: List<QualityProfile>,
    rootFolders: List<RootFolder>,
    tags: List<Tag>,
    addItemStatus: OperationStatus,
    preferences: InstancePreferences,
    onInstanceSelected: (Instance) -> Unit,
    onSmartAdd: (ArrMedia, Boolean, Long?) -> Unit,
    onUpdatePreferences: (InstancePreferences) -> Unit,
    onDismiss: () -> Unit,
    canSwitchToRequest: Boolean = false,
    instanceTypeName: String? = null,
    onSwitchToRequest: (() -> Unit)? = null,
) {
    if (!visible) return

    val arrMedia = state.arrMedia ?: return
    val effectiveQualityProfiles = addSheetUiState.qualityProfiles.ifEmpty { qualityProfiles }
    val effectiveRootFolders = addSheetUiState.rootFolders.ifEmpty { rootFolders }
    val effectiveTags = addSheetUiState.tags.ifEmpty { tags }
    val isAddInProgress = addItemStatus is OperationStatus.InProgress

    when (arrMedia) {
        is ArrSeries ->
            AddSeriesSheet(
                item = arrMedia,
                qualityProfiles = effectiveQualityProfiles,
                rootFolders = effectiveRootFolders,
                tags = effectiveTags,
                addInProgress = isAddInProgress,
                preferences = preferences,
                instances = addSheetUiState.availableInstances,
                selectedInstance = addSheetUiState.targetInstance,
                onInstanceSelected = onInstanceSelected,
                onAddItem = { newItem, searchOnAdd ->
                    onSmartAdd(newItem, searchOnAdd, addSheetUiState.targetInstance?.id)
                },
                onUpdatePreferences = onUpdatePreferences,
                onDismiss = onDismiss,
                canSwitchToRequest = canSwitchToRequest,
                instanceTypeName = instanceTypeName,
                onSwitchToRequest = onSwitchToRequest,
            )

        is ArrMovie ->
            AddMovieSheet(
                item = arrMedia,
                qualityProfiles = effectiveQualityProfiles,
                rootFolders = effectiveRootFolders,
                tags = effectiveTags,
                addInProgress = isAddInProgress,
                preferences = preferences,
                instances = addSheetUiState.availableInstances,
                selectedInstance = addSheetUiState.targetInstance,
                onInstanceSelected = onInstanceSelected,
                onAddItem = { newItem, searchOnAdd ->
                    onSmartAdd(newItem, searchOnAdd, addSheetUiState.targetInstance?.id)
                },
                onUpdatePreferences = onUpdatePreferences,
                onDismiss = onDismiss,
                canSwitchToRequest = canSwitchToRequest,
                instanceTypeName = instanceTypeName,
                onSwitchToRequest = onSwitchToRequest,
            )

        is Arrtist ->
            AddArtistSheet(
                item = arrMedia,
                qualityProfiles = effectiveQualityProfiles,
                rootFolders = effectiveRootFolders,
                tags = effectiveTags,
                addInProgress = isAddInProgress,
                preferences = preferences,
                instances = addSheetUiState.availableInstances,
                selectedInstance = addSheetUiState.targetInstance,
                onInstanceSelected = onInstanceSelected,
                onAddItem = { newItem, searchOnAdd ->
                    onSmartAdd(newItem, searchOnAdd, addSheetUiState.targetInstance?.id)
                },
                onUpdatePreferences = onUpdatePreferences,
                onDismiss = onDismiss,
            )

        is Author ->
            AddAuthorSheet(
                item = arrMedia,
                qualityProfiles = effectiveQualityProfiles,
                rootFolders = effectiveRootFolders,
                tags = effectiveTags,
                addInProgress = isAddInProgress,
                preferences = preferences,
                instances = addSheetUiState.availableInstances,
                selectedInstance = addSheetUiState.targetInstance,
                onInstanceSelected = onInstanceSelected,
                onAddItem = { newItem, searchOnAdd ->
                    onSmartAdd(newItem, searchOnAdd, addSheetUiState.targetInstance?.id)
                },
                onUpdatePreferences = onUpdatePreferences,
                onDismiss = onDismiss,
            )

        is SearchAudiobook ->
            AddAudiobookSheet(
                item = arrMedia,
                qualityProfiles = effectiveQualityProfiles,
                rootFolders = effectiveRootFolders,
                relativePath = "",
                addInProgress = isAddInProgress,
                preferences = preferences,
                instances = addSheetUiState.availableInstances,
                selectedInstance = addSheetUiState.targetInstance,
                onInstanceSelected = onInstanceSelected,
                onAddItem = { newItem, searchOnAdd ->
                    onSmartAdd(newItem, searchOnAdd, addSheetUiState.targetInstance?.id)
                },
                onUpdatePreferences = onUpdatePreferences,
                onDismiss = onDismiss,
            )

        is Audiobook -> {
            AddAudiobookSheet(
                item = arrMedia.toSearchAudiobook(),
                qualityProfiles = effectiveQualityProfiles,
                rootFolders = effectiveRootFolders,
                relativePath = "",
                addInProgress = isAddInProgress,
                preferences = preferences,
                instances = addSheetUiState.availableInstances,
                selectedInstance = addSheetUiState.targetInstance,
                onInstanceSelected = onInstanceSelected,
                onAddItem = { newItem, searchOnAdd ->
                    onSmartAdd(newItem, searchOnAdd, addSheetUiState.targetInstance?.id)
                },
                onUpdatePreferences = onUpdatePreferences,
                onDismiss = onDismiss,
            )
        }

        else -> {}
    }
}
