package com.dnfapps.arrmatey.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.backup.state.ImportUiState
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.AMOutlinedTextField
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.LabelledCheckbox
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
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
    onToggleImportUiPreferences: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = mokoString(MR.strings.import_data),
                style = MaterialTheme.typography.headlineSmall,
            )

            if (importState.decryptedBackup == null) {
                Text(
                    text = mokoString(MR.strings.import_password_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                AMOutlinedTextField(
                    value = importState.password,
                    onValueChange = onPasswordChanged,
                    label = mokoString(MR.strings.password),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = importState.error != null,
                    errorMessage = importState.error,
                )
            } else {
                Text(
                    text = mokoString(MR.strings.select_items_to_import),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )

                if (importState.decryptedBackup?.instances?.isNotEmpty() == true) {
                    Text(
                        text = mokoString(MR.strings.instances),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    ContainerCard(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        importState.decryptedBackup?.instances?.forEachIndexed { index, instance ->
                            LabelledCheckbox(
                                label = instance.label,
                                checked = importState.selectedInstanceIndices.contains(index),
                                onCheckedChange = { onToggleInstanceSelection(index) },
                            )
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
                        color = MaterialTheme.colorScheme.primary,
                    )
                    ContainerCard(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        importState.decryptedBackup?.downloadClients?.forEachIndexed { index, client ->
                            LabelledCheckbox(
                                label = client.label,
                                checked = importState.selectedDownloadClientIndices.contains(index),
                                onCheckedChange = { onToggleDownloadClientSelection(index) },
                            )
                        }
                    }
                }

                if (importState.decryptedBackup?.globalPreferences != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = mokoString(MR.strings.backup_restore),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    ContainerCard(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (importState.decryptedBackup?.globalPreferences?.tabPreferences != null) {
                            LabelledCheckbox(
                                label = mokoString(MR.strings.navigation_bar_configuration),
                                checked = importState.importTabPreferences,
                                onCheckedChange = { onToggleImportTabPreferences() },
                            )
                        }

                        if (importState.decryptedBackup?.globalPreferences?.useServiceNavLogos != null ||
                            importState.decryptedBackup?.globalPreferences?.hideInstanceSwitcher != null
                        ) {
                            LabelledCheckbox(
                                label = mokoString(MR.strings.user_interface),
                                checked = importState.importUiPreferences,
                                onCheckedChange = { onToggleImportUiPreferences() },
                            )
                        }
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                if (importState.decryptedBackup == null) {
                    Button(
                        enabled = importState.password.isNotBlank(),
                        onClick = onConfirmDecrypt,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(mokoString(MR.strings.ok))
                    }
                } else {
                    Button(
                        enabled =
                            importState.selectedInstanceIndices.isNotEmpty() ||
                                importState.selectedDownloadClientIndices.isNotEmpty(),
                        onClick = onConfirmImport,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(mokoString(MR.strings.import_data))
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(mokoString(MR.strings.cancel))
                }
            }
        }
    }
}


