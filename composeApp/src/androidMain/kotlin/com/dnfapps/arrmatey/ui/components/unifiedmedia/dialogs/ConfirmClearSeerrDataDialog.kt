package com.dnfapps.arrmatey.ui.components.unifiedmedia.dialogs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.sheets.ArrDestructiveConfirmationSheet
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmClearSeerrDataDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ArrDestructiveConfirmationSheet(
        onDismissRequest = onDismiss,
        onConfirm = onConfirm,
        title = mokoString(MR.strings.are_you_sure),
        text = mokoString(MR.strings.clear_data_confirm),
        confirmText = mokoString(MR.strings.yes),
        dismissText = mokoString(MR.strings.no),
    )
}
