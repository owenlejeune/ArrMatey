package com.dnfapps.arrmatey.ui.sheets.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.AuthorMonitorType
import com.dnfapps.arrmatey.arr.api.model.BookMediaType
import com.dnfapps.arrmatey.arr.api.model.MetadataProfile
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.LabelledCheckbox
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChaptarrMediaTypeSelector(
    selectedMediaType: BookMediaType,
    onMediaTypeSelected: (BookMediaType) -> Unit,
    onSetAsDefault: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            val mediaOptions = listOf(BookMediaType.Audiobook, BookMediaType.Both, BookMediaType.EBook)
            mediaOptions.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = selectedMediaType == option,
                    onClick = { onMediaTypeSelected(option) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = mediaOptions.size),
                    label = {
                        Text(
                            text = when (option) {
                                BookMediaType.Audiobook -> mokoString(MR.strings.audiobooks)
                                BookMediaType.Both -> mokoString(MR.strings.both)
                                BookMediaType.EBook -> mokoString(MR.strings.ebooks)
                            },
                        )
                    },
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                onClick = onSetAsDefault,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
            ) {
                Text(
                    text = mokoString(MR.strings.set_as_default),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
fun ChaptarrMediaSection(
    title: String?,
    rootFolders: List<RootFolder>,
    selectedRootFolder: RootFolder?,
    onRootFolderSelected: (RootFolder) -> Unit,
    rootFolderLabel: String,
    monitorOptions: List<AuthorMonitorType>,
    selectedMonitor: AuthorMonitorType,
    onMonitorSelected: (AuthorMonitorType) -> Unit,
    monitorLabel: String,
    monitorNew: Boolean,
    onMonitorNewChanged: (Boolean) -> Unit,
    monitorNewLabel: String,
    qualityProfiles: List<QualityProfile>,
    selectedQualityProfile: QualityProfile?,
    onQualityProfileSelected: (QualityProfile) -> Unit,
    qualityProfileLabel: String,
    metadataProfiles: List<MetadataProfile>,
    selectedMetadataProfile: MetadataProfile?,
    onMetadataProfileSelected: (MetadataProfile) -> Unit,
    metadataProfileLabel: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    ContainerCard(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }

        if (rootFolders.isNotEmpty()) {
            DropdownPicker(
                options = rootFolders,
                modifier = Modifier.fillMaxWidth(),
                selectedOption = selectedRootFolder,
                onOptionSelected = onRootFolderSelected,
                label = { Text(rootFolderLabel) },
                getOptionLabel = { "${it.path} (${it.freeSpace.bytesAsFileSizeString()})" },
                enabled = enabled,
            )
        }

        DropdownPicker(
            options = monitorOptions,
            modifier = Modifier.fillMaxWidth(),
            selectedOption = selectedMonitor,
            onOptionSelected = onMonitorSelected,
            getOptionLabel = { mokoString(it.resource) },
            label = { Text(monitorLabel) },
            enabled = enabled,
        )

        LabelledCheckbox(
            label = monitorNewLabel,
            checked = monitorNew,
            onCheckedChange = onMonitorNewChanged,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
        )

        if (qualityProfiles.isNotEmpty()) {
            DropdownPicker(
                options = qualityProfiles,
                modifier = Modifier.fillMaxWidth(),
                selectedOption = selectedQualityProfile,
                onOptionSelected = onQualityProfileSelected,
                getOptionLabel = { it.name ?: "" },
                label = { Text(qualityProfileLabel) },
                enabled = enabled,
            )
        }

        if (metadataProfiles.isNotEmpty()) {
            DropdownPicker(
                options = metadataProfiles,
                modifier = Modifier.fillMaxWidth(),
                selectedOption = selectedMetadataProfile,
                onOptionSelected = onMetadataProfileSelected,
                getOptionLabel = { it.name ?: "" },
                label = { Text(metadataProfileLabel) },
                enabled = enabled,
            )
        }
    }
}
