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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.AMOutlinedTextField
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAudiobookSheet(
    item: SearchAudiobook,
    qualityProfiles: List<QualityProfile>,
    rootFolders: List<RootFolder>,
    relativePath: String,
    addInProgress: Boolean,
    preferences: InstancePreferences,
    onUpdatePreferences: (InstancePreferences) -> Unit,
    onAddItem: (ArrMedia, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var monitored by remember(preferences.addAudiobookMonitored) { mutableStateOf(preferences.addAudiobookMonitored) }
    var qualityProfile by remember(qualityProfiles, preferences.addQualityProfileId) {
        mutableStateOf(
            qualityProfiles.firstOrNull { it.id == preferences.addQualityProfileId }
                ?: qualityProfiles.firstOrNull()
        )
    }
    var rootFolder by remember(rootFolders, preferences.addRootFolderPath) {
        mutableStateOf(
            rootFolders.firstOrNull { it.path == preferences.addRootFolderPath }
                ?: rootFolders.firstOrNull { it.isDefault }
                ?: rootFolders.firstOrNull()
        )
    }
    var relativePath by remember { mutableStateOf(relativePath) }
    var searchOnAdd by remember(preferences.addSearchOnAdd) { mutableStateOf(preferences.addSearchOnAdd) }

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
                label = { Text(mokoString(MR.strings.quality_profile)) },
                unknownValueLabel = mokoString(MR.strings.default_label)
            )

            DropdownPicker(
                options = rootFolders,
                modifier = Modifier.fillMaxWidth(),
                selectedOption = rootFolder,
                onOptionSelected = { rootFolder = it },
                label = { Text(mokoString(MR.strings.root_folder)) },
                getOptionLabel = {
                    buildString {
                        append(it.path)
                        if (it.isDefault) {
                            append(" (${mokoString(MR.strings.default_label)})")
                        }
                    }
                }
            )

            AMOutlinedTextField(
                value = relativePath,
                onValueChange = { relativePath = it },
                modifier = Modifier.fillMaxWidth(),
                label = mokoString(MR.strings.relative_path)
            )

            LabelledSwitch(
                label = mokoString(MR.strings.search_on_add_label),
                checked = searchOnAdd,
                onCheckedChange = { searchOnAdd = it }
            )

            Button(
                onClick = {
                    val rf = rootFolder
                    if (rf != null) {
                        onUpdatePreferences(
                            preferences.copy(
                                addAudiobookMonitored = monitored,
                                addQualityProfileId = qualityProfile?.id,
                                addRootFolderPath = rf.path,
                                addSearchOnAdd = searchOnAdd
                            )
                        )
                        val newItem = item.copyForCreation(
                            monitored = monitored,
                            qualityProfileId = qualityProfile?.id ?: 0,
                            rootFolderPath = rf.path,
                            relativePath = relativePath
                        )
                        onAddItem(newItem, searchOnAdd)
                    }
                },
                enabled = !addInProgress && rootFolder != null
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