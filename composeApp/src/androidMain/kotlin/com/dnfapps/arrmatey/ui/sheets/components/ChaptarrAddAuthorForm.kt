package com.dnfapps.arrmatey.ui.sheets.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.AuthorMonitorType
import com.dnfapps.arrmatey.arr.api.model.BookMediaType
import com.dnfapps.arrmatey.arr.api.model.MetadataProfile
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.arr.api.model.filterForAudiobook
import com.dnfapps.arrmatey.arr.api.model.filterForEbook
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun ChaptarrAddAuthorForm(
    mediaType: BookMediaType,
    onMediaTypeSelected: (BookMediaType) -> Unit,
    onSetAsDefault: () -> Unit,
    rootFolders: List<RootFolder>,
    qualityProfiles: List<QualityProfile>,
    metadataProfiles: List<MetadataProfile>,
    tags: List<Tag>,
    selectedTags: SnapshotStateList<Int>,
    audiobookRootFolder: RootFolder?,
    onAudiobookRootFolderSelected: (RootFolder) -> Unit,
    audiobookMonitor: AuthorMonitorType,
    onAudiobookMonitorSelected: (AuthorMonitorType) -> Unit,
    audiobookMonitorNew: Boolean,
    onAudiobookMonitorNewChanged: (Boolean) -> Unit,
    audiobookQualityProfile: QualityProfile?,
    onAudiobookQualityProfileSelected: (QualityProfile) -> Unit,
    audiobookMetadataProfile: MetadataProfile?,
    onAudiobookMetadataProfileSelected: (MetadataProfile) -> Unit,
    ebookRootFolder: RootFolder?,
    onEbookRootFolderSelected: (RootFolder) -> Unit,
    ebookMonitor: AuthorMonitorType,
    onEbookMonitorSelected: (AuthorMonitorType) -> Unit,
    ebookMonitorNew: Boolean,
    onEbookMonitorNewChanged: (Boolean) -> Unit,
    ebookQualityProfile: QualityProfile?,
    onEbookQualityProfileSelected: (QualityProfile) -> Unit,
    ebookMetadataProfile: MetadataProfile?,
    onEbookMetadataProfileSelected: (MetadataProfile) -> Unit,
    searchOnAdd: Boolean,
    onSearchOnAddChanged: (Boolean) -> Unit,
    monitorOptions: List<AuthorMonitorType>,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ChaptarrMediaTypeSelector(
            selectedMediaType = mediaType,
            onMediaTypeSelected = onMediaTypeSelected,
            onSetAsDefault = onSetAsDefault,
        )

        if (mediaType == BookMediaType.Audiobook || mediaType == BookMediaType.Both) {
            ChaptarrMediaSection(
                title = if (mediaType == BookMediaType.Both) mokoString(MR.strings.audiobooks) else null,
                rootFolders = rootFolders,
                selectedRootFolder = audiobookRootFolder,
                onRootFolderSelected = onAudiobookRootFolderSelected,
                rootFolderLabel = mokoString(MR.strings.audiobook_root_folder),
                monitorOptions = monitorOptions,
                selectedMonitor = audiobookMonitor,
                onMonitorSelected = onAudiobookMonitorSelected,
                monitorLabel = mokoString(MR.strings.monitor_authors_audiobooks),
                monitorNew = audiobookMonitorNew,
                onMonitorNewChanged = onAudiobookMonitorNewChanged,
                monitorNewLabel = mokoString(MR.strings.monitor_new_audiobooks),
                qualityProfiles = qualityProfiles.filterForAudiobook(),
                selectedQualityProfile = audiobookQualityProfile,
                onQualityProfileSelected = onAudiobookQualityProfileSelected,
                qualityProfileLabel = mokoString(MR.strings.audiobook_quality_profile),
                metadataProfiles = metadataProfiles.filterForAudiobook(),
                selectedMetadataProfile = audiobookMetadataProfile,
                onMetadataProfileSelected = onAudiobookMetadataProfileSelected,
                metadataProfileLabel = mokoString(MR.strings.audiobook_metadata_profile),
                enabled = enabled,
            )
        }

        TagsSelectionCard(
            tags = tags,
            selectedTags = selectedTags,
            enabled = enabled,
        )

        if (mediaType == BookMediaType.EBook || mediaType == BookMediaType.Both) {
            ChaptarrMediaSection(
                title = if (mediaType == BookMediaType.Both) mokoString(MR.strings.ebooks) else null,
                rootFolders = rootFolders,
                selectedRootFolder = ebookRootFolder,
                onRootFolderSelected = onEbookRootFolderSelected,
                rootFolderLabel = mokoString(MR.strings.ebook_root_folder),
                monitorOptions = monitorOptions,
                selectedMonitor = ebookMonitor,
                onMonitorSelected = onEbookMonitorSelected,
                monitorLabel = mokoString(MR.strings.monitor_authors_ebooks),
                monitorNew = ebookMonitorNew,
                onMonitorNewChanged = onEbookMonitorNewChanged,
                monitorNewLabel = mokoString(MR.strings.monitor_new_ebooks),
                qualityProfiles = qualityProfiles.filterForEbook(),
                selectedQualityProfile = ebookQualityProfile,
                onQualityProfileSelected = onEbookQualityProfileSelected,
                qualityProfileLabel = mokoString(MR.strings.ebook_quality_profile),
                metadataProfiles = metadataProfiles.filterForEbook(),
                selectedMetadataProfile = ebookMetadataProfile,
                onMetadataProfileSelected = onEbookMetadataProfileSelected,
                metadataProfileLabel = mokoString(MR.strings.ebook_metadata_profile),
                enabled = enabled,
            )
        }

        SearchOnAddCard(
            checked = searchOnAdd,
            onCheckedChange = onSearchOnAddChanged,
            label = mokoString(MR.strings.start_search_for_missing_books),
            enabled = enabled,
        )
    }
}
