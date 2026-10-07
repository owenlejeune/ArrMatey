package com.dnfapps.arrmatey.ui.sheets.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.AuthorMonitorType
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun ReadarrAddAuthorForm(
    monitor: AuthorMonitorType,
    onMonitorSelected: (AuthorMonitorType) -> Unit,
    monitorNewBooks: AuthorMonitorType,
    onMonitorNewBooksSelected: (AuthorMonitorType) -> Unit,
    qualityProfiles: List<QualityProfile>,
    selectedQualityProfile: QualityProfile?,
    onQualityProfileSelected: (QualityProfile) -> Unit,
    rootFolders: List<RootFolder>,
    selectedRootFolder: RootFolder?,
    onRootFolderSelected: (RootFolder) -> Unit,
    tags: List<Tag>,
    selectedTags: SnapshotStateList<Int>,
    searchOnAdd: Boolean,
    onSearchOnAddChanged: (Boolean) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ContainerCard(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DropdownPicker(
                options = AuthorMonitorType.entries.toList(),
                modifier = Modifier.fillMaxWidth(),
                selectedOption = monitor,
                onOptionSelected = onMonitorSelected,
                getOptionLabel = { mokoString(it.resource) },
                label = { Text(mokoString(MR.strings.monitor)) },
                enabled = enabled,
            )

            DropdownPicker(
                options = listOf(
                    AuthorMonitorType.All,
                    AuthorMonitorType.None,
                    AuthorMonitorType.Future,
                ),
                modifier = Modifier.fillMaxWidth(),
                selectedOption = monitorNewBooks,
                onOptionSelected = onMonitorNewBooksSelected,
                getOptionLabel = { mokoString(it.resource) },
                label = { Text(mokoString(MR.strings.monitor_new_books)) },
                enabled = enabled,
            )

            if (qualityProfiles.isNotEmpty()) {
                DropdownPicker(
                    options = qualityProfiles,
                    modifier = Modifier.fillMaxWidth(),
                    selectedOption = selectedQualityProfile,
                    onOptionSelected = onQualityProfileSelected,
                    getOptionLabel = { it.name ?: "" },
                    label = { Text(mokoString(MR.strings.quality_profile)) },
                    enabled = enabled,
                )
            }

            TagsSelectionCard(
                tags = tags,
                selectedTags = selectedTags,
                enabled = enabled,
            )
        }

        if (rootFolders.size > 1) {
            ContainerCard(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            ) {
                DropdownPicker(
                    options = rootFolders,
                    modifier = Modifier.fillMaxWidth(),
                    selectedOption = selectedRootFolder,
                    onOptionSelected = onRootFolderSelected,
                    label = { Text(mokoString(MR.strings.root_folder)) },
                    getOptionLabel = { "${it.path} (${it.freeSpace.bytesAsFileSizeString()})" },
                    enabled = enabled,
                )
            }
        }

        SearchOnAddCard(
            checked = searchOnAdd,
            onCheckedChange = onSearchOnAddChanged,
            label = mokoString(MR.strings.search_on_add_label),
            enabled = enabled,
        )
    }
}
