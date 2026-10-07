package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.sheets.components.AddMediaActionButton
import com.dnfapps.arrmatey.ui.sheets.components.AddMediaSheetHeader
import com.dnfapps.arrmatey.ui.sheets.components.ChaptarrAddAuthorForm
import com.dnfapps.arrmatey.ui.sheets.components.ReadarrAddAuthorForm
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAuthorSheet(
    item: Author,
    qualityProfiles: List<QualityProfile>,
    metadataProfiles: List<MetadataProfile> = emptyList(),
    rootFolders: List<RootFolder>,
    tags: List<Tag>,
    addInProgress: Boolean,
    preferences: InstancePreferences,
    onUpdatePreferences: (InstancePreferences) -> Unit,
    onAddItem: (ArrMedia, Boolean) -> Unit,
    onDismiss: () -> Unit,
    instances: List<Instance> = emptyList(),
    selectedInstance: Instance? = null,
    onInstanceSelected: (Instance) -> Unit = {},
) {
    val isChaptarr = (selectedInstance?.type ?: item.instanceType) == InstanceType.Chaptarr

    // Chaptarr state
    var chaptarrMediaType by remember(preferences.addChaptarrMediaType, selectedInstance?.id) {
        mutableStateOf(preferences.addChaptarrMediaType)
    }
    var audiobookRootFolder by remember(
        rootFolders,
        preferences.addChaptarrAudiobookRootFolderPath,
        selectedInstance?.id,
    ) {
        mutableStateOf(rootFolders.defaultForAudiobook(preferences.addChaptarrAudiobookRootFolderPath))
    }
    var audiobookMonitor by remember(preferences.addChaptarrAudiobookMonitorExisting, selectedInstance?.id) {
        mutableStateOf(preferences.addChaptarrAudiobookMonitorExisting)
    }
    var audiobookMonitorNew by remember(preferences.addChaptarrAudiobookMonitorFuture, selectedInstance?.id) {
        mutableStateOf(preferences.addChaptarrAudiobookMonitorFuture)
    }
    var audiobookQualityProfile by remember(
        qualityProfiles,
        preferences.addChaptarrAudiobookQualityProfileId,
        selectedInstance?.id,
    ) {
        mutableStateOf(qualityProfiles.defaultForAudiobook(preferences.addChaptarrAudiobookQualityProfileId))
    }
    var audiobookMetadataProfile by remember(
        metadataProfiles,
        preferences.addChaptarrAudiobookMetadataProfileId,
        selectedInstance?.id,
    ) {
        mutableStateOf(metadataProfiles.defaultForAudiobook(preferences.addChaptarrAudiobookMetadataProfileId))
    }

    var ebookRootFolder by remember(rootFolders, preferences.addChaptarrEbookRootFolderPath, selectedInstance?.id) {
        mutableStateOf(rootFolders.defaultForEbook(preferences.addChaptarrEbookRootFolderPath))
    }
    var ebookMonitor by remember(preferences.addChaptarrEbookMonitorExisting, selectedInstance?.id) {
        mutableStateOf(preferences.addChaptarrEbookMonitorExisting)
    }
    var ebookMonitorNew by remember(preferences.addChaptarrEbookMonitorFuture, selectedInstance?.id) {
        mutableStateOf(preferences.addChaptarrEbookMonitorFuture)
    }
    var ebookQualityProfile by remember(
        qualityProfiles,
        preferences.addChaptarrEbookQualityProfileId,
        selectedInstance?.id,
    ) {
        mutableStateOf(qualityProfiles.defaultForEbook(preferences.addChaptarrEbookQualityProfileId))
    }
    var ebookMetadataProfile by remember(
        metadataProfiles,
        preferences.addChaptarrEbookMetadataProfileId,
        selectedInstance?.id,
    ) {
        mutableStateOf(metadataProfiles.defaultForEbook(preferences.addChaptarrEbookMetadataProfileId))
    }

    // Standard Readarr state
    var monitor by remember(
        preferences.addAuthorMonitor,
        selectedInstance?.id,
    ) { mutableStateOf(preferences.addAuthorMonitor) }
    var monitorNewBooks by remember(
        preferences.addAuthorMonitorNew,
        selectedInstance?.id,
    ) { mutableStateOf(preferences.addAuthorMonitorNew) }
    var qualityProfile by remember(qualityProfiles, preferences.addQualityProfileId, selectedInstance?.id) {
        mutableStateOf(
            qualityProfiles.firstOrNull { it.id == preferences.addQualityProfileId }
                ?: qualityProfiles.firstOrNull(),
        )
    }
    var rootFolder by remember(rootFolders, preferences.addRootFolderPath, selectedInstance?.id) {
        mutableStateOf(
            rootFolders.firstOrNull { it.path == preferences.addRootFolderPath }
                ?: rootFolders.firstOrNull(),
        )
    }
    val selectedTags = remember(selectedInstance?.id) { mutableStateListOf<Int>() }
    var searchOnAdd by remember(
        preferences.addSearchOnAdd,
        selectedInstance?.id,
    ) { mutableStateOf(preferences.addSearchOnAdd) }

    val chaptarrMonitorOptions = AuthorMonitorType.chaptarrOptions

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
                AddMediaSheetHeader(
                    type = mokoString(MR.strings.type_author),
                    title = item.title ?: "",
                )

                if (instances.size > 1 && selectedInstance != null) {
                    DropdownPicker(
                        options = instances,
                        modifier = Modifier.fillMaxWidth(),
                        selectedOption = selectedInstance,
                        onOptionSelected = onInstanceSelected,
                        getOptionLabel = { it.label },
                        label = { Text(mokoString(MR.strings.instances)) },
                        enabled = !addInProgress,
                    )
                }

                if (isChaptarr) {
                    ChaptarrAddAuthorForm(
                        mediaType = chaptarrMediaType,
                        onMediaTypeSelected = { chaptarrMediaType = it },
                        onSetAsDefault = {
                            onUpdatePreferences(preferences.copyWithChaptarrMediaType(mediaType = chaptarrMediaType))
                        },
                        rootFolders = rootFolders,
                        qualityProfiles = qualityProfiles,
                        metadataProfiles = metadataProfiles,
                        tags = tags,
                        selectedTags = selectedTags,
                        audiobookRootFolder = audiobookRootFolder,
                        onAudiobookRootFolderSelected = { audiobookRootFolder = it },
                        audiobookMonitor = audiobookMonitor,
                        onAudiobookMonitorSelected = { audiobookMonitor = it },
                        audiobookMonitorNew = audiobookMonitorNew,
                        onAudiobookMonitorNewChanged = { audiobookMonitorNew = it },
                        audiobookQualityProfile = audiobookQualityProfile,
                        onAudiobookQualityProfileSelected = { audiobookQualityProfile = it },
                        audiobookMetadataProfile = audiobookMetadataProfile,
                        onAudiobookMetadataProfileSelected = { audiobookMetadataProfile = it },
                        ebookRootFolder = ebookRootFolder,
                        onEbookRootFolderSelected = { ebookRootFolder = it },
                        ebookMonitor = ebookMonitor,
                        onEbookMonitorSelected = { ebookMonitor = it },
                        ebookMonitorNew = ebookMonitorNew,
                        onEbookMonitorNewChanged = { ebookMonitorNew = it },
                        ebookQualityProfile = ebookQualityProfile,
                        onEbookQualityProfileSelected = { ebookQualityProfile = it },
                        ebookMetadataProfile = ebookMetadataProfile,
                        onEbookMetadataProfileSelected = { ebookMetadataProfile = it },
                        searchOnAdd = searchOnAdd,
                        onSearchOnAddChanged = { searchOnAdd = it },
                        monitorOptions = chaptarrMonitorOptions,
                        enabled = !addInProgress,
                    )
                } else {
                    ReadarrAddAuthorForm(
                        monitor = monitor,
                        onMonitorSelected = { monitor = it },
                        monitorNewBooks = monitorNewBooks,
                        onMonitorNewBooksSelected = { monitorNewBooks = it },
                        qualityProfiles = qualityProfiles,
                        selectedQualityProfile = qualityProfile,
                        onQualityProfileSelected = { qualityProfile = it },
                        rootFolders = rootFolders,
                        selectedRootFolder = rootFolder,
                        onRootFolderSelected = { rootFolder = it },
                        tags = tags,
                        selectedTags = selectedTags,
                        searchOnAdd = searchOnAdd,
                        onSearchOnAddChanged = { searchOnAdd = it },
                        enabled = !addInProgress,
                    )
                }
            }

            val buttonLabel = if (isChaptarr) {
                when (chaptarrMediaType) {
                    BookMediaType.Audiobook -> mokoString(MR.strings.add_audiobooks)
                    BookMediaType.EBook -> mokoString(MR.strings.add_ebooks)
                    BookMediaType.Both -> mokoString(MR.strings.add_audiobooks_ebooks)
                }
            } else {
                mokoString(MR.strings.save)
            }

            val isChaptarrValid = when (chaptarrMediaType) {
                BookMediaType.Audiobook -> audiobookRootFolder != null && audiobookQualityProfile != null
                BookMediaType.EBook -> ebookRootFolder != null && ebookQualityProfile != null
                BookMediaType.Both -> audiobookRootFolder != null && audiobookQualityProfile != null && ebookRootFolder != null && ebookQualityProfile != null
            }
            val isStandardValid = qualityProfile != null && rootFolder != null

            AddMediaActionButton(
                text = buttonLabel,
                onClick = {
                    if (isChaptarr) {
                        onUpdatePreferences(
                            preferences.copyWithChaptarrAddDefaults(
                                mediaType = chaptarrMediaType,
                                audiobookQualityProfileId = audiobookQualityProfile?.id,
                                audiobookMetadataProfileId = audiobookMetadataProfile?.id,
                                audiobookRootFolderPath = audiobookRootFolder?.path,
                                audiobookMonitorExisting = audiobookMonitor,
                                audiobookMonitorFuture = audiobookMonitorNew,
                                ebookQualityProfileId = ebookQualityProfile?.id,
                                ebookMetadataProfileId = ebookMetadataProfile?.id,
                                ebookRootFolderPath = ebookRootFolder?.path,
                                ebookMonitorExisting = ebookMonitor,
                                ebookMonitorFuture = ebookMonitorNew,
                                searchOnAdd = searchOnAdd,
                            ),
                        )
                        val newItem = item.copyForChaptarrCreation(
                            selectedMediaType = chaptarrMediaType,
                            audiobookQualityProfileId = audiobookQualityProfile?.id,
                            audiobookMetadataProfileId = audiobookMetadataProfile?.id,
                            audiobookRootFolderPath = audiobookRootFolder?.path,
                            audiobookMonitorExisting = chaptarrMonitorOptions.indexOf(audiobookMonitor)
                                .takeIf { it >= 0 },
                            audiobookMonitorFuture = audiobookMonitorNew,
                            audiobookTags = selectedTags.toList(),
                            ebookQualityProfileId = ebookQualityProfile?.id,
                            ebookMetadataProfileId = ebookMetadataProfile?.id,
                            ebookRootFolderPath = ebookRootFolder?.path,
                            ebookMonitorExisting = chaptarrMonitorOptions.indexOf(ebookMonitor).takeIf { it >= 0 },
                            ebookMonitorFuture = ebookMonitorNew,
                            ebookTags = selectedTags.toList(),
                            tags = selectedTags.toList(),
                            searchForMissingBooks = searchOnAdd,
                        )
                        onAddItem(newItem, searchOnAdd)
                    } else {
                        val qp = qualityProfile
                        val rf = rootFolder
                        if (qp != null && rf != null) {
                            onUpdatePreferences(
                                preferences.copyWithAuthorAddDefaults(
                                    monitor = monitor,
                                    monitorNew = monitorNewBooks,
                                    qualityProfileId = qp.id,
                                    rootFolderPath = rf.path,
                                    searchOnAdd = searchOnAdd,
                                ),
                            )
                            val newItem = item.copyForCreation(
                                monitor = monitor,
                                monitorNew = monitorNewBooks,
                                qualityProfileId = qp.id,
                                rootFolderPath = rf.path,
                                tags = selectedTags,
                            )
                            onAddItem(newItem, searchOnAdd)
                        }
                    }
                },
                isLoading = addInProgress,
                enabled = if (isChaptarr) isChaptarrValid else isStandardValid,
            )
        }
    }
}
