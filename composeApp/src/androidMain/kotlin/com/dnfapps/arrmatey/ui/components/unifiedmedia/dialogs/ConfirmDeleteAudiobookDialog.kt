package com.dnfapps.arrmatey.ui.components.unifiedmedia.dialogs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.sheets.ArrDestructiveConfirmationSheet
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDeleteAudiobookDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ArrDestructiveConfirmationSheet(
        onDismissRequest = onDismiss,
        onConfirm = onConfirm,
        title = mokoString(MR.strings.confirm_delete),
        text = mokoString(MR.strings.confirm_delete_file),
        confirmText = mokoString(MR.strings.confirm),
        dismissText = mokoString(MR.strings.cancel),
    )
}
