package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
import com.dnfapps.arrmatey.arr.api.model.defaultForAudiobook
import com.dnfapps.arrmatey.arr.api.model.defaultForEbook
import com.dnfapps.arrmatey.arr.api.model.filterForAudiobook
import com.dnfapps.arrmatey.arr.api.model.filterForEbook
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
    initialMediaType: BookMediaType? = null,
) {
    val isChaptarr = item.isChaptarr

    val isAudiobookConfiguredInitially = item.hasAudiobookConfigured
    val isEbookConfiguredInitially = item.hasEbookConfigured

    var audiobookEnabled by remember {
        mutableStateOf(if (!isAudiobookConfiguredInitially && !isEbookConfiguredInitially) true else isAudiobookConfiguredInitially)
    }
    var ebookEnabled by remember {
        mutableStateOf(if (!isAudiobookConfiguredInitially && !isEbookConfiguredInitially) false else isEbookConfiguredInitially)
    }

    val chaptarrMonitorOptions = AuthorMonitorType.chaptarrOptions

    // Chaptarr state
    val initialTab = initialMediaType ?: if (item.hasEbookConfigured && !item.hasAudiobookConfigured) BookMediaType.EBook else BookMediaType.Audiobook
    var chaptarrSelectedTab by remember { mutableStateOf(initialTab) }

    var audiobookRootFolder by remember(rootFolders, item.audiobookRootFolderPath) {
        mutableStateOf(rootFolders.defaultForAudiobook(item.audiobookRootFolderPath))
    }
    var audiobookMonitor by remember(item.audiobookMonitorExisting) {
        mutableStateOf(
            item.audiobookMonitorExisting?.let { AuthorMonitorType.fromChaptarrIndex(it) }
                ?: if (item.monitored && item.hasAudiobookConfigured) AuthorMonitorType.All else AuthorMonitorType.None,
        )
    }
    var audiobookMonitorNew by remember(item.audiobookMonitorFuture) {
        mutableStateOf(item.audiobookMonitorFuture ?: false)
    }
    var audiobookQualityProfile by remember(qualityProfiles, item.audiobookQualityProfileId) {
        mutableStateOf(qualityProfiles.defaultForAudiobook(item.audiobookQualityProfileId))
    }
    var audiobookMetadataProfile by remember(metadataProfiles, item.audiobookMetadataProfileId) {
        mutableStateOf(metadataProfiles.defaultForAudiobook(item.audiobookMetadataProfileId))
    }
    val selectedAudiobookTags = remember { (item.audiobookTags.ifEmpty { item.tags }).toMutableStateList() }

    var ebookRootFolder by remember(rootFolders, item.ebookRootFolderPath) {
        mutableStateOf(rootFolders.defaultForEbook(item.ebookRootFolderPath))
    }
    var ebookMonitor by remember(item.ebookMonitorExisting) {
        mutableStateOf(
            item.ebookMonitorExisting?.let { AuthorMonitorType.fromChaptarrIndex(it) }
                ?: if (item.monitored && item.hasEbookConfigured) AuthorMonitorType.All else AuthorMonitorType.None,
        )
    }
    var ebookMonitorNew by remember(item.ebookMonitorFuture) {
        mutableStateOf(item.ebookMonitorFuture ?: false)
    }
    var ebookQualityProfile by remember(qualityProfiles, item.ebookQualityProfileId) {
        mutableStateOf(qualityProfiles.defaultForEbook(item.ebookQualityProfileId))
    }
    var ebookMetadataProfile by remember(metadataProfiles, item.ebookMetadataProfileId) {
        mutableStateOf(metadataProfiles.defaultForEbook(item.ebookMetadataProfileId))
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
                        if (!audiobookEnabled) {
                            ContainerCard(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                                shape = MaterialTheme.shapes.large,
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = mokoString(MR.strings.audiobooks_not_configured),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = mokoString(MR.strings.audiobooks_not_configured_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Button(
                                    onClick = { audiobookEnabled = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !editInProgress,
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(mokoString(MR.strings.configure_audiobooks))
                                }
                            }
                        } else {
                            if (!isAudiobookConfiguredInitially) {
                                ContainerCard(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    shape = MaterialTheme.shapes.large,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                        Text(
                                            text = mokoString(MR.strings.saving_will_add_audiobook_support),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        )
                                    }
                                }
                            }

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
                                qualityProfiles = remember(qualityProfiles) {
                                    qualityProfiles.filterForAudiobook()
                                },
                                selectedQualityProfile = audiobookQualityProfile,
                                onQualityProfileSelected = { audiobookQualityProfile = it },
                                qualityProfileLabel = mokoString(MR.strings.audiobook_quality_profile),
                                metadataProfiles = remember(metadataProfiles) {
                                    metadataProfiles.filterForAudiobook()
                                },
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

                            if (ebookEnabled) {
                                OutlinedButton(
                                    onClick = { audiobookEnabled = false },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !editInProgress,
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(mokoString(MR.strings.remove_audiobooks))
                                }
                            }
                        }
                    } else {
                        if (!ebookEnabled) {
                            ContainerCard(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                                shape = MaterialTheme.shapes.large,
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = mokoString(MR.strings.ebooks_not_configured),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = mokoString(MR.strings.ebooks_not_configured_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Button(
                                    onClick = { ebookEnabled = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !editInProgress,
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(mokoString(MR.strings.configure_ebooks))
                                }
                            }
                        } else {
                            if (!isEbookConfiguredInitially) {
                                ContainerCard(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    shape = MaterialTheme.shapes.large,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                        Text(
                                            text = mokoString(MR.strings.saving_will_add_ebook_support),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        )
                                    }
                                }
                            }

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
                                qualityProfiles = remember(qualityProfiles) {
                                    qualityProfiles.filterForEbook()
                                },
                                selectedQualityProfile = ebookQualityProfile,
                                onQualityProfileSelected = { ebookQualityProfile = it },
                                qualityProfileLabel = mokoString(MR.strings.ebook_quality_profile),
                                metadataProfiles = remember(metadataProfiles) {
                                    metadataProfiles.filterForEbook()
                                },
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

                            if (audiobookEnabled) {
                                OutlinedButton(
                                    onClick = { ebookEnabled = false },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !editInProgress,
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(mokoString(MR.strings.remove_ebooks))
                                }
                            }
                        }
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
                            audiobookQualityProfileId = if (audiobookEnabled) audiobookQualityProfile?.id else null,
                            audiobookMetadataProfileId = if (audiobookEnabled) audiobookMetadataProfile?.id else null,
                            audiobookRootFolderPath = if (audiobookEnabled) audiobookRootFolder?.path else null,
                            audiobookMonitorExisting = if (audiobookEnabled) AuthorMonitorType.toChaptarrIndex(audiobookMonitor) else null,
                            audiobookMonitorFuture = if (audiobookEnabled) audiobookMonitorNew else null,
                            audiobookTags = if (audiobookEnabled) selectedAudiobookTags.toList() else emptyList(),
                            ebookQualityProfileId = if (ebookEnabled) ebookQualityProfile?.id else null,
                            ebookMetadataProfileId = if (ebookEnabled) ebookMetadataProfile?.id else null,
                            ebookRootFolderPath = if (ebookEnabled) ebookRootFolder?.path else null,
                            ebookMonitorExisting = if (ebookEnabled) AuthorMonitorType.toChaptarrIndex(ebookMonitor) else null,
                            ebookMonitorFuture = if (ebookEnabled) ebookMonitorNew else null,
                            ebookTags = if (ebookEnabled) selectedEbookTags.toList() else emptyList(),
                            tags = ((if (audiobookEnabled) selectedAudiobookTags else emptyList()) + (if (ebookEnabled) selectedEbookTags else emptyList())).distinct(),
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
                enabled = !editInProgress && (!isChaptarr || audiobookEnabled || ebookEnabled),
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
