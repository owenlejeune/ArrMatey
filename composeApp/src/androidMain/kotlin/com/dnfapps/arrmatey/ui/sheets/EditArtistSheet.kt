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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.ArtistMonitorType
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.ui.components.MultiSelectDropdownPicker
import com.dnfapps.arrmatey.utils.mokoPlural
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditArtistSheet(
    item: Arrtist,
    qualityProfiles: List<QualityProfile>,
    rootFolders: List<RootFolder>,
    tags: List<Tag>,
    editInProgress: Boolean,
    onEditItem: (ArrMedia) -> Unit,
    onDismiss: () -> Unit
) {
    var monitor by remember { mutableStateOf(item.monitored) }
    var monitorNewAlbums by remember { mutableStateOf(item.monitorNewItems) }
    var qualityProfileId by remember { mutableIntStateOf(item.qualityProfileId) }
    var rootFolder by remember { mutableStateOf(item.rootFolderPath) }
    val selectedTags = remember { item.tags.toMutableStateList() }

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
            LabelledSwitch(
                label = mokoString(MR.strings.monitored),
                checked = monitor,
                onCheckedChange = { monitor = it }
            )

            DropdownPicker(
                options = listOf(
                    ArtistMonitorType.All,
                    ArtistMonitorType.None,
                    ArtistMonitorType.Future
                ),
                modifier = Modifier.fillMaxWidth(),
                selectedOption = monitorNewAlbums,
                onOptionSelected = { monitorNewAlbums = it },
                getOptionLabel = { mokoString(it.resource) },
                label = { Text(mokoString(MR.strings.monitor_new_albums)) }
            )

            qualityProfiles
                .firstOrNull { it.id == qualityProfileId }
                ?.let { profile ->
                    DropdownPicker(
                        options = qualityProfiles,
                        modifier = Modifier.fillMaxWidth(),
                        selectedOption = profile,
                        onOptionSelected = { qualityProfileId = it.id },
                        getOptionLabel = { it.name ?: "" },
                        label = { Text(mokoString(MR.strings.quality_profile)) }
                    )
                }

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
                rootFolders
                    .firstOrNull { it.path == rootFolder }
                    ?.let { folder ->
                        DropdownPicker(
                            options = rootFolders,
                            modifier = Modifier.fillMaxWidth(),
                            selectedOption = folder,
                            onOptionSelected = { rootFolder = it.path },
                            label = { Text(mokoString(MR.strings.root_folder)) },
                            getOptionLabel = { "${it.path} (${it.freeSpace.bytesAsFileSizeString()})" }
                        )
                    }
            }

            Button(
                onClick = {
                    val newItem = item.copyForEdit(
                        monitored = monitor,
                        monitorNew = monitorNewAlbums,
                        qualityProfileId = qualityProfileId,
                        rootFolderPath = rootFolder,
                        tags = selectedTags
                    )
                    onEditItem(newItem)
                },
                enabled = !editInProgress
            ) {
                if (editInProgress) {
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