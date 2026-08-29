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
import androidx.compose.material3.MaterialTheme
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
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPathSheet(
    item: ArrMedia,
    rootFolders: List<RootFolder>,
    editInProgress: Boolean,
    onEditItem: (ArrMedia, moveFiles: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val initialRootFolder =
        remember(item, rootFolders) {
            item.findCurrentRoot(rootFolders) ?: rootFolders.firstOrNull()
        }

    var selectedRootFolder by remember { mutableStateOf(initialRootFolder) }

    val isPathChanged = selectedRootFolder?.path != initialRootFolder?.path
    var moveFiles by remember { mutableStateOf(value = false) }

    ModalBottomSheet(
        onDismissRequest = {
            if (!editInProgress) {
                onDismiss()
            }
        },
        sheetState =
            rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { !editInProgress },
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = mokoString(MR.strings.edit_path),
                style = MaterialTheme.typography.titleLarge,
            )

            selectedRootFolder?.let { folder ->
                DropdownPicker(
                    options = rootFolders,
                    modifier = Modifier.fillMaxWidth(),
                    selectedOption = folder,
                    onOptionSelected = { selectedRootFolder = it },
                    label = { Text(mokoString(MR.strings.root_folder)) },
                    getOptionLabel = { "${it.path} (${it.freeSpace.bytesAsFileSizeString()})" },
                    enabled = !editInProgress,
                )
            }

            LabelledSwitch(
                label = mokoString(MR.strings.move_files),
                checked = moveFiles,
                onCheckedChange = { moveFiles = it },
                enabled = !editInProgress && isPathChanged,
            )

            Button(
                onClick = {
                    val rootFolderPath = selectedRootFolder?.path ?: ""
                    val updatedItem = item.withNewRoot(rootFolderPath, initialRootFolder?.path)
                    onEditItem(updatedItem, moveFiles)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !editInProgress && isPathChanged,
            ) {
                if (editInProgress) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                    )
                    Text(mokoString(MR.strings.save))
                }
            }
        }
    }
}
