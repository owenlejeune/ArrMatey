package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.SeriesMonitorType
import com.dnfapps.arrmatey.arr.api.model.SeriesType
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.ui.components.MultiSelectDropdownPicker
import com.dnfapps.arrmatey.utils.mokoPlural
import com.dnfapps.arrmatey.utils.mokoString
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSeriesSheet(
    item: ArrSeries,
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
        AddSeriesSheetContent(
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
fun AddSeriesSheetContent(
    item: ArrSeries,
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
    var monitor by remember(preferences.addSeriesMonitor, selectedInstance?.id) { mutableStateOf(preferences.addSeriesMonitor) }
    var qualityProfile by remember(qualityProfiles, preferences.addQualityProfileId, selectedInstance?.id) {
        mutableStateOf(
            qualityProfiles.firstOrNull { it.id == preferences.addQualityProfileId }
                ?: qualityProfiles.firstOrNull(),
        )
    }
    var seriesType by remember(preferences.addSeriesType, selectedInstance?.id) { mutableStateOf(preferences.addSeriesType) }
    var seasonFolders by remember(
        preferences.addSeriesSeasonFolder,
        selectedInstance?.id,
    ) { mutableStateOf(preferences.addSeriesSeasonFolder) }
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
            Column {
                Text(
                    text = mokoString(MR.strings.type_series).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = item.title ?: "",
                    style = MaterialTheme.typography.headlineMediumEmphasized,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

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
                        label = { Text(mokoString(MR.strings.add_to_arr, instanceTypeName ?: "Sonarr")) },
                    )
                }
            }

            SeriesAddConfigurationContent(
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
                monitor = monitor,
                onMonitorChange = { monitor = it },
                seriesType = seriesType,
                onSeriesTypeChange = { seriesType = it },
                seasonFolders = seasonFolders,
                onSeasonFoldersChange = { seasonFolders = it },
                searchOnAdd = searchOnAdd,
                onSearchOnAddChange = { searchOnAdd = it },
                enabled = !addInProgress,
            )
        }

        Button(
            onClick = {
                val qp = qualityProfile
                val rf = rootFolder
                if (qp != null && rf != null) {
                    onUpdatePreferences(
                        preferences.copy(
                            addSeriesMonitor = monitor,
                            addQualityProfileId = qp.id,
                            addSeriesType = seriesType,
                            addSeriesSeasonFolder = seasonFolders,
                            addRootFolderPath = rf.path,
                            addSearchOnAdd = searchOnAdd,
                        ),
                    )
                    val newItem =
                        item.copyForCreation(
                            monitor = monitor,
                            qualityProfileId = qp.id,
                            seriesType = seriesType,
                            seasonFolder = seasonFolders,
                            rootFolderPath = rf.path,
                            tags = selectedTags,
                        )
                    onAddItem(newItem, searchOnAdd)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !addInProgress && qualityProfile != null && rootFolder != null,
        ) {
            if (addInProgress) {
                CircularProgressIndicator(Modifier.size(24.dp))
            } else {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                )
                Spacer(Modifier.width(8.dp))
                Text(text = mokoString(MR.strings.save))
            }
        }
    }
}

@Composable
fun SeriesAddConfigurationContent(
    instances: List<Instance>,
    selectedInstance: Instance?,
    onInstanceSelected: (Instance) -> Unit,
    qualityProfiles: List<QualityProfile>,
    qualityProfile: QualityProfile?,
    onQualityProfileChange: (QualityProfile?) -> Unit,
    rootFolders: List<RootFolder>,
    rootFolder: RootFolder?,
    onRootFolderChange: (RootFolder?) -> Unit,
    tags: List<Tag>,
    selectedTags: List<Int>,
    monitor: SeriesMonitorType,
    onMonitorChange: (SeriesMonitorType) -> Unit,
    seriesType: SeriesType,
    onSeriesTypeChange: (SeriesType) -> Unit,
    seasonFolders: Boolean,
    onSeasonFoldersChange: (Boolean) -> Unit,
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
            DropdownPicker(
                options =
                    SeriesMonitorType.entries.filter {
                        it != SeriesMonitorType.Unknown &&
                            it != SeriesMonitorType.LatestSeason &&
                            it != SeriesMonitorType.Skip
                    },
                modifier = Modifier.fillMaxWidth(),
                selectedOption = monitor,
                onOptionSelected = onMonitorChange,
                getOptionLabel = { mokoString(it.resource) },
                label = { Text(mokoString(MR.strings.monitor)) },
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
                options = SeriesType.entries,
                modifier = Modifier.fillMaxWidth(),
                selectedOption = seriesType,
                onOptionSelected = onSeriesTypeChange,
                getOptionLabel = { mokoString(it.resource) },
                label = { Text(mokoString(MR.strings.series_type)) },
                enabled = enabled,
            )

            if (tags.isNotEmpty()) {
                val mutableTags = selectedTags as? androidx.compose.runtime.snapshots.SnapshotStateList<Int>
                MultiSelectDropdownPicker(
                    options = tags.map { it.id },
                    selectedOptions =
                        mutableTags
                            ?: androidx.compose.runtime.remember(
                                selectedTags,
                            ) { androidx.compose.runtime.mutableStateListOf(*selectedTags.toTypedArray()) },
                    valueLabel = mokoPlural(MR.plurals.tag_count, selectedTags.size),
                    onOptionSelected = { tag, isSelected ->
                        if (mutableTags != null) {
                            if (isSelected) {
                                mutableTags.add(tag)
                            } else {
                                mutableTags.remove(tag)
                            }
                        }
                    },
                    getOptionLabel = { tag ->
                        tags.firstOrNull { tag == it.id }?.label
                            ?: mokoString(MR.strings.unknown)
                    },
                    label = { Text(mokoString(MR.strings.tags)) },
                    enabled = enabled,
                )
            }
        }

        ContainerCard(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LabelledSwitch(
                label = mokoString(MR.strings.season_folders),
                checked = seasonFolders,
                onCheckedChange = onSeasonFoldersChange,
                enabled = enabled,
            )

            if (rootFolders.size > 1) {
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

        ContainerCard(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
        ) {
            LabelledSwitch(
                label = mokoString(MR.strings.search_on_add_label),
                checked = searchOnAdd,
                onCheckedChange = onSearchOnAddChange,
                enabled = enabled,
            )
        }
    }
}
