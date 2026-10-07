package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.MediaStatus
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.ui.sheets.components.AddMediaActionButton
import com.dnfapps.arrmatey.ui.sheets.components.AddMediaSheetHeader
import com.dnfapps.arrmatey.ui.sheets.components.SearchOnAddCard
import com.dnfapps.arrmatey.ui.sheets.components.TagsSelectionCard
import com.dnfapps.arrmatey.utils.mokoString
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMovieSheet(
    item: ArrMovie,
    qualityProfiles: List<QualityProfile>,
    rootFolders: List<RootFolder>,
    tags: List<Tag>,
    addInProgress: Boolean,
    preferences: InstancePreferences,
    onUpdatePreferences: (InstancePreferences) -> Unit,
    onAddItem: (ArrMedia, Boolean) -> Unit,
    onDismiss: () -> Unit,
    instances: List<Instance> = emptyList(),
    selectedInstance: Instance? = null,
    onInstanceSelected: (Instance) -> Unit = {},
    canSwitchToRequest: Boolean = false,
    instanceTypeName: String? = null,
    onSwitchToRequest: (() -> Unit)? = null,
) {
    ModalBottomSheet(
        onDismissRequest = {
            if (!addInProgress) {
                onDismiss()
            }
        },
        sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { !addInProgress },
        ),
    ) {
        AddMovieSheetContent(
            item = item,
            qualityProfiles = qualityProfiles,
            rootFolders = rootFolders,
            tags = tags,
            addInProgress = addInProgress,
            preferences = preferences,
            onUpdatePreferences = onUpdatePreferences,
            onAddItem = onAddItem,
            onDismiss = onDismiss,
            instances = instances,
            selectedInstance = selectedInstance,
            onInstanceSelected = onInstanceSelected,
            canSwitchToRequest = canSwitchToRequest,
            instanceTypeName = instanceTypeName,
            onSwitchToRequest = onSwitchToRequest,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalTime::class)
@Composable
fun AddMovieSheetContent(
    item: ArrMovie,
    qualityProfiles: List<QualityProfile>,
    rootFolders: List<RootFolder>,
    tags: List<Tag>,
    addInProgress: Boolean,
    preferences: InstancePreferences,
    onUpdatePreferences: (InstancePreferences) -> Unit,
    onAddItem: (ArrMedia, Boolean) -> Unit,
    onDismiss: () -> Unit,
    instances: List<Instance> = emptyList(),
    selectedInstance: Instance? = null,
    onInstanceSelected: (Instance) -> Unit = {},
    canSwitchToRequest: Boolean = false,
    instanceTypeName: String? = null,
    onSwitchToRequest: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var monitored by remember(preferences.addMovieMonitored, selectedInstance?.id) { mutableStateOf(preferences.addMovieMonitored) }
    var minimumAvailability by remember(preferences.addMovieMinimumAvailability, selectedInstance?.id) {
        mutableStateOf(preferences.addMovieMinimumAvailability)
    }
    var qualityProfile by remember(qualityProfiles, preferences.addQualityProfileId, selectedInstance?.id) {
        mutableStateOf(
            qualityProfiles.firstOrNull { it.id == preferences.addQualityProfileId }
                ?: qualityProfiles.firstOrNull(),
        )
    }
    var rootFolder by remember(rootFolders, preferences.addRootFolderPath, selectedInstance?.id) {
        mutableStateOf(
            rootFolders.firstOrNull { it.path == preferences.addRootFolderPath }
                ?: rootFolders.firstOrNull(),
        )
    }
    val selectedTags = remember(selectedInstance?.id) { mutableStateListOf<Int>() }
    var searchOnAdd by remember(preferences.addSearchOnAdd, selectedInstance?.id) { mutableStateOf(preferences.addSearchOnAdd) }

    Column(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AddMediaSheetHeader(
                type = mokoString(MR.strings.type_movie),
                title = item.title ?: "",
            )

            if (canSwitchToRequest && onSwitchToRequest != null) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SegmentedButton(
                        selected = false,
                        onClick = onSwitchToRequest,
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        label = { Text(mokoString(MR.strings.request)) },
                    )
                    SegmentedButton(
                        selected = true,
                        onClick = {},
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        label = { Text(mokoString(MR.strings.add_to_arr, instanceTypeName ?: "Radarr")) },
                    )
                }
            }

            MovieAddConfigurationContent(
                instances = instances,
                selectedInstance = selectedInstance,
                onInstanceSelected = onInstanceSelected,
                qualityProfiles = qualityProfiles,
                qualityProfile = qualityProfile,
                onQualityProfileChange = { qualityProfile = it },
                rootFolders = rootFolders,
                rootFolder = rootFolder,
                onRootFolderChange = { rootFolder = it },
                tags = tags,
                selectedTags = selectedTags,
                monitored = monitored,
                onMonitoredChange = { monitored = it },
                minimumAvailability = minimumAvailability,
                onMinimumAvailabilityChange = { minimumAvailability = it },
                searchOnAdd = searchOnAdd,
                onSearchOnAddChange = { searchOnAdd = it },
                enabled = !addInProgress,
            )
        }

        AddMediaActionButton(
            text = mokoString(MR.strings.save),
            onClick = {
                val qp = qualityProfile
                val rf = rootFolder
                if (qp != null && rf != null) {
                    onUpdatePreferences(
                        preferences.copyWithMovieAddDefaults(
                            monitored = monitored,
                            minAvailability = minimumAvailability,
                            qualityProfileId = qp.id,
                            rootFolderPath = rf.path,
                            searchOnAdd = searchOnAdd,
                        ),
                    )
                    val newItem =
                        item.copyForCreation(
                            monitored = monitored,
                            minimumAvailability = minimumAvailability,
                            qualityProfileId = qp.id,
                            rootFolderPath = rf.path,
                            tags = selectedTags,
                        )
                    onAddItem(newItem, searchOnAdd)
                }
            },
            isLoading = addInProgress,
            enabled = qualityProfile != null && rootFolder != null,
        )
    }
}

@Composable
fun MovieAddConfigurationContent(
    instances: List<Instance>,
    selectedInstance: Instance?,
    onInstanceSelected: (Instance) -> Unit,
    qualityProfiles: List<QualityProfile>,
    qualityProfile: QualityProfile?,
    onQualityProfileChange: (QualityProfile) -> Unit,
    rootFolders: List<RootFolder>,
    rootFolder: RootFolder?,
    onRootFolderChange: (RootFolder) -> Unit,
    tags: List<Tag>,
    selectedTags: List<Int>,
    monitored: Boolean,
    onMonitoredChange: (Boolean) -> Unit,
    minimumAvailability: MediaStatus,
    onMinimumAvailabilityChange: (MediaStatus) -> Unit,
    searchOnAdd: Boolean,
    onSearchOnAddChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (instances.size > 1 && selectedInstance != null) {
            DropdownPicker(
                options = instances,
                modifier = Modifier.fillMaxWidth(),
                selectedOption = selectedInstance,
                onOptionSelected = onInstanceSelected,
                getOptionLabel = { it.label },
                label = { Text(mokoString(MR.strings.instances)) },
                enabled = enabled,
            )
        }

        ContainerCard(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LabelledSwitch(
                label = mokoString(MR.strings.monitored),
                checked = monitored,
                onCheckedChange = onMonitoredChange,
                enabled = enabled,
            )

            DropdownPicker(
                options = qualityProfiles,
                modifier = Modifier.fillMaxWidth(),
                selectedOption = qualityProfile,
                onOptionSelected = onQualityProfileChange,
                getOptionLabel = { it.name ?: "" },
                label = { Text(mokoString(MR.strings.quality_profile)) },
                enabled = enabled,
            )

            DropdownPicker(
                options =
                listOf(
                    MediaStatus.Announced,
                    MediaStatus.InCinemas,
                    MediaStatus.Released,
                ),
                modifier = Modifier.fillMaxWidth(),
                selectedOption = minimumAvailability,
                onOptionSelected = onMinimumAvailabilityChange,
                getOptionLabel = { mokoString(it.resource) },
                label = { Text(mokoString(MR.strings.minimum_availability)) },
                enabled = enabled,
            )

            val mutableTags = (selectedTags as? androidx.compose.runtime.snapshots.SnapshotStateList<Int>)
                ?: androidx.compose.runtime.remember(selectedTags) {
                    androidx.compose.runtime.mutableStateListOf(*selectedTags.toTypedArray())
                }
            TagsSelectionCard(
                tags = tags,
                selectedTags = mutableTags,
                enabled = enabled,
            )
        }

        if (rootFolders.size > 1) {
            ContainerCard(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            ) {
                DropdownPicker(
                    options = rootFolders,
                    modifier = Modifier.fillMaxWidth(),
                    selectedOption = rootFolder,
                    onOptionSelected = onRootFolderChange,
                    label = { Text(mokoString(MR.strings.root_folder)) },
                    getOptionLabel = { "${it.path} (${it.freeSpace.bytesAsFileSizeString()})" },
                    enabled = enabled,
                )
            }
        }

        SearchOnAddCard(
            checked = searchOnAdd,
            onCheckedChange = onSearchOnAddChange,
            enabled = enabled,
        )
    }
}
