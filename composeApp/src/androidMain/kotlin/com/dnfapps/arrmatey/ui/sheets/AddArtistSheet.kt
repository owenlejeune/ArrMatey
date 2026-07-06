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
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.ArtistMonitorType
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.ui.components.MultiSelectDropdownPicker
import com.dnfapps.arrmatey.utils.mokoPlural
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddArtistSheet(
    item: Arrtist,
    qualityProfiles: List<QualityProfile>,
    rootFolders: List<RootFolder>,
    tags: List<Tag>,
    addInProgress: Boolean,
    preferences: InstancePreferences,
    onUpdatePreferences: (InstancePreferences) -> Unit,
    onAddItem: (ArrMedia, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var monitor by remember(preferences.addArtistMonitor) { mutableStateOf(preferences.addArtistMonitor) }
    var qualityProfile by remember(qualityProfiles, preferences.addQualityProfileId) {
        mutableStateOf(
            qualityProfiles.firstOrNull { it.id == preferences.addQualityProfileId }
                ?: qualityProfiles.firstOrNull()
        )
    }
    var monitorNew by remember(preferences.addArtistMonitorNew) { mutableStateOf(preferences.addArtistMonitorNew) }
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
                options = ArtistMonitorType.entries.toList(),
                modifier = Modifier.fillMaxWidth(),
                selectedOption = monitor,
                onOptionSelected = { monitor = it },
                getOptionLabel = { mokoString(it.resource) },
                label = { Text(mokoString(MR.strings.monitor)) }
            )

            DropdownPicker(
                options = listOf(
                    ArtistMonitorType.All,
                    ArtistMonitorType.None,
                    ArtistMonitorType.Future
                ),
                modifier = Modifier.fillMaxWidth(),
                selectedOption = monitorNew,
                onOptionSelected = { monitorNew = it },
                getOptionLabel = { mokoString(it.resource) },
                label = { Text(mokoString(MR.strings.monitor_new_albums)) }
            )

            DropdownPicker(
                options = qualityProfiles,
                modifier = Modifier.fillMaxWidth(),
                selectedOption = qualityProfile,
                onOptionSelected = { qualityProfile = it },
                getOptionLabel = { it.name ?: "" },
                label = { Text(mokoString(MR.strings.quality_profile)) }
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
                                addArtistMonitor = monitor,
                                addArtistMonitorNew = monitorNew,
                                addQualityProfileId = qp.id,
                                addRootFolderPath = rf.path,
                                addSearchOnAdd = searchOnAdd
                            )
                        )
                        val newItem = item.copyForCreation(
                            monitor = monitor,
                            monitorNew = monitorNew,
                            qualityProfileId = qp.id,
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