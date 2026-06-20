package com.dnfapps.arrmatey.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.backup.state.ExportUiState
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun ExportDialog(
    exportState: ExportUiState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onPasswordChanged: (String) -> Unit,
    onToggleIncludePreferences: () -> Unit,
    onToggleInstanceSelection: (Long) -> Unit,
    onToggleDownloadClientSelection: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(mokoString(MR.strings.export_data)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(mokoString(MR.strings.export_password_prompt))

                OutlinedTextField(
                    value = exportState.password,
                    onValueChange = onPasswordChanged,
                    label = { Text(mokoString(MR.strings.password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = exportState.includePreferences,
                        onCheckedChange = { onToggleIncludePreferences() }
                    )
                    Text(mokoString(MR.strings.include_preferences))
                }

                HorizontalDivider()

                Text(
                    text = mokoString(MR.strings.select_items_to_export),
                    style = MaterialTheme.typography.titleSmall
                )

                exportState.instances.forEach { instance ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = exportState.selectedInstanceIds.contains(instance.id),
                            onCheckedChange = { onToggleInstanceSelection(instance.id) }
                        )
                        Text(text = instance.label)
                    }
                }

                exportState.downloadClients.forEach { client ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = exportState.selectedDownloadClientIds.contains(client.id),
                            onCheckedChange = { onToggleDownloadClientSelection(client.id) }
                        )
                        Text(text = client.label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = exportState.password.isNotBlank() && (exportState.selectedInstanceIds.isNotEmpty() || exportState.selectedDownloadClientIds.isNotEmpty()),
                onClick = onConfirm
            ) {
                Text(mokoString(MR.strings.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(mokoString(MR.strings.cancel))
            }
        }
    )
}