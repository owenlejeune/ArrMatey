package com.dnfapps.arrmatey.ui.components.unifiedmedia.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.model.SmartAddSeerrAction
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun PendingSeerrRequestDialog(
    onAction: (SmartAddSeerrAction, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var rememberChoice by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(mokoString(MR.strings.smart_add_seerr_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(mokoString(MR.strings.smart_add_seerr_message))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable { rememberChoice = !rememberChoice },
                ) {
                    Checkbox(checked = rememberChoice, onCheckedChange = { rememberChoice = it })
                    Text(mokoString(MR.strings.remember_choice))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onAction(SmartAddSeerrAction.Approve, rememberChoice)
            }) {
                Text(mokoString(MR.strings.approve))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onAction(SmartAddSeerrAction.Decline, rememberChoice)
            }) {
                Text(mokoString(MR.strings.decline))
            }
        },
    )
}
