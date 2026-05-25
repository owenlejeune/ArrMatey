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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.AMOutlinedTextField
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAudiobookSheet(
    item: Audiobook,
    qualityProfiles: List<QualityProfile>,
    rootFolders: List<RootFolder>,
    editInProgress: Boolean,
    onEditItem: (Audiobook) -> Unit,
    onDismiss: () -> Unit
) {
    var monitored by remember { mutableStateOf(item.monitored) }
    var selectedQualityProfileId by remember { mutableIntStateOf(item.qualityProfileId) }
    var selectedRootFolder by remember { mutableStateOf(rootFolders.first { item.path?.startsWith(it.path) == true }) }
    var relativePath by remember { mutableStateOf(item.path?.removePrefix(selectedRootFolder.path) ?: "") }

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
                    selectedOption = selectedRootFolder,
                    onOptionSelected = { selectedRootFolder = it },
                    getOptionLabel = { it.path },
                    label = { Text(mokoString(MR.strings.root_folder)) }
                )
            }

            AMOutlinedTextField(
                value = relativePath,
                onValueChange = { relativePath = it },
                modifier = Modifier.fillMaxWidth(),
                label = mokoString(MR.strings.relative_path)
            )

            Button(
                onClick = {
                    onEditItem(
                        item.copyForEdit(
                            monitored = monitored,
                            qualityProfileId = selectedQualityProfileId,
                            rootFolderPath = selectedRootFolder.path,
                            relativePath = relativePath
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
