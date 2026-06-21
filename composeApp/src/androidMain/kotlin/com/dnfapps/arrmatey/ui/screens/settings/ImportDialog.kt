package com.dnfapps.arrmatey.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.backup.state.ImportUiState
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun ImportDialog(
    importState: ImportUiState,
    onDismiss: () -> Unit,
    onConfirmDecrypt: () -> Unit,
    onConfirmImport: () -> Unit,
    onPasswordChanged: (String) -> Unit,
    onToggleInstanceSelection: (Int) -> Unit,
    onToggleDownloadClientSelection: (Int) -> Unit,
    onToggleImportTabPreferences: () -> Unit,
    onToggleImportUiPreferences: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(mokoString(MR.strings.import_data)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                if (importState.decryptedBackup == null) {
                    Text(mokoString(MR.strings.import_password_prompt))

                    OutlinedTextField(
                        value = importState.password,
                        onValueChange = onPasswordChanged,
                        label = { Text(mokoString(MR.strings.password)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (importState.error != null) {
                        Text(
                            text = importState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    Text(
                        text = mokoString(MR.strings.select_items_to_import),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    if (importState.decryptedBackup?.instances?.isNotEmpty() == true) {
                        Text(
                            text = mokoString(MR.strings.instances),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        importState.decryptedBackup?.instances?.forEachIndexed { index, instance ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = importState.selectedInstanceIndices.contains(index),
                                    onCheckedChange = { onToggleInstanceSelection(index) }
                                )
                                Text(text = instance.label)
                            }
                        }
                    }

                    if (importState.decryptedBackup?.downloadClients?.isNotEmpty() == true) {
                        if (importState.decryptedBackup?.instances?.isNotEmpty() == true) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                        Text(
                            text = mokoString(MR.strings.download_clients),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        importState.decryptedBackup?.downloadClients?.forEachIndexed { index, client ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = importState.selectedDownloadClientIndices.contains(index),
                                    onCheckedChange = { onToggleDownloadClientSelection(index) }
                                )
                                Text(text = client.label)
                            }
                        }
                    }
                    
                    if (importState.decryptedBackup?.globalPreferences != null) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            text = mokoString(MR.strings.backup_restore),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        if (importState.decryptedBackup?.globalPreferences?.tabPreferences != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = importState.importTabPreferences,
                                    onCheckedChange = { onToggleImportTabPreferences() }
                                )
                                Text(text = mokoString(MR.strings.navigation_bar_configuration))
                            }
                        }

                        if (importState.decryptedBackup?.globalPreferences?.useServiceNavLogos != null || 
                            importState.decryptedBackup?.globalPreferences?.hideInstanceSwitcher != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = importState.importUiPreferences,
                                    onCheckedChange = { onToggleImportUiPreferences() }
                                )
                                Text(text = mokoString(MR.strings.user_interface))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (importState.decryptedBackup == null) {
                TextButton(
                    enabled = importState.password.isNotBlank(),
                    onClick = onConfirmDecrypt
                ) {
                    Text(mokoString(MR.strings.ok))
                }
            } else {
                TextButton(
                    enabled = importState.selectedInstanceIndices.isNotEmpty() ||
                            importState.selectedDownloadClientIndices.isNotEmpty(),
                    onClick = onConfirmImport
                ) {
                    Text(mokoString(MR.strings.import_data))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(mokoString(MR.strings.cancel))
            }
        }
    )
}
