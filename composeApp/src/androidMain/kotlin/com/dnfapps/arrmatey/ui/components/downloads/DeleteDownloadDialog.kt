package com.dnfapps.arrmatey.ui.components.downloads

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.downloadclient.state.DownloadClientCommandState
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.LabelledCheckbox
import com.dnfapps.arrmatey.ui.components.sheets.ArrDestructiveConfirmationSheet
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteDownloadDialog(
    commandState: DownloadClientCommandState,
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit,
    preferencesStore: PreferencesStore = koinInject(),
) {
    val savedDeleteFiles by preferencesStore.downloadDeleteFiles.collectAsStateWithLifecycle(false)
    var deleteFiles by remember(savedDeleteFiles) { mutableStateOf(savedDeleteFiles) }

    ArrDestructiveConfirmationSheet(
        onDismissRequest = onDismiss,
        onConfirm = {
            preferencesStore.setDownloadDeleteFiles(deleteFiles)
            onConfirm(deleteFiles)
        },
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
