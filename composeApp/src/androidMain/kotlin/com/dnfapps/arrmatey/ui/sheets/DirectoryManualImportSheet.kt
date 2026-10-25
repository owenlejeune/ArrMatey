package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ManualImportFile
import com.dnfapps.arrmatey.arr.api.model.ManualImportMode
import com.dnfapps.arrmatey.arr.viewmodel.ActivityQueueViewModel
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shared.browse_folder
import com.dnfapps.arrmatey.shared.cancel
import com.dnfapps.arrmatey.shared.error_enter_folder_path
import com.dnfapps.arrmatey.shared.filter_existing_files
import com.dnfapps.arrmatey.shared.filter_existing_files_description
import com.dnfapps.arrmatey.shared.folder_path
import com.dnfapps.arrmatey.shared.folder_path_placeholder
import com.dnfapps.arrmatey.shared.import_mode
import com.dnfapps.arrmatey.shared.interactive_import
import com.dnfapps.arrmatey.shared.manual_import
import com.dnfapps.arrmatey.shared.manual_import_directory_description
import com.dnfapps.arrmatey.shared.service
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectoryManualImportSheet(
    instances: List<Instance>,
    initialInstanceId: Long? = null,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: ActivityQueueViewModel = koinViewModel(),
    moko: MokoStrings = koinInject(),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedInstanceId by remember {
        mutableLongStateOf(initialInstanceId ?: instances.firstOrNull()?.id ?: 0L)
    }
    var folderPath by remember { mutableStateOf("") }
    var importMode by remember { mutableStateOf(ManualImportMode.Move) }
    var filterExisting by remember { mutableStateOf(true) }
    var showFilesystemBrowser by remember { mutableStateOf(false) }
    var isWorking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var scannedFiles by remember { mutableStateOf<List<ManualImportFile>?>(null) }

    val selectedInstance = instances.firstOrNull { it.id == selectedInstanceId }

    val currentScannedFiles = scannedFiles
    if (currentScannedFiles != null) {
        InteractiveImportSheet(
            instance = selectedInstance,
            folderPath = folderPath,
            importMode = importMode,
            initialFiles = currentScannedFiles,
            onDismiss = {
                scannedFiles = null
                onDismiss()
            },
            onSuccess = {
                onSuccess()
                onDismiss()
            },
            viewModel = viewModel,
        )
    } else {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = mokoString(MR.strings.manual_import),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = mokoString(MR.strings.manual_import_directory_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (instances.isNotEmpty()) {
                    DropdownPicker(
                        options = instances,
                        selectedOption = selectedInstance,
                        onOptionSelected = { selectedInstanceId = it.id },
                        label = { Text(mokoString(MR.strings.service)) },
                        getOptionLabel = { it.label },
                        enabled = instances.size > 1,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                OutlinedTextField(
                    value = folderPath,
                    onValueChange = { folderPath = it },
                    label = { Text(mokoString(MR.strings.folder_path)) },
                    placeholder = { Text(mokoString(MR.strings.folder_path_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showFilesystemBrowser = true }) {
                            Icon(Icons.Default.Folder, contentDescription = mokoString(MR.strings.browse_folder))
                        }
                    },
                )

                val importModes = ManualImportMode.entries
                DropdownPicker(
                    options = importModes,
                    selectedOption = importMode,
                    onOptionSelected = { importMode = it },
                    label = { Text(mokoString(MR.strings.import_mode)) },
                    getOptionLabel = { mokoString(it.label) },
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mokoString(MR.strings.filter_existing_files),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = mokoString(MR.strings.filter_existing_files_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = filterExisting,
                        onCheckedChange = { filterExisting = it },
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text(mokoString(MR.strings.cancel))
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val trimmed = folderPath.trim()
                            if (trimmed.isBlank()) {
                                errorMessage = moko.getString(MR.strings.error_enter_folder_path)
                                return@Button
                            }
                            isWorking = true
                            errorMessage = null
                            viewModel.scanManualImportFolder(
                                instanceId = selectedInstanceId,
                                folderPath = trimmed,
                                filterExisting = filterExisting,
                                onSuccess = { files ->
                                    isWorking = false
                                    scannedFiles = files
                                },
                                onError = { msg ->
                                    isWorking = false
                                    errorMessage = msg
                                },
                            )
                        },
                        enabled = folderPath.isNotBlank() && !isWorking,
                    ) {
                        if (isWorking) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text(mokoString(MR.strings.interactive_import))
                        }
                    }
                }
            }
        }
    }

    if (showFilesystemBrowser) {
        RemoteFilesystemBrowserSheet(
            instanceId = selectedInstanceId,
            initialPath = folderPath.takeIf { it.isNotBlank() },
            onSelectPath = { folderPath = it },
            onDismiss = { showFilesystemBrowser = false },
        )
    }
}
