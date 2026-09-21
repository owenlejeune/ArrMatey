package com.dnfapps.arrmatey.ui.components.unifiedmedia.dialogs

import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.sheets.ArrConfirmationSheet
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmMoveFilesDialog(
    rootFolderPath: String?,
    onConfirmMove: () -> Unit,
    onConfirmKeep: () -> Unit,
    onDismiss: () -> Unit,
) {
    ArrConfirmationSheet(
        onDismissRequest = onDismiss,
        title = mokoString(MR.strings.move_files_confirm, rootFolderPath ?: ""),
        confirmButton = {
            Button(
                onClick = onConfirmMove,
                modifier = Modifier.weight(1f),
            ) {
                Text(mokoString(MR.strings.yes))
            }
        },
        dismissButton = {
            FilledTonalButton(
                onClick = onConfirmKeep,
                modifier = Modifier.weight(1f),
            ) {
                Text(mokoString(MR.strings.no))
            }
        },
    )
}
