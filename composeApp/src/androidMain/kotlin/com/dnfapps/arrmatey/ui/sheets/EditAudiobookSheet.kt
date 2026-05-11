package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.ui.components.MultiSelectDropdownPicker
import com.dnfapps.arrmatey.utils.mokoPlural
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAudiobookSheet(
    item: Audiobook,
    qualityProfiles: List<QualityProfile>,
    rootFolders: List<RootFolder>,
    tags: List<Tag>,
    editInProgress: Boolean,
    onEditItem: (Audiobook) -> Unit,
    onDismiss: () -> Unit
) {
    var monitored by remember { mutableStateOf(item.monitored) }
    var selectedQualityProfileId by remember { mutableStateOf(item.qualityProfileId) }
    var selectedRootFolderPath by remember { mutableStateOf(item.path ?: "") }
    val selectedTags = remember { item.tags.toMutableStateList() }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LabelledSwitch(
                label = mokoString(MR.strings.monitored),
                checked = monitored,
                onCheckedChange = { monitored = it }
            )

            DropdownPicker(
                options = qualityProfiles,
                modifier = Modifier.fillMaxWidth(),
                selectedOption = qualityProfiles.firstOrNull { it.id == selectedQualityProfileId } ?: qualityProfiles.firstOrNull(),
                onOptionSelected = { selectedQualityProfileId = it.id },
                getOptionLabel = { it.name ?: "" },
                label = { Text(mokoString(MR.strings.quality_profile)) }
            )

            if (rootFolders.isNotEmpty()) {
                DropdownPicker(
                    options = rootFolders,
                    modifier = Modifier.fillMaxWidth(),
                    selectedOption = rootFolders.firstOrNull { it.path == selectedRootFolderPath } ?: rootFolders.firstOrNull(),
                    onOptionSelected = { selectedRootFolderPath = it.path },
                    getOptionLabel = { it.path },
                    label = { Text(mokoString(MR.strings.root_folder)) }
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

            Button(
                onClick = {
                    onEditItem(
                        item.copy(
                            monitored = monitored,
                            qualityProfileId = selectedQualityProfileId,
                            tags = selectedTags.toList(),
                            basePath = selectedRootFolderPath
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !editInProgress
            ) {
                Text(mokoString(MR.strings.save))
            }
        }
    }
}
