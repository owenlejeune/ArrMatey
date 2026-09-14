package com.dnfapps.arrmatey.ui.components.unifiedmedia.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun ConfirmMoveFilesDialog(
    rootFolderPath: String?,
    onConfirmMove: () -> Unit,
    onConfirmKeep: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(mokoString(MR.strings.move_files_confirm, rootFolderPath ?: ""))
        },
        confirmButton = {
            TextButton(onClick = onConfirmMove) {
                Text(mokoString(MR.strings.yes))
            }
        },
        dismissButton = {
            TextButton(onClick = onConfirmKeep) {
                Text(mokoString(MR.strings.no))
            }
        },
    )
}
