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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.AuthorMonitorType
import com.dnfapps.arrmatey.arr.api.model.BookMediaType
import com.dnfapps.arrmatey.arr.api.model.MetadataProfile
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.ui.components.MultiSelectDropdownPicker
import com.dnfapps.arrmatey.ui.sheets.components.ChaptarrMediaSection
import com.dnfapps.arrmatey.ui.sheets.components.TagsSelectionCard
import com.dnfapps.arrmatey.utils.mokoPlural
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditAuthorSheet(
    item: Author,
    qualityProfiles: List<QualityProfile>,
    metadataProfiles: List<MetadataProfile> = emptyList(),
    rootFolders: List<RootFolder>,
    tags: List<Tag>,
    editInProgress: Boolean,
    onEditItem: (ArrMedia) -> Unit,
    onDismiss: () -> Unit,
) {
    val isChaptarr = item.isChaptarr

    val chaptarrMonitorOptions = remember {
        listOf(
            AuthorMonitorType.None,
            AuthorMonitorType.All,
            AuthorMonitorType.Future,
            AuthorMonitorType.Missing,
            AuthorMonitorType.Existing,
            AuthorMonitorType.FirstBook,
            AuthorMonitorType.LatestBook,
        )
    }

    // Chaptarr state
    val initialTab = if (item.hasEbookConfigured && !item.hasAudiobookConfigured) BookMediaType.EBook else BookMediaType.Audiobook
    var chaptarrSelectedTab by remember { mutableStateOf(initialTab) }

    var audiobookRootFolder by remember(rootFolders, item.audiobookRootFolderPath) {
        mutableStateOf(
            rootFolders.firstOrNull { it.path == item.audiobookRootFolderPath }
                ?: rootFolders.firstOrNull { it.path.contains("audio", ignoreCase = true) }
                ?: rootFolders.firstOrNull(),
        )
    }
    var audiobookMonitor by remember(item.audiobookMonitorExisting) {
        mutableStateOf(
            item.audiobookMonitorExisting?.let { chaptarrMonitorOptions.getOrNull(it) }
                ?: if (item.monitored && item.hasAudiobookConfigured) AuthorMonitorType.All else AuthorMonitorType.None,
        )
    }
    var audiobookMonitorNew by remember(item.audiobookMonitorFuture) {
        mutableStateOf(item.audiobookMonitorFuture ?: false)
    }
    var audiobookQualityProfile by remember(qualityProfiles, item.audiobookQualityProfileId) {
        mutableStateOf(
            qualityProfiles.firstOrNull { it.id == item.audiobookQualityProfileId }
                ?: qualityProfiles.firstOrNull { it.name?.contains("audio", ignoreCase = true) == true }
                ?: qualityProfiles.firstOrNull(),
        )
    }
    var audiobookMetadataProfile by remember(metadataProfiles, item.audiobookMetadataProfileId) {
        mutableStateOf(
            metadataProfiles.firstOrNull { it.id == item.audiobookMetadataProfileId }
                ?: metadataProfiles.firstOrNull { it.name?.contains("audio", ignoreCase = true) == true }
                ?: metadataProfiles.firstOrNull(),
        )
    }
    val selectedAudiobookTags = remember { (item.audiobookTags.ifEmpty { item.tags }).toMutableStateList() }

    var ebookRootFolder by remember(rootFolders, item.ebookRootFolderPath) {
        mutableStateOf(
            rootFolders.firstOrNull { it.path == item.ebookRootFolderPath }
                ?: rootFolders.firstOrNull { !it.path.contains("audio", ignoreCase = true) }
                ?: rootFolders.firstOrNull(),
        )
    }
    var ebookMonitor by remember(item.ebookMonitorExisting) {
        mutableStateOf(
            item.ebookMonitorExisting?.let { chaptarrMonitorOptions.getOrNull(it) }
                ?: if (item.monitored && item.hasEbookConfigured) AuthorMonitorType.All else AuthorMonitorType.None,
        )
    }
    var ebookMonitorNew by remember(item.ebookMonitorFuture) {
        mutableStateOf(item.ebookMonitorFuture ?: false)
    }
    var ebookQualityProfile by remember(qualityProfiles, item.ebookQualityProfileId) {
        mutableStateOf(
            qualityProfiles.firstOrNull { it.id == item.ebookQualityProfileId }
                ?: qualityProfiles.firstOrNull { it.name?.contains("ebook", ignoreCase = true) == true }
                ?: qualityProfiles.firstOrNull { it.name?.contains("audio", ignoreCase = true) != true }
                ?: qualityProfiles.firstOrNull(),
        )
    }
    var ebookMetadataProfile by remember(metadataProfiles, item.ebookMetadataProfileId) {
        mutableStateOf(
            metadataProfiles.firstOrNull { it.id == item.ebookMetadataProfileId }
                ?: metadataProfiles.firstOrNull { it.name?.contains("ebook", ignoreCase = true) == true }
                ?: metadataProfiles.firstOrNull { it.name?.contains("audio", ignoreCase = true) != true }
                ?: metadataProfiles.firstOrNull(),
        )
    }
    val selectedEbookTags = remember { (item.ebookTags.ifEmpty { item.tags }).toMutableStateList() }

    // Standard Readarr state
    var monitor by remember { mutableStateOf(item.monitored) }
    var monitorNewBooks by remember { mutableStateOf(item.monitorNewItems) }
    var qualityProfileId by remember { mutableIntStateOf(item.qualityProfileId) }
    var rootFolder by remember { mutableStateOf(item.rootFolderPath) }
    val selectedTags = remember { item.tags.toMutableStateList() }

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
            Column(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = item.authorNameLastFirst ?: item.title ?: "",
                    style = MaterialTheme.typography.headlineMediumEmphasized,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (isChaptarr) {
                    val mediaOptions = listOf(BookMediaType.Audiobook, BookMediaType.EBook)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        mediaOptions.forEachIndexed { index, option ->
                            SegmentedButton(
                                selected = chaptarrSelectedTab == option,
                                onClick = { chaptarrSelectedTab = option },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = mediaOptions.size),
                                label = {
                                    Text(
                                        text = when (option) {
                                            BookMediaType.Audiobook -> mokoString(MR.strings.audiobooks)
                                            BookMediaType.EBook -> mokoString(MR.strings.ebooks)
                                            else -> ""
                                        },
                                    )
                                },
                            )
                        }
                    }

                    if (chaptarrSelectedTab == BookMediaType.Audiobook) {
                        ChaptarrMediaSection(
                            title = null,
                            rootFolders = rootFolders,
                            selectedRootFolder = audiobookRootFolder,
                            onRootFolderSelected = { audiobookRootFolder = it },
                            rootFolderLabel = mokoString(MR.strings.audiobook_root_folder),
                            monitorOptions = chaptarrMonitorOptions,
                            selectedMonitor = audiobookMonitor,
                            onMonitorSelected = { audiobookMonitor = it },
                            monitorLabel = mokoString(MR.strings.monitor_authors_audiobooks),
                            monitorNew = audiobookMonitorNew,
                            onMonitorNewChanged = { audiobookMonitorNew = it },
                            monitorNewLabel = mokoString(MR.strings.monitor_new_audiobooks),
                            qualityProfiles = qualityProfiles,
                            selectedQualityProfile = audiobookQualityProfile,
                            onQualityProfileSelected = { audiobookQualityProfile = it },
                            qualityProfileLabel = mokoString(MR.strings.audiobook_quality_profile),
                            metadataProfiles = metadataProfiles,
                            selectedMetadataProfile = audiobookMetadataProfile,
                            onMetadataProfileSelected = { audiobookMetadataProfile = it },
                            metadataProfileLabel = mokoString(MR.strings.audiobook_metadata_profile),
                            enabled = !editInProgress,
                        )

                        TagsSelectionCard(
                            tags = tags,
                            selectedTags = selectedAudiobookTags,
                            enabled = !editInProgress,
                        )
                    } else {
                        ChaptarrMediaSection(
                            title = null,
                            rootFolders = rootFolders,
                            selectedRootFolder = ebookRootFolder,
                            onRootFolderSelected = { ebookRootFolder = it },
                            rootFolderLabel = mokoString(MR.strings.ebook_root_folder),
                            monitorOptions = chaptarrMonitorOptions,
                            selectedMonitor = ebookMonitor,
                            onMonitorSelected = { ebookMonitor = it },
                            monitorLabel = mokoString(MR.strings.monitor_authors_ebooks),
                            monitorNew = ebookMonitorNew,
                            onMonitorNewChanged = { ebookMonitorNew = it },
                            monitorNewLabel = mokoString(MR.strings.monitor_new_ebooks),
                            qualityProfiles = qualityProfiles,
                            selectedQualityProfile = ebookQualityProfile,
                            onQualityProfileSelected = { ebookQualityProfile = it },
                            qualityProfileLabel = mokoString(MR.strings.ebook_quality_profile),
                            metadataProfiles = metadataProfiles,
                            selectedMetadataProfile = ebookMetadataProfile,
                            onMetadataProfileSelected = { ebookMetadataProfile = it },
                            metadataProfileLabel = mokoString(MR.strings.ebook_metadata_profile),
                            enabled = !editInProgress,
                        )

                        TagsSelectionCard(
                            tags = tags,
                            selectedTags = selectedEbookTags,
                            enabled = !editInProgress,
                        )
                    }
                } else {
                    ContainerCard(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        LabelledSwitch(
                            label = mokoString(MR.strings.monitored),
                            checked = monitor,
                            onCheckedChange = { monitor = it },
                            enabled = !editInProgress,
                        )

                        DropdownPicker(
                            options =
                            listOf(
                                AuthorMonitorType.All,
                                AuthorMonitorType.None,
                                AuthorMonitorType.Future,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            selectedOption = monitorNewBooks,
                            onOptionSelected = { monitorNewBooks = it },
                            getOptionLabel = { mokoString(it.resource) },
                            label = { Text(mokoString(MR.strings.monitor_new_books)) },
                            enabled = !editInProgress,
                        )

                        qualityProfiles
                            .firstOrNull { it.id == qualityProfileId }
                            ?.let { profile ->
                                DropdownPicker(
                                    options = qualityProfiles,
                                    modifier = Modifier.fillMaxWidth(),
                                    selectedOption = profile,
                                    onOptionSelected = { qualityProfileId = it.id },
                                    getOptionLabel = { it.name ?: "" },
                                    label = { Text(mokoString(MR.strings.quality_profile)) },
                                    enabled = !editInProgress,
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
                                label = { Text(mokoString(MR.strings.tags)) },
                                enabled = !editInProgress,
                            )
                        }
                    }

                    if (rootFolders.size > 1) {
                        ContainerCard(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            rootFolders
                                .firstOrNull { it.path == rootFolder }
                                ?.let { folder ->
                                    DropdownPicker(
                                        options = rootFolders,
                                        modifier = Modifier.fillMaxWidth(),
                                        selectedOption = folder,
                                        onOptionSelected = { rootFolder = it.path },
                                        label = { Text(mokoString(MR.strings.root_folder)) },
                                        getOptionLabel = { "${it.path} (${it.freeSpace.bytesAsFileSizeString()})" },
                                        enabled = !editInProgress,
                                    )
                                }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (isChaptarr) {
                        val newItem = item.copyForChaptarrEdit(
                            audiobookQualityProfileId = audiobookQualityProfile?.id,
                            audiobookMetadataProfileId = audiobookMetadataProfile?.id,
                            audiobookRootFolderPath = audiobookRootFolder?.path,
                            audiobookMonitorExisting = chaptarrMonitorOptions.indexOf(audiobookMonitor).takeIf { it >= 0 },
                            audiobookMonitorFuture = audiobookMonitorNew,
                            audiobookTags = selectedAudiobookTags.toList(),
                            ebookQualityProfileId = ebookQualityProfile?.id,
                            ebookMetadataProfileId = ebookMetadataProfile?.id,
                            ebookRootFolderPath = ebookRootFolder?.path,
                            ebookMonitorExisting = chaptarrMonitorOptions.indexOf(ebookMonitor).takeIf { it >= 0 },
                            ebookMonitorFuture = ebookMonitorNew,
                            ebookTags = selectedEbookTags.toList(),
                            tags = (selectedAudiobookTags + selectedEbookTags).distinct(),
                        )
                        onEditItem(newItem)
                    } else {
                        val newItem =
                            item.copyForEdit(
                                monitored = monitor,
                                monitorNew = monitorNewBooks,
                                qualityProfileId = qualityProfileId,
                                rootFolderPath = rootFolder,
                                tags = selectedTags,
                            )
                        onEditItem(newItem)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !editInProgress,
            ) {
                if (editInProgress) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = mokoString(MR.strings.save))
                }
            }
        }
    }
}
