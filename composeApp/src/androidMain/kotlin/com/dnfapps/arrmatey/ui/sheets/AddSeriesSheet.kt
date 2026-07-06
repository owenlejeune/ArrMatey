package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
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
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.SeriesMonitorType
import com.dnfapps.arrmatey.arr.api.model.SeriesType
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.ui.components.MultiSelectDropdownPicker
import com.dnfapps.arrmatey.utils.mokoPlural
import com.dnfapps.arrmatey.utils.mokoString
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)
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
    onDismiss: () -> Unit
) {
    var monitor by remember(preferences.addSeriesMonitor) { mutableStateOf(preferences.addSeriesMonitor) }
    var qualityProfile by remember(qualityProfiles, preferences.addQualityProfileId) {
        mutableStateOf(
            qualityProfiles.firstOrNull { it.id == preferences.addQualityProfileId }
                ?: qualityProfiles.firstOrNull()
        )
    }
    var seriesType by remember(preferences.addSeriesType) { mutableStateOf(preferences.addSeriesType) }
    var seasonFolders by remember(preferences.addSeriesSeasonFolder) { mutableStateOf(preferences.addSeriesSeasonFolder) }
    var rootFolder by remember(rootFolders, preferences.addRootFolderPath) {
        mutableStateOf(
            rootFolders.firstOrNull { it.path == preferences.addRootFolderPath }
                ?: rootFolders.firstOrNull()
        )
    }
    val selectedTags = remember { mutableStateListOf<Int>() }
    var searchOnAdd by remember(preferences.addSearchOnAdd) { mutableStateOf(preferences.addSearchOnAdd) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DropdownPicker(
                options = SeriesMonitorType.entries.filter {
                    it != SeriesMonitorType.Unknown &&
                            it != SeriesMonitorType.LatestSeason &&
                            it != SeriesMonitorType.Skip
                },
                modifier = Modifier.fillMaxWidth(),
                selectedOption = monitor,
                onOptionSelected = { monitor = it },
                getOptionLabel = { mokoString(it.resource) },
                label = { Text(mokoString(MR.strings.monitor)) }
            )

            LabelledSwitch(
                label = mokoString(MR.strings.season_folders),
                checked = seasonFolders,
                onCheckedChange = { seasonFolders = it }
            )

            DropdownPicker(
                options = qualityProfiles,
                modifier = Modifier.fillMaxWidth(),
                selectedOption = qualityProfile,
                onOptionSelected = { qualityProfile = it },
                getOptionLabel = { it.name ?: "" },
                label = { Text(mokoString(MR.strings.quality_profile)) }
            )

            DropdownPicker(
                options = SeriesType.entries,
                modifier = Modifier.fillMaxWidth(),
                selectedOption = seriesType,
                onOptionSelected = { seriesType = it },
                getOptionLabel = { mokoString(it.resource) },
                label = { Text(mokoString(MR.strings.series_type)) }
            )

            if (tags.isNotEmpty()) {
                MultiSelectDropdownPicker(
                    options = tags.map { it.id },
                    selectedOptions = selectedTags,
                    valueLabel = mokoPlural(MR.plurals.tag_count, selectedTags.size),
                    onOptionSelected = { tag, isSelected ->
                        if (isSelected) {
                            selectedTags.add(tag)
                        } else {
                            selectedTags.remove(tag)
                        }
                    },
                    getOptionLabel = { tag ->
                        tags.firstOrNull { tag == it.id }?.label
                            ?: mokoString(MR.strings.unknown)
                    },
                    label = { Text(mokoString(MR.strings.tags)) }
                )
            }

            if (rootFolders.size > 1) {
                DropdownPicker(
                    options = rootFolders,
                    modifier = Modifier.fillMaxWidth(),
                    selectedOption = rootFolder,
                    onOptionSelected = { rootFolder = it },
                    label = { Text(mokoString(MR.strings.root_folder)) },
                    getOptionLabel = { "${it.path} (${it.freeSpace.bytesAsFileSizeString()})" }
                )
            }

            LabelledSwitch(
                label = mokoString(MR.strings.search_on_add_label),
                checked = searchOnAdd,
                onCheckedChange = { searchOnAdd = it }
            )

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
                                addSearchOnAdd = searchOnAdd
                            )
                        )
                        val newItem = item.copyForCreation(
                            monitor = monitor,
                            qualityProfileId = qp.id,
                            seriesType = seriesType,
                            seasonFolder = seasonFolders,
                            rootFolderPath = rf.path,
                            tags = selectedTags
                        )
                        onAddItem(newItem, searchOnAdd)
                    }
                },
                enabled = !addInProgress && qualityProfile != null && rootFolder != null
            ) {
                if (addInProgress) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null
                    )
                    Text(
                        text = mokoString(MR.strings.save)
                    )
                }
            }
        }
    }
}