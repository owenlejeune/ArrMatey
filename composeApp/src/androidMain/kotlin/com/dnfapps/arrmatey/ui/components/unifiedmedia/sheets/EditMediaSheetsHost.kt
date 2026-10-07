package com.dnfapps.arrmatey.ui.components.unifiedmedia.sheets

import androidx.compose.runtime.Composable
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.BookMediaType
import com.dnfapps.arrmatey.arr.api.model.MetadataProfile
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.arr.api.model.hasRootFolderChanged
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.ui.sheets.EditAlbumSheet
import com.dnfapps.arrmatey.ui.sheets.EditMediaSheet
import com.dnfapps.arrmatey.ui.sheets.EditPathSheet

@Composable
fun EditMediaSheetsHost(
    showEditPathSheet: Boolean,
    showEditSheet: Boolean,
    editAlbum: ArrAlbum?,
    arrMedia: ArrMedia?,
    qualityProfiles: List<QualityProfile>,
    metadataProfiles: List<MetadataProfile> = emptyList(),
    rootFolders: List<RootFolder>,
    tags: List<Tag>,
    editStatus: OperationStatus,
    onEditItem: (ArrMedia, Boolean) -> Unit,
    onEditMedia: (ArrMedia) -> Unit,
    onUpdateAlbum: (ArrAlbum) -> Unit,
    onRequestMoveFiles: (ArrMedia) -> Unit,
    onDismissEditPath: () -> Unit,
    onDismissEditMedia: () -> Unit,
    onDismissEditAlbum: () -> Unit,
    initialMediaType: BookMediaType? = null,
) {
    if (showEditPathSheet && arrMedia != null) {
        EditPathSheet(
            item = arrMedia,
            rootFolders = rootFolders,
            editInProgress = editStatus is OperationStatus.InProgress,
            onEditItem = { updatedItem, moveFiles ->
                onEditItem(updatedItem, moveFiles)
                onDismissEditPath()
            },
            onDismiss = onDismissEditPath,
        )
    }

    if (showEditSheet && arrMedia != null) {
        EditMediaSheet(
            item = arrMedia,
            qualityProfiles = qualityProfiles,
            metadataProfiles = metadataProfiles,
            rootFolders = rootFolders,
            tags = tags,
            editInProgress = editStatus is OperationStatus.InProgress,
            onEditItem = {
                if (arrMedia.hasRootFolderChanged(it)) {
                    onRequestMoveFiles(it)
                } else {
                    onEditMedia(it)
                }
            },
            onDismiss = onDismissEditMedia,
            initialMediaType = initialMediaType,
        )
    }

    editAlbum?.let { album ->
        EditAlbumSheet(
            album = album,
            editInProgress = editStatus is OperationStatus.InProgress,
            onEditAlbum = onUpdateAlbum,
            onDismiss = onDismissEditAlbum,
        )
    }
}
