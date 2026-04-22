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
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.MediaStatus
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
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun AddMovieSheet(
    item: ArrMovie,
    qualityProfiles: List<QualityProfile>,
    rootFolders: List<RootFolder>,
    tags: List<Tag>,
    addInProgress: Boolean,
    onAddItem: (ArrMedia, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var monitored by remember { mutableStateOf(true) }
    var minimumAvailability by remember { mutableStateOf(MediaStatus.Announced) }
    var qualityProfile by remember { mutableStateOf(qualityProfiles.first()) }
    var rootFolder by remember { mutableStateOf(rootFolders.first()) }
    val selectedTags = remember { mutableStateListOf<Int>() }
    var searchOnAdd by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LabelledSwitch(
                label = mokoString(MR.strings.monitored),
                checked = monitored,
                onCheckedChange = { monitored = it }
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
                options = listOf(
                    MediaStatus.Announced,
                    MediaStatus.InCinemas,
                    MediaStatus.Released
                ),
                modifier = Modifier.fillMaxWidth(),
                selectedOption = minimumAvailability,
                onOptionSelected = { minimumAvailability = it },
                getOptionLabel = { mokoString(it.resource) },
                label = { Text(mokoString(MR.strings.minimum_availability)) }
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
                    val newItem = item.copyForCreation(
                        monitored = monitored,
                        minimumAvailability = minimumAvailability,
                        qualityProfileId = qualityProfile.id,
                        rootFolderPath = rootFolder.path,
                        tags = selectedTags
                    )
                    onAddItem(newItem, searchOnAdd)
                },
                enabled = !addInProgress
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