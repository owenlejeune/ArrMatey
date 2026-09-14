package com.dnfapps.arrmatey.ui.components.unifiedmedia.sheets

import androidx.compose.runtime.Composable
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag
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
            rootFolders = rootFolders,
            tags = tags,
            editInProgress = editStatus is OperationStatus.InProgress,
            onEditItem = {
                if (arrMedia.rootFolderPath != it.rootFolderPath) {
                    onRequestMoveFiles(it)
                } else {
                    onEditMedia(it)
                }
            },
            onDismiss = onDismissEditMedia,
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
