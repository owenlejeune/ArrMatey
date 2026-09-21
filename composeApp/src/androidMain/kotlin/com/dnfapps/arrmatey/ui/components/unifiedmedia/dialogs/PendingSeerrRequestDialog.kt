package com.dnfapps.arrmatey.ui.components.unifiedmedia.dialogs

import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.dnfapps.arrmatey.model.SmartAddSeerrAction
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.LabelledCheckbox
import com.dnfapps.arrmatey.ui.components.sheets.ArrConfirmationSheet
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingSeerrRequestDialog(
    onAction: (SmartAddSeerrAction, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var rememberChoice by remember { mutableStateOf(false) }
    ArrConfirmationSheet(
        onDismissRequest = onDismiss,
        title = mokoString(MR.strings.smart_add_seerr_title),
        text = mokoString(MR.strings.smart_add_seerr_message),
        content = {
            LabelledCheckbox(
                label = mokoString(MR.strings.remember_choice),
                checked = rememberChoice,
                onCheckedChange = { rememberChoice = it },
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onAction(SmartAddSeerrAction.Approve, rememberChoice)
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(mokoString(MR.strings.approve))
            }
        },
        dismissButton = {
            FilledTonalButton(
                onClick = {
                    onAction(SmartAddSeerrAction.Decline, rememberChoice)
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(mokoString(MR.strings.decline))
            }
        },
    )
}
