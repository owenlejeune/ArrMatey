package com.dnfapps.arrmatey.ui.components.downloads

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dnfapps.arrmatey.downloadclient.state.DownloadClientCommandState
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.LabelledCheckbox
import com.dnfapps.arrmatey.ui.components.sheets.ArrDestructiveConfirmationSheet
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteDownloadDialog(
    commandState: DownloadClientCommandState,
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit,
) {
    var deleteFiles by remember { mutableStateOf(false) }

    ArrDestructiveConfirmationSheet(
        onDismissRequest = onDismiss,
        onConfirm = { onConfirm(deleteFiles) },
        title = mokoString(MR.strings.confirm),
        text = "Remove this download?",
        confirmText = mokoString(MR.strings.yes),
        dismissText = mokoString(MR.strings.no),
        confirmEnabled = commandState !is DownloadClientCommandState.Loading,
        inProgress = commandState is DownloadClientCommandState.Loading,
        content = {
            LabelledCheckbox(
                label = mokoString(MR.strings.delete_files),
                checked = deleteFiles,
                onCheckedChange = { deleteFiles = it },
            )
        },
    )
}


