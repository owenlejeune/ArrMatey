package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ComicVolume
import com.dnfapps.arrmatey.arr.api.model.MonitoringScheme
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.SpecialVersion
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.sheets.components.AddMediaActionButton
import com.dnfapps.arrmatey.ui.sheets.components.AddMediaSheetHeader
import com.dnfapps.arrmatey.ui.sheets.components.SearchOnAddCard
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddComicSheet(
    item: ComicVolume,
    rootFolders: List<RootFolder>,
    addInProgress: Boolean,
    preferences: InstancePreferences,
    onUpdatePreferences: (InstancePreferences) -> Unit,
    onAddItem: (ArrMedia, Boolean) -> Unit,
    onDismiss: () -> Unit,
    instances: List<Instance> = emptyList(),
    selectedInstance: Instance? = null,
    onInstanceSelected: (Instance) -> Unit = {},
) {
    ModalBottomSheet(
        onDismissRequest = {
            if (!addInProgress) {
                onDismiss()
            }
        },
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { !addInProgress },
        ),
    ) {
        AddComicSheetContent(
            item = item,
            rootFolders = rootFolders,
            addInProgress = addInProgress,
            preferences = preferences,
            onUpdatePreferences = onUpdatePreferences,
            onAddItem = onAddItem,
            onDismiss = onDismiss,
            instances = instances,
            selectedInstance = selectedInstance,
            onInstanceSelected = onInstanceSelected,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddComicSheetContent(
    item: ComicVolume,
    rootFolders: List<RootFolder>,
    addInProgress: Boolean,
    preferences: InstancePreferences,
    onUpdatePreferences: (InstancePreferences) -> Unit,
    onAddItem: (ArrMedia, Boolean) -> Unit,
    onDismiss: () -> Unit,
    instances: List<Instance> = emptyList(),
    selectedInstance: Instance? = null,
    onInstanceSelected: (Instance) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val fallbackVolumeLabel = mokoString(MR.strings.type_volume)

    var monitorVolume by remember(preferences.addKapowarrMonitorVolume, selectedInstance?.id) { mutableStateOf(preferences.addKapowarrMonitorVolume) }
    var monitorNewIssues by remember(preferences.addKapowarrMonitorNewIssues, selectedInstance?.id) { mutableStateOf(preferences.addKapowarrMonitorNewIssues) }

    var selectedMonitoringScheme by remember(preferences.addKapowarrMonitoringScheme, selectedInstance?.id) { mutableStateOf(preferences.addKapowarrMonitoringScheme) }
    var selectedSpecialVersion by remember(preferences.addKapowarrSpecialVersion, selectedInstance?.id) { mutableStateOf(preferences.addKapowarrSpecialVersion) }

    val defaultVolumeFolder = remember(item, fallbackVolumeLabel) {
        val volNum = item.volumeNumber?.let { if (it < 10) "0$it" else "$it" } ?: "01"
        "${item.title ?: fallbackVolumeLabel}/$fallbackVolumeLabel $volNum${item.year?.let { " ($it)" } ?: ""}"
    }
    var volumeFolder by remember(item, selectedInstance?.id) { mutableStateOf(item.folder ?: defaultVolumeFolder) }

    var rootFolder by remember(rootFolders, preferences.addRootFolderPath, selectedInstance?.id) {
        mutableStateOf(
            rootFolders.firstOrNull { it.path == preferences.addRootFolderPath }
                ?: rootFolders.firstOrNull(),
        )
    }

    var searchOnAdd by remember(preferences.addSearchOnAdd, selectedInstance?.id) {
        mutableStateOf(preferences.addSearchOnAdd)
    }

    Column(
        modifier = modifier
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
            AddMediaSheetHeader(
                type = mokoString(MR.strings.comics),
                title = listOfNotNull(item.title, item.year?.let { "($it)" }).joinToString(" "),
            )

            ComicAddConfigurationContent(
                instances = instances,
                selectedInstance = selectedInstance,
                onInstanceSelected = onInstanceSelected,
                rootFolders = rootFolders,
                rootFolder = rootFolder,
                onRootFolderChange = { rootFolder = it },
                volumeFolder = volumeFolder,
                onVolumeFolderChange = { volumeFolder = it },
                monitorVolume = monitorVolume,
                onMonitorVolumeChange = { monitorVolume = it },
                monitorNewIssues = monitorNewIssues,
                onMonitorNewIssuesChange = { monitorNewIssues = it },
                selectedMonitoringScheme = selectedMonitoringScheme,
                onMonitoringSchemeChange = { selectedMonitoringScheme = it },
                selectedSpecialVersion = selectedSpecialVersion,
                onSpecialVersionChange = { selectedSpecialVersion = it },
                searchOnAdd = searchOnAdd,
                onSearchOnAddChange = { searchOnAdd = it },
                enabled = !addInProgress,
            )
        }

        AddMediaActionButton(
            text = mokoString(MR.strings.save),
            onClick = {
                val rf = rootFolder
                if (rf != null) {
                    onUpdatePreferences(
                        preferences.copyWithKapowarrAddDefaults(
                            monitorVolume = monitorVolume,
                            monitorNewIssues = monitorNewIssues,
                            monitoringScheme = selectedMonitoringScheme,
                            specialVersion = selectedSpecialVersion,
                            rootFolderPath = rf.path,
                            searchOnAdd = searchOnAdd,
                        ),
                    )

                    val newItem = item.copy(
                        monitored = monitorVolume,
                        monitorNewIssues = monitorNewIssues,
                        folder = volumeFolder,
                        volumeFolder = volumeFolder,
                        specialVersion = selectedSpecialVersion.value,
                        monitoringScheme = selectedMonitoringScheme,
                        rootFolder = rf.id,
                        searchOnAdd = searchOnAdd,
                    )
                    onAddItem(newItem, searchOnAdd)
                }
            },
            isLoading = addInProgress,
            enabled = rootFolder != null && volumeFolder.isNotBlank(),
        )
    }
}

@Composable
fun ComicAddConfigurationContent(
    instances: List<Instance>,
    selectedInstance: Instance?,
    onInstanceSelected: (Instance) -> Unit,
    rootFolders: List<RootFolder>,
    rootFolder: RootFolder?,
    onRootFolderChange: (RootFolder?) -> Unit,
    volumeFolder: String,
    onVolumeFolderChange: (String) -> Unit,
    monitorVolume: Boolean,
    onMonitorVolumeChange: (Boolean) -> Unit,
    monitorNewIssues: Boolean,
    onMonitorNewIssuesChange: (Boolean) -> Unit,
    selectedMonitoringScheme: MonitoringScheme,
    onMonitoringSchemeChange: (MonitoringScheme) -> Unit,
    selectedSpecialVersion: SpecialVersion,
    onSpecialVersionChange: (SpecialVersion) -> Unit,
    searchOnAdd: Boolean,
    onSearchOnAddChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (instances.size > 1 && selectedInstance != null) {
            DropdownPicker(
                options = instances,
                selectedOption = selectedInstance,
                onOptionSelected = onInstanceSelected,
                getOptionLabel = { it.label },
                label = { Text(mokoString(MR.strings.instances)) },
                enabled = enabled,
            )
        }

        DropdownPicker(
            options = rootFolders,
            selectedOption = rootFolder,
            onOptionSelected = onRootFolderChange,
            getOptionLabel = { it.path },
            label = { Text(mokoString(MR.strings.root_folder)) },
            enabled = enabled,
        )

        OutlinedTextField(
            value = volumeFolder,
            onValueChange = onVolumeFolderChange,
            label = { Text(mokoString(MR.strings.volume_folder)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = enabled,
        )

        DropdownPicker(
            options = listOf(true, false),
            selectedOption = monitorVolume,
            onOptionSelected = onMonitorVolumeChange,
            getOptionLabel = { if (it) mokoString(MR.strings.yes) else mokoString(MR.strings.no) },
            label = { Text(mokoString(MR.strings.monitor_volume)) },
            enabled = enabled,
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            DropdownPicker(
                options = listOf(true, false),
                selectedOption = monitorNewIssues,
                onOptionSelected = onMonitorNewIssuesChange,
                getOptionLabel = { if (it) mokoString(MR.strings.yes) else mokoString(MR.strings.no) },
                label = { Text(mokoString(MR.strings.monitor_new_issues)) },
                enabled = enabled,
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
                onOptionSelected = onMonitoringSchemeChange,
                getOptionLabel = { mokoString(it.resource) },
                label = { Text(mokoString(MR.strings.monitoring_scheme)) },
                enabled = enabled,
            )
            Text(
                text = mokoString(MR.strings.monitoring_scheme_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp),
            )
        }

        DropdownPicker(
            options = SpecialVersion.entries,
            selectedOption = selectedSpecialVersion,
            onOptionSelected = onSpecialVersionChange,
            getOptionLabel = { mokoString(it.resource) },
            label = { Text(mokoString(MR.strings.special_version)) },
            enabled = enabled,
        )

        SearchOnAddCard(
            checked = searchOnAdd,
            onCheckedChange = onSearchOnAddChange,
            label = mokoString(MR.strings.search_missing_volume),
            enabled = enabled,
        )
    }
}
