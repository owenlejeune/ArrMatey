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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ComicVolume
import com.dnfapps.arrmatey.arr.api.model.MonitoringScheme
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.SpecialVersion
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditComicSheet(
    item: ComicVolume,
    rootFolders: List<RootFolder>,
    editInProgress: Boolean,
    onEditItem: (ArrMedia) -> Unit,
    onDismiss: () -> Unit,
) {
    val yesString = mokoString(MR.strings.yes)
    val noString = mokoString(MR.strings.no)

    var monitored by remember { mutableStateOf(item.monitored) }
    var monitorNewIssues by remember { mutableStateOf(item.monitorNewIssues) }
    var selectedMonitoringScheme by remember {
        mutableStateOf(
            MonitoringScheme.entries.firstOrNull { it.value.equals(item.monitoringScheme?.value, ignoreCase = true) }
                ?: MonitoringScheme.All,
        )
    }
    var selectedSpecialVersion by remember {
        mutableStateOf(
            SpecialVersion.entries.firstOrNull { it.value.equals(item.specialVersion, ignoreCase = true) }
                ?: SpecialVersion.Automatic,
        )
    }
    var volumeFolder by remember { mutableStateOf(item.volumeFolder ?: item.folder ?: "") }
    var rootFolder by remember {
        mutableStateOf(
            rootFolders.firstOrNull { it.id == item.rootFolder }
                ?: rootFolders.firstOrNull(),
        )
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (!editInProgress) {
                onDismiss()
            }
        },
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { !editInProgress },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = listOfNotNull(item.title, item.year?.let { "($it)" }).joinToString(" "),
                    style = MaterialTheme.typography.headlineMediumEmphasized,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                DropdownPicker(
                    options = listOf(true, false),
                    selectedOption = monitored,
                    onOptionSelected = { monitored = it },
                    getOptionLabel = { if (it) yesString else noString },
                    label = { Text(mokoString(MR.strings.monitor_volume)) },
                    enabled = !editInProgress,
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    DropdownPicker(
                        options = listOf(true, false),
                        selectedOption = monitorNewIssues,
                        onOptionSelected = { monitorNewIssues = it },
                        getOptionLabel = { if (it) yesString else noString },
                        label = { Text(mokoString(MR.strings.monitor_new_issues)) },
                        enabled = !editInProgress,
                    )
                    Text(
                        text = mokoString(MR.strings.monitor_new_issues_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    DropdownPicker(
                        options = MonitoringScheme.entries,
                        selectedOption = selectedMonitoringScheme,
                        onOptionSelected = { selectedMonitoringScheme = it },
                        getOptionLabel = { mokoString(it.resource) },
                        label = { Text(mokoString(MR.strings.monitoring_scheme)) },
                        enabled = !editInProgress,
                    )
                    Text(
                        text = mokoString(MR.strings.monitoring_scheme_edit_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }

                DropdownPicker(
                    options = rootFolders,
                    selectedOption = rootFolder,
                    onOptionSelected = { rootFolder = it },
                    getOptionLabel = { it.path },
                    label = { Text(mokoString(MR.strings.root_folder)) },
                    enabled = !editInProgress,
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = volumeFolder,
                        onValueChange = { volumeFolder = it },
                        label = { Text(mokoString(MR.strings.volume_folder)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !editInProgress,
                    )
                    Text(
                        text = mokoString(MR.strings.volume_folder_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    DropdownPicker(
                        options = SpecialVersion.entries,
                        selectedOption = selectedSpecialVersion,
                        onOptionSelected = { selectedSpecialVersion = it },
                        getOptionLabel = { mokoString(it.resource) },
                        label = { Text(mokoString(MR.strings.special_version)) },
                        enabled = !editInProgress,
                    )
                    Text(
                        text = mokoString(MR.strings.special_version_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }

            Button(
                onClick = {
                    val updatedItem = item.copy(
                        monitored = monitored,
                        monitorNewIssues = monitorNewIssues,
                        folder = volumeFolder,
                        volumeFolder = volumeFolder,
                        specialVersion = selectedSpecialVersion.value,
                        monitoringScheme = selectedMonitoringScheme,
                        rootFolder = rootFolder?.id,
                    )
                    onEditItem(updatedItem)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !editInProgress && rootFolder != null,
            ) {
                if (editInProgress) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(mokoString(MR.strings.save))
                }
            }
        }
    }
}
